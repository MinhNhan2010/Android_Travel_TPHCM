package com.example.nhom_15_dulichtphcm.data.local;

import android.content.Context;

import com.example.nhom_15_dulichtphcm.data.db.SaigonTravelDatabaseHelper;
import com.example.nhom_15_dulichtphcm.data.model.Permission;
import com.example.nhom_15_dulichtphcm.data.model.ResultBox;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Per-user favorites manager persisted in SQLite table favorites (saigon_travel.db).
 */
public final class FavoritesManager {

    private static final String TYPE_ATTRACTION = "attraction";
    private static final String TYPE_FOOD = "food";

    private static final Map<String, Set<String>> memAttractionsByScope = new HashMap<>();
    private static final Map<String, Set<String>> memFoodsByScope = new HashMap<>();

    private static Set<String> attractionIds = new LinkedHashSet<>();
    private static Set<String> foodIds = new LinkedHashSet<>();

    private FavoritesManager() {}

    public static synchronized void initialize(Context context) {
        if (context != null) {
            SaigonTravelDatabaseHelper.getInstance(context);
        }
        reloadActiveScope();
    }

    public static synchronized void reloadActiveScope() {
        String scope = AuthManager.getActiveUserScope();
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            attractionIds = new LinkedHashSet<>(db.getFavoriteIds(scope, TYPE_ATTRACTION));
            foodIds = new LinkedHashSet<>(db.getFavoriteIds(scope, TYPE_FOOD));
        } else {
            attractionIds = memAttractionsByScope.computeIfAbsent(scope, k -> new LinkedHashSet<>());
            foodIds = memFoodsByScope.computeIfAbsent(scope, k -> new LinkedHashSet<>());
        }
    }

    public static synchronized void resetForTesting() {
        memAttractionsByScope.clear();
        memFoodsByScope.clear();
        attractionIds = new LinkedHashSet<>();
        foodIds = new LinkedHashSet<>();
        reloadActiveScope();
    }

    public static synchronized Set<String> getAttractionIds() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(attractionIds));
    }

    public static synchronized Set<String> getFoodIds() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(foodIds));
    }

    public static synchronized int getTotalCount() {
        return attractionIds.size() + foodIds.size();
    }

    public static synchronized boolean isAttractionFavorite(String id) {
        return id != null && attractionIds.contains(id);
    }

    public static synchronized boolean isFoodFavorite(String id) {
        return id != null && foodIds.contains(id);
    }

    public static synchronized ResultBox<Boolean> toggleAttraction(String id) {
        ResultBox<Void> perm = AuthManager.requirePermission(Permission.SAVE_PERSONAL_COLLECTION);
        if (perm.isFailure()) return ResultBox.failure(perm.exceptionOrNull());
        if (id == null || id.isEmpty()) return ResultBox.failureMessage("Invalid id");

        boolean next = !attractionIds.contains(id);
        if (next) attractionIds.add(id); else attractionIds.remove(id);

        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            db.setFavorite(AuthManager.getActiveUserScope(), TYPE_ATTRACTION, id, next);
        }
        return ResultBox.success(next);
    }

    public static synchronized ResultBox<Boolean> toggleFood(String id) {
        ResultBox<Void> perm = AuthManager.requirePermission(Permission.SAVE_PERSONAL_COLLECTION);
        if (perm.isFailure()) return ResultBox.failure(perm.exceptionOrNull());
        if (id == null || id.isEmpty()) return ResultBox.failureMessage("Invalid id");

        boolean next = !foodIds.contains(id);
        if (next) foodIds.add(id); else foodIds.remove(id);

        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            db.setFavorite(AuthManager.getActiveUserScope(), TYPE_FOOD, id, next);
        }
        return ResultBox.success(next);
    }
}
