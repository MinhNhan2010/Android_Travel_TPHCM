package com.example.nhom_15_dulichtphcm.data.repository;

import android.content.Context;

import com.example.nhom_15_dulichtphcm.data.db.SaigonTravelDatabaseHelper;
import com.example.nhom_15_dulichtphcm.data.local.AuthManager;
import com.example.nhom_15_dulichtphcm.data.model.Permission;
import com.example.nhom_15_dulichtphcm.data.model.ResultBox;
import com.example.nhom_15_dulichtphcm.data.model.SpecialtyFood;
import com.example.nhom_15_dulichtphcm.util.SeedImageResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SQLite-backed repository for Southern Vietnamese specialty foods and curated restaurants.
 */
public final class FoodRepository {

    private static final List<SpecialtyFood> items = new ArrayList<>(FoodSeedData.createDefaultFoods());
    private static volatile int contentVersion = 1;
    private static final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    private FoodRepository() {}

    public static synchronized void initialize(Context context) {
        if (context == null) return;
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.getInstance(context);
        List<SpecialtyFood> dbFoods = db.getAllFoods();
        items.clear();
        if (dbFoods.isEmpty()) {
            List<SpecialtyFood> defaults = FoodSeedData.createDefaultFoods();
            items.addAll(defaults);
            db.replaceAllFoods(defaults);
        } else {
            Map<String, Integer> healedIds = new HashMap<>();
            for (SpecialtyFood raw : dbFoods) {
                SpecialtyFood healed = SeedImageResolver.healFood(context.getResources(), raw);
                items.add(healed);
                if (healed.imageResId != raw.imageResId) {
                    healedIds.put(healed.id, healed.imageResId);
                }
            }
            if (!healedIds.isEmpty()) {
                db.updateFoodImageResIds(healedIds);
            }
        }
        contentVersion++;
        notifyListeners();
    }

    public static synchronized void resetForTesting() {
        items.clear();
        items.addAll(FoodSeedData.createDefaultFoods());
        contentVersion = 1;
        notifyListeners();
    }

    public static synchronized List<SpecialtyFood> getSpecialtyFoods() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

    public static synchronized SpecialtyFood getFoodById(String id) {
        if (id == null) return null;
        for (SpecialtyFood f : items) {
            if (f.id.equals(id)) return f;
        }
        return null;
    }

    public static int getContentVersion() {
        return contentVersion;
    }

    public static synchronized ResultBox<SpecialtyFood> upsertFood(SpecialtyFood food) {
        ResultBox<Void> perm = AuthManager.requirePermission(Permission.MANAGE_CONTENT);
        if (!perm.isSuccess()) {
            return ResultBox.failure(perm.getError());
        }
        if (food == null || food.id.trim().isEmpty()) {
            return ResultBox.failureMessage("Invalid food ID");
        }
        int idx = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).id.equals(food.id)) {
                idx = i;
                break;
            }
        }
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (idx >= 0) {
            items.set(idx, food);
            if (db != null) db.upsertFood(food, idx);
            AuthManager.recordAudit("Cập nhật món ăn: " + food.nameVi, "Updated food: " + food.nameEn);
        } else {
            items.add(0, food);
            if (db != null) db.replaceAllFoods(items);
            AuthManager.recordAudit("Thêm món ăn mới: " + food.nameVi, "Created food: " + food.nameEn);
        }
        contentVersion++;
        notifyListeners();
        return ResultBox.success(food);
    }

    public static synchronized ResultBox<Void> deleteFood(String id) {
        ResultBox<Void> perm = AuthManager.requirePermission(Permission.DELETE_CONTENT);
        if (!perm.isSuccess()) {
            return ResultBox.failure(perm.getError());
        }
        boolean removed = items.removeIf(f -> f.id.equals(id));
        if (!removed) {
            return ResultBox.failureMessage("Food not found");
        }
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            db.deleteFood(id);
        }
        AuthManager.recordAudit("Xóa món ăn ID=" + id, "Deleted food ID=" + id);
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
        items.addAll(FoodSeedData.createDefaultFoods());
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            db.replaceAllFoods(items);
        }
        AuthManager.recordAudit("Khôi phục dữ liệu ẩm thực gốc", "Reset foods to factory defaults");
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
