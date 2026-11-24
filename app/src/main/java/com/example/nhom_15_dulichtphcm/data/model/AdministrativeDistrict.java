package com.example.nhom_15_dulichtphcm.data.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AdministrativeDistrict {
    public final String id;
    public final String nameVi;
    public final String nameEn;
    public final AdministrativeType type;
    public final RegionZone zone;
    public final double areaKm2;
    public final String population;
    public final int wardCount;
    public final String mergerStatus;
    public final String description;
    public final List<String> touristHighlights;
    public final double centerLat;
    public final double centerLng;
    public final List<LatLngPoint> boundaryPolygon;

    public AdministrativeDistrict(
            String id,
            String nameVi,
            String nameEn,
            AdministrativeType type,
            RegionZone zone,
            double areaKm2,
            String population,
            int wardCount,
            String mergerStatus,
            String description,
            List<String> touristHighlights,
            double centerLat,
            double centerLng,
            List<LatLngPoint> boundaryPolygon
    ) {
        this.id = id != null ? id : "";
        this.nameVi = nameVi != null ? nameVi : "";
        this.nameEn = nameEn != null ? nameEn : "";
        this.type = type != null ? type : AdministrativeType.URBAN_DISTRICT;
        this.zone = zone != null ? zone : RegionZone.CENTRAL_CORE;
        this.areaKm2 = areaKm2;
        this.population = population != null ? population : "";
        this.wardCount = wardCount;
        this.mergerStatus = mergerStatus != null ? mergerStatus : "";
        this.description = description != null ? description : "";
        this.touristHighlights = touristHighlights != null
                ? Collections.unmodifiableList(new ArrayList<>(touristHighlights))
                : Collections.emptyList();
        this.centerLat = centerLat;
        this.centerLng = centerLng;
        this.boundaryPolygon = boundaryPolygon != null
                ? Collections.unmodifiableList(new ArrayList<>(boundaryPolygon))
                : Collections.emptyList();
    }

    public String getId() { return id; }
    public String getNameVi() { return nameVi; }
    public String getNameEn() { return nameEn; }
    public AdministrativeType getType() { return type; }
    public RegionZone getZone() { return zone; }
    public double getAreaKm2() { return areaKm2; }
    public String getPopulation() { return population; }
    public int getWardCount() { return wardCount; }
    public String getMergerStatus() { return mergerStatus; }
    public String getDescription() { return description; }
    public List<String> getTouristHighlights() { return touristHighlights; }
    public double getCenterLat() { return centerLat; }
    public double getCenterLng() { return centerLng; }
    public List<LatLngPoint> getBoundaryPolygon() { return boundaryPolygon; }
}
