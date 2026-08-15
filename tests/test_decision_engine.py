from datetime import datetime, timedelta
import unittest

from src.weather_or_not.engine import (
    Appointment,
    DecisionEngine,
    ImpactConfig,
    ImpactLevel,
    WeatherAlert,
    WeatherCondition,
    WeatherImpactEngine,
    should_notify_recommendation_change,
)


class DecisionEngineTestCase(unittest.TestCase):
    def setUp(self) -> None:
        self.start_time = datetime(2026, 9, 14, 19, 0)
        self.appointment = Appointment(
            name="Faculdade",
            start_time=self.start_time,
            preparation_minutes=30,
            travel_minutes=30,
            safety_margin_minutes=10,
        )
        self.engine = DecisionEngine(WeatherImpactEngine(ImpactConfig()))

    def baseline_window(self):
        departure = self.start_time - timedelta(minutes=40)
        arrival = departure + timedelta(minutes=30)
        return departure, arrival

    def test_normal_calculation_without_weather_impact(self):
        recommendation = self.engine.calculate(self.appointment, [])

        self.assertEqual(recommendation.weather_impact_level, ImpactLevel.NONE)
        self.assertEqual(recommendation.additional_travel_minutes, 0)
        self.assertEqual(recommendation.recommended_departure_time, datetime(2026, 9, 14, 18, 20))
        self.assertEqual(recommendation.recommended_preparation_time, datetime(2026, 9, 14, 17, 50))
        self.assertEqual(recommendation.estimated_arrival_time, datetime(2026, 9, 14, 18, 50))

    def test_light_rain_adds_low_impact(self):
        departure, _ = self.baseline_window()
        forecast = [
            WeatherCondition(timestamp=departure + timedelta(minutes=5), precipitation_mm=1.0),
        ]

        recommendation = self.engine.calculate(self.appointment, forecast)

        self.assertEqual(recommendation.weather_impact_level, ImpactLevel.LOW)
        self.assertEqual(recommendation.additional_travel_minutes, 5)
        self.assertEqual(recommendation.recommended_departure_time, datetime(2026, 9, 14, 18, 15))
        self.assertEqual(recommendation.recommended_preparation_time, datetime(2026, 9, 14, 17, 45))

    def test_heavy_rain_adds_high_impact(self):
        departure, _ = self.baseline_window()
        forecast = [
            WeatherCondition(timestamp=departure + timedelta(minutes=15), precipitation_mm=15.0),
        ]

        recommendation = self.engine.calculate(self.appointment, forecast)

        self.assertEqual(recommendation.weather_impact_level, ImpactLevel.HIGH)
        self.assertEqual(recommendation.additional_travel_minutes, 15)
        self.assertEqual(recommendation.recommended_departure_time, datetime(2026, 9, 14, 18, 5))
        self.assertEqual(recommendation.recommended_preparation_time, datetime(2026, 9, 14, 17, 35))

    def test_severe_weather_alert_adds_severe_impact(self):
        alert = WeatherAlert(active=True)

        recommendation = self.engine.calculate(self.appointment, [], alert)

        self.assertEqual(recommendation.weather_impact_level, ImpactLevel.SEVERE)
        self.assertEqual(recommendation.additional_travel_minutes, 30)
        self.assertEqual(recommendation.recommended_departure_time, datetime(2026, 9, 14, 17, 50))
        self.assertEqual(recommendation.recommended_preparation_time, datetime(2026, 9, 14, 17, 20))

    def test_rain_outside_travel_window_does_not_change_departure(self):
        departure, arrival = self.baseline_window()
        forecast = [
            WeatherCondition(timestamp=arrival + timedelta(minutes=10), precipitation_mm=20.0),
            WeatherCondition(timestamp=departure - timedelta(minutes=10), precipitation_mm=20.0),
        ]

        recommendation = self.engine.calculate(self.appointment, forecast)

        self.assertEqual(recommendation.weather_impact_level, ImpactLevel.NONE)
        self.assertEqual(recommendation.recommended_departure_time, datetime(2026, 9, 14, 18, 20))

    def test_should_notify_only_when_change_is_significant(self):
        previous = datetime(2026, 9, 14, 18, 20)
        small_change = datetime(2026, 9, 14, 18, 18)
        significant_change = datetime(2026, 9, 14, 18, 0)

        self.assertFalse(should_notify_recommendation_change(previous, small_change, 10))
        self.assertTrue(should_notify_recommendation_change(previous, significant_change, 10))


if __name__ == "__main__":
    unittest.main()
