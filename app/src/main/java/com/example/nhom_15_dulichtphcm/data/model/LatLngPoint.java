package com.example.nhom_15_dulichtphcm.data.model;

import java.util.Objects;

/**
 * Immutable geographic coordinate pair (latitude, longitude).
 */
public final class LatLngPoint {
    public final double latitude;
    public final double longitude;

    public LatLngPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LatLngPoint)) return false;
        LatLngPoint that = (LatLngPoint) o;
        return Double.compare(that.latitude, latitude) == 0
                && Double.compare(that.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Override
    public String toString() {
        return latitude + ", " + longitude;
    }
}
