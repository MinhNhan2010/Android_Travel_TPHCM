package com.example.nhom_15_dulichtphcm.data.local;

/**
 * Expense categories for the Travel Budget & Group Expense Tracker.
 */
public enum ExpenseCategory {
    FOOD("🍜", "Ẩm thực", "Food & Dining"),
    TRANSPORT("🚕", "Di chuyển", "Transport"),
    TICKET("🎟️", "Vé tham quan", "Tickets"),
    SHOPPING("🛍️", "Mua sắm", "Shopping"),
    HOTEL("🏨", "Lưu trú", "Accommodation"),
    OTHER("💡", "Khác", "Other");

    public final String emoji;
    public final String labelVi;
    public final String labelEn;

    ExpenseCategory(String emoji, String labelVi, String labelEn) {
        this.emoji = emoji;
        this.labelVi = labelVi;
        this.labelEn = labelEn;
    }

    public static ExpenseCategory fromName(String raw) {
        if (raw == null) return OTHER;
        for (ExpenseCategory c : values()) {
            if (c.name().equalsIgnoreCase(raw)) return c;
        }
        return OTHER;
    }
}
