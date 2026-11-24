package com.example.nhom_15_dulichtphcm.data.model;

import java.util.Objects;

public final class Attraction {
    public final String id;
    public final String nameEn;
    public final String nameVi;
    public final AttractionCategory category;
    public final String imageUrl;
    public final int imageResId;
    public final String district;
    public final String address;
    public final double latitude;
    public final double longitude;
    public final String ticketPrice;
    public final String openingHours;
    public final String shortSummary;
    public final String history;
    public final String culture;
    public final String foreignerTips;
    public final String province;

    public Attraction(
            String id,
            String nameEn,
            String nameVi,
            AttractionCategory category,
            String imageUrl,
            int imageResId,
            String district,
            String address,
            double latitude,
            double longitude,
            String ticketPrice,
            String openingHours,
            String shortSummary,
            String history,
            String culture,
            String foreignerTips,
            String province
    ) {
        this.id = id != null ? id : "";
        this.nameEn = nameEn != null ? nameEn : "";
        this.nameVi = nameVi != null ? nameVi : "";
        this.category = category != null ? category : AttractionCategory.CULTURAL;
        this.imageUrl = imageUrl != null ? imageUrl : "";
        this.imageResId = imageResId;
        this.district = district != null ? district : "";
        this.address = address != null ? address : "";
        this.latitude = latitude;
        this.longitude = longitude;
        this.ticketPrice = ticketPrice != null ? ticketPrice : "";
        this.openingHours = openingHours != null ? openingHours : "";
        this.shortSummary = shortSummary != null ? shortSummary : "";
        this.history = history != null ? history : "";
        this.culture = culture != null ? culture : "";
        this.foreignerTips = foreignerTips != null ? foreignerTips : "";
        this.province = (province != null && !province.trim().isEmpty()) ? province : "TP. Hồ Chí Minh";
    }

    public Attraction(
            String id,
            String nameEn,
            String nameVi,
            AttractionCategory category,
            String imageUrl,
            int imageResId,
            String district,
            String address,
            double latitude,
            double longitude,
            String ticketPrice,
            String openingHours,
            String shortSummary,
            String history,
            String culture,
            String foreignerTips
    ) {
        this(id, nameEn, nameVi, category, imageUrl, imageResId, district, address,
                latitude, longitude, ticketPrice, openingHours, shortSummary,
                history, culture, foreignerTips, "TP. Hồ Chí Minh");
    }

    public String getId() { return id; }
    public String getNameEn() { return nameEn; }
    public String getNameVi() { return nameVi; }
    public AttractionCategory getCategory() { return category; }
    public String getImageUrl() { return imageUrl; }
    public int getImageResId() { return imageResId; }
    public String getDistrict() { return district; }
    public String getAddress() { return address; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getTicketPrice() { return ticketPrice; }
    public String getOpeningHours() { return openingHours; }
    public String getShortSummary() { return shortSummary; }
    public String getHistory() { return history; }
    public String getCulture() { return culture; }
    public String getForeignerTips() { return foreignerTips; }
    public String getProvince() { return province; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Attraction)) return false;
        Attraction that = (Attraction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
