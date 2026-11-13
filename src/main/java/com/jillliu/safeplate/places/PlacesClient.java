package com.jillliu.safeplate.places;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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

    public String searchText(String query, LatLng center, double radius) throws IOException, InterruptedException {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("textQuery", query);

        ObjectNode locationNode = root.putObject("locationBias");
        ObjectNode circleNode = locationNode.putObject("circle");
        ObjectNode centernNode = circleNode.putObject("center");
        centernNode.put("latitude", center.lat());
        centernNode.put("longitude", center.lng());
        circleNode.put("radius", radius);

        String body = objectMapper.writeValueAsString(root);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://places.googleapis.com/v1/places:searchText"))
                .header("Content-Type", "application/json")
                .header("X-Goog-Api-Key", apiKey)
                .header("X-Goog-FieldMask",
                        "places.id,places.displayName,places.formattedAddress,places.rating,places.location,places.priceLevel,places.types,places.userRatingCount,places.websiteUri,places.googleMapsUri")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "Places searchText failed: HTTP " + response.statusCode() + " " + response.body());
        }
        return response.body();
    }

    public String getDetails(String placeId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://places.googleapis.com/v1/places/" + placeId))
                .header("Content-Type", "application/json")
                .header("X-Goog-Api-Key", apiKey)
                .header("X-Goog-FieldMask",
                        "id,displayName,formattedAddress,rating,location,priceLevel,types,userRatingCount,websiteUri,googleMapsUri,reviews")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "Places getDetails failed: HTTP " + response.statusCode() + " " + response.body());
        }
        return response.body();
    }

}
