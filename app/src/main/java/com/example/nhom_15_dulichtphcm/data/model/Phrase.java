package com.example.nhom_15_dulichtphcm.data.model;

import java.util.Objects;

public final class Phrase {
    public final String id;
    public final String english;
    public final String vietnamese;
    public final String vietnameseUnaccented;
    public final String pronunciation;
    public final String explanation;
    public final PhraseCategory category;
    public final boolean isSaved;
    public final boolean isCustom;

    public Phrase(
            String id,
            String english,
            String vietnamese,
            String vietnameseUnaccented,
            String pronunciation,
            String explanation,
            PhraseCategory category,
            boolean isSaved,
            boolean isCustom
    ) {
        this.id = id != null ? id : "";
        this.english = english != null ? english : "";
        this.vietnamese = vietnamese != null ? vietnamese : "";
        this.vietnameseUnaccented = vietnameseUnaccented != null ? vietnameseUnaccented : "";
        this.pronunciation = pronunciation != null ? pronunciation : "";
        this.explanation = explanation != null ? explanation : "";
        this.category = category != null ? category : PhraseCategory.GREETING;
        this.isSaved = isSaved;
        this.isCustom = isCustom;
    }

    public Phrase withSaved(boolean saved) {
        return new Phrase(id, english, vietnamese, vietnameseUnaccented,
                pronunciation, explanation, category, saved, isCustom);
    }

    public String getId() { return id; }
    public String getEnglish() { return english; }
    public String getVietnamese() { return vietnamese; }
    public String getVietnameseUnaccented() { return vietnameseUnaccented; }
    public String getPronunciation() { return pronunciation; }
    public String getExplanation() { return explanation; }
    public PhraseCategory getCategory() { return category; }
    public boolean isSaved() { return isSaved; }
    public boolean isCustom() { return isCustom; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Phrase)) return false;
        Phrase phrase = (Phrase) o;
        return Objects.equals(id, phrase.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
