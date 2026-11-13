package com.jillliu.safeplate.places;

public record LatLng(double lat, double lng) {
    private static final double EARTH_RADIUS_METERS = 6_371_000;

    public LatLng {
        if (!(lat >= -90 && lat <= 90)) {
            throw new IllegalArgumentException("Invalid latitude range: " + lat);
        }
        if (!(lng >= -180 && lng <= 180)) {
            throw new IllegalArgumentException("Invalid longitude range: " + lng);
        }
    }

    public double distanceMetersTo(LatLng other) {
        double startLat = Math.toRadians(this.lat);
        double endLat = Math.toRadians(other.lat);
        double latDiff = endLat - startLat;

        double startLng = Math.toRadians(this.lng);
        double endLng = Math.toRadians(other.lng);
        double lngDiff = endLng - startLng;

        double a = Math.pow(Math.sin(latDiff / 2), 2)
                + Math.cos(startLat) * Math.cos(endLat) * Math.pow(Math.sin(lngDiff / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = EARTH_RADIUS_METERS * c;

        return d;
    }
}
