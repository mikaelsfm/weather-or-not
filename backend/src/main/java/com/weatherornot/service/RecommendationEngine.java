package com.weatherornot.service;

import com.weatherornot.model.Appointment;
import com.weatherornot.model.Recommendation;
import com.weatherornot.model.WeatherImpact;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;

@ApplicationScoped
public class RecommendationEngine {
    public Recommendation calculate(Appointment appointment, LocalDateTime appointmentStart, WeatherImpact weatherImpact) {
        throw new UnsupportedOperationException("Use calculate with tempos de rota e configurações da rotina");
    }

    public Recommendation calculate(int preparationMinutes, int safetyMarginMinutes, LocalDateTime appointmentStart,
                                    int staticTravelMinutes, int trafficTravelMinutes, WeatherImpact weatherImpact) {
        int adjustedTravel = trafficTravelMinutes + weatherImpact.additionalTravelMinutes();
        LocalDateTime departure = appointmentStart.minusMinutes(adjustedTravel + safetyMarginMinutes);
        LocalDateTime preparation = departure.minusMinutes(preparationMinutes);
        LocalDateTime arrival = departure.plusMinutes(adjustedTravel);
        int trafficImpact = Math.max(0, trafficTravelMinutes - staticTravelMinutes);
        String reason = trafficImpact > 0
                ? "O trânsito adiciona " + trafficImpact + " min ao seu deslocamento."
                : weatherImpact.reason();
        return new Recommendation(preparation, departure, arrival, trafficImpact, weatherImpact, reason);
    }
}
