package com.example.nhom_15_dulichtphcm.data.model;

import java.util.Objects;

public final class Restaurant {
    public final String id;
    public final String name;
    public final String address;
    public final String district;
    public final double latitude;
    public final double longitude;
    public final String openingHours;
    public final String priceRange;
    public final String highlight;
    public final double rating;

    public Restaurant(
            String id,
            String name,
            String address,
            String district,
            double latitude,
            double longitude,
            String openingHours,
            String priceRange,
            String highlight,
            double rating
    ) {
        this.id = id != null ? id : "";
        this.name = name != null ? name : "";
        this.address = address != null ? address : "";
        this.district = district != null ? district : "";
        this.latitude = latitude;
        this.longitude = longitude;
        this.openingHours = openingHours != null ? openingHours : "";
        this.priceRange = priceRange != null ? priceRange : "";
        this.highlight = highlight != null ? highlight : "";
        this.rating = rating;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getDistrict() { return district; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getOpeningHours() { return openingHours; }
    public String getPriceRange() { return priceRange; }
    public String getHighlight() { return highlight; }
    public double getRating() { return rating; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Restaurant)) return false;
        Restaurant that = (Restaurant) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
