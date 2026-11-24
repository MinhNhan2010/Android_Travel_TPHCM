package com.example.nhom_15_dulichtphcm.data.local;

/**
 * Individual trip expense entry backed by SQLite table expenses.
 */
public final class TripExpense {
    public final String id;
    public final String title;
    public final long amountVnd;
    public final ExpenseCategory category;
    public final long createdAt;

    public TripExpense(String id, String title, long amountVnd, ExpenseCategory category, long createdAt) {
        this.id = id != null ? id : "";
        this.title = title != null ? title : "";
        this.amountVnd = amountVnd;
        this.category = category != null ? category : ExpenseCategory.OTHER;
        this.createdAt = createdAt;
    }
}
