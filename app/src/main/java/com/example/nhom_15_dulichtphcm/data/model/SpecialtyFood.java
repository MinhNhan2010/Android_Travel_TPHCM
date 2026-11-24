package com.example.nhom_15_dulichtphcm.data.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class SpecialtyFood {
    public final String id;
    public final String nameEn;
    public final String nameVi;
    public final FoodCategory category;
    public final String imageUrl;
    public final int imageResId;
    public final String shortDescription;
    public final String storyAndHistory;
    public final String howToEatLikeALocal;
    public final List<String> ingredients;
    public final List<Restaurant> restaurants;

    public SpecialtyFood(
            String id,
            String nameEn,
            String nameVi,
            FoodCategory category,
            String imageUrl,
            int imageResId,
            String shortDescription,
            String storyAndHistory,
            String howToEatLikeALocal,
            List<String> ingredients,
            List<Restaurant> restaurants
    ) {
        this.id = id != null ? id : "";
        this.nameEn = nameEn != null ? nameEn : "";
        this.nameVi = nameVi != null ? nameVi : "";
        this.category = category != null ? category : FoodCategory.STREET_SNACKS;
        this.imageUrl = imageUrl != null ? imageUrl : "";
        this.imageResId = imageResId;
        this.shortDescription = shortDescription != null ? shortDescription : "";
        this.storyAndHistory = storyAndHistory != null ? storyAndHistory : "";
        this.howToEatLikeALocal = howToEatLikeALocal != null ? howToEatLikeALocal : "";
        this.ingredients = ingredients != null
                ? Collections.unmodifiableList(new ArrayList<>(ingredients))
                : Collections.emptyList();
        this.restaurants = restaurants != null
                ? Collections.unmodifiableList(new ArrayList<>(restaurants))
                : Collections.emptyList();
    }

    public String getId() { return id; }
    public String getNameEn() { return nameEn; }
    public String getNameVi() { return nameVi; }
    public FoodCategory getCategory() { return category; }
    public String getImageUrl() { return imageUrl; }
    public int getImageResId() { return imageResId; }
    public String getShortDescription() { return shortDescription; }
    public String getStoryAndHistory() { return storyAndHistory; }
    public String getHowToEatLikeALocal() { return howToEatLikeALocal; }
    public List<String> getIngredients() { return ingredients; }
    public List<Restaurant> getRestaurants() { return restaurants; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SpecialtyFood)) return false;
        SpecialtyFood that = (SpecialtyFood) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
