package com.example.nhom_15_dulichtphcm.data.local;

import android.content.Context;

import com.example.nhom_15_dulichtphcm.data.db.SaigonTravelDatabaseHelper;
import com.example.nhom_15_dulichtphcm.data.model.Attraction;
import com.example.nhom_15_dulichtphcm.data.model.Permission;
import com.example.nhom_15_dulichtphcm.data.model.ResultBox;
import com.example.nhom_15_dulichtphcm.util.Localized;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * E-Ticket QR Wallet manager backed by SQLite table tickets (saigon_travel.db).
 */
public final class TicketWalletManager {

    private static final List<SaigonTravelDatabaseHelper.TicketRow> tickets = new ArrayList<>();

    private TicketWalletManager() {}

    public static synchronized void initialize(Context context) {
        if (context != null) {
            SaigonTravelDatabaseHelper.getInstance(context);
        }
        reloadActiveScope();
    }

    public static synchronized void reloadActiveScope() {
        tickets.clear();
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            tickets.addAll(db.getTickets(AuthManager.getActiveUserScope()));
        }
    }

    public static synchronized void resetForTesting() {
        tickets.clear();
    }

    public static synchronized List<SaigonTravelDatabaseHelper.TicketRow> getTickets() {
        return Collections.unmodifiableList(new ArrayList<>(tickets));
    }

    public static synchronized int getTicketCount() {
        return tickets.size();
    }

    /**
     * Extracts base adult price in VND from human-readable attraction.ticketPrice.
     * For free landmarks, returns 0L (Free E-Pass Reservation).
     */
    public static long parseAdultPriceVnd(String rawPrice) {
        if (rawPrice == null) return 40_000L;
        String lower = rawPrice.toLowerCase(Locale.ROOT);
        if (lower.contains("miễn phí") || lower.contains("free")) {
            return 0L;
        }
        Matcher m = Pattern.compile("(\\d{1,3}(?:[\\.,]\\d{3})+)").matcher(rawPrice);
        if (m.find()) {
            String digits = m.group(1).replace(".", "").replace(",", "");
            try {
                return Long.parseLong(digits);
            } catch (NumberFormatException ignored) {}
        }
        return 40_000L;
    }

    public static long calculateTotalVnd(long baseAdultVnd, int adults, int students, int children) {
        long adultPart = Math.max(0, adults) * baseAdultVnd;
        long studentPart = Math.max(0, students) * (baseAdultVnd / 2L);
        long childPart = Math.max(0, children) * (baseAdultVnd / 4L);
        return adultPart + studentPart + childPart;
    }

    public static synchronized ResultBox<SaigonTravelDatabaseHelper.TicketRow> bookTicket(
            Attraction attraction,
            String visitDate,
            String timeSlot,
            int adultQty,
            int studentQty,
            int childQty,
            boolean recordToBudget
    ) {
        ResultBox<Void> perm = AuthManager.requirePermission(Permission.SAVE_PERSONAL_COLLECTION);
        if (!perm.isSuccess()) {
            return ResultBox.failure(perm.getError());
        }
        if (attraction == null) {
            return ResultBox.failureMessage("Invalid attraction");
        }
        int totalGuests = Math.max(0, adultQty) + Math.max(0, studentQty) + Math.max(0, childQty);
        if (totalGuests <= 0) {
            return ResultBox.failureMessage(Localized.tr(
                    "Vui lòng chọn ít nhất 1 khách tham quan",
                    "Please select at least 1 visitor"));
        }

        long basePrice = parseAdultPriceVnd(attraction.ticketPrice);
        long totalVnd = calculateTotalVnd(basePrice, adultQty, studentQty, childQty);
        String cleanDate = (visitDate != null && !visitDate.trim().isEmpty()) ? visitDate.trim() : "Hôm nay";
        String cleanSlot = (timeSlot != null && !timeSlot.trim().isEmpty()) ? timeSlot.trim() : "08:30 - 10:30";

        String code = "SGN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.US);
        String scope = AuthManager.getActiveUserScope();
        String qrPayload = "SAIGON_PASS|" + code + "|" + attraction.id + "|" + cleanDate
                + "|" + cleanSlot + "|PAX=" + totalGuests + "|VND=" + totalVnd + "|USR=" + scope;

        SaigonTravelDatabaseHelper.TicketRow created = new SaigonTravelDatabaseHelper.TicketRow(
                code,
                attraction.id,
                attraction.nameVi,
                cleanDate,
                cleanSlot,
                Math.max(0, adultQty),
                Math.max(0, studentQty),
                Math.max(0, childQty),
                totalVnd,
                qrPayload,
                System.currentTimeMillis()
        );
        tickets.add(0, created);

        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            db.upsertTicket(scope, created);
        }

        if (recordToBudget && totalVnd > 0) {
            TripExpenseManager.addExpense(
                    "🎫 Vé " + attraction.nameVi + " (" + totalGuests + " khách)",
                    totalVnd,
                    ExpenseCategory.TICKET
            );
        }

        AuthManager.recordAudit(
                "Đặt vé điện tử QR " + code + " (" + attraction.nameVi + ")",
                "Booked QR E-Ticket " + code + " (" + attraction.nameEn + ")"
        );
        return ResultBox.success(created);
    }

    public static synchronized void cancelTicket(String ticketId) {
        if (ticketId == null) return;
        tickets.removeIf(t -> ticketId.equals(t.id));
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            db.deleteTicket(ticketId);
        }
    }
}
