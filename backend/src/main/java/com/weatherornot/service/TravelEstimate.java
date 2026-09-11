package com.weatherornot.service;

import java.time.Duration;

public record TravelEstimate(Duration staticDuration, Duration trafficDuration, int distanceMeters) {
}
