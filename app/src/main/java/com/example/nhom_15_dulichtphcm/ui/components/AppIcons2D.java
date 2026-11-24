package com.example.nhom_15_dulichtphcm.ui.components;

import com.example.nhom_15_dulichtphcm.R;
import com.example.nhom_15_dulichtphcm.data.model.UserRole;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Pure Java registry for the 16 custom 2D multi-color vector drawables in res/drawable/.
 */
public final class AppIcons2D {

    public static final int NAV_ATTRACTIONS = R.drawable.ic_2d_nav_attractions;
    public static final int NAV_FOOD = R.drawable.ic_2d_nav_food;
    public static final int NAV_UTILITIES = R.drawable.ic_2d_nav_utilities;
    public static final int NAV_PHRASES = R.drawable.ic_2d_nav_phrases;
    public static final int NAV_MAP = R.drawable.ic_2d_nav_map;

    public static final int GUIDE_COMPASS = R.drawable.ic_2d_guide_compass;
    public static final int FAVORITE = R.drawable.ic_2d_favorite;
    public static final int TRIP_PLAN = R.drawable.ic_2d_trip_plan;
    public static final int RIDE = R.drawable.ic_2d_ride;
    public static final int TOOLKIT = R.drawable.ic_2d_toolkit;

    public static final int REGION_ALL = R.drawable.ic_2d_region_all;
    public static final int REGION_HCM = R.drawable.ic_2d_region_hcm;
    public static final int REGION_BINH_DUONG = R.drawable.ic_2d_region_binhduong;
    public static final int REGION_VUNG_TAU = R.drawable.ic_2d_region_vungtau;

    public static final int ROLE_ADMIN = R.drawable.ic_2d_role_admin;
    public static final int ROLE_MEMBER = R.drawable.ic_2d_role_member;

    public static final List<String> ALL_REQUIRED_2D_DRAWABLES = Collections.unmodifiableList(Arrays.asList(
            "ic_2d_favorite.xml",
            "ic_2d_guide_compass.xml",
            "ic_2d_nav_attractions.xml",
            "ic_2d_nav_food.xml",
            "ic_2d_nav_map.xml",
            "ic_2d_nav_phrases.xml",
            "ic_2d_nav_utilities.xml",
            "ic_2d_region_all.xml",
            "ic_2d_region_binhduong.xml",
            "ic_2d_region_hcm.xml",
            "ic_2d_region_vungtau.xml",
            "ic_2d_ride.xml",
            "ic_2d_role_admin.xml",
            "ic_2d_role_member.xml",
            "ic_2d_toolkit.xml",
            "ic_2d_trip_plan.xml"
    ));

    private AppIcons2D() {}

    public static int regionIconRes(String regionKey) {
        if (regionKey == null) return REGION_ALL;
        String k = regionKey.toLowerCase(Locale.ROOT);
        if (k.contains("hcm") || k.contains("hồ chí minh")) return REGION_HCM;
        if (k.contains("bd") || k.contains("bình dương") || k.contains("binh duong")) return REGION_BINH_DUONG;
        if (k.contains("vt") || k.contains("vũng tàu") || k.contains("vung tau")) return REGION_VUNG_TAU;
        return REGION_ALL;
    }

    public static int roleIconRes(UserRole role) {
        if (role == UserRole.ADMIN || role == UserRole.EDITOR) {
            return ROLE_ADMIN;
        }
        return ROLE_MEMBER;
    }
}
