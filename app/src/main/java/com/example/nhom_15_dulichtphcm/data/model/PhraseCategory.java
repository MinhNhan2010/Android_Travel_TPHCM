package com.example.nhom_15_dulichtphcm.data.model;

public enum PhraseCategory {
    ALL("All Phrases", "Tất cả", "💬"),
    GREETING("Greetings", "Chào hỏi", "👋"),
    SHOPPING("Shopping", "Mua sắm", "🛍️"),
    DINING("Dining & Food", "Ăn uống", "🍜"),
    DIRECTION("Directions", "Chỉ đường", "🧭"),
    EMERGENCY("Emergency", "Khẩn cấp", "🆘");

    public final String displayName;
    public final String displayNameVi;
    public final String emoji;

    PhraseCategory(String displayName, String displayNameVi, String emoji) {
        this.displayName = displayName;
        this.displayNameVi = displayNameVi;
        this.emoji = emoji;
    }

    public String getDisplayName() { return displayName; }
    public String getDisplayNameVi() { return displayNameVi; }
    public String getEmoji() { return emoji; }

    public static PhraseCategory fromName(String raw) {
        if (raw == null) return GREETING;
        for (PhraseCategory cat : values()) {
            if (cat.name().equalsIgnoreCase(raw.trim())) {
                return cat;
            }
        }
        return GREETING;
    }
}
