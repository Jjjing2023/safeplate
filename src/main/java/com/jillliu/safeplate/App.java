package com.jillliu.safeplate;

import java.io.IOException;
import java.util.List;

import com.jillliu.safeplate.places.PlacesClient;
import com.jillliu.safeplate.places.PlacesJsonParser;
import com.jillliu.safeplate.places.Restaurant;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws IOException, InterruptedException {
        PlacesClient client = new PlacesClient(System.getenv("PLACES_API_KEY"));
        PlacesJsonParser parser = new PlacesJsonParser();
        List<Restaurant> restaurants = parser.parse(client.searchText("Thai food near Union Square, New York"));
        for (Restaurant r : restaurants) {
            System.out.println(r);
        }
        System.out.println("Total: " + restaurants.size());
    }
}
