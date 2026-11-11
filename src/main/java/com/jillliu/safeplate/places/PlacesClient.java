package com.jillliu.safeplate.places;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PlacesClient {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public PlacesClient(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("PLACES_API_KEY is not set. Did you run: source ~/.safeplate.env ?");
        }
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public String searchText(String query) throws IOException, InterruptedException {
        String body = objectMapper.writeValueAsString(Map.of("textQuery", query));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://places.googleapis.com/v1/places:searchText"))
                .header("Content-Type", "application/json")
                .header("X-Goog-Api-Key", apiKey)
                .header("X-Goog-FieldMask",
                        "places.id,places.displayName,places.formattedAddress,places.rating,places.location,places.priceLevel,places.types,places.userRatingCount,places.websiteUri,places.googleMapsUri")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "Places searchText failed: HTTP " + response.statusCode() + " " + response.body());
        }
        return response.body();
    }

}
