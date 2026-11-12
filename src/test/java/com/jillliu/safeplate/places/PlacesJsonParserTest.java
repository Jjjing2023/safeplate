package com.jillliu.safeplate.places;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

public class PlacesJsonParserTest {

    @Test
    void parseAllFiledsOfAFullRestaurant() throws IOException {
        String json = Files.readString(Path.of("src/test/resources/search-response.json"));
        PlacesJsonParser parser = new PlacesJsonParser();
        List<Restaurant> restaurants = parser.parseRestaurants(json);
        assertEquals(2, restaurants.size());
        Restaurant full = restaurants.get(0);
        assertEquals("Full Restaurant", full.name());
        assertEquals("1 Test St, New York, NY", full.address());
        assertEquals(new LatLng(40.7, -73.9), full.location());
        assertEquals(PriceLevel.MODERATE, full.priceLevel());
        assertEquals(List.of("thai_restaurant", "restaurant"), full.types());
        assertEquals(4.5, full.rating());
        assertEquals(100, full.userRatingCount());
        assertEquals("https://example.com", full.websiteUri());
        assertEquals("https://maps.google.com/?cid=1", full.googleMapsUri());
    }

    @Test
    void missingFieldsBecomeNull() throws IOException {
        String json = Files.readString(Path.of("src/test/resources/search-response.json"));
        PlacesJsonParser parser = new PlacesJsonParser();
        List<Restaurant> restaurants = parser.parseRestaurants(json);
        Restaurant NewPlace = restaurants.get(1);
        assertEquals(PriceLevel.UNSPECIFIED, NewPlace.priceLevel());
        assertNull(NewPlace.rating());
        assertNull(NewPlace.userRatingCount());
        assertNull(NewPlace.websiteUri());
    }

}
