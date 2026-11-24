package com.example.nhom_15_dulichtphcm.data.model;

import java.util.Objects;

public final class PublicUtility {
    public final String id;
    public final String nameEn;
    public final String nameVi;
    public final UtilityCategory category;
    public final String district;
    public final String address;
    public final double latitude;
    public final double longitude;
    public final String operatingHours;
    public final String price;
    public final String phone;
    public final String descriptionEn;
    public final String descriptionVi;
    public final String tips;

    public PublicUtility(
            String id,
            String nameEn,
            String nameVi,
            UtilityCategory category,
            String district,
            String address,
            double latitude,
            double longitude,
            String operatingHours,
            String price,
            String phone,
            String descriptionEn,
            String descriptionVi,
            String tips
    ) {
        this.id = id != null ? id : "";
        this.nameEn = nameEn != null ? nameEn : "";
        this.nameVi = nameVi != null ? nameVi : "";
        this.category = category != null ? category : UtilityCategory.TOURIST_INFO_SIM;
        this.district = district != null ? district : "";
        this.address = address != null ? address : "";
        this.latitude = latitude;
        this.longitude = longitude;
        this.operatingHours = operatingHours != null ? operatingHours : "";
        this.price = price != null ? price : "";
        this.phone = (phone != null && !phone.trim().isEmpty()) ? phone.trim() : null;
        this.descriptionEn = descriptionEn != null ? descriptionEn : "";
        this.descriptionVi = descriptionVi != null ? descriptionVi : "";
        this.tips = tips != null ? tips : "";
    }

    public String getId() { return id; }
    public String getNameEn() { return nameEn; }
    public String getNameVi() { return nameVi; }
    public UtilityCategory getCategory() { return category; }
    public String getDistrict() { return district; }
    public String getAddress() { return address; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getOperatingHours() { return operatingHours; }
    public String getPrice() { return price; }
    public String getPhone() { return phone; }
    public String getDescriptionEn() { return descriptionEn; }
    public String getDescriptionVi() { return descriptionVi; }
    public String getTips() { return tips; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PublicUtility)) return false;
        PublicUtility that = (PublicUtility) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
