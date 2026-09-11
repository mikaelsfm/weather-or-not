package com.weatherornot.repository;

import com.weatherornot.model.RoutineSettings;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RoutineSettingsRepository implements PanacheRepository<RoutineSettings> {
}
