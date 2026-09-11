package com.weatherornot.service;

import com.weatherornot.dto.AppointmentRequest;
import com.weatherornot.dto.AppointmentResponse;
import com.weatherornot.model.Appointment;
import com.weatherornot.repository.AppointmentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class AppointmentService {
    private final AppointmentRepository repository;

    public AppointmentService(AppointmentRepository repository) {
        this.repository = repository;
    }

    public List<AppointmentResponse> list() {
        return repository.findAllOrderedByStartTime().stream().map(this::toResponse).toList();
    }

    public AppointmentResponse get(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public AppointmentResponse create(AppointmentRequest request) {
        validateRecurrence(request);
        Appointment appointment = new Appointment();
        apply(request, appointment);
        repository.persist(appointment);
        return toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse update(Long id, AppointmentRequest request) {
        validateRecurrence(request);
        Appointment appointment = find(id);
        apply(request, appointment);
        return toResponse(appointment);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private Appointment find(Long id) {
        return repository.findOptionalById(id).orElseThrow(NotFoundException::new);
    }

    private void validateRecurrence(AppointmentRequest request) {
        if (request.recurring() && (request.recurringDays() == null || request.recurringDays().isEmpty())) {
            throw new IllegalArgumentException("Recurring appointments require at least one recurring day");
        }
        if (!request.recurring() && request.date() == null) {
            throw new IllegalArgumentException("One-time appointments require a date");
        }
    }

    private void apply(AppointmentRequest source, Appointment target) {
        target.name = source.name();
        target.type = source.type();
        target.startTime = source.startTime();
        target.date = source.date();
        target.locationName = source.locationName();
        target.latitude = source.latitude();
        target.longitude = source.longitude();
        target.preparationMinutes = source.preparationMinutes();
        target.travelMinutes = source.travelMinutes();
        target.safetyMarginMinutes = source.safetyMarginMinutes();
        target.recurring = source.recurring();
        target.recurringDays = source.recurringDays() == null ? null : source.recurringDays().stream()
                .map(DayOfWeek::getValue).sorted().map(String::valueOf).collect(Collectors.joining(","));
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        Set<DayOfWeek> days = appointment.recurrenceDays();
        return new AppointmentResponse(appointment.id, appointment.name, appointment.type, appointment.startTime,
                appointment.date, appointment.locationName, appointment.latitude, appointment.longitude,
                appointment.preparationMinutes, appointment.travelMinutes, appointment.safetyMarginMinutes,
                appointment.recurring, days);
    }
}
