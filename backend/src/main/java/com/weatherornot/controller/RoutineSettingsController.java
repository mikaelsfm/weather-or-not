package com.weatherornot.controller;

import com.weatherornot.dto.RoutineSettingsRequest;
import com.weatherornot.dto.RoutineSettingsResponse;
import com.weatherornot.service.RoutineSettingsService;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/routine-settings")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RoutineSettingsController {
    private final RoutineSettingsService service;

    public RoutineSettingsController(RoutineSettingsService service) {
        this.service = service;
    }

    @GET
    public RoutineSettingsResponse get() {
        return service.get();
    }

    @PUT
    public RoutineSettingsResponse save(@Valid RoutineSettingsRequest request) {
        return service.save(request);
    }
}
