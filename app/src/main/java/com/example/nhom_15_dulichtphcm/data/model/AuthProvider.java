package com.example.nhom_15_dulichtphcm.data.model;

import com.example.nhom_15_dulichtphcm.util.Localized;

public enum AuthProvider {
    LOCAL("local", "Tài khoản nội bộ", "Local Account", "Local", 0xFF00796B),
    GOOGLE("google", "Google (Gmail)", "Google (Gmail)", "Google", 0xFFDB4437),
    FACEBOOK("facebook", "Facebook", "Facebook", "Facebook", 0xFF1877F2),
    ZALO("zalo", "Zalo", "Zalo", "Zalo", 0xFF0068FF),
    APPLE("apple", "Apple ID", "Apple ID", "Apple", 0xFF1E293B);

    public final String code;
    public final String nameVi;
    public final String nameEn;
    public final String badgeLabel;
    public final int brandColorHex;

    AuthProvider(String code, String nameVi, String nameEn, String badgeLabel, int brandColorHex) {
        this.code = code;
        this.nameVi = nameVi;
        this.nameEn = nameEn;
        this.badgeLabel = badgeLabel;
        this.brandColorHex = brandColorHex;
    }

    public String localizedName() {
        return Localized.tr(nameVi, nameEn);
    }

    public static AuthProvider fromCode(String raw) {
        if (raw == null) return LOCAL;
        for (AuthProvider p : values()) {
            if (p.code.equalsIgnoreCase(raw.trim()) || p.name().equalsIgnoreCase(raw.trim())) {
                return p;
            }
        }
        return LOCAL;
    }
}
