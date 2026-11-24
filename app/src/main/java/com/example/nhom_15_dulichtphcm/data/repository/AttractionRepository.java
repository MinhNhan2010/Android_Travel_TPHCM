package com.example.nhom_15_dulichtphcm.data.repository;

import android.content.Context;

import com.example.nhom_15_dulichtphcm.data.db.SaigonTravelDatabaseHelper;
import com.example.nhom_15_dulichtphcm.data.local.AuthManager;
import com.example.nhom_15_dulichtphcm.data.model.Attraction;
import com.example.nhom_15_dulichtphcm.data.model.Permission;
import com.example.nhom_15_dulichtphcm.data.model.ResultBox;
import com.example.nhom_15_dulichtphcm.util.SearchUtils;
import com.example.nhom_15_dulichtphcm.util.SeedImageResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SQLite-backed repository for attractions across Ho Chi Minh City, Binh Duong, and Ba Ria - Vung Tau.
 */
public final class AttractionRepository {

    public static final String AUTO_SYNC_ID_PREFIX = "sync_";
    private static final String KEY_AUTO_SYNC_ENABLED = "auto_sync_enabled";
    private static final String KEY_LAST_SYNC_AT = "last_sync_at";
    private static final long SYNC_INTERVAL_MS = 6L * 60L * 60L * 1000L; // 6 hours

    public static final class SyncMergeSummary {
        public final int addedCount;
        public final int updatedCount;
        public final int totalAutoSynced;

        public SyncMergeSummary(int addedCount, int updatedCount, int totalAutoSynced) {
            this.addedCount = addedCount;
            this.updatedCount = updatedCount;
            this.totalAutoSynced = totalAutoSynced;
        }
    }

    private static final List<Attraction> items = new ArrayList<>(AttractionSeedData.createDefaultAttractions());
    private static volatile int contentVersion = 1;
    private static volatile boolean autoSyncEnabled = true;
    private static volatile long lastAutoSyncEpochMs = 0L;
    private static final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    private AttractionRepository() {}

    public static synchronized void initialize(Context context) {
        if (context == null) return;
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.getInstance(context);
        List<Attraction> dbItems = db.getAllAttractions();
        items.clear();
        if (dbItems.isEmpty()) {
            List<Attraction> defaults = AttractionSeedData.createDefaultAttractions();
            items.addAll(defaults);
            db.replaceAllAttractions(defaults);
        } else {
            Map<String, Integer> healedIds = new HashMap<>();
            for (Attraction raw : dbItems) {
                Attraction healed = SeedImageResolver.healAttraction(context.getResources(), raw);
                items.add(healed);
                if (healed.imageResId != raw.imageResId) {
                    healedIds.put(healed.id, healed.imageResId);
                }
            }
            if (!healedIds.isEmpty()) {
                db.updateAttractionImageResIds(healedIds);
            }
        }
        String syncPref = db.getSetting(KEY_AUTO_SYNC_ENABLED, "true");
        autoSyncEnabled = !"false".equalsIgnoreCase(syncPref);
        String lastSync = db.getSetting(KEY_LAST_SYNC_AT, "0");
        try {
            lastAutoSyncEpochMs = Long.parseLong(lastSync);
        } catch (NumberFormatException ignored) {
            lastAutoSyncEpochMs = 0L;
        }
        contentVersion++;
        notifyListeners();
    }

    public static synchronized void resetForTesting() {
        items.clear();
        items.addAll(AttractionSeedData.createDefaultAttractions());
        autoSyncEnabled = true;
        lastAutoSyncEpochMs = 0L;
        contentVersion = 1;
        notifyListeners();
    }

    public static synchronized List<Attraction> getAttractions() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

    public static synchronized Attraction getAttractionById(String id) {
        if (id == null) return null;
        for (Attraction a : items) {
            if (a.id.equals(id)) return a;
        }
        return null;
    }

    public static int getContentVersion() {
        return contentVersion;
    }

    public static boolean isAutoSyncEnabled() {
        return autoSyncEnabled;
    }

    public static long getLastAutoSyncEpochMs() {
        return lastAutoSyncEpochMs;
    }

    public static synchronized void setAutoSyncPreference(boolean enabled) {
        autoSyncEnabled = enabled;
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            db.putSetting(KEY_AUTO_SYNC_ENABLED, String.valueOf(enabled));
        }
        contentVersion++;
        notifyListeners();
    }

    public static synchronized int getAutoSyncedCount() {
        int count = 0;
        for (Attraction a : items) {
            if (a.id != null && a.id.startsWith(AUTO_SYNC_ID_PREFIX)) {
                count++;
            }
        }
        return count;
    }

    public static boolean shouldAutoSyncNow() {
        if (!autoSyncEnabled) return false;
        long now = System.currentTimeMillis();
        return (now - lastAutoSyncEpochMs) >= SYNC_INTERVAL_MS || getAutoSyncedCount() == 0;
    }

    /**
     * Merges live-discovered places into the repository and SQLite, deduplicating by ID,
     * normalized name similarity, or close geographic proximity (< 120 meters).
     */
    public static synchronized SyncMergeSummary mergeAutoSyncedPlaces(List<Attraction> candidates, long syncTimestampMs) {
        if (candidates == null || candidates.isEmpty()) {
            lastAutoSyncEpochMs = syncTimestampMs;
            SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
            if (db != null) {
                db.putSetting(KEY_LAST_SYNC_AT, String.valueOf(syncTimestampMs));
            }
            return new SyncMergeSummary(0, 0, getAutoSyncedCount());
        }

        int added = 0;
        int updated = 0;
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();

        for (Attraction cand : candidates) {
            if (cand == null || cand.nameEn.trim().isEmpty()) continue;

            int existingById = -1;
            boolean duplicateCurated = false;
            String normCandVi = SearchUtils.normalize(cand.nameVi);
            String normCandEn = SearchUtils.normalize(cand.nameEn);

            for (int i = 0; i < items.size(); i++) {
                Attraction cur = items.get(i);
                if (cur.id.equals(cand.id)) {
                    existingById = i;
                    break;
                }
                String normCurVi = SearchUtils.normalize(cur.nameVi);
                String normCurEn = SearchUtils.normalize(cur.nameEn);
                boolean sameName = (!normCandVi.isEmpty() && normCandVi.equals(normCurVi))
                        || (!normCandEn.isEmpty() && normCandEn.equals(normCurEn));
                double distMeters = approxDistanceMeters(cur.latitude, cur.longitude, cand.latitude, cand.longitude);
                if (sameName || distMeters < 110.0) {
                    if (cur.id.startsWith(AUTO_SYNC_ID_PREFIX)) {
                        existingById = i;
                    } else {
                        duplicateCurated = true;
                    }
                    break;
                }
            }

            if (duplicateCurated) {
                continue;
            }

            Attraction normalized = cand.id.startsWith(AUTO_SYNC_ID_PREFIX)
                    ? cand
                    : new Attraction(
                            AUTO_SYNC_ID_PREFIX + cand.id,
                            cand.nameEn,
                            cand.nameVi,
                            cand.category,
                            cand.imageUrl,
                            cand.imageResId,
                            cand.district,
                            cand.address,
                            cand.latitude,
                            cand.longitude,
                            cand.ticketPrice,
                            cand.openingHours,
                            cand.shortSummary,
                            cand.history,
                            cand.culture,
                            cand.foreignerTips,
                            cand.province
                    );

            if (existingById >= 0) {
                items.set(existingById, normalized);
                if (db != null) db.upsertAttraction(normalized, existingById);
                updated++;
            } else {
                items.add(normalized);
                if (db != null) db.upsertAttraction(normalized, items.size() - 1);
                added++;
            }
        }

        lastAutoSyncEpochMs = syncTimestampMs;
        if (db != null) {
            db.putSetting(KEY_LAST_SYNC_AT, String.valueOf(syncTimestampMs));
        }
        if (added > 0 || updated > 0) {
            contentVersion++;
            notifyListeners();
        }
        return new SyncMergeSummary(added, updated, getAutoSyncedCount());
    }

    public static synchronized ResultBox<Attraction> upsertAttraction(Attraction item) {
        ResultBox<Void> perm = AuthManager.requirePermission(Permission.MANAGE_CONTENT);
        if (!perm.isSuccess()) {
            return ResultBox.failure(perm.getError());
        }
        if (item == null || item.id.trim().isEmpty()) {
            return ResultBox.failureMessage("Invalid attraction ID");
        }

        int idx = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).id.equals(item.id)) {
                idx = i;
                break;
            }
        }

        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (idx >= 0) {
            items.set(idx, item);
            if (db != null) db.upsertAttraction(item, idx);
            AuthManager.recordAudit("Cập nhật địa điểm: " + item.nameVi, "Updated attraction: " + item.nameEn);
        } else {
            items.add(0, item);
            if (db != null) db.replaceAllAttractions(items);
            AuthManager.recordAudit("Thêm địa điểm mới: " + item.nameVi, "Created attraction: " + item.nameEn);
        }
        contentVersion++;
        notifyListeners();
        return ResultBox.success(item);
    }

    public static synchronized ResultBox<Void> deleteAttraction(String id) {
        ResultBox<Void> perm = AuthManager.requirePermission(Permission.DELETE_CONTENT);
        if (!perm.isSuccess()) {
            return ResultBox.failure(perm.getError());
        }
        boolean removed = items.removeIf(a -> a.id.equals(id));
        if (!removed) {
            return ResultBox.failureMessage("Attraction not found");
        }
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            db.deleteAttraction(id);
        }
        AuthManager.recordAudit("Xóa địa điểm ID=" + id, "Deleted attraction ID=" + id);
        contentVersion++;
        notifyListeners();
        return ResultBox.success(null);
    }

    public static synchronized ResultBox<Void> resetToDefaults() {
        ResultBox<Void> perm = AuthManager.requirePermission(Permission.DELETE_CONTENT);
        if (!perm.isSuccess()) {
            return ResultBox.failure(perm.getError());
        }
        items.clear();
        items.addAll(AttractionSeedData.createDefaultAttractions());
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            db.replaceAllAttractions(items);
        }
        AuthManager.recordAudit("Khôi phục dữ liệu địa điểm gốc", "Reset attractions to factory defaults");
        contentVersion++;
        notifyListeners();
        return ResultBox.success(null);
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

    private static double approxDistanceMeters(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 6_371_000.0 * 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
    }
}
