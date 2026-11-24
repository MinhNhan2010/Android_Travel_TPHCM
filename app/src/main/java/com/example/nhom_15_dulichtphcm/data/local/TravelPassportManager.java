package com.example.nhom_15_dulichtphcm.data.local;

import android.content.Context;

import com.example.nhom_15_dulichtphcm.data.db.SaigonTravelDatabaseHelper;
import com.example.nhom_15_dulichtphcm.data.model.Attraction;
import com.example.nhom_15_dulichtphcm.data.model.Permission;
import com.example.nhom_15_dulichtphcm.data.model.ResultBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Digital Travel Passport manager persisted in SQLite table passport_stamps.
 */
public final class TravelPassportManager {

    private static final List<CheckInStamp> stamps = new ArrayList<>();
    private static final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    private TravelPassportManager() {}

    public static synchronized void initialize(Context context) {
        if (context != null) {
            SaigonTravelDatabaseHelper.getInstance(context);
        }
        reloadActiveScope();
    }

    public static synchronized void reloadActiveScope() {
        stamps.clear();
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            String scope = AuthManager.getActiveUserScope();
            for (SaigonTravelDatabaseHelper.StampRow row : db.getPassportStamps(scope)) {
                stamps.add(new CheckInStamp(row.attractionId, row.attractionName, row.province, row.checkedInAt));
            }
        }
        notifyListeners();
    }

    public static synchronized void resetForTesting() {
        stamps.clear();
        notifyListeners();
    }

    public static synchronized List<CheckInStamp> getStamps() {
        return Collections.unmodifiableList(new ArrayList<>(stamps));
    }

    public static synchronized boolean isCheckedIn(String attractionId) {
        if (attractionId == null) return false;
        for (CheckInStamp s : stamps) {
            if (s.attractionId.equals(attractionId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Toggles check-in stamp for an attraction. Requires SAVE_PERSONAL_COLLECTION permission.
     * Returns ResultBox<Boolean> where true = stamped, false = removed.
     */
    public static synchronized ResultBox<Boolean> toggleCheckIn(Attraction attraction) {
        ResultBox<Void> perm = AuthManager.requirePermission(Permission.SAVE_PERSONAL_COLLECTION);
        if (!perm.isSuccess()) {
            return ResultBox.failure(perm.getError());
        }
        if (attraction == null) {
            return ResultBox.failureMessage("Invalid attraction");
        }
        String scope = AuthManager.getActiveUserScope();
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();

        for (int i = 0; i < stamps.size(); i++) {
            if (stamps.get(i).attractionId.equals(attraction.id)) {
                stamps.remove(i);
                if (db != null) {
                    db.deletePassportStamp(scope, attraction.id);
                }
                notifyListeners();
                return ResultBox.success(Boolean.FALSE);
            }
        }

        long now = System.currentTimeMillis();
        CheckInStamp stamp = new CheckInStamp(attraction.id, attraction.nameVi, attraction.province, now);
        stamps.add(0, stamp);
        if (db != null) {
            db.upsertPassportStamp(scope, attraction.id, attraction.nameVi, attraction.province, now);
        }
        notifyListeners();
        return ResultBox.success(Boolean.TRUE);
    }

    public static synchronized ExplorerRank currentRank() {
        return ExplorerRank.forCount(stamps.size());
    }

    public static synchronized ExplorerRank nextRank() {
        return ExplorerRank.nextAfter(stamps.size());
    }

    /**
     * Returns regional stamp counts as int[]{hcmCount, binhDuongCount, vungTauCount}.
     */
    public static synchronized int[] regionalCounts() {
        int hcm = 0;
        int bd = 0;
        int vt = 0;
        for (CheckInStamp s : stamps) {
            String p = s.province != null ? s.province.toLowerCase(Locale.ROOT) : "";
            if (p.contains("bình dương") || p.contains("binh duong")) {
                bd++;
            } else if (p.contains("vũng tàu") || p.contains("vung tau") || p.contains("bà rịa")) {
                vt++;
            } else {
                hcm++;
            }
        }
        return new int[]{hcm, bd, vt};
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
