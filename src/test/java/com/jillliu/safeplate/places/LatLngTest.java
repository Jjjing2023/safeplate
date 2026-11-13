package com.jillliu.safeplate.places;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class LatLngTest {

    @Test
    void distanceToSamePointIsZero() {
        LatLng point = new LatLng(0, 0);
        assertEquals(0.0, point.distanceMetersTo(point));
    }

    @Test
    void oneDegreeOfLatitude() {
        LatLng one = new LatLng(0, 0);
        LatLng two = new LatLng(1, 0);
        assertEquals(111_194.9, one.distanceMetersTo(two), 0.1);
    }

    @Test
    void distanceIsSymmetric() {
        LatLng one = new LatLng(0, 0);
        LatLng two = new LatLng(1, 0);
        assertEquals(one.distanceMetersTo(two), two.distanceMetersTo(one));
    }

    @Test
    void unionSquareToTimesSquare() {
        LatLng unionSquare = new LatLng(40.7359, -73.9911);
        LatLng timesSquare = new LatLng(40.7580, -73.9855);
        assertEquals(2502.3, unionSquare.distanceMetersTo(timesSquare), 1.0);
    }

    @Test
    void crossesAntimeridian() {
        LatLng one = new LatLng(0, 179);
        LatLng two = new LatLng(0, -179);
        assertEquals(222_389.9, one.distanceMetersTo(two), 0.1);
    }

    @Test
    void rejectsInvalidLatitude() {
        assertThrows(IllegalArgumentException.class, () -> new LatLng(91, 0));
    }

    @Test
    void rejectsNaNLatitude() {
        assertThrows(IllegalArgumentException.class, () -> new LatLng(Double.NaN, 0));
    }

    @Test
    void rejectsNaNLongitude() {
        assertThrows(IllegalArgumentException.class, () -> new LatLng(0, Double.NaN));
    }

}
