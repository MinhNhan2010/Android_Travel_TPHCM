package com.example.nhom_15_dulichtphcm.data.local;

import android.content.Context;

import com.example.nhom_15_dulichtphcm.data.db.SaigonTravelDatabaseHelper;
import com.example.nhom_15_dulichtphcm.data.model.Permission;
import com.example.nhom_15_dulichtphcm.data.model.ResultBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Per-user trip itinerary manager persisted in SQLite tables trip_stops & trip_settings (saigon_travel.db).
 */
public final class TripPlanManager {

    public static final int DEFAULT_START_MINUTES = 8 * 60 + 30; // 08:30

    private static final Map<String, List<String>> memStopsByScope = new HashMap<>();
    private static final Map<String, Integer> memStartByScope = new HashMap<>();

    private static List<String> stopIds = new ArrayList<>();
    private static int startMinutes = DEFAULT_START_MINUTES;

    private TripPlanManager() {}

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
            stopIds = new ArrayList<>(db.getTripStopIds(scope));
            startMinutes = db.getTripStartMinutes(scope, DEFAULT_START_MINUTES);
        } else {
            stopIds = memStopsByScope.computeIfAbsent(scope, k -> new ArrayList<>());
            startMinutes = memStartByScope.getOrDefault(scope, DEFAULT_START_MINUTES);
        }
    }

    public static synchronized void resetForTesting() {
        memStopsByScope.clear();
        memStartByScope.clear();
        stopIds = new ArrayList<>();
        startMinutes = DEFAULT_START_MINUTES;
        reloadActiveScope();
    }

    public static synchronized List<String> getStopIds() {
        return Collections.unmodifiableList(new ArrayList<>(stopIds));
    }

    public static synchronized int getStartMinutes() {
        return startMinutes;
    }

    public static synchronized boolean contains(String attractionId) {
        return attractionId != null && stopIds.contains(attractionId);
    }

    public static synchronized ResultBox<Boolean> toggle(String attractionId) {
        ResultBox<Void> perm = AuthManager.requirePermission(Permission.SAVE_PERSONAL_COLLECTION);
        if (perm.isFailure()) return ResultBox.failure(perm.exceptionOrNull());
        if (attractionId == null || attractionId.isEmpty()) return ResultBox.failureMessage("Invalid id");

        boolean added;
        if (stopIds.contains(attractionId)) {
            stopIds.remove(attractionId);
            added = false;
        } else {
            stopIds.add(attractionId);
            added = true;
        }
        persistStops();
        return ResultBox.success(added);
    }

    public static synchronized ResultBox<Void> add(String attractionId) {
        ResultBox<Void> perm = AuthManager.requirePermission(Permission.SAVE_PERSONAL_COLLECTION);
        if (perm.isFailure()) return perm;
        if (attractionId != null && !attractionId.isEmpty() && !stopIds.contains(attractionId)) {
            stopIds.add(attractionId);
            persistStops();
        }
        return ResultBox.success(null);
    }

    public static synchronized void remove(String attractionId) {
        if (stopIds.remove(attractionId)) {
            persistStops();
        }
    }

    public static synchronized void move(int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex >= stopIds.size() || toIndex < 0 || toIndex >= stopIds.size()) {
            return;
        }
        String item = stopIds.remove(fromIndex);
        stopIds.add(toIndex, item);
        persistStops();
    }

    public static synchronized ResultBox<Void> replaceOrder(List<String> newOrder) {
        ResultBox<Void> perm = AuthManager.requirePermission(Permission.SAVE_PERSONAL_COLLECTION);
        if (perm.isFailure()) return perm;
        stopIds.clear();
        if (newOrder != null) {
            for (String id : newOrder) {
                if (id != null && !id.isEmpty() && !stopIds.contains(id)) {
                    stopIds.add(id);
                }
            }
        }
        persistStops();
        return ResultBox.success(null);
    }

    public static synchronized void clear() {
        stopIds.clear();
        persistStops();
    }

    public static synchronized void updateStartMinutes(int minutes) {
        int clamped = Math.max(5 * 60, Math.min(20 * 60, minutes));
        startMinutes = clamped;
        String scope = AuthManager.getActiveUserScope();
        memStartByScope.put(scope, clamped);
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            db.setTripStartMinutes(scope, clamped);
        }
    }

    private static void persistStops() {
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            db.replaceTripStops(AuthManager.getActiveUserScope(), stopIds);
        }
    }
}
