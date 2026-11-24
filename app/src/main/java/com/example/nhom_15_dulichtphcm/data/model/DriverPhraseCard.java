package com.example.nhom_15_dulichtphcm.data.model;

public final class DriverPhraseCard {
    public final String id;
    public final String english;
    public final String vietnamese;
    public final String pronunciation;
    public final String category;
    public final String tip;

    public DriverPhraseCard(
            String id,
            String english,
            String vietnamese,
            String pronunciation,
            String category,
            String tip
    ) {
        this.id = id != null ? id : "";
        this.english = english != null ? english : "";
        this.vietnamese = vietnamese != null ? vietnamese : "";
        this.pronunciation = pronunciation != null ? pronunciation : "";
        this.category = category != null ? category : "";
        this.tip = tip != null ? tip : "";
    }
}
