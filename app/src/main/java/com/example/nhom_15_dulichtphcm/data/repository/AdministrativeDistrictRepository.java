package com.example.nhom_15_dulichtphcm.data.repository;

import com.example.nhom_15_dulichtphcm.data.model.AdministrativeDistrict;

import java.util.Collections;
import java.util.List;

/**
 * Provides the 13 post-merger administrative wards, districts, and regional corridors
 * across Ho Chi Minh City, Binh Duong, and Ba Ria - Vung Tau.
 */
public final class AdministrativeDistrictRepository {

    private static final List<AdministrativeDistrict> DISTRICTS =
            Collections.unmodifiableList(AdministrativeDistrictSeedData.createDefaultDistricts());

    private AdministrativeDistrictRepository() {}

    public static List<AdministrativeDistrict> getDistricts() {
        return DISTRICTS;
    }

    public static AdministrativeDistrict getById(String id) {
        if (id == null) return null;
        for (AdministrativeDistrict d : DISTRICTS) {
            if (d.id.equals(id)) return d;
        }
        return null;
    }
}
