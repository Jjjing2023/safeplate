package com.jillliu.safeplate.places;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PlacesJsonParser {
    private final ObjectMapper objectMapper;

    public PlacesJsonParser() {
        this.objectMapper = new ObjectMapper();
    }

    public List<Restaurant> parseRestaurants(String json) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(json);
        List<Restaurant> restaurants = new ArrayList<>();
        for (JsonNode place : root.path("places")) {
            String placeId = place.path("id").asText();
            String name = place.path("displayName").path("text").asText();
            String address = place.path("formattedAddress").asText();
            double lat = place.path("location").path("latitude").asDouble();
            double lng = place.path("location").path("longitude").asDouble();
            LatLng location = new LatLng(lat, lng);
            PriceLevel priceLevel = PriceLevel.fromApi(textOrNull(place.path("priceLevel")));
            List<String> types = new ArrayList<>();
            for (JsonNode type : place.path("types")) {
                types.add(type.asText());
            }

            Double rating = doubleOrNull(place.path("rating"));
            Integer userRatingCount = intOrNull(place.path("userRatingCount"));
            String websiteUri = textOrNull(place.path("websiteUri"));
            String googleMapsUri = place.path("googleMapsUri").asText();

            Restaurant restaurant = new Restaurant(
                    placeId,
                    name,
                    address,
                    location,
                    priceLevel,
                    types,
                    rating,
                    userRatingCount,
                    websiteUri,
                    googleMapsUri);
            restaurants.add(restaurant);
        }
        return restaurants;
    }

    public List<Review> parseReviews(String json) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(json);
        List<Review> reviews = new ArrayList<>();
        for (JsonNode node : root.path("reviews")) {
            String text = textOrNull(node.path("text").path("text"));
            Integer rating = intOrNull(node.path("rating"));
            String publishTime = textOrNull(node.path("publishTime"));
            String sourceUri = textOrNull(node.path("googleMapsUri"));

            Review review = new Review(
                    text,
                    rating,
                    publishTime,
                    sourceUri);
            reviews.add(review);
        }
        return reviews;
    }

    // Returns null for a missing/null node (instead of asDouble()'s default 0.0).
    public static Double doubleOrNull(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) {
            return null;
        }
        return node.asDouble();
    }

    public static Integer intOrNull(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) {
            return null;
        }
        return node.asInt();
    }

    public static String textOrNull(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) {
            return null;
        }
        return node.asText();
    }

}
