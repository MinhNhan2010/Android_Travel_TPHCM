package com.example.nhom_15_dulichtphcm.data.local;

import com.example.nhom_15_dulichtphcm.data.db.SaigonTravelDatabaseHelper;
import com.example.nhom_15_dulichtphcm.data.model.Attraction;
import com.example.nhom_15_dulichtphcm.data.model.ResultBox;
import com.example.nhom_15_dulichtphcm.data.model.UserAccount;
import com.example.nhom_15_dulichtphcm.data.repository.AttractionRepository;
import com.example.nhom_15_dulichtphcm.util.Localized;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Exports/Restores full personal SQLite data (JSON) and generates printable Trip Summary Reports,
 * plus stores Cultural Quiz High-Score & Dietary Allergy Flags in SQLite app_settings.
 */
public final class UserDataBackupManager {

    private UserDataBackupManager() {}

    // =========================================================================
    // QUIZ HIGH SCORE & DIETARY ALLERGY FLAGS (SQLite app_settings)
    // =========================================================================

    public static int getBestQuizScore() {
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db == null) return 0;
        String raw = db.getSetting("quiz_best_" + AuthManager.getActiveUserScope(), "0");
        try {
            return Integer.parseInt(raw);
        } catch (Exception e) {
            return 0;
        }
    }

    public static void recordQuizScore(int score) {
        int prev = getBestQuizScore();
        if (score > prev) {
            SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
            if (db != null) {
                db.putSetting("quiz_best_" + AuthManager.getActiveUserScope(), String.valueOf(score));
            }
        }
    }

    public static boolean hasCulturalScholarBadge() {
        return getBestQuizScore() >= 7;
    }

    public static Set<String> getSelectedAllergyCodes() {
        Set<String> set = new LinkedHashSet<>();
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db == null) return set;
        String raw = db.getSetting("allergy_" + AuthManager.getActiveUserScope(), "");
        if (raw != null && !raw.trim().isEmpty()) {
            for (String p : raw.split(",")) {
                if (!p.trim().isEmpty()) set.add(p.trim());
            }
        }
        return set;
    }

    public static void toggleAllergyCode(String code) {
        if (code == null || code.isEmpty()) return;
        Set<String> cur = getSelectedAllergyCodes();
        if (cur.contains(code)) cur.remove(code);
        else cur.add(code);
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            db.putSetting("allergy_" + AuthManager.getActiveUserScope(), String.join(",", cur));
        }
    }

    // =========================================================================
    // JSON BACKUP & RESTORE
    // =========================================================================

    public static String exportPersonalBackupJson() {
        try {
            JSONObject root = new JSONObject();
            UserAccount u = AuthManager.getCurrentUser();
            root.put("app", "ExploreSaigon_SQLite_v2");
            root.put("exportedAt", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()));
            root.put("userScope", AuthManager.getActiveUserScope());
            root.put("displayName", u != null ? u.fullName : "Guest");
            root.put("quizBestScore", getBestQuizScore());

            JSONArray favAttr = new JSONArray();
            for (String id : FavoritesManager.getAttractionIds()) favAttr.put(id);
            root.put("favoriteAttractions", favAttr);

            JSONArray favFood = new JSONArray();
            for (String id : FavoritesManager.getFoodIds()) favFood.put(id);
            root.put("favoriteFoods", favFood);

            JSONArray tripStops = new JSONArray();
            for (String id : TripPlanManager.getStopIds()) tripStops.put(id);
            root.put("tripStops", tripStops);

            JSONArray stamps = new JSONArray();
            for (CheckInStamp s : TravelPassportManager.getStamps()) {
                JSONObject obj = new JSONObject();
                obj.put("attractionId", s.attractionId);
                obj.put("attractionName", s.attractionName);
                obj.put("province", s.province);
                obj.put("checkedInAt", s.checkedInAt);
                stamps.put(obj);
            }
            root.put("passportStamps", stamps);

            JSONArray journals = new JSONArray();
            for (SaigonTravelDatabaseHelper.JournalRow j : TravelJournalManager.getJournals()) {
                JSONObject obj = new JSONObject();
                obj.put("attractionId", j.attractionId);
                obj.put("attractionName", j.attractionName);
                obj.put("moodEmoji", j.moodEmoji);
                obj.put("note", j.note);
                obj.put("visitDate", j.visitDate);
                journals.put(obj);
            }
            root.put("travelJournals", journals);

            JSONArray tickets = new JSONArray();
            for (SaigonTravelDatabaseHelper.TicketRow t : TicketWalletManager.getTickets()) {
                JSONObject obj = new JSONObject();
                obj.put("id", t.id);
                obj.put("attractionId", t.attractionId);
                obj.put("attractionName", t.attractionName);
                obj.put("visitDate", t.visitDate);
                obj.put("timeSlot", t.timeSlot);
                obj.put("adultQty", t.adultQty);
                obj.put("studentQty", t.studentQty);
                obj.put("childQty", t.childQty);
                obj.put("totalVnd", t.totalVnd);
                obj.put("qrPayload", t.qrPayload);
                tickets.put(obj);
            }
            root.put("bookedTickets", tickets);

            return root.toString(2);
        } catch (Exception e) {
            return "{}";
        }
    }

    public static ResultBox<Integer> importPersonalBackupJson(String rawJson) {
        if (rawJson == null || rawJson.trim().isEmpty()) {
            return ResultBox.failureMessage(Localized.tr("Dữ liệu JSON trống", "JSON backup is empty"));
        }
        try {
            JSONObject root = new JSONObject(rawJson.trim());
            SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
            String scope = AuthManager.getActiveUserScope();
            int restoredItems = 0;

            if (root.has("quizBestScore")) {
                recordQuizScore(root.optInt("quizBestScore", 0));
            }

            JSONArray tripStops = root.optJSONArray("tripStops");
            if (tripStops != null) {
                List<String> ids = new ArrayList<>();
                for (int i = 0; i < tripStops.length(); i++) {
                    ids.add(tripStops.getString(i));
                    restoredItems++;
                }
                TripPlanManager.replaceOrder(ids);
            }

            if (db != null) {
                JSONArray favAttr = root.optJSONArray("favoriteAttractions");
                if (favAttr != null) {
                    for (int i = 0; i < favAttr.length(); i++) {
                        db.setFavorite(scope, "attraction", favAttr.getString(i), true);
                        restoredItems++;
                    }
                }
                JSONArray favFood = root.optJSONArray("favoriteFoods");
                if (favFood != null) {
                    for (int i = 0; i < favFood.length(); i++) {
                        db.setFavorite(scope, "food", favFood.getString(i), true);
                        restoredItems++;
                    }
                }
                JSONArray stamps = root.optJSONArray("passportStamps");
                if (stamps != null) {
                    for (int i = 0; i < stamps.length(); i++) {
                        JSONObject s = stamps.getJSONObject(i);
                        db.upsertPassportStamp(
                                scope,
                                s.getString("attractionId"),
                                s.optString("attractionName", ""),
                                s.optString("province", "TP. Hồ Chí Minh"),
                                s.optLong("checkedInAt", System.currentTimeMillis())
                        );
                        restoredItems++;
                    }
                }
                JSONArray journals = root.optJSONArray("travelJournals");
                if (journals != null) {
                    for (int i = 0; i < journals.length(); i++) {
                        JSONObject j = journals.getJSONObject(i);
                        db.upsertJournal(scope, new SaigonTravelDatabaseHelper.JournalRow(
                                j.getString("attractionId"),
                                j.optString("attractionName", ""),
                                j.optString("moodEmoji", "😍"),
                                j.optString("note", ""),
                                j.optString("visitDate", ""),
                                System.currentTimeMillis()
                        ));
                        restoredItems++;
                    }
                }
                JSONArray tickets = root.optJSONArray("bookedTickets");
                if (tickets != null) {
                    for (int i = 0; i < tickets.length(); i++) {
                        JSONObject t = tickets.getJSONObject(i);
                        db.upsertTicket(scope, new SaigonTravelDatabaseHelper.TicketRow(
                                t.getString("id"),
                                t.optString("attractionId", ""),
                                t.optString("attractionName", ""),
                                t.optString("visitDate", ""),
                                t.optString("timeSlot", ""),
                                t.optInt("adultQty", 1),
                                t.optInt("studentQty", 0),
                                t.optInt("childQty", 0),
                                t.optLong("totalVnd", 0L),
                                t.optString("qrPayload", "SAIGON_PASS"),
                                System.currentTimeMillis()
                        ));
                        restoredItems++;
                    }
                }
                FavoritesManager.reloadActiveScope();
                TravelPassportManager.reloadActiveScope();
                TravelJournalManager.reloadActiveScope();
                TicketWalletManager.reloadActiveScope();
            }

            AuthManager.recordAudit(
                    "Khôi phục thành công " + restoredItems + " mục dữ liệu từ bản sao lưu JSON",
                    "Restored " + restoredItems + " items from JSON backup"
            );
            return ResultBox.success(restoredItems);
        } catch (Exception e) {
            return ResultBox.failureMessage(Localized.tr(
                    "Định dạng JSON sao lưu không hợp lệ: " + e.getMessage(),
                    "Invalid backup JSON format: " + e.getMessage()));
        }
    }

    // =========================================================================
    // PRINTABLE / SHAREABLE FULL TRIP DOSSIER REPORT
    // =========================================================================

    public static String buildShareableTripReport() {
        UserAccount u = AuthManager.getCurrentUser();
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        ExplorerRank rank = TravelPassportManager.currentRank();

        StringBuilder sb = new StringBuilder();
        sb.append("🇻🇳 HỒ SƠ HÀNH TRÌNH DU LỊCH — EXPLORE SAIGON & BEYOND\n");
        sb.append("══════════════════════════════════════════\n");
        sb.append("👤 Du khách: ").append(u != null ? u.fullName + " (@" + u.username + ")" : "Khách (Guest)").append("\n");
        sb.append("🏅 Hạng Hộ chiếu: ").append(rank.badgeEmoji).append(" ").append(rank.titleVi);
        if (hasCulturalScholarBadge()) {
            sb.append(" • 🎓 Học Giả Văn Hóa (").append(getBestQuizScore()).append("/10)");
        }
        sb.append("\n\n");

        // 1. Itinerary
        List<String> stopIds = TripPlanManager.getStopIds();
        sb.append("🗓️ LỊCH TRÌNH ĐÃ CHỌN (").append(stopIds.size()).append(" điểm):\n");
        if (stopIds.isEmpty()) {
            sb.append("  • (Chưa thêm điểm dừng nào)\n");
        } else {
            for (int i = 0; i < stopIds.size(); i++) {
                Attraction a = AttractionRepository.getAttractionById(stopIds.get(i));
                if (a != null) {
                    sb.append("  ").append(i + 1).append(". ").append(a.nameVi)
                            .append(" — ").append(a.district).append(" (").append(a.ticketPrice).append(")\n");
                }
            }
        }
        sb.append("\n");

        // 2. Booked QR Tickets
        List<SaigonTravelDatabaseHelper.TicketRow> tks = TicketWalletManager.getTickets();
        sb.append("🎫 VÍ VÉ ĐIỆN TỬ QR (").append(tks.size()).append(" vé):\n");
        if (tks.isEmpty()) {
            sb.append("  • (Chưa có vé điện tử nào)\n");
        } else {
            for (SaigonTravelDatabaseHelper.TicketRow t : tks) {
                sb.append("  • [").append(t.id).append("] ").append(t.attractionName)
                        .append(" | ").append(t.visitDate).append(" ").append(t.timeSlot)
                        .append(" | ").append(nf.format(t.totalVnd)).append("đ\n");
            }
        }
        sb.append("\n");

        // 3. Budget Summary
        sb.append("💰 TỔNG KẾT CHI TIÊU:\n");
        sb.append("  • Đã chi: ").append(nf.format(TripExpenseManager.getTotalSpentVnd()))
                .append("đ / Ngân sách: ").append(nf.format(TripExpenseManager.getBudgetVnd())).append("đ\n\n");

        // 4. Memories / Journals
        List<SaigonTravelDatabaseHelper.JournalRow> jns = TravelJournalManager.getJournals();
        if (!jns.isEmpty()) {
            sb.append("📸 NHẬT KÝ KỶ NIỆM (").append(jns.size()).append("):\n");
            for (SaigonTravelDatabaseHelper.JournalRow j : jns) {
                sb.append("  • ").append(j.moodEmoji).append(" ").append(j.attractionName)
                        .append(" (").append(j.visitDate).append("): ").append(j.note).append("\n");
            }
            sb.append("\n");
        }

        sb.append("📦 Xuất tự động từ CSDL SQLite saigon_travel.db");
        return sb.toString();
    }
}
