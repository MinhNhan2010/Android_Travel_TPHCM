package com.example.nhom_15_dulichtphcm.data.model;

public enum RegionZone {
    CENTRAL_CORE("Khu Trung tâm Lịch sử", "Historic Central Core", "#E53935"),
    EAST_THU_DUC("Khu Đông - TP. Thủ Đức", "East Innovation Hub", "#7C4DFF"),
    WEST_CHO_LON("Khu Tây - Chợ Lớn", "West Chinatown", "#FB8C00"),
    SOUTH_ZONE("Khu Nam & Sinh thái Cần Giờ", "South & Eco Biosphere", "#00897B"),
    NORTH_ZONE("Khu Tây Bắc - Củ Chi", "Northwest Cu Chi", "#43A047"),
    BINH_DUONG_GATEWAY("Cửa ngõ Phía Bắc - Bình Dương", "Northern Gateway - Binh Duong", "#00ACC1"),
    VUNG_TAU_COASTAL("Đô thị Biển - Vũng Tàu", "Coastal Gateway - Vung Tau", "#0277BD");

    public final String labelVi;
    public final String labelEn;
    public final String colorHex;

    RegionZone(String labelVi, String labelEn, String colorHex) {
        this.labelVi = labelVi;
        this.labelEn = labelEn;
        this.colorHex = colorHex;
    }

    public String getLabelVi() { return labelVi; }
    public String getLabelEn() { return labelEn; }
    public String getColorHex() { return colorHex; }
}
