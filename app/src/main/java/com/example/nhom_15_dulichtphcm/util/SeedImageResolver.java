package com.example.nhom_15_dulichtphcm.util;

import android.content.res.Resources;

import androidx.annotation.Nullable;

import com.example.nhom_15_dulichtphcm.R;
import com.example.nhom_15_dulichtphcm.data.model.Attraction;
import com.example.nhom_15_dulichtphcm.data.model.AttractionCategory;
import com.example.nhom_15_dulichtphcm.data.model.FoodCategory;
import com.example.nhom_15_dulichtphcm.data.model.SpecialtyFood;
import com.example.nhom_15_dulichtphcm.data.repository.AttractionSeedData;
import com.example.nhom_15_dulichtphcm.data.repository.FoodSeedData;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Canonical, build-proof drawable resolver and SQLite self-healer.
 *
 * <p>Android AAPT2 re-indexes {@code R.drawable.*} integer IDs alphabetically whenever new XML or
 * raster drawables are added to {@code res/drawable/}. Persisting raw integer resource IDs in
 * SQLite ({@code saigon_travel.db}) can cause previously saved rows to point to unrelated drawables
 * (such as AppCompat checkmarks or gradient backgrounds) after an incremental app update.
 *
 * <p>This resolver maps item IDs and categories to the current APK's compiled {@code R.drawable.*}
 * constants and validates any dynamic resource IDs before rendering.
 */
public final class SeedImageResolver {

    private static final Map<String, Integer> ATTRACTION_ID_TO_DRAWABLE;
    private static final Map<String, Integer> FOOD_ID_TO_DRAWABLE;
    private static final Set<Integer> VALID_ATTRACTION_DRAWABLES;
    private static final Set<Integer> VALID_FOOD_DRAWABLES;

    static {
        Map<String, Integer> attractionMap = new HashMap<>();
        Set<Integer> attractionSet = new HashSet<>();
        for (Attraction a : AttractionSeedData.createDefaultAttractions()) {
            if (a.id != null && a.imageResId != 0) {
                attractionMap.put(a.id, a.imageResId);
                attractionSet.add(a.imageResId);
            }
        }
        // Curated auto-synced landmarks (TravelDataService.curatedLatestPlaces)
        attractionMap.put("sync_curated_riverfront_park", R.drawable.attraction_thu_thiem_bason);
        attractionMap.put("sync_curated_metro_ben_thanh", R.drawable.attraction_ben_thanh_market);
        attractionMap.put("sync_curated_hikari_bd", R.drawable.attraction_landmark_81);
        attractionMap.put("sync_curated_ho_tram_bridge", R.drawable.attraction_tuong_chua_kito);

        ATTRACTION_ID_TO_DRAWABLE = Collections.unmodifiableMap(attractionMap);
        VALID_ATTRACTION_DRAWABLES = Collections.unmodifiableSet(attractionSet);

        Map<String, Integer> foodMap = new HashMap<>();
        Set<Integer> foodSet = new HashSet<>();
        for (SpecialtyFood f : FoodSeedData.createDefaultFoods()) {
            if (f.id != null && f.imageResId != 0) {
                foodMap.put(f.id, f.imageResId);
                foodSet.add(f.imageResId);
            }
        }
        FOOD_ID_TO_DRAWABLE = Collections.unmodifiableMap(foodMap);
        VALID_FOOD_DRAWABLES = Collections.unmodifiableSet(foodSet);
    }

    private SeedImageResolver() {}

    public static boolean isCuratedAttractionId(@Nullable String id) {
        return id != null && ATTRACTION_ID_TO_DRAWABLE.containsKey(id);
    }

    public static boolean isCuratedFoodId(@Nullable String id) {
        return id != null && FOOD_ID_TO_DRAWABLE.containsKey(id);
    }

    public static int defaultAttractionDrawable(@Nullable AttractionCategory category) {
        if (category == null) {
            return R.drawable.attraction_ben_thanh_market;
        }
        switch (category) {
            case HISTORICAL:
                return R.drawable.attraction_independence_palace;
            case SPIRITUAL:
                return R.drawable.attraction_jade_emperor_pagoda;
            case MARKET:
                return R.drawable.attraction_ben_thanh_market;
            case MODERN:
                return R.drawable.attraction_thu_thiem_bason;
            case CULTURAL:
            default:
                return R.drawable.attraction_saigon_opera_house;
        }
    }

    public static int defaultFoodDrawable(@Nullable FoodCategory category) {
        if (category == null) {
            return R.drawable.food_com_tam;
        }
        switch (category) {
            case RICE_AND_NOODLES:
                return R.drawable.food_com_tam;
            case STREET_SNACKS:
                return R.drawable.food_banh_mi;
            case BEVERAGES_DESSERTS:
                return R.drawable.food_ca_phe_vot;
            case SEAFOOD:
                return R.drawable.food_oc_saigon;
            default:
                return R.drawable.food_com_tam;
        }
    }

    /**
     * Checks that {@code resId} belongs to our curated raster photo set ({@code attraction_*} or
     * {@code food_*}) and never points to UI shapes ({@code bg_*}), icons ({@code ic_*}), or
     * AppCompat internal drawables ({@code abc_*}).
     */
    public static boolean isValidPhotoDrawable(@Nullable Resources res, int resId, boolean foodOnly) {
        if (resId == 0) return false;
        if (foodOnly) {
            if (!VALID_FOOD_DRAWABLES.contains(resId)) return false;
        } else {
            if (!VALID_ATTRACTION_DRAWABLES.contains(resId) && !VALID_FOOD_DRAWABLES.contains(resId)) {
                return false;
            }
        }
        if (res != null) {
            try {
                String type = res.getResourceTypeName(resId);
                String entry = res.getResourceEntryName(resId);
                if (!"drawable".equals(type) || entry == null) {
                    return false;
                }
                if (foodOnly) {
                    return entry.startsWith("food_");
                }
                return entry.startsWith("attraction_") || entry.startsWith("food_");
            } catch (Resources.NotFoundException e) {
                return false;
            }
        }
        return true;
    }

    public static int resolveAttractionDrawable(@Nullable Resources res, @Nullable Attraction a) {
        if (a == null) {
            return R.drawable.attraction_ben_thanh_market;
        }
        Integer curated = ATTRACTION_ID_TO_DRAWABLE.get(a.id);
        if (curated != null) {
            return curated;
        }
        if (a.id != null && (a.id.startsWith("sync_wiki_") || a.id.startsWith("sync_osm_"))) {
            return defaultAttractionDrawable(a.category);
        }
        if (VALID_ATTRACTION_DRAWABLES.contains(a.imageResId) && isValidPhotoDrawable(res, a.imageResId, false)) {
            return a.imageResId;
        }
        return defaultAttractionDrawable(a.category);
    }

    public static int resolveFoodDrawable(@Nullable Resources res, @Nullable SpecialtyFood f) {
        if (f == null) {
            return R.drawable.food_com_tam;
        }
        Integer curated = FOOD_ID_TO_DRAWABLE.get(f.id);
        if (curated != null) {
            return curated;
        }
        if (VALID_FOOD_DRAWABLES.contains(f.imageResId) && isValidPhotoDrawable(res, f.imageResId, true)) {
            return f.imageResId;
        }
        return defaultFoodDrawable(f.category);
    }

    public static Attraction healAttraction(@Nullable Resources res, Attraction a) {
        if (a == null) return null;
        int resolved = resolveAttractionDrawable(res, a);
        if (a.imageResId == resolved) {
            return a;
        }
        return new Attraction(
                a.id,
                a.nameEn,
                a.nameVi,
                a.category,
                a.imageUrl,
                resolved,
                a.district,
                a.address,
                a.latitude,
                a.longitude,
                a.ticketPrice,
                a.openingHours,
                a.shortSummary,
                a.history,
                a.culture,
                a.foreignerTips,
                a.province
        );
    }

    public static SpecialtyFood healFood(@Nullable Resources res, SpecialtyFood f) {
        if (f == null) return null;
        int resolved = resolveFoodDrawable(res, f);
        if (f.imageResId == resolved) {
            return f;
        }
        return new SpecialtyFood(
                f.id,
                f.nameEn,
                f.nameVi,
                f.category,
                f.imageUrl,
                resolved,
                f.shortDescription,
                f.storyAndHistory,
                f.howToEatLikeALocal,
                f.ingredients,
                f.restaurants
        );
    }
}
