from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime, timedelta
from enum import Enum


class ImpactLevel(str, Enum):
    NONE = "NONE"
    LOW = "LOW"
    MEDIUM = "MEDIUM"
    HIGH = "HIGH"
    SEVERE = "SEVERE"


@dataclass(frozen=True)
class ImpactConfig:
    low_minutes: int = 5
    medium_minutes: int = 10
    high_minutes: int = 15
    severe_minutes: int = 30


@dataclass(frozen=True)
class Appointment:
    name: str
    start_time: datetime
    preparation_minutes: int
    travel_minutes: int
    safety_margin_minutes: int


@dataclass(frozen=True)
class WeatherCondition:
    timestamp: datetime
    precipitation_probability: float = 0.0
    precipitation_mm: float = 0.0
    thunderstorm: bool = False


@dataclass(frozen=True)
class WeatherAlert:
    active: bool
    reason: str = "Alerta meteorológico previsto durante o deslocamento"


@dataclass(frozen=True)
class WeatherImpact:
    level: ImpactLevel
    additional_travel_minutes: int
    reason: str


@dataclass(frozen=True)
class Recommendation:
    recommended_preparation_time: datetime
    recommended_departure_time: datetime
    estimated_arrival_time: datetime
    weather_impact_level: ImpactLevel
    additional_travel_minutes: int
    reason: str


class WeatherImpactEngine:
    def __init__(self, config: ImpactConfig | None = None) -> None:
        self._config = config or ImpactConfig()

    def calculate(self, forecast_window: list[WeatherCondition], alert: WeatherAlert | None = None) -> WeatherImpact:
        if alert and alert.active:
            return WeatherImpact(ImpactLevel.SEVERE, self._config.severe_minutes, alert.reason)

        if not forecast_window:
            return WeatherImpact(ImpactLevel.NONE, 0, "Condições normais na janela de deslocamento")

        max_precipitation = max(condition.precipitation_mm for condition in forecast_window)
        max_probability = max(condition.precipitation_probability for condition in forecast_window)
        has_thunderstorm = any(condition.thunderstorm for condition in forecast_window)

        if has_thunderstorm or max_precipitation >= 25:
            return WeatherImpact(
                ImpactLevel.SEVERE,
                self._config.severe_minutes,
                "Condições severas previstas durante o deslocamento",
            )

        if max_precipitation >= 12 or max_probability >= 0.8:
            return WeatherImpact(
                ImpactLevel.HIGH,
                self._config.high_minutes,
                "Chuva forte prevista durante o deslocamento",
            )

        if max_precipitation >= 5 or max_probability >= 0.6:
            return WeatherImpact(
                ImpactLevel.MEDIUM,
                self._config.medium_minutes,
                "Chuva moderada prevista durante o deslocamento",
            )

        if max_precipitation > 0 or max_probability >= 0.3:
            return WeatherImpact(
                ImpactLevel.LOW,
                self._config.low_minutes,
                "Chuva leve prevista durante o deslocamento",
            )

        return WeatherImpact(ImpactLevel.NONE, 0, "Condições normais na janela de deslocamento")


class DecisionEngine:
    def __init__(self, impact_engine: WeatherImpactEngine) -> None:
        self._impact_engine = impact_engine

    @staticmethod
    def _baseline_departure(appointment: Appointment) -> datetime:
        return appointment.start_time - timedelta(
            minutes=appointment.travel_minutes + appointment.safety_margin_minutes
        )

    def calculate(
        self,
        appointment: Appointment,
        forecast: list[WeatherCondition],
        alert: WeatherAlert | None = None,
    ) -> Recommendation:
        baseline_departure = self._baseline_departure(appointment)
        baseline_arrival = baseline_departure + timedelta(minutes=appointment.travel_minutes)
        relevant_forecast = [
            condition
            for condition in forecast
            if baseline_departure <= condition.timestamp <= baseline_arrival
        ]

        impact = self._impact_engine.calculate(relevant_forecast, alert)
        adjusted_travel_minutes = appointment.travel_minutes + impact.additional_travel_minutes

        recommended_departure = appointment.start_time - timedelta(
            minutes=adjusted_travel_minutes + appointment.safety_margin_minutes
        )
        recommended_preparation = recommended_departure - timedelta(minutes=appointment.preparation_minutes)
        estimated_arrival = recommended_departure + timedelta(minutes=adjusted_travel_minutes)

        return Recommendation(
            recommended_preparation_time=recommended_preparation,
            recommended_departure_time=recommended_departure,
            estimated_arrival_time=estimated_arrival,
            weather_impact_level=impact.level,
            additional_travel_minutes=impact.additional_travel_minutes,
            reason=impact.reason,
        )


def should_notify_recommendation_change(
    previous_departure: datetime,
    current_departure: datetime,
    minimum_change_minutes: int,
) -> bool:
    change = abs(int((previous_departure - current_departure).total_seconds() // 60))
    return change >= minimum_change_minutes
