package com.weatherornot.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public record AppointmentRequest(
        @NotBlank String name,
        @NotBlank String type,
        @NotNull LocalTime startTime,
        LocalDate date,
        String locationName,
        Double latitude,
        Double longitude,
        @Min(0) int preparationMinutes,
        @Min(0) int travelMinutes,
        @Min(0) int safetyMarginMinutes,
        boolean recurring,
        Set<DayOfWeek> recurringDays) {
}
