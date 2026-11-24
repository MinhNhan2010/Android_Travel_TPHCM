package com.example.nhom_15_dulichtphcm.data.model;

public final class AiItineraryDay {
    public final String dayTitle;
    public final String morningPlan;
    public final String afternoonPlan;
    public final String eveningPlan;

    public AiItineraryDay(
            String dayTitle,
            String morningPlan,
            String afternoonPlan,
            String eveningPlan
    ) {
        this.dayTitle = dayTitle != null ? dayTitle : "";
        this.morningPlan = morningPlan != null ? morningPlan : "";
        this.afternoonPlan = afternoonPlan != null ? afternoonPlan : "";
        this.eveningPlan = eveningPlan != null ? eveningPlan : "";
    }
}
