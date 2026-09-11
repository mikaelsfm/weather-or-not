package com.weatherornot.model;

public record WeatherImpact(ImpactLevel level, int additionalTravelMinutes, String reason) {
    public static WeatherImpact none() {
        return new WeatherImpact(ImpactLevel.NONE, 0, "Condições normais de deslocamento");
    }
}
