package com.weatherornot.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public record AppointmentResponse(
        Long id, String name, String type, LocalTime startTime, LocalDate date,
        String locationName, Double latitude, Double longitude,
        int preparationMinutes, int travelMinutes, int safetyMarginMinutes,
        boolean recurring, Set<DayOfWeek> recurringDays) {
}
