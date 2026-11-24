package com.example.nhom_15_dulichtphcm.data.model;

public enum UtilityCategory {
    ALL("All Utilities", "Tất cả", "🏙️"),
    RESTROOM("Public Restrooms", "Nhà vệ sinh", "🚻"),
    CURRENCY_EXCHANGE("Money & ATM", "Đổi tiền & ATM", "💱"),
    TRANSIT_METRO("Metro & Transit", "Metro & Xe buýt", "🚇"),
    MEDICAL("Hospitals & Clinics", "Bệnh viện & Y tế", "🏥"),
    TOURIST_INFO_SIM("Tourist Info & SIM", "Thông tin & SIM", "📶"),
    EMERGENCY_HOTLINE("Emergency 24/7", "Khẩn cấp 24/7", "🆘");

    public final String labelEn;
    public final String labelVi;
    public final String iconEmoji;

    UtilityCategory(String labelEn, String labelVi, String iconEmoji) {
        this.labelEn = labelEn;
        this.labelVi = labelVi;
        this.iconEmoji = iconEmoji;
    }

    public String getLabelEn() { return labelEn; }
    public String getLabelVi() { return labelVi; }
    public String getIconEmoji() { return iconEmoji; }

    public static UtilityCategory fromName(String raw) {
        if (raw == null) return TOURIST_INFO_SIM;
        for (UtilityCategory cat : values()) {
            if (cat.name().equalsIgnoreCase(raw.trim())) {
                return cat;
            }
        }
        return TOURIST_INFO_SIM;
    }
}
