package com.weatherornot.model;

import java.time.LocalDateTime;

public record Recommendation(
        LocalDateTime preparationTime,
        LocalDateTime departureTime,
        LocalDateTime estimatedArrivalTime,
        WeatherImpact weatherImpact) {
}
