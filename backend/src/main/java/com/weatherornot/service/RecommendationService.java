package com.weatherornot.service;

import com.weatherornot.dto.RecommendationResponse;
import com.weatherornot.model.Appointment;
import com.weatherornot.model.Recommendation;
import com.weatherornot.model.RoutineSettings;
import com.weatherornot.model.WeatherImpact;
import com.weatherornot.repository.AppointmentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;

import java.time.LocalDateTime;

@ApplicationScoped
public class RecommendationService {
    private final AppointmentRepository appointments;
    private final RoutineSettingsService settingsService;
    private final TravelTimeProvider travelTimeProvider;
    private final RecommendationEngine engine;

    public RecommendationService(AppointmentRepository appointments, RoutineSettingsService settingsService,
                                 TravelTimeProvider travelTimeProvider, RecommendationEngine engine) {
        this.appointments = appointments;
        this.settingsService = settingsService;
        this.travelTimeProvider = travelTimeProvider;
        this.engine = engine;
    }

    public RecommendationResponse calculate(Long appointmentId, LocalDateTime appointmentStart) {
        Appointment appointment = appointments.findByIdOptional(appointmentId).orElseThrow(NotFoundException::new);
        RoutineSettings settings = settingsService.requireSettings();

        // First obtain a no-traffic duration, then query traffic at the resulting departure time.
        LocalDateTime initialDeparture = appointmentStart
                .minusMinutes(settings.getPreparationMinutes() + settings.getSafetyMarginMinutes());
        TravelEstimate baseline = travelTimeProvider.estimate(settings.getHome(), appointment.getDestination(), initialDeparture, false);
        LocalDateTime trafficDeparture = appointmentStart
                .minus(baseline.staticDuration())
                .minusMinutes(settings.getSafetyMarginMinutes());
        TravelEstimate traffic = travelTimeProvider.estimate(settings.getHome(), appointment.getDestination(), trafficDeparture, true);

        Recommendation recommendation = engine.calculate(settings.getPreparationMinutes(), settings.getSafetyMarginMinutes(),
                appointmentStart, (int) baseline.staticDuration().toMinutes(), (int) traffic.trafficDuration().toMinutes(),
                WeatherImpact.none());
        return new RecommendationResponse(recommendation.preparationTime(), recommendation.departureTime(),
                recommendation.estimatedArrivalTime(), recommendation.trafficImpactMinutes(),
                recommendation.weatherImpact().level(), recommendation.weatherImpact().additionalTravelMinutes(),
                recommendation.reason());
    }
}
