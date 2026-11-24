package com.example.nhom_15_dulichtphcm.data.model;

public enum AttractionCategory {
    ALL("All Places", "Tất cả"),
    HISTORICAL("Historical", "Di tích lịch sử"),
    CULTURAL("Cultural", "Văn hóa & Bảo tàng"),
    MODERN("Modern", "Hiện đại & Đô thị"),
    SPIRITUAL("Spiritual", "Tâm linh & Chùa"),
    MARKET("Markets", "Chợ & Khám phá");

    public final String displayName;
    public final String displayNameVi;

    AttractionCategory(String displayName, String displayNameVi) {
        this.displayName = displayName;
        this.displayNameVi = displayNameVi;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDisplayNameVi() {
        return displayNameVi;
    }

    public static AttractionCategory fromName(String raw) {
        if (raw == null) return CULTURAL;
        for (AttractionCategory cat : values()) {
            if (cat.name().equalsIgnoreCase(raw.trim())) {
                return cat;
            }
        }
        return CULTURAL;
    }
}
