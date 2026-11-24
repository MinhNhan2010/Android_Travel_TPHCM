package com.example.nhom_15_dulichtphcm.data.model;

public enum AdministrativeType {
    CITY("Thành phố", "City"),
    URBAN_DISTRICT("Quận", "Urban District"),
    RURAL_DISTRICT("Huyện", "Rural District");

    public final String displayNameVi;
    public final String displayNameEn;

    AdministrativeType(String displayNameVi, String displayNameEn) {
        this.displayNameVi = displayNameVi;
        this.displayNameEn = displayNameEn;
    }

    public String getDisplayNameVi() { return displayNameVi; }
    public String getDisplayNameEn() { return displayNameEn; }
}
