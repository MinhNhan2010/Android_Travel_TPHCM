package com.example.nhom_15_dulichtphcm.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.nhom_15_dulichtphcm.data.model.Attraction;
import com.example.nhom_15_dulichtphcm.data.model.AttractionCategory;
import com.example.nhom_15_dulichtphcm.data.model.AuditLogEntry;
import com.example.nhom_15_dulichtphcm.data.model.AuthProvider;
import com.example.nhom_15_dulichtphcm.data.model.FoodCategory;
import com.example.nhom_15_dulichtphcm.data.model.Permission;
import com.example.nhom_15_dulichtphcm.data.model.Phrase;
import com.example.nhom_15_dulichtphcm.data.model.PhraseCategory;
import com.example.nhom_15_dulichtphcm.data.model.PlaceReview;
import com.example.nhom_15_dulichtphcm.data.model.PublicUtility;
import com.example.nhom_15_dulichtphcm.data.model.Restaurant;
import com.example.nhom_15_dulichtphcm.data.model.SpecialtyFood;
import com.example.nhom_15_dulichtphcm.data.model.UserAccount;
import com.example.nhom_15_dulichtphcm.data.model.UserRole;
import com.example.nhom_15_dulichtphcm.data.model.UtilityCategory;
import com.example.nhom_15_dulichtphcm.data.repository.AttractionSeedData;
import com.example.nhom_15_dulichtphcm.data.repository.FoodSeedData;
import com.example.nhom_15_dulichtphcm.data.repository.PhraseSeedData;
import com.example.nhom_15_dulichtphcm.data.repository.UtilitySeedData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Native SQLiteOpenHelper managing all local relational tables in saigon_travel.db.
 * Replaces SharedPreferences completely across the entire application.
 */
public final class SaigonTravelDatabaseHelper extends SQLiteOpenHelper {

    private static final String PIPE_SEP = "||";
    private static volatile SaigonTravelDatabaseHelper instance;

    public static synchronized SaigonTravelDatabaseHelper getInstance(Context context) {
        if (instance == null && context != null) {
            instance = new SaigonTravelDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public static SaigonTravelDatabaseHelper peekInstance() {
        return instance;
    }

    private SaigonTravelDatabaseHelper(Context context) {
        super(context, SaigonTravelContract.DATABASE_NAME, null, SaigonTravelContract.DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            for (String ddl : SaigonTravelContract.ALL_CREATE_STATEMENTS) {
                db.execSQL(ddl);
            }
            seedInitialMasterData(db);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.beginTransaction();
        try {
            // Non-destructive additive schema migration so existing user data is preserved
            for (String ddl : SaigonTravelContract.ALL_CREATE_STATEMENTS) {
                db.execSQL(ddl);
            }
            ensureColumnExists(db, SaigonTravelContract.TABLE_USERS, "avatar_photo_path", "TEXT NOT NULL DEFAULT ''");
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void ensureColumnExists(SQLiteDatabase db, String tableName, String columnName, String columnDef) {
        boolean found = false;
        try (Cursor c = db.rawQuery("PRAGMA table_info(" + tableName + ")", null)) {
            int nameIdx = c.getColumnIndex("name");
            while (c.moveToNext()) {
                if (nameIdx >= 0 && columnName.equalsIgnoreCase(c.getString(nameIdx))) {
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDef);
        }
    }

    private void seedInitialMasterData(SQLiteDatabase db) {
        List<Attraction> attractions = AttractionSeedData.createDefaultAttractions();
        for (int i = 0; i < attractions.size(); i++) {
            insertAttractionRow(db, attractions.get(i), i);
        }

        List<SpecialtyFood> foods = FoodSeedData.createDefaultFoods();
        for (int i = 0; i < foods.size(); i++) {
            insertFoodAndRestaurants(db, foods.get(i), i);
        }

        List<PublicUtility> utilities = UtilitySeedData.createDefaultUtilities();
        for (int i = 0; i < utilities.size(); i++) {
            insertUtilityRow(db, utilities.get(i), i);
        }

        List<Phrase> phrases = PhraseSeedData.createDefaultPhrases();
        for (int i = 0; i < phrases.size(); i++) {
            insertPhraseRow(db, phrases.get(i), i);
        }
    }

    // =========================================================================
    // 1. APP SETTINGS (Key-Value table inside SQLite)
    // =========================================================================

    public String getSetting(String key, String defaultValue) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(
                SaigonTravelContract.TABLE_APP_SETTINGS,
                new String[]{"setting_value"},
                "setting_key = ?",
                new String[]{key},
                null, null, null)) {
            if (c.moveToFirst()) {
                return c.getString(0);
            }
        }
        return defaultValue;
    }

    public void putSetting(String key, String value) {
        if (key == null) return;
        if (value == null) {
            removeSetting(key);
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("setting_key", key);
        cv.put("setting_value", value);
        db.insertWithOnConflict(SaigonTravelContract.TABLE_APP_SETTINGS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void removeSetting(String key) {
        if (key == null) return;
        SQLiteDatabase db = getWritableDatabase();
        db.delete(SaigonTravelContract.TABLE_APP_SETTINGS, "setting_key = ?", new String[]{key});
    }

    // =========================================================================
    // 2. ATTRACTIONS
    // =========================================================================

    private void insertAttractionRow(SQLiteDatabase db, Attraction a, int sortOrder) {
        ContentValues cv = new ContentValues();
        cv.put("id", a.id);
        cv.put("name_en", a.nameEn);
        cv.put("name_vi", a.nameVi);
        cv.put("category", a.category.name());
        cv.put("image_url", a.imageUrl);
        cv.put("image_res_id", a.imageResId);
        cv.put("district", a.district);
        cv.put("address", a.address);
        cv.put("latitude", a.latitude);
        cv.put("longitude", a.longitude);
        cv.put("ticket_price", a.ticketPrice);
        cv.put("opening_hours", a.openingHours);
        cv.put("short_summary", a.shortSummary);
        cv.put("history", a.history);
        cv.put("culture", a.culture);
        cv.put("foreigner_tips", a.foreignerTips);
        cv.put("province", a.province);
        cv.put("sort_order", sortOrder);
        db.insertWithOnConflict(SaigonTravelContract.TABLE_ATTRACTIONS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public List<Attraction> getAllAttractions() {
        List<Attraction> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(SaigonTravelContract.TABLE_ATTRACTIONS, null, null, null, null, null, "sort_order ASC")) {
            while (c.moveToNext()) {
                list.add(new Attraction(
                        c.getString(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("name_en")),
                        c.getString(c.getColumnIndexOrThrow("name_vi")),
                        AttractionCategory.fromName(c.getString(c.getColumnIndexOrThrow("category"))),
                        c.getString(c.getColumnIndexOrThrow("image_url")),
                        c.getInt(c.getColumnIndexOrThrow("image_res_id")),
                        c.getString(c.getColumnIndexOrThrow("district")),
                        c.getString(c.getColumnIndexOrThrow("address")),
                        c.getDouble(c.getColumnIndexOrThrow("latitude")),
                        c.getDouble(c.getColumnIndexOrThrow("longitude")),
                        c.getString(c.getColumnIndexOrThrow("ticket_price")),
                        c.getString(c.getColumnIndexOrThrow("opening_hours")),
                        c.getString(c.getColumnIndexOrThrow("short_summary")),
                        c.getString(c.getColumnIndexOrThrow("history")),
                        c.getString(c.getColumnIndexOrThrow("culture")),
                        c.getString(c.getColumnIndexOrThrow("foreigner_tips")),
                        c.getString(c.getColumnIndexOrThrow("province"))
                ));
            }
        }
        return list;
    }

    public void replaceAllAttractions(List<Attraction> list) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(SaigonTravelContract.TABLE_ATTRACTIONS, null, null);
            for (int i = 0; i < list.size(); i++) {
                insertAttractionRow(db, list.get(i), i);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void upsertAttraction(Attraction a, int sortOrder) {
        SQLiteDatabase db = getWritableDatabase();
        insertAttractionRow(db, a, sortOrder);
    }

    public void updateAttractionImageResIds(Map<String, Integer> healedIds) {
        if (healedIds == null || healedIds.isEmpty()) return;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (Map.Entry<String, Integer> entry : healedIds.entrySet()) {
                ContentValues cv = new ContentValues();
                cv.put("image_res_id", entry.getValue());
                db.update(SaigonTravelContract.TABLE_ATTRACTIONS, cv, "id = ?", new String[]{entry.getKey()});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void deleteAttraction(String id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(SaigonTravelContract.TABLE_ATTRACTIONS, "id = ?", new String[]{id});
    }

    // =========================================================================
    // 3. FOODS & RESTAURANTS
    // =========================================================================

    private void insertFoodAndRestaurants(SQLiteDatabase db, SpecialtyFood f, int sortOrder) {
        ContentValues cv = new ContentValues();
        cv.put("id", f.id);
        cv.put("name_en", f.nameEn);
        cv.put("name_vi", f.nameVi);
        cv.put("category", f.category.name());
        cv.put("image_url", f.imageUrl);
        cv.put("image_res_id", f.imageResId);
        cv.put("short_description", f.shortDescription);
        cv.put("story_and_history", f.storyAndHistory);
        cv.put("how_to_eat", f.howToEatLikeALocal);
        cv.put("ingredients_pipe", String.join(PIPE_SEP, f.ingredients));
        cv.put("sort_order", sortOrder);
        db.insertWithOnConflict(SaigonTravelContract.TABLE_FOODS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);

        db.delete(SaigonTravelContract.TABLE_RESTAURANTS, "food_id = ?", new String[]{f.id});
        for (int rIdx = 0; rIdx < f.restaurants.size(); rIdx++) {
            Restaurant r = f.restaurants.get(rIdx);
            ContentValues rcv = new ContentValues();
            rcv.put("id", r.id);
            rcv.put("food_id", f.id);
            rcv.put("name", r.name);
            rcv.put("address", r.address);
            rcv.put("district", r.district);
            rcv.put("latitude", r.latitude);
            rcv.put("longitude", r.longitude);
            rcv.put("opening_hours", r.openingHours);
            rcv.put("price_range", r.priceRange);
            rcv.put("highlight", r.highlight);
            rcv.put("rating", r.rating);
            rcv.put("sort_order", rIdx);
            db.insertWithOnConflict(SaigonTravelContract.TABLE_RESTAURANTS, null, rcv, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    public List<SpecialtyFood> getAllFoods() {
        SQLiteDatabase db = getReadableDatabase();
        Map<String, List<Restaurant>> restMap = new HashMap<>();
        try (Cursor rc = db.query(SaigonTravelContract.TABLE_RESTAURANTS, null, null, null, null, null, "sort_order ASC")) {
            while (rc.moveToNext()) {
                String foodId = rc.getString(rc.getColumnIndexOrThrow("food_id"));
                Restaurant r = new Restaurant(
                        rc.getString(rc.getColumnIndexOrThrow("id")),
                        rc.getString(rc.getColumnIndexOrThrow("name")),
                        rc.getString(rc.getColumnIndexOrThrow("address")),
                        rc.getString(rc.getColumnIndexOrThrow("district")),
                        rc.getDouble(rc.getColumnIndexOrThrow("latitude")),
                        rc.getDouble(rc.getColumnIndexOrThrow("longitude")),
                        rc.getString(rc.getColumnIndexOrThrow("opening_hours")),
                        rc.getString(rc.getColumnIndexOrThrow("price_range")),
                        rc.getString(rc.getColumnIndexOrThrow("highlight")),
                        rc.getDouble(rc.getColumnIndexOrThrow("rating"))
                );
                restMap.computeIfAbsent(foodId, k -> new ArrayList<>()).add(r);
            }
        }

        List<SpecialtyFood> foods = new ArrayList<>();
        try (Cursor fc = db.query(SaigonTravelContract.TABLE_FOODS, null, null, null, null, null, "sort_order ASC")) {
            while (fc.moveToNext()) {
                String id = fc.getString(fc.getColumnIndexOrThrow("id"));
                String pipe = fc.getString(fc.getColumnIndexOrThrow("ingredients_pipe"));
                List<String> ings = new ArrayList<>();
                if (pipe != null && !pipe.isEmpty()) {
                    String[] parts = pipe.split("\\|\\|");
                    for (String p : parts) {
                        if (!p.trim().isEmpty()) ings.add(p.trim());
                    }
                }
                List<Restaurant> rests = restMap.getOrDefault(id, Collections.emptyList());
                foods.add(new SpecialtyFood(
                        id,
                        fc.getString(fc.getColumnIndexOrThrow("name_en")),
                        fc.getString(fc.getColumnIndexOrThrow("name_vi")),
                        FoodCategory.fromName(fc.getString(fc.getColumnIndexOrThrow("category"))),
                        fc.getString(fc.getColumnIndexOrThrow("image_url")),
                        fc.getInt(fc.getColumnIndexOrThrow("image_res_id")),
                        fc.getString(fc.getColumnIndexOrThrow("short_description")),
                        fc.getString(fc.getColumnIndexOrThrow("story_and_history")),
                        fc.getString(fc.getColumnIndexOrThrow("how_to_eat")),
                        ings,
                        rests
                ));
            }
        }
        return foods;
    }

    public void replaceAllFoods(List<SpecialtyFood> foods) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(SaigonTravelContract.TABLE_RESTAURANTS, null, null);
            db.delete(SaigonTravelContract.TABLE_FOODS, null, null);
            for (int i = 0; i < foods.size(); i++) {
                insertFoodAndRestaurants(db, foods.get(i), i);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void upsertFood(SpecialtyFood food, int sortOrder) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            insertFoodAndRestaurants(db, food, sortOrder);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void updateFoodImageResIds(Map<String, Integer> healedIds) {
        if (healedIds == null || healedIds.isEmpty()) return;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (Map.Entry<String, Integer> entry : healedIds.entrySet()) {
                ContentValues cv = new ContentValues();
                cv.put("image_res_id", entry.getValue());
                db.update(SaigonTravelContract.TABLE_FOODS, cv, "id = ?", new String[]{entry.getKey()});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void deleteFood(String id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(SaigonTravelContract.TABLE_RESTAURANTS, "food_id = ?", new String[]{id});
        db.delete(SaigonTravelContract.TABLE_FOODS, "id = ?", new String[]{id});
    }

    // =========================================================================
    // 4. PUBLIC UTILITIES
    // =========================================================================

    private void insertUtilityRow(SQLiteDatabase db, PublicUtility u, int sortOrder) {
        ContentValues cv = new ContentValues();
        cv.put("id", u.id);
        cv.put("name_en", u.nameEn);
        cv.put("name_vi", u.nameVi);
        cv.put("category", u.category.name());
        cv.put("district", u.district);
        cv.put("address", u.address);
        cv.put("latitude", u.latitude);
        cv.put("longitude", u.longitude);
        cv.put("operating_hours", u.operatingHours);
        cv.put("price", u.price);
        cv.put("phone", u.phone);
        cv.put("description_en", u.descriptionEn);
        cv.put("description_vi", u.descriptionVi);
        cv.put("tips", u.tips);
        cv.put("sort_order", sortOrder);
        db.insertWithOnConflict(SaigonTravelContract.TABLE_UTILITIES, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public List<PublicUtility> getAllUtilities() {
        List<PublicUtility> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(SaigonTravelContract.TABLE_UTILITIES, null, null, null, null, null, "sort_order ASC")) {
            while (c.moveToNext()) {
                int phoneIdx = c.getColumnIndexOrThrow("phone");
                list.add(new PublicUtility(
                        c.getString(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("name_en")),
                        c.getString(c.getColumnIndexOrThrow("name_vi")),
                        UtilityCategory.fromName(c.getString(c.getColumnIndexOrThrow("category"))),
                        c.getString(c.getColumnIndexOrThrow("district")),
                        c.getString(c.getColumnIndexOrThrow("address")),
                        c.getDouble(c.getColumnIndexOrThrow("latitude")),
                        c.getDouble(c.getColumnIndexOrThrow("longitude")),
                        c.getString(c.getColumnIndexOrThrow("operating_hours")),
                        c.getString(c.getColumnIndexOrThrow("price")),
                        c.isNull(phoneIdx) ? null : c.getString(phoneIdx),
                        c.getString(c.getColumnIndexOrThrow("description_en")),
                        c.getString(c.getColumnIndexOrThrow("description_vi")),
                        c.getString(c.getColumnIndexOrThrow("tips"))
                ));
            }
        }
        return list;
    }

    public void replaceAllUtilities(List<PublicUtility> utilities) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(SaigonTravelContract.TABLE_UTILITIES, null, null);
            for (int i = 0; i < utilities.size(); i++) {
                insertUtilityRow(db, utilities.get(i), i);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void upsertUtility(PublicUtility u, int sortOrder) {
        SQLiteDatabase db = getWritableDatabase();
        insertUtilityRow(db, u, sortOrder);
    }

    public void deleteUtility(String id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(SaigonTravelContract.TABLE_UTILITIES, "id = ?", new String[]{id});
    }

    // =========================================================================
    // 5. PHRASES & SAVED PHRASES
    // =========================================================================

    private void insertPhraseRow(SQLiteDatabase db, Phrase p, int sortOrder) {
        ContentValues cv = new ContentValues();
        cv.put("id", p.id);
        cv.put("english", p.english);
        cv.put("vietnamese", p.vietnamese);
        cv.put("vietnamese_unaccented", p.vietnameseUnaccented);
        cv.put("pronunciation", p.pronunciation);
        cv.put("explanation", p.explanation);
        cv.put("category", p.category.name());
        cv.put("is_custom", p.isCustom ? 1 : 0);
        cv.put("sort_order", sortOrder);
        db.insertWithOnConflict(SaigonTravelContract.TABLE_PHRASES, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public Set<String> getSavedPhraseIds(String userScope) {
        Set<String> ids = new HashSet<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(
                SaigonTravelContract.TABLE_SAVED_PHRASES,
                new String[]{"phrase_id"},
                "user_scope = ?",
                new String[]{userScope},
                null, null, null)) {
            while (c.moveToNext()) {
                ids.add(c.getString(0));
            }
        }
        return ids;
    }

    public void setPhraseSaved(String userScope, String phraseId, boolean saved) {
        SQLiteDatabase db = getWritableDatabase();
        if (saved) {
            ContentValues cv = new ContentValues();
            cv.put("user_scope", userScope);
            cv.put("phrase_id", phraseId);
            db.insertWithOnConflict(SaigonTravelContract.TABLE_SAVED_PHRASES, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        } else {
            db.delete(SaigonTravelContract.TABLE_SAVED_PHRASES, "user_scope = ? AND phrase_id = ?", new String[]{userScope, phraseId});
        }
    }

    public List<Phrase> getAllPhrases(String userScope) {
        Set<String> savedIds = getSavedPhraseIds(userScope);
        List<Phrase> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(SaigonTravelContract.TABLE_PHRASES, null, null, null, null, null, "is_custom DESC, sort_order ASC")) {
            while (c.moveToNext()) {
                String id = c.getString(c.getColumnIndexOrThrow("id"));
                list.add(new Phrase(
                        id,
                        c.getString(c.getColumnIndexOrThrow("english")),
                        c.getString(c.getColumnIndexOrThrow("vietnamese")),
                        c.getString(c.getColumnIndexOrThrow("vietnamese_unaccented")),
                        c.getString(c.getColumnIndexOrThrow("pronunciation")),
                        c.getString(c.getColumnIndexOrThrow("explanation")),
                        PhraseCategory.fromName(c.getString(c.getColumnIndexOrThrow("category"))),
                        savedIds.contains(id),
                        c.getInt(c.getColumnIndexOrThrow("is_custom")) == 1
                ));
            }
        }
        return list;
    }

    public void insertCustomPhrase(Phrase p) {
        SQLiteDatabase db = getWritableDatabase();
        insertPhraseRow(db, p, 0);
    }

    public void deleteCustomPhrase(String id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(SaigonTravelContract.TABLE_PHRASES, "id = ? AND is_custom = 1", new String[]{id});
        db.delete(SaigonTravelContract.TABLE_SAVED_PHRASES, "phrase_id = ?", new String[]{id});
    }

    // =========================================================================
    // 6. USERS & AUDIT LOGS
    // =========================================================================

    public List<UserAccount> getAllUsers() {
        List<UserAccount> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(SaigonTravelContract.TABLE_USERS, null, null, null, null, null, "created_at ASC")) {
            int photoIdx = c.getColumnIndex("avatar_photo_path");
            while (c.moveToNext()) {
                String photoPath = photoIdx >= 0 ? c.getString(photoIdx) : "";
                list.add(new UserAccount(
                        c.getString(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("username")),
                        c.getString(c.getColumnIndexOrThrow("full_name")),
                        c.getString(c.getColumnIndexOrThrow("email")),
                        c.getString(c.getColumnIndexOrThrow("password_hash")),
                        c.getString(c.getColumnIndexOrThrow("password_salt")),
                        UserRole.fromName(c.getString(c.getColumnIndexOrThrow("role"))),
                        c.getString(c.getColumnIndexOrThrow("avatar_emoji")),
                        photoPath,
                        c.getInt(c.getColumnIndexOrThrow("is_locked")) == 1,
                        c.getLong(c.getColumnIndexOrThrow("created_at")),
                        AuthProvider.fromCode(c.getString(c.getColumnIndexOrThrow("auth_provider"))),
                        c.getString(c.getColumnIndexOrThrow("provider_account_id"))
                ));
            }
        }
        return list;
    }

    public void upsertUser(UserAccount u) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("id", u.id);
        cv.put("username", u.username);
        cv.put("full_name", u.fullName);
        cv.put("email", u.email);
        cv.put("password_hash", u.passwordHash);
        cv.put("password_salt", u.passwordSalt);
        cv.put("role", u.role.name());
        cv.put("avatar_emoji", u.avatarEmoji);
        cv.put("avatar_photo_path", u.avatarPhotoPath);
        cv.put("is_locked", u.isLocked ? 1 : 0);
        cv.put("created_at", u.createdAt);
        cv.put("auth_provider", u.authProvider.code);
        cv.put("provider_account_id", u.providerAccountId);
        db.insertWithOnConflict(SaigonTravelContract.TABLE_USERS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void replaceAllUsers(List<UserAccount> users) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(SaigonTravelContract.TABLE_USERS, null, null);
            for (UserAccount u : users) {
                upsertUser(u);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void deleteUser(String userId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(SaigonTravelContract.TABLE_USERS, "id = ?", new String[]{userId});
    }

    public List<AuditLogEntry> getAuditLogs(int limit) {
        List<AuditLogEntry> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(
                SaigonTravelContract.TABLE_AUDIT_LOGS,
                null, null, null, null, null,
                "timestamp DESC",
                String.valueOf(limit))) {
            while (c.moveToNext()) {
                list.add(new AuditLogEntry(
                        c.getString(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("actor_username")),
                        UserRole.fromName(c.getString(c.getColumnIndexOrThrow("actor_role"))),
                        c.getString(c.getColumnIndexOrThrow("action_vi")),
                        c.getString(c.getColumnIndexOrThrow("action_en")),
                        c.getLong(c.getColumnIndexOrThrow("timestamp"))
                ));
            }
        }
        return list;
    }

    public void insertAuditLog(AuditLogEntry e) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("id", e.id);
        cv.put("actor_username", e.actorUsername);
        cv.put("actor_role", e.actorRole.name());
        cv.put("action_vi", e.actionVi);
        cv.put("action_en", e.actionEn);
        cv.put("timestamp", e.timestamp);
        db.insertWithOnConflict(SaigonTravelContract.TABLE_AUDIT_LOGS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    // =========================================================================
    // 7. COMMUNITY REVIEWS
    // =========================================================================

    public List<PlaceReview> getAllReviews() {
        List<PlaceReview> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(SaigonTravelContract.TABLE_REVIEWS, null, null, null, null, null, "created_at DESC")) {
            while (c.moveToNext()) {
                list.add(new PlaceReview(
                        c.getString(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("target_id")),
                        c.getString(c.getColumnIndexOrThrow("author_id")),
                        c.getString(c.getColumnIndexOrThrow("author_name")),
                        UserRole.fromName(c.getString(c.getColumnIndexOrThrow("author_role"))),
                        c.getInt(c.getColumnIndexOrThrow("rating")),
                        c.getString(c.getColumnIndexOrThrow("comment")),
                        c.getLong(c.getColumnIndexOrThrow("created_at"))
                ));
            }
        }
        return list;
    }

    public void insertReview(PlaceReview r) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("id", r.id);
        cv.put("target_id", r.targetId);
        cv.put("author_id", r.authorId);
        cv.put("author_name", r.authorName);
        cv.put("author_role", r.authorRole.name());
        cv.put("rating", r.rating);
        cv.put("comment", r.comment);
        cv.put("created_at", r.createdAt);
        db.insertWithOnConflict(SaigonTravelContract.TABLE_REVIEWS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void replaceAllReviews(List<PlaceReview> reviews) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(SaigonTravelContract.TABLE_REVIEWS, null, null);
            for (PlaceReview r : reviews) {
                insertReview(r);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void deleteReview(String reviewId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(SaigonTravelContract.TABLE_REVIEWS, "id = ?", new String[]{reviewId});
    }

    // =========================================================================
    // 8. FAVORITES
    // =========================================================================

    public Set<String> getFavoriteIds(String userScope, String itemType) {
        Set<String> set = new LinkedHashSet<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(
                SaigonTravelContract.TABLE_FAVORITES,
                new String[]{"item_id"},
                "user_scope = ? AND item_type = ?",
                new String[]{userScope, itemType},
                null, null, "created_at ASC")) {
            while (c.moveToNext()) {
                set.add(c.getString(0));
            }
        }
        return set;
    }

    public void setFavorite(String userScope, String itemType, String itemId, boolean favorite) {
        SQLiteDatabase db = getWritableDatabase();
        if (favorite) {
            ContentValues cv = new ContentValues();
            cv.put("user_scope", userScope);
            cv.put("item_type", itemType);
            cv.put("item_id", itemId);
            cv.put("created_at", System.currentTimeMillis());
            db.insertWithOnConflict(SaigonTravelContract.TABLE_FAVORITES, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        } else {
            db.delete(SaigonTravelContract.TABLE_FAVORITES,
                    "user_scope = ? AND item_type = ? AND item_id = ?",
                    new String[]{userScope, itemType, itemId});
        }
    }

    // =========================================================================
    // 9. TRIP PLANNER
    // =========================================================================

    public List<String> getTripStopIds(String userScope) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(
                SaigonTravelContract.TABLE_TRIP_STOPS,
                new String[]{"attraction_id"},
                "user_scope = ?",
                new String[]{userScope},
                null, null, "position ASC")) {
            while (c.moveToNext()) {
                list.add(c.getString(0));
            }
        }
        return list;
    }

    public void replaceTripStops(String userScope, List<String> stopIds) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(SaigonTravelContract.TABLE_TRIP_STOPS, "user_scope = ?", new String[]{userScope});
            for (int i = 0; i < stopIds.size(); i++) {
                ContentValues cv = new ContentValues();
                cv.put("user_scope", userScope);
                cv.put("position", i);
                cv.put("attraction_id", stopIds.get(i));
                db.insertWithOnConflict(SaigonTravelContract.TABLE_TRIP_STOPS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public int getTripStartMinutes(String userScope, int defaultVal) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(
                SaigonTravelContract.TABLE_TRIP_SETTINGS,
                new String[]{"start_minutes"},
                "user_scope = ?",
                new String[]{userScope},
                null, null, null)) {
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
        }
        return defaultVal;
    }

    public void setTripStartMinutes(String userScope, int startMinutes) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_scope", userScope);
        cv.put("start_minutes", startMinutes);
        db.insertWithOnConflict(SaigonTravelContract.TABLE_TRIP_SETTINGS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    // =========================================================================
    // 10. TRAVEL PASSPORT STAMPS
    // =========================================================================

    public static final class StampRow {
        public final String attractionId;
        public final String attractionName;
        public final String province;
        public final long checkedInAt;

        public StampRow(String attractionId, String attractionName, String province, long checkedInAt) {
            this.attractionId = attractionId;
            this.attractionName = attractionName;
            this.province = province;
            this.checkedInAt = checkedInAt;
        }
    }

    public List<StampRow> getPassportStamps(String userScope) {
        List<StampRow> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(
                SaigonTravelContract.TABLE_PASSPORT_STAMPS,
                null,
                "user_scope = ?",
                new String[]{userScope},
                null, null, "checked_in_at DESC")) {
            while (c.moveToNext()) {
                list.add(new StampRow(
                        c.getString(c.getColumnIndexOrThrow("attraction_id")),
                        c.getString(c.getColumnIndexOrThrow("attraction_name")),
                        c.getString(c.getColumnIndexOrThrow("province")),
                        c.getLong(c.getColumnIndexOrThrow("checked_in_at"))
                ));
            }
        }
        return list;
    }

    public void upsertPassportStamp(String userScope, String attractionId, String attractionName, String province, long checkedInAt) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_scope", userScope);
        cv.put("attraction_id", attractionId);
        cv.put("attraction_name", attractionName);
        cv.put("province", province);
        cv.put("checked_in_at", checkedInAt);
        db.insertWithOnConflict(SaigonTravelContract.TABLE_PASSPORT_STAMPS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void deletePassportStamp(String userScope, String attractionId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(SaigonTravelContract.TABLE_PASSPORT_STAMPS,
                "user_scope = ? AND attraction_id = ?",
                new String[]{userScope, attractionId});
    }

    // =========================================================================
    // 11. TRIP EXPENSES
    // =========================================================================

    public static final class ExpenseRow {
        public final String id;
        public final String title;
        public final long amountVnd;
        public final String category;
        public final long createdAt;

        public ExpenseRow(String id, String title, long amountVnd, String category, long createdAt) {
            this.id = id;
            this.title = title;
            this.amountVnd = amountVnd;
            this.category = category;
            this.createdAt = createdAt;
        }
    }

    public List<ExpenseRow> getExpenses(String userScope) {
        List<ExpenseRow> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(
                SaigonTravelContract.TABLE_EXPENSES,
                null,
                "user_scope = ?",
                new String[]{userScope},
                null, null, "created_at DESC")) {
            while (c.moveToNext()) {
                list.add(new ExpenseRow(
                        c.getString(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("title")),
                        c.getLong(c.getColumnIndexOrThrow("amount_vnd")),
                        c.getString(c.getColumnIndexOrThrow("category")),
                        c.getLong(c.getColumnIndexOrThrow("created_at"))
                ));
            }
        }
        return list;
    }

    public void insertExpense(String userScope, String id, String title, long amountVnd, String category, long createdAt) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("id", id);
        cv.put("user_scope", userScope);
        cv.put("title", title);
        cv.put("amount_vnd", amountVnd);
        cv.put("category", category);
        cv.put("created_at", createdAt);
        db.insertWithOnConflict(SaigonTravelContract.TABLE_EXPENSES, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void deleteExpense(String id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(SaigonTravelContract.TABLE_EXPENSES, "id = ?", new String[]{id});
    }

    public void clearExpenses(String userScope) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(SaigonTravelContract.TABLE_EXPENSES, "user_scope = ?", new String[]{userScope});
    }

    // =========================================================================
    // 12. E-TICKET QR WALLET
    // =========================================================================

    public static final class TicketRow {
        public final String id;
        public final String attractionId;
        public final String attractionName;
        public final String visitDate;
        public final String timeSlot;
        public final int adultQty;
        public final int studentQty;
        public final int childQty;
        public final long totalVnd;
        public final String qrPayload;
        public final long createdAt;

        public TicketRow(
                String id,
                String attractionId,
                String attractionName,
                String visitDate,
                String timeSlot,
                int adultQty,
                int studentQty,
                int childQty,
                long totalVnd,
                String qrPayload,
                long createdAt
        ) {
            this.id = id;
            this.attractionId = attractionId;
            this.attractionName = attractionName;
            this.visitDate = visitDate;
            this.timeSlot = timeSlot;
            this.adultQty = adultQty;
            this.studentQty = studentQty;
            this.childQty = childQty;
            this.totalVnd = totalVnd;
            this.qrPayload = qrPayload;
            this.createdAt = createdAt;
        }
    }

    public List<TicketRow> getTickets(String userScope) {
        List<TicketRow> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(
                SaigonTravelContract.TABLE_TICKETS,
                null,
                "user_scope = ?",
                new String[]{userScope},
                null, null, "created_at DESC")) {
            while (c.moveToNext()) {
                list.add(new TicketRow(
                        c.getString(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("attraction_id")),
                        c.getString(c.getColumnIndexOrThrow("attraction_name")),
                        c.getString(c.getColumnIndexOrThrow("visit_date")),
                        c.getString(c.getColumnIndexOrThrow("time_slot")),
                        c.getInt(c.getColumnIndexOrThrow("adult_qty")),
                        c.getInt(c.getColumnIndexOrThrow("student_qty")),
                        c.getInt(c.getColumnIndexOrThrow("child_qty")),
                        c.getLong(c.getColumnIndexOrThrow("total_vnd")),
                        c.getString(c.getColumnIndexOrThrow("qr_payload")),
                        c.getLong(c.getColumnIndexOrThrow("created_at"))
                ));
            }
        }
        return list;
    }

    public void upsertTicket(String userScope, TicketRow row) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("id", row.id);
        cv.put("user_scope", userScope);
        cv.put("attraction_id", row.attractionId);
        cv.put("attraction_name", row.attractionName);
        cv.put("visit_date", row.visitDate);
        cv.put("time_slot", row.timeSlot);
        cv.put("adult_qty", row.adultQty);
        cv.put("student_qty", row.studentQty);
        cv.put("child_qty", row.childQty);
        cv.put("total_vnd", row.totalVnd);
        cv.put("qr_payload", row.qrPayload);
        cv.put("created_at", row.createdAt);
        db.insertWithOnConflict(SaigonTravelContract.TABLE_TICKETS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void deleteTicket(String id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(SaigonTravelContract.TABLE_TICKETS, "id = ?", new String[]{id});
    }

    // =========================================================================
    // 13. TRAVEL JOURNALS (MEMORIES & POSTCARDS)
    // =========================================================================

    public static final class JournalRow {
        public final String attractionId;
        public final String attractionName;
        public final String moodEmoji;
        public final String note;
        public final String visitDate;
        public final long updatedAt;

        public JournalRow(
                String attractionId,
                String attractionName,
                String moodEmoji,
                String note,
                String visitDate,
                long updatedAt
        ) {
            this.attractionId = attractionId;
            this.attractionName = attractionName;
            this.moodEmoji = moodEmoji;
            this.note = note;
            this.visitDate = visitDate;
            this.updatedAt = updatedAt;
        }
    }

    public List<JournalRow> getJournals(String userScope) {
        List<JournalRow> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(
                SaigonTravelContract.TABLE_TRAVEL_JOURNALS,
                null,
                "user_scope = ?",
                new String[]{userScope},
                null, null, "updated_at DESC")) {
            while (c.moveToNext()) {
                list.add(new JournalRow(
                        c.getString(c.getColumnIndexOrThrow("attraction_id")),
                        c.getString(c.getColumnIndexOrThrow("attraction_name")),
                        c.getString(c.getColumnIndexOrThrow("mood_emoji")),
                        c.getString(c.getColumnIndexOrThrow("note")),
                        c.getString(c.getColumnIndexOrThrow("visit_date")),
                        c.getLong(c.getColumnIndexOrThrow("updated_at"))
                ));
            }
        }
        return list;
    }

    public void upsertJournal(String userScope, JournalRow row) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_scope", userScope);
        cv.put("attraction_id", row.attractionId);
        cv.put("attraction_name", row.attractionName);
        cv.put("mood_emoji", row.moodEmoji);
        cv.put("note", row.note);
        cv.put("visit_date", row.visitDate);
        cv.put("updated_at", row.updatedAt);
        db.insertWithOnConflict(SaigonTravelContract.TABLE_TRAVEL_JOURNALS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void deleteJournal(String userScope, String attractionId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(SaigonTravelContract.TABLE_TRAVEL_JOURNALS,
                "user_scope = ? AND attraction_id = ?",
                new String[]{userScope, attractionId});
    }

    // =========================================================================
    // 14. SMART PACKING CHECKLIST
    // =========================================================================

    public static final class PackingRow {
        public final String id;
        public final String presetGroup;
        public final String titleVi;
        public final String titleEn;
        public final boolean isChecked;
        public final int sortOrder;

        public PackingRow(
                String id,
                String presetGroup,
                String titleVi,
                String titleEn,
                boolean isChecked,
                int sortOrder
        ) {
            this.id = id;
            this.presetGroup = presetGroup;
            this.titleVi = titleVi;
            this.titleEn = titleEn;
            this.isChecked = isChecked;
            this.sortOrder = sortOrder;
        }
    }

    public List<PackingRow> getPackingItems(String userScope) {
        List<PackingRow> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(
                SaigonTravelContract.TABLE_PACKING_ITEMS,
                null,
                "user_scope = ?",
                new String[]{userScope},
                null, null, "sort_order ASC")) {
            while (c.moveToNext()) {
                list.add(new PackingRow(
                        c.getString(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("preset_group")),
                        c.getString(c.getColumnIndexOrThrow("title_vi")),
                        c.getString(c.getColumnIndexOrThrow("title_en")),
                        c.getInt(c.getColumnIndexOrThrow("is_checked")) != 0,
                        c.getInt(c.getColumnIndexOrThrow("sort_order"))
                ));
            }
        }
        return list;
    }

    public void upsertPackingItem(String userScope, PackingRow row) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("id", row.id);
        cv.put("user_scope", userScope);
        cv.put("preset_group", row.presetGroup);
        cv.put("title_vi", row.titleVi);
        cv.put("title_en", row.titleEn);
        cv.put("is_checked", row.isChecked ? 1 : 0);
        cv.put("sort_order", row.sortOrder);
        db.insertWithOnConflict(SaigonTravelContract.TABLE_PACKING_ITEMS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void deletePackingItem(String id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(SaigonTravelContract.TABLE_PACKING_ITEMS, "id = ?", new String[]{id});
    }

    public void clearPackingItems(String userScope) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(SaigonTravelContract.TABLE_PACKING_ITEMS, "user_scope = ?", new String[]{userScope});
    }
}
