package com.weatherornot.service;

import com.weatherornot.model.Place;

import java.time.LocalDateTime;

public interface TravelTimeProvider {
    TravelEstimate estimate(Place origin, Place destination, LocalDateTime departureTime, boolean trafficAware);
}
