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
        target.setName(source.name());
        target.setStartTime(source.startTime());
        target.setDate(source.date());
        target.setRecurring(source.recurring());
        target.setRecurringDays(source.recurringDays() == null ? Set.of() : Set.copyOf(source.recurringDays()));
        AppointmentPlace.apply(source, target);
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        var destination = appointment.getDestination();
        return new AppointmentResponse(appointment.getId(), appointment.getName(), appointment.getStartTime(),
                appointment.getDate(), destination.getDisplayName(), destination.getGooglePlaceId(),
                destination.getLatitude(), destination.getLongitude(), appointment.isRecurring(), appointment.getRecurringDays());
    }

    private static final class AppointmentPlace {
        private static void apply(AppointmentRequest source, Appointment target) {
            var destination = new com.weatherornot.model.Place();
            destination.setDisplayName(source.destinationName());
            destination.setGooglePlaceId(source.destinationPlaceId());
            destination.setLatitude(source.destinationLatitude());
            destination.setLongitude(source.destinationLongitude());
            target.setDestination(destination);
        }
    }
}
