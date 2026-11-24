package com.example.nhom_15_dulichtphcm.data.repository;

import android.content.Context;

import com.example.nhom_15_dulichtphcm.data.db.SaigonTravelDatabaseHelper;
import com.example.nhom_15_dulichtphcm.data.local.AuthManager;
import com.example.nhom_15_dulichtphcm.data.model.Permission;
import com.example.nhom_15_dulichtphcm.data.model.PublicUtility;
import com.example.nhom_15_dulichtphcm.data.model.ResultBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SQLite-backed repository for essential public utilities (Metro, Ferry, Hospitals, Embassies, SIM/ATM).
 */
public final class UtilityRepository {

    private static final List<PublicUtility> items = new ArrayList<>(UtilitySeedData.createDefaultUtilities());
    private static volatile int contentVersion = 1;
    private static final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    private UtilityRepository() {}

    public static synchronized void initialize(Context context) {
        if (context == null) return;
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.getInstance(context);
        List<PublicUtility> dbItems = db.getAllUtilities();
        items.clear();
        if (dbItems.isEmpty()) {
            List<PublicUtility> defaults = UtilitySeedData.createDefaultUtilities();
            items.addAll(defaults);
            db.replaceAllUtilities(defaults);
        } else {
            items.addAll(dbItems);
        }
        contentVersion++;
        notifyListeners();
    }

    public static synchronized void resetForTesting() {
        items.clear();
        items.addAll(UtilitySeedData.createDefaultUtilities());
        contentVersion = 1;
        notifyListeners();
    }

    public static synchronized List<PublicUtility> getUtilities() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

    public static int getContentVersion() {
        return contentVersion;
    }

    public static synchronized ResultBox<PublicUtility> upsertUtility(PublicUtility util) {
        ResultBox<Void> perm = AuthManager.requirePermission(Permission.MANAGE_CONTENT);
        if (!perm.isSuccess()) {
            return ResultBox.failure(perm.getError());
        }
        if (util == null || util.id.trim().isEmpty()) {
            return ResultBox.failureMessage("Invalid utility ID");
        }
        int idx = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).id.equals(util.id)) {
                idx = i;
                break;
            }
        }
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (idx >= 0) {
            items.set(idx, util);
            if (db != null) db.upsertUtility(util, idx);
            AuthManager.recordAudit("Cập nhật tiện ích: " + util.nameVi, "Updated utility: " + util.nameEn);
        } else {
            items.add(0, util);
            if (db != null) db.replaceAllUtilities(items);
            AuthManager.recordAudit("Thêm tiện ích mới: " + util.nameVi, "Created utility: " + util.nameEn);
        }
        contentVersion++;
        notifyListeners();
        return ResultBox.success(util);
    }

    public static synchronized ResultBox<Void> deleteUtility(String id) {
        ResultBox<Void> perm = AuthManager.requirePermission(Permission.DELETE_CONTENT);
        if (!perm.isSuccess()) {
            return ResultBox.failure(perm.getError());
        }
        boolean removed = items.removeIf(u -> u.id.equals(id));
        if (!removed) {
            return ResultBox.failureMessage("Utility not found");
        }
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            db.deleteUtility(id);
        }
        AuthManager.recordAudit("Xóa tiện ích ID=" + id, "Deleted utility ID=" + id);
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
        items.addAll(UtilitySeedData.createDefaultUtilities());
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            db.replaceAllUtilities(items);
        }
        AuthManager.recordAudit("Khôi phục dữ liệu tiện ích gốc", "Reset utilities to factory defaults");
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
}
