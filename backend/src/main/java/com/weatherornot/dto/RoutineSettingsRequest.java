package com.weatherornot.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record RoutineSettingsRequest(
        @NotBlank String homeName,
        String homePlaceId,
        Double homeLatitude,
        Double homeLongitude,
        @Min(0) int preparationMinutes,
        @Min(0) int safetyMarginMinutes) {
}
