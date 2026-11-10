package com.jillliu.safeplate.places;

import java.util.List;

public record Restaurant(
    String placeId,
    String name,
    String address,
    LatLng location,
    PriceLevel priceLevel, // UNSPECIFIED if Google has no data
    List<String> types,
    Double rating, // null if no ratings yet
    Integer userRatingCount, // null if no ratings yet
    String websiteUri, // null if no website
    String googleMapsUri) {
}
