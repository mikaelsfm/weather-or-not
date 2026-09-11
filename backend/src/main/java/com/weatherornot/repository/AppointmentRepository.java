package com.weatherornot.repository;

import com.weatherornot.model.Appointment;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AppointmentRepository implements PanacheRepository<Appointment> {
    public List<Appointment> findAllOrderedByStartTime() {
        return listAll(Sort.ascending("startTime"));
    }

    public Optional<Appointment> findOptionalById(Long id) {
        return findByIdOptional(id);
    }
}
