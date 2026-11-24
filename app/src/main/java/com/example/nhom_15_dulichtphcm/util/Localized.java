package com.example.nhom_15_dulichtphcm.util;

import java.util.Locale;

/**
 * Fast bilingual & VND formatting utilities for Java UI and models.
 */
public final class Localized {

    private Localized() {}

    public static String tr(String vi, String en) {
        return LanguageManager.getCurrentLanguage() == AppLanguage.VIETNAMESE ? vi : en;
    }

    public static boolean isVietnameseUi() {
        return LanguageManager.getCurrentLanguage() == AppLanguage.VIETNAMESE;
    }

    public static String formatVnd(Number amount) {
        long val = amount != null ? amount.longValue() : 0L;
        return String.format(Locale.US, "%,d₫", val).replace(',', '.');
    }
}
