package com.example.nhom_15_dulichtphcm.data.db;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Relational SQLite schema contract for Explore Saigon (saigon_travel.db).
 * Stored on device at: /data/data/com.example.nhom_15_dulichtphcm/databases/saigon_travel.db
 */
public final class SaigonTravelContract {

    private SaigonTravelContract() {}

    public static final String DATABASE_NAME = "saigon_travel.db";
    public static final int DATABASE_VERSION = 4;

    // Table names
    public static final String TABLE_ATTRACTIONS = "attractions";
    public static final String TABLE_FOODS = "foods";
    public static final String TABLE_RESTAURANTS = "restaurants";
    public static final String TABLE_UTILITIES = "utilities";
    public static final String TABLE_PHRASES = "phrases";
    public static final String TABLE_SAVED_PHRASES = "saved_phrases";
    public static final String TABLE_USERS = "users";
    public static final String TABLE_AUDIT_LOGS = "audit_logs";
    public static final String TABLE_REVIEWS = "reviews";
    public static final String TABLE_FAVORITES = "favorites";
    public static final String TABLE_TRIP_STOPS = "trip_stops";
    public static final String TABLE_TRIP_SETTINGS = "trip_settings";
    public static final String TABLE_PASSPORT_STAMPS = "passport_stamps";
    public static final String TABLE_EXPENSES = "expenses";
    public static final String TABLE_APP_SETTINGS = "app_settings";
    public static final String TABLE_TICKETS = "tickets";
    public static final String TABLE_TRAVEL_JOURNALS = "travel_journals";
    public static final String TABLE_PACKING_ITEMS = "packing_items";

    public static final List<String> ALL_TABLES = Collections.unmodifiableList(Arrays.asList(
            TABLE_ATTRACTIONS,
            TABLE_FOODS,
            TABLE_RESTAURANTS,
            TABLE_UTILITIES,
            TABLE_PHRASES,
            TABLE_SAVED_PHRASES,
            TABLE_USERS,
            TABLE_AUDIT_LOGS,
            TABLE_REVIEWS,
            TABLE_FAVORITES,
            TABLE_TRIP_STOPS,
            TABLE_TRIP_SETTINGS,
            TABLE_PASSPORT_STAMPS,
            TABLE_EXPENSES,
            TABLE_APP_SETTINGS,
            TABLE_TICKETS,
            TABLE_TRAVEL_JOURNALS,
            TABLE_PACKING_ITEMS
    ));

    // DDL statements
    public static final String SQL_CREATE_ATTRACTIONS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_ATTRACTIONS + " ("
                    + "id TEXT PRIMARY KEY, "
                    + "name_en TEXT NOT NULL, "
                    + "name_vi TEXT NOT NULL, "
                    + "category TEXT NOT NULL, "
                    + "image_url TEXT NOT NULL, "
                    + "image_res_id INTEGER NOT NULL DEFAULT 0, "
                    + "district TEXT NOT NULL, "
                    + "address TEXT NOT NULL, "
                    + "latitude REAL NOT NULL, "
                    + "longitude REAL NOT NULL, "
                    + "ticket_price TEXT NOT NULL, "
                    + "opening_hours TEXT NOT NULL, "
                    + "short_summary TEXT NOT NULL, "
                    + "history TEXT NOT NULL, "
                    + "culture TEXT NOT NULL, "
                    + "foreigner_tips TEXT NOT NULL, "
                    + "province TEXT NOT NULL, "
                    + "sort_order INTEGER NOT NULL DEFAULT 0"
                    + ");";

    public static final String SQL_CREATE_FOODS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_FOODS + " ("
                    + "id TEXT PRIMARY KEY, "
                    + "name_en TEXT NOT NULL, "
                    + "name_vi TEXT NOT NULL, "
                    + "category TEXT NOT NULL, "
                    + "image_url TEXT NOT NULL, "
                    + "image_res_id INTEGER NOT NULL DEFAULT 0, "
                    + "short_description TEXT NOT NULL, "
                    + "story_and_history TEXT NOT NULL, "
                    + "how_to_eat TEXT NOT NULL, "
                    + "ingredients_pipe TEXT NOT NULL, "
                    + "sort_order INTEGER NOT NULL DEFAULT 0"
                    + ");";

    public static final String SQL_CREATE_RESTAURANTS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_RESTAURANTS + " ("
                    + "id TEXT PRIMARY KEY, "
                    + "food_id TEXT NOT NULL, "
                    + "name TEXT NOT NULL, "
                    + "address TEXT NOT NULL, "
                    + "district TEXT NOT NULL, "
                    + "latitude REAL NOT NULL, "
                    + "longitude REAL NOT NULL, "
                    + "opening_hours TEXT NOT NULL, "
                    + "price_range TEXT NOT NULL, "
                    + "highlight TEXT NOT NULL, "
                    + "rating REAL NOT NULL DEFAULT 4.8, "
                    + "sort_order INTEGER NOT NULL DEFAULT 0, "
                    + "FOREIGN KEY(food_id) REFERENCES " + TABLE_FOODS + "(id) ON DELETE CASCADE"
                    + ");";

    public static final String SQL_CREATE_UTILITIES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_UTILITIES + " ("
                    + "id TEXT PRIMARY KEY, "
                    + "name_en TEXT NOT NULL, "
                    + "name_vi TEXT NOT NULL, "
                    + "category TEXT NOT NULL, "
                    + "district TEXT NOT NULL, "
                    + "address TEXT NOT NULL, "
                    + "latitude REAL NOT NULL, "
                    + "longitude REAL NOT NULL, "
                    + "operating_hours TEXT NOT NULL, "
                    + "price TEXT NOT NULL, "
                    + "phone TEXT, "
                    + "description_en TEXT NOT NULL, "
                    + "description_vi TEXT NOT NULL, "
                    + "tips TEXT NOT NULL, "
                    + "sort_order INTEGER NOT NULL DEFAULT 0"
                    + ");";

    public static final String SQL_CREATE_PHRASES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_PHRASES + " ("
                    + "id TEXT PRIMARY KEY, "
                    + "english TEXT NOT NULL, "
                    + "vietnamese TEXT NOT NULL, "
                    + "vietnamese_unaccented TEXT NOT NULL, "
                    + "pronunciation TEXT NOT NULL, "
                    + "explanation TEXT NOT NULL, "
                    + "category TEXT NOT NULL, "
                    + "is_custom INTEGER NOT NULL DEFAULT 0, "
                    + "sort_order INTEGER NOT NULL DEFAULT 0"
                    + ");";

    public static final String SQL_CREATE_SAVED_PHRASES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_SAVED_PHRASES + " ("
                    + "user_scope TEXT NOT NULL, "
                    + "phrase_id TEXT NOT NULL, "
                    + "PRIMARY KEY(user_scope, phrase_id)"
                    + ");";

    public static final String SQL_CREATE_USERS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " ("
                    + "id TEXT PRIMARY KEY, "
                    + "username TEXT NOT NULL UNIQUE, "
                    + "full_name TEXT NOT NULL, "
                    + "email TEXT NOT NULL, "
                    + "password_hash TEXT NOT NULL, "
                    + "password_salt TEXT NOT NULL, "
                    + "role TEXT NOT NULL, "
                    + "avatar_emoji TEXT NOT NULL, "
                    + "avatar_photo_path TEXT NOT NULL DEFAULT '', "
                    + "is_locked INTEGER NOT NULL DEFAULT 0, "
                    + "created_at INTEGER NOT NULL, "
                    + "auth_provider TEXT NOT NULL, "
                    + "provider_account_id TEXT NOT NULL"
                    + ");";

    public static final String SQL_CREATE_AUDIT_LOGS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_AUDIT_LOGS + " ("
                    + "id TEXT PRIMARY KEY, "
                    + "actor_username TEXT NOT NULL, "
                    + "actor_role TEXT NOT NULL, "
                    + "action_vi TEXT NOT NULL, "
                    + "action_en TEXT NOT NULL, "
                    + "timestamp INTEGER NOT NULL"
                    + ");";

    public static final String SQL_CREATE_REVIEWS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_REVIEWS + " ("
                    + "id TEXT PRIMARY KEY, "
                    + "target_id TEXT NOT NULL, "
                    + "author_id TEXT NOT NULL, "
                    + "author_name TEXT NOT NULL, "
                    + "author_role TEXT NOT NULL, "
                    + "rating INTEGER NOT NULL, "
                    + "comment TEXT NOT NULL, "
                    + "created_at INTEGER NOT NULL"
                    + ");";

    public static final String SQL_CREATE_FAVORITES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_FAVORITES + " ("
                    + "user_scope TEXT NOT NULL, "
                    + "item_type TEXT NOT NULL, "
                    + "item_id TEXT NOT NULL, "
                    + "created_at INTEGER NOT NULL, "
                    + "PRIMARY KEY(user_scope, item_type, item_id)"
                    + ");";

    public static final String SQL_CREATE_TRIP_STOPS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_TRIP_STOPS + " ("
                    + "user_scope TEXT NOT NULL, "
                    + "position INTEGER NOT NULL, "
                    + "attraction_id TEXT NOT NULL, "
                    + "PRIMARY KEY(user_scope, position)"
                    + ");";

    public static final String SQL_CREATE_TRIP_SETTINGS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_TRIP_SETTINGS + " ("
                    + "user_scope TEXT PRIMARY KEY, "
                    + "start_minutes INTEGER NOT NULL DEFAULT 510"
                    + ");";

    public static final String SQL_CREATE_PASSPORT_STAMPS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_PASSPORT_STAMPS + " ("
                    + "user_scope TEXT NOT NULL, "
                    + "attraction_id TEXT NOT NULL, "
                    + "attraction_name TEXT NOT NULL, "
                    + "province TEXT NOT NULL, "
                    + "checked_in_at INTEGER NOT NULL, "
                    + "PRIMARY KEY(user_scope, attraction_id)"
                    + ");";

    public static final String SQL_CREATE_EXPENSES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_EXPENSES + " ("
                    + "id TEXT PRIMARY KEY, "
                    + "user_scope TEXT NOT NULL, "
                    + "title TEXT NOT NULL, "
                    + "amount_vnd INTEGER NOT NULL, "
                    + "category TEXT NOT NULL, "
                    + "created_at INTEGER NOT NULL"
                    + ");";

    public static final String SQL_CREATE_APP_SETTINGS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_APP_SETTINGS + " ("
                    + "setting_key TEXT PRIMARY KEY, "
                    + "setting_value TEXT NOT NULL"
                    + ");";

    public static final String SQL_CREATE_TICKETS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_TICKETS + " ("
                    + "id TEXT PRIMARY KEY, "
                    + "user_scope TEXT NOT NULL, "
                    + "attraction_id TEXT NOT NULL, "
                    + "attraction_name TEXT NOT NULL, "
                    + "visit_date TEXT NOT NULL, "
                    + "time_slot TEXT NOT NULL, "
                    + "adult_qty INTEGER NOT NULL, "
                    + "student_qty INTEGER NOT NULL, "
                    + "child_qty INTEGER NOT NULL, "
                    + "total_vnd INTEGER NOT NULL, "
                    + "qr_payload TEXT NOT NULL, "
                    + "created_at INTEGER NOT NULL"
                    + ");";

    public static final String SQL_CREATE_TRAVEL_JOURNALS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_TRAVEL_JOURNALS + " ("
                    + "user_scope TEXT NOT NULL, "
                    + "attraction_id TEXT NOT NULL, "
                    + "attraction_name TEXT NOT NULL, "
                    + "mood_emoji TEXT NOT NULL, "
                    + "note TEXT NOT NULL, "
                    + "visit_date TEXT NOT NULL, "
                    + "updated_at INTEGER NOT NULL, "
                    + "PRIMARY KEY(user_scope, attraction_id)"
                    + ");";

    public static final String SQL_CREATE_PACKING_ITEMS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_PACKING_ITEMS + " ("
                    + "id TEXT PRIMARY KEY, "
                    + "user_scope TEXT NOT NULL, "
                    + "preset_group TEXT NOT NULL, "
                    + "title_vi TEXT NOT NULL, "
                    + "title_en TEXT NOT NULL, "
                    + "is_checked INTEGER NOT NULL DEFAULT 0, "
                    + "sort_order INTEGER NOT NULL DEFAULT 0"
                    + ");";

    public static final List<String> ALL_CREATE_STATEMENTS = Collections.unmodifiableList(Arrays.asList(
            SQL_CREATE_ATTRACTIONS,
            SQL_CREATE_FOODS,
            SQL_CREATE_RESTAURANTS,
            SQL_CREATE_UTILITIES,
            SQL_CREATE_PHRASES,
            SQL_CREATE_SAVED_PHRASES,
            SQL_CREATE_USERS,
            SQL_CREATE_AUDIT_LOGS,
            SQL_CREATE_REVIEWS,
            SQL_CREATE_FAVORITES,
            SQL_CREATE_TRIP_STOPS,
            SQL_CREATE_TRIP_SETTINGS,
            SQL_CREATE_PASSPORT_STAMPS,
            SQL_CREATE_EXPENSES,
            SQL_CREATE_APP_SETTINGS,
            SQL_CREATE_TICKETS,
            SQL_CREATE_TRAVEL_JOURNALS,
            SQL_CREATE_PACKING_ITEMS,
            "CREATE INDEX IF NOT EXISTS idx_attractions_province ON " + TABLE_ATTRACTIONS + "(province);",
            "CREATE INDEX IF NOT EXISTS idx_restaurants_food_id ON " + TABLE_RESTAURANTS + "(food_id);",
            "CREATE INDEX IF NOT EXISTS idx_reviews_target_id ON " + TABLE_REVIEWS + "(target_id);",
            "CREATE INDEX IF NOT EXISTS idx_expenses_user_scope ON " + TABLE_EXPENSES + "(user_scope, created_at DESC);",
            "CREATE INDEX IF NOT EXISTS idx_tickets_user_scope ON " + TABLE_TICKETS + "(user_scope, created_at DESC);",
            "CREATE INDEX IF NOT EXISTS idx_packing_user_scope ON " + TABLE_PACKING_ITEMS + "(user_scope, sort_order ASC);"
    ));
}
