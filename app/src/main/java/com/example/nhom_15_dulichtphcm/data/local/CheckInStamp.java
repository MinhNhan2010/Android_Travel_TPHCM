package com.example.nhom_15_dulichtphcm.data.local;

/**
 * Digital passport check-in stamp model backed by SQLite table passport_stamps.
 */
public final class CheckInStamp {
    public final String attractionId;
    public final String attractionName;
    public final String province;
    public final long checkedInAt;

    public CheckInStamp(String attractionId, String attractionName, String province, long checkedInAt) {
        this.attractionId = attractionId != null ? attractionId : "";
        this.attractionName = attractionName != null ? attractionName : "";
        this.province = province != null ? province : "TP.HCM";
        this.checkedInAt = checkedInAt;
    }
}
