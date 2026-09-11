package com.weatherornot.dto;

import com.weatherornot.model.ImpactLevel;

import java.time.LocalDateTime;

public record RecommendationResponse(
        LocalDateTime preparationTime,
        LocalDateTime departureTime,
        LocalDateTime estimatedArrivalTime,
        int trafficImpactMinutes,
        ImpactLevel weatherImpactLevel,
        int weatherImpactMinutes,
        String reason) {
}
