package com.weatherornot.service;

import com.weatherornot.model.Appointment;
import com.weatherornot.model.ImpactLevel;
import com.weatherornot.model.Recommendation;
import com.weatherornot.model.WeatherImpact;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecommendationEngineTest {
    private final RecommendationEngine engine = new RecommendationEngine();

    @Test
    void calculatesNormalRecommendation() {
        Recommendation recommendation = engine.calculate(appointment(), LocalDateTime.of(2026, 8, 17, 19, 0), WeatherImpact.none());

        assertEquals(LocalDateTime.of(2026, 8, 17, 17, 50), recommendation.preparationTime());
        assertEquals(LocalDateTime.of(2026, 8, 17, 18, 20), recommendation.departureTime());
        assertEquals(LocalDateTime.of(2026, 8, 17, 18, 50), recommendation.estimatedArrivalTime());
    }

    @Test
    void addsWeatherImpactToTravelTime() {
        WeatherImpact rain = new WeatherImpact(ImpactLevel.HIGH, 15, "Chuva forte prevista durante o deslocamento");
        Recommendation recommendation = engine.calculate(appointment(), LocalDateTime.of(2026, 8, 17, 19, 0), rain);

        assertEquals(LocalDateTime.of(2026, 8, 17, 17, 35), recommendation.preparationTime());
        assertEquals(LocalDateTime.of(2026, 8, 17, 18, 5), recommendation.departureTime());
        assertEquals(LocalDateTime.of(2026, 8, 17, 18, 50), recommendation.estimatedArrivalTime());
    }

    private Appointment appointment() {
        Appointment appointment = new Appointment();
        appointment.preparationMinutes = 30;
        appointment.travelMinutes = 30;
        appointment.safetyMarginMinutes = 10;
        return appointment;
    }
}
