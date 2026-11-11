package com.jillliu.safeplate;

import java.io.IOException;

import com.jillliu.safeplate.places.PlacesClient;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws IOException, InterruptedException {
        PlacesClient client = new PlacesClient(System.getenv("PLACES_API_KEY"));
        System.out.println(client.searchText("Thai food near Union Square, New York"));
    }
}
