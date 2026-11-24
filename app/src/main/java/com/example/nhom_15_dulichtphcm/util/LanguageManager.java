package com.example.nhom_15_dulichtphcm.util;

import android.content.Context;

import com.example.nhom_15_dulichtphcm.data.db.SaigonTravelDatabaseHelper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages active UI language persisted in SQLite table app_settings (saigon_travel.db).
 */
public final class LanguageManager {

    private static final String KEY_LANG = "selected_lang";
    private static volatile AppLanguage currentLanguage = AppLanguage.VIETNAMESE;
    private static final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    private LanguageManager() {}

    public static AppLanguage getCurrentLanguage() {
        return currentLanguage;
    }

    public static void initialize(Context context) {
        if (context == null) return;
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.getInstance(context);
        String saved = db.getSetting(KEY_LANG, AppLanguage.VIETNAMESE.code);
        currentLanguage = AppLanguage.fromCode(saved);
    }

    public static void setLanguage(Context context, AppLanguage language) {
        if (language == null) return;
        currentLanguage = language;
        SaigonTravelDatabaseHelper db = context != null
                ? SaigonTravelDatabaseHelper.getInstance(context)
                : SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            db.putSetting(KEY_LANG, language.code);
        }
        notifyListeners();
    }

    public static void setLanguageForTest(AppLanguage language) {
        if (language != null) {
            currentLanguage = language;
        }
    }

    public static void addListener(Runnable listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public static void removeListener(Runnable listener) {
        listeners.remove(listener);
    }

    private static void notifyListeners() {
        for (Runnable r : listeners) {
            r.run();
        }
    }
}
