package com.weatherornot.service;

import com.weatherornot.dto.RoutineSettingsRequest;
import com.weatherornot.dto.RoutineSettingsResponse;
import com.weatherornot.model.Place;
import com.weatherornot.model.RoutineSettings;
import com.weatherornot.repository.RoutineSettingsRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class RoutineSettingsService {
    private final RoutineSettingsRepository repository;

    public RoutineSettingsService(RoutineSettingsRepository repository) {
        this.repository = repository;
    }

    public RoutineSettings requireSettings() {
        return repository.findByIdOptional(RoutineSettings.SINGLETON_ID)
                .orElseThrow(() -> new NotFoundException("Configure sua rotina antes de calcular uma recomendação"));
    }

    public RoutineSettingsResponse get() {
        return toResponse(requireSettings());
    }

    @Transactional
    public RoutineSettingsResponse save(RoutineSettingsRequest request) {
        RoutineSettings settings = repository.findByIdOptional(RoutineSettings.SINGLETON_ID)
                .orElseGet(RoutineSettings::new);
        Place home = new Place();
        home.setDisplayName(request.homeName());
        home.setGooglePlaceId(request.homePlaceId());
        home.setLatitude(request.homeLatitude());
        home.setLongitude(request.homeLongitude());
        settings.setHome(home);
        settings.setPreparationMinutes(request.preparationMinutes());
        settings.setSafetyMarginMinutes(request.safetyMarginMinutes());
        repository.persist(settings);
        return toResponse(settings);
    }

    private RoutineSettingsResponse toResponse(RoutineSettings settings) {
        Place home = settings.getHome();
        return new RoutineSettingsResponse(home.getDisplayName(), home.getGooglePlaceId(), home.getLatitude(),
                home.getLongitude(), settings.getPreparationMinutes(), settings.getSafetyMarginMinutes());
    }
}
