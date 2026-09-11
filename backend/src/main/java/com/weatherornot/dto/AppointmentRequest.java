package com.weatherornot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public record AppointmentRequest(
        @NotBlank String name,
        @NotNull LocalTime startTime,
        LocalDate date,
        @NotBlank String destinationName,
        String destinationPlaceId,
        Double destinationLatitude,
        Double destinationLongitude,
        boolean recurring,
        Set<DayOfWeek> recurringDays) {
}
