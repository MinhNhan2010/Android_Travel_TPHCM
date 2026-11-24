package com.example.nhom_15_dulichtphcm.data.model;

public enum FoodCategory {
    ALL("All Dishes", "Tất cả món"),
    RICE_AND_NOODLES("Rice & Noodles", "Cơm & Mì Phở"),
    STREET_SNACKS("Street Food & Snacks", "Ăn vặt & Đường phố"),
    BEVERAGES_DESSERTS("Coffee & Desserts", "Cà phê & Đồ uống"),
    SEAFOOD("Seafood & Snails", "Hải sản & Quán Ốc");

    public final String label;
    public final String labelVi;

    FoodCategory(String label, String labelVi) {
        this.label = label;
        this.labelVi = labelVi;
    }

    public String getLabel() {
        return label;
    }

    public String getLabelVi() {
        return labelVi;
    }

    public static FoodCategory fromName(String raw) {
        if (raw == null) return STREET_SNACKS;
        for (FoodCategory cat : values()) {
            if (cat.name().equalsIgnoreCase(raw.trim())) {
                return cat;
            }
        }
        return STREET_SNACKS;
    }
}
