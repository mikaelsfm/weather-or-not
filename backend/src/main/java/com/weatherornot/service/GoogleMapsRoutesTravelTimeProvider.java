package com.weatherornot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weatherornot.model.Place;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

@ApplicationScoped
public class GoogleMapsRoutesTravelTimeProvider implements TravelTimeProvider {
    private static final URI COMPUTE_ROUTES_URI = URI.create("https://routes.googleapis.com/directions/v2:computeRoutes");
    private static final String FIELD_MASK = "routes.duration,routes.staticDuration,routes.distanceMeters";

    private final String apiKey;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public GoogleMapsRoutesTravelTimeProvider(@ConfigProperty(name = "google.maps.api-key") String apiKey,
                                              ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
    }

    @Override
    public TravelEstimate estimate(Place origin, Place destination, LocalDateTime departureTime, boolean trafficAware) {
        if (!origin.isRoutable() || !destination.isRoutable()) {
            throw new IllegalArgumentException("Origem e destino precisam de um Google Place ID ou coordenadas");
        }
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("origin", waypoint(origin));
            body.put("destination", waypoint(destination));
            body.put("travelMode", "DRIVE");
            body.put("routingPreference", trafficAware ? "TRAFFIC_AWARE_OPTIMAL" : "TRAFFIC_UNAWARE");
            body.put("departureTime", departureTime.atZone(ZoneId.systemDefault()).toInstant().toString());

            HttpRequest request = HttpRequest.newBuilder(COMPUTE_ROUTES_URI)
                    .header("Content-Type", "application/json")
                    .header("X-Goog-Api-Key", apiKey)
                    .header("X-Goog-FieldMask", FIELD_MASK)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Google Routes API retornou HTTP " + response.statusCode());
            }
            JsonNode route = objectMapper.readTree(response.body()).path("routes").path(0);
            if (route.isMissingNode()) throw new IllegalStateException("Nenhuma rota encontrada para o destino");
            Duration staticDuration = parseDuration(route.path("staticDuration").asText());
            Duration trafficDuration = trafficAware ? parseDuration(route.path("duration").asText()) : staticDuration;
            return new TravelEstimate(staticDuration, trafficDuration, route.path("distanceMeters").asInt());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("A consulta de rota foi interrompida", exception);
        } catch (Exception exception) {
            throw new IllegalStateException("Não foi possível consultar a rota no Google Maps", exception);
        }
    }

    private Map<String, Object> waypoint(Place place) {
        if (place.getGooglePlaceId() != null && !place.getGooglePlaceId().isBlank()) {
            return Map.of("placeId", place.getGooglePlaceId());
        }
        return Map.of("location", Map.of("latLng", Map.of("latitude", place.getLatitude(), "longitude", place.getLongitude())));
    }

    private Duration parseDuration(String value) {
        return Duration.ofMillis(Math.round(Double.parseDouble(value.replace("s", "")) * 1000));
    }
}
