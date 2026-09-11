package com.weatherornot.service;

import com.weatherornot.model.Appointment;
import com.weatherornot.model.Recommendation;
import com.weatherornot.model.WeatherImpact;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;

@ApplicationScoped
public class RecommendationEngine {
    public Recommendation calculate(Appointment appointment, LocalDateTime appointmentStart, WeatherImpact weatherImpact) {
        int adjustedTravel = appointment.travelMinutes + weatherImpact.additionalTravelMinutes();
        LocalDateTime departure = appointmentStart.minusMinutes(adjustedTravel + appointment.safetyMarginMinutes);
        LocalDateTime preparation = departure.minusMinutes(appointment.preparationMinutes);
        LocalDateTime arrival = departure.plusMinutes(adjustedTravel);
        return new Recommendation(preparation, departure, arrival, weatherImpact);
    }
}
