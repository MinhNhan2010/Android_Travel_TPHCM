package com.example.nhom_15_dulichtphcm.data.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RideProvider {
    public final String id;
    public final String name;
    public final String tagLine;
    public final String badge;
    public final String iconEmoji;
    public final String packageName;
    public final String webUrl;
    public final String hotline;
    public final String descriptionEn;
    public final String descriptionVi;
    public final List<String> services;
    public final List<String> paymentMethods;
    public final List<String> touristPros;
    public final int bikeBaseFare;
    public final int bikePerKm;
    public final int carBaseFare;
    public final int carPerKm;

    public RideProvider(
            String id,
            String name,
            String tagLine,
            String badge,
            String iconEmoji,
            String packageName,
            String webUrl,
            String hotline,
            String descriptionEn,
            String descriptionVi,
            List<String> services,
            List<String> paymentMethods,
            List<String> touristPros,
            int bikeBaseFare,
            int bikePerKm,
            int carBaseFare,
            int carPerKm
    ) {
        this.id = id != null ? id : "";
        this.name = name != null ? name : "";
        this.tagLine = tagLine != null ? tagLine : "";
        this.badge = badge != null ? badge : "";
        this.iconEmoji = iconEmoji != null ? iconEmoji : "🚕";
        this.packageName = packageName != null ? packageName : "";
        this.webUrl = webUrl != null ? webUrl : "";
        this.hotline = (hotline != null && !hotline.trim().isEmpty()) ? hotline.trim() : null;
        this.descriptionEn = descriptionEn != null ? descriptionEn : "";
        this.descriptionVi = descriptionVi != null ? descriptionVi : "";
        this.services = services != null
                ? Collections.unmodifiableList(new ArrayList<>(services))
                : Collections.emptyList();
        this.paymentMethods = paymentMethods != null
                ? Collections.unmodifiableList(new ArrayList<>(paymentMethods))
                : Collections.emptyList();
        this.touristPros = touristPros != null
                ? Collections.unmodifiableList(new ArrayList<>(touristPros))
                : Collections.emptyList();
        this.bikeBaseFare = bikeBaseFare;
        this.bikePerKm = bikePerKm;
        this.carBaseFare = carBaseFare;
        this.carPerKm = carPerKm;
    }
}
