package com.weatherornot.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public record AppointmentResponse(
        Long id, String name, LocalTime startTime, LocalDate date,
        String destinationName, String destinationPlaceId, Double destinationLatitude, Double destinationLongitude,
        boolean recurring, Set<DayOfWeek> recurringDays) {
}
