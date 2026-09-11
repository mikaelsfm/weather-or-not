package com.weatherornot.dto;

public record RoutineSettingsResponse(
        String homeName,
        String homePlaceId,
        Double homeLatitude,
        Double homeLongitude,
        int preparationMinutes,
        int safetyMarginMinutes) {
}
