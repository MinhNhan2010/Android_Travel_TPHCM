package com.example.nhom_15_dulichtphcm.data.model;

public final class RideEstimate {
    public final double distanceKm;
    public final int durationMinutes;
    public final int bikeFareVnd;
    public final int car4FareVnd;
    public final int car7FareVnd;

    public RideEstimate(
            double distanceKm,
            int durationMinutes,
            int bikeFareVnd,
            int car4FareVnd,
            int car7FareVnd
    ) {
        this.distanceKm = distanceKm;
        this.durationMinutes = durationMinutes;
        this.bikeFareVnd = bikeFareVnd;
        this.car4FareVnd = car4FareVnd;
        this.car7FareVnd = car7FareVnd;
    }
}
