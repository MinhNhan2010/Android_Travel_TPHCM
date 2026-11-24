package com.example.nhom_15_dulichtphcm.data.local;

import android.content.Context;

import com.example.nhom_15_dulichtphcm.data.db.SaigonTravelDatabaseHelper;
import com.example.nhom_15_dulichtphcm.data.model.ResultBox;
import com.example.nhom_15_dulichtphcm.util.Localized;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Trip Budget & Group Split Manager persisted in SQLite tables expenses and app_settings.
 */
public final class TripExpenseManager {

    public static final long DEFAULT_BUDGET_VND = 5_000_000L;
    private static final String BUDGET_KEY_PREFIX = "trip_budget_vnd_";

    private static final List<TripExpense> expenses = new ArrayList<>();
    private static volatile long budgetVnd = DEFAULT_BUDGET_VND;
    private static final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    private TripExpenseManager() {}

    public static synchronized void initialize(Context context) {
        if (context != null) {
            SaigonTravelDatabaseHelper.getInstance(context);
        }
        reloadActiveScope();
    }

    public static synchronized void reloadActiveScope() {
        expenses.clear();
        budgetVnd = DEFAULT_BUDGET_VND;
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            String scope = AuthManager.getActiveUserScope();
            for (SaigonTravelDatabaseHelper.ExpenseRow row : db.getExpenses(scope)) {
                expenses.add(new TripExpense(
                        row.id,
                        row.title,
                        row.amountVnd,
                        ExpenseCategory.fromName(row.category),
                        row.createdAt
                ));
            }
            String savedBudget = db.getSetting(BUDGET_KEY_PREFIX + scope, null);
            if (savedBudget != null) {
                try {
                    long parsed = Long.parseLong(savedBudget);
                    if (parsed > 0) budgetVnd = parsed;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        notifyListeners();
    }

    public static synchronized void resetForTesting() {
        expenses.clear();
        budgetVnd = DEFAULT_BUDGET_VND;
        notifyListeners();
    }

    public static synchronized List<TripExpense> getExpenses() {
        return Collections.unmodifiableList(new ArrayList<>(expenses));
    }

    public static long getBudgetVnd() {
        return budgetVnd;
    }

    public static synchronized long getTotalSpentVnd() {
        long sum = 0L;
        for (TripExpense e : expenses) {
            sum += e.amountVnd;
        }
        return sum;
    }

    public static synchronized long getRemainingBudgetVnd() {
        return budgetVnd - getTotalSpentVnd();
    }

    public static synchronized ResultBox<Long> setBudget(long newBudgetVnd) {
        if (newBudgetVnd < 100_000L) {
            return ResultBox.failureMessage(Localized.tr(
                    "Ngân sách tối thiểu là 100.000đ",
                    "Minimum budget is 100,000 VND"));
        }
        budgetVnd = newBudgetVnd;
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            db.putSetting(BUDGET_KEY_PREFIX + AuthManager.getActiveUserScope(), String.valueOf(newBudgetVnd));
        }
        notifyListeners();
        return ResultBox.success(newBudgetVnd);
    }

    public static synchronized ResultBox<TripExpense> addExpense(
            String title,
            long amountVnd,
            ExpenseCategory category
    ) {
        String cleanTitle = title != null ? title.trim() : "";
        if (cleanTitle.isEmpty()) {
            return ResultBox.failureMessage(Localized.tr(
                    "Vui lòng nhập tên khoản chi",
                    "Please enter an expense title"));
        }
        if (amountVnd <= 0L) {
            return ResultBox.failureMessage(Localized.tr(
                    "Số tiền phải lớn hơn 0đ",
                    "Amount must be greater than 0 VND"));
        }
        ExpenseCategory cat = category != null ? category : ExpenseCategory.OTHER;
        TripExpense item = new TripExpense(
                "exp_" + UUID.randomUUID().toString().substring(0, 8),
                cleanTitle,
                amountVnd,
                cat,
                System.currentTimeMillis()
        );
        expenses.add(0, item);
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            db.insertExpense(AuthManager.getActiveUserScope(), item.id, item.title, item.amountVnd, item.category.name(), item.createdAt);
        }
        notifyListeners();
        return ResultBox.success(item);
    }

    public static synchronized void deleteExpense(String id) {
        if (id == null) return;
        expenses.removeIf(e -> e.id.equals(id));
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            db.deleteExpense(id);
        }
        notifyListeners();
    }

    public static synchronized void clearExpenses() {
        expenses.clear();
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            db.clearExpenses(AuthManager.getActiveUserScope());
        }
        notifyListeners();
    }

    /**
     * Ceiling division per person so the group total is always covered.
     */
    public static synchronized long splitPerPerson(int peopleCount) {
        int p = Math.max(1, peopleCount);
        long total = getTotalSpentVnd();
        if (total <= 0L) return 0L;
        return (total + p - 1L) / p;
    }

    public static synchronized Map<ExpenseCategory, Long> breakdownByCategory() {
        Map<ExpenseCategory, Long> map = new EnumMap<>(ExpenseCategory.class);
        for (ExpenseCategory c : ExpenseCategory.values()) {
            map.put(c, 0L);
        }
        for (TripExpense e : expenses) {
            map.put(e.category, map.get(e.category) + e.amountVnd);
        }
        return map;
    }

    public static void addListener(Runnable listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public static void removeListener(Runnable listener) {
        listeners.remove(listener);
    }

    private static void notifyListeners() {
        for (Runnable r : listeners) {
            r.run();
        }
    }
}
