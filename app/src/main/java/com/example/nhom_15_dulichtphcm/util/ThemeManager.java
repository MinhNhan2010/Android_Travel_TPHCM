package com.example.nhom_15_dulichtphcm.util;

import android.content.Context;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.nhom_15_dulichtphcm.data.db.SaigonTravelDatabaseHelper;

/**
 * Manages Light / Dark / System UI theme persisted in SQLite table app_settings (saigon_travel.db).
 * Zero SharedPreferences usage.
 */
public final class ThemeManager {

    public enum ThemeMode {
        LIGHT("light", AppCompatDelegate.MODE_NIGHT_NO),
        DARK("dark", AppCompatDelegate.MODE_NIGHT_YES),
        SYSTEM("system", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        public final String code;
        public final int nightModeFlag;

        ThemeMode(String code, int nightModeFlag) {
            this.code = code;
            this.nightModeFlag = nightModeFlag;
        }

        public static ThemeMode fromCode(String raw) {
            if (raw != null) {
                for (ThemeMode m : values()) {
                    if (m.code.equalsIgnoreCase(raw.trim())) {
                        return m;
                    }
                }
            }
            return LIGHT;
        }
    }

    private static final String KEY_THEME_MODE = "ui_theme_mode";
    private static volatile ThemeMode currentMode = ThemeMode.LIGHT;

    private ThemeManager() {}

    public static ThemeMode getCurrentMode() {
        return currentMode;
    }

    public static void initialize(Context context) {
        if (context == null) return;
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.getInstance(context);
        String saved = db.getSetting(KEY_THEME_MODE, ThemeMode.LIGHT.code);
        currentMode = ThemeMode.fromCode(saved);
        if (AppCompatDelegate.getDefaultNightMode() != currentMode.nightModeFlag) {
            AppCompatDelegate.setDefaultNightMode(currentMode.nightModeFlag);
        }
    }

    public static void setThemeMode(Context context, ThemeMode mode) {
        if (mode == null) return;
        currentMode = mode;
        SaigonTravelDatabaseHelper db = context != null
                ? SaigonTravelDatabaseHelper.getInstance(context)
                : SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            db.putSetting(KEY_THEME_MODE, mode.code);
        }
        AppCompatDelegate.setDefaultNightMode(mode.nightModeFlag);
    }

    public static boolean isDarkActive(Context context) {
        if (currentMode == ThemeMode.DARK) return true;
        if (currentMode == ThemeMode.LIGHT) return false;
        if (context == null) return false;
        int nightMask = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightMask == Configuration.UI_MODE_NIGHT_YES;
    }

    public static void toggleLightDark(Context context) {
        boolean darkNow = isDarkActive(context);
        setThemeMode(context, darkNow ? ThemeMode.LIGHT : ThemeMode.DARK);
    }
}
