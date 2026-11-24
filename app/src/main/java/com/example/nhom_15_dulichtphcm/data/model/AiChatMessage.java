package com.example.nhom_15_dulichtphcm.data.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class AiChatMessage {
    public final String id;
    public final boolean isUser;
    public final String text;
    public final List<Attraction> recommendedAttractions;
    public final List<SpecialtyFood> recommendedFoods;
    public final List<AiItineraryDay> itineraryDays;
    public final List<String> quickFollowUps;
    public final boolean fromGemini;

    public AiChatMessage(
            String id,
            boolean isUser,
            String text,
            List<Attraction> recommendedAttractions,
            List<SpecialtyFood> recommendedFoods,
            List<AiItineraryDay> itineraryDays,
            List<String> quickFollowUps,
            boolean fromGemini
    ) {
        this.id = (id != null && !id.isEmpty()) ? id : UUID.randomUUID().toString();
        this.isUser = isUser;
        this.text = text != null ? text : "";
        this.recommendedAttractions = recommendedAttractions != null
                ? Collections.unmodifiableList(new ArrayList<>(recommendedAttractions))
                : Collections.emptyList();
        this.recommendedFoods = recommendedFoods != null
                ? Collections.unmodifiableList(new ArrayList<>(recommendedFoods))
                : Collections.emptyList();
        this.itineraryDays = itineraryDays != null
                ? Collections.unmodifiableList(new ArrayList<>(itineraryDays))
                : Collections.emptyList();
        this.quickFollowUps = quickFollowUps != null
                ? Collections.unmodifiableList(new ArrayList<>(quickFollowUps))
                : Collections.emptyList();
        this.fromGemini = fromGemini;
    }

    public static AiChatMessage userMessage(String text) {
        return new AiChatMessage(
                UUID.randomUUID().toString(),
                true,
                text,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                false
        );
    }
}
