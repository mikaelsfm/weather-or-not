package com.weatherornot.controller;

import com.weatherornot.dto.AppointmentRequest;
import com.weatherornot.dto.AppointmentResponse;
import com.weatherornot.service.AppointmentService;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;

@Path("/appointments")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AppointmentController {
    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    @GET
    public List<AppointmentResponse> list() {
        return service.list();
    }

    @GET
    @Path("/{id}")
    public AppointmentResponse get(@PathParam("id") Long id) {
        return service.get(id);
    }

    @POST
    public Response create(@Valid AppointmentRequest request) {
        AppointmentResponse appointment = service.create(request);
        return Response.created(URI.create("/appointments/" + appointment.id())).entity(appointment).build();
    }

    @PUT
    @Path("/{id}")
    public AppointmentResponse update(@PathParam("id") Long id, @Valid AppointmentRequest request) {
        return service.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
