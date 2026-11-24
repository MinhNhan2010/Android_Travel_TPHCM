package com.example.nhom_15_dulichtphcm.data.local;

/**
 * Gamified Explorer Rank tiers earned by collecting Travel Passport stamps.
 */
public enum ExplorerRank {
    NEWCOMER(0, "🌱", "Tân binh Sài Gòn", "Saigon Newcomer"),
    CITY_WALKER(3, "🚶", "Người Lữ Hành Phố Thị", "Urban Explorer"),
    HERITAGE_HUNTER(7, "🏛️", "Thợ Săn Di Sản", "Heritage Hunter"),
    SOUTHERN_AMBASSADOR(12, "👑", "Đại Sứ Siêu Đô Thị", "Southern Megacity Ambassador");

    public final int minStamps;
    public final String badgeEmoji;
    public final String titleVi;
    public final String titleEn;

    ExplorerRank(int minStamps, String badgeEmoji, String titleVi, String titleEn) {
        this.minStamps = minStamps;
        this.badgeEmoji = badgeEmoji;
        this.titleVi = titleVi;
        this.titleEn = titleEn;
    }

    public static ExplorerRank forCount(int count) {
        ExplorerRank result = NEWCOMER;
        for (ExplorerRank rank : values()) {
            if (count >= rank.minStamps) {
                result = rank;
            }
        }
        return result;
    }

    public static ExplorerRank nextAfter(int count) {
        for (ExplorerRank rank : values()) {
            if (rank.minStamps > count) {
                return rank;
            }
        }
        return null;
    }
}
