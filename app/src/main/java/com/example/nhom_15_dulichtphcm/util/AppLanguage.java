package com.example.nhom_15_dulichtphcm.util;

public enum AppLanguage {
    ENGLISH("en", "English", "🇺🇸"),
    VIETNAMESE("vi", "Tiếng Việt", "🇻🇳"),
    FRENCH("fr", "Français", "🇫🇷"),
    JAPANESE("ja", "日本語", "🇯🇵"),
    KOREAN("ko", "한국어", "🇰🇷"),
    CHINESE("zh", "中文", "🇨🇳");

    public final String code;
    public final String displayName;
    public final String flag;

    AppLanguage(String code, String displayName, String flag) {
        this.code = code;
        this.displayName = displayName;
        this.flag = flag;
    }

    public static AppLanguage fromCode(String raw) {
        if (raw == null) return ENGLISH;
        for (AppLanguage lang : values()) {
            if (lang.code.equalsIgnoreCase(raw.trim())) {
                return lang;
            }
        }
        return ENGLISH;
    }
}
