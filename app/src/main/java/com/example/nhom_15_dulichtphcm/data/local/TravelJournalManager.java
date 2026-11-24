package com.example.nhom_15_dulichtphcm.data.local;

import android.content.Context;

import com.example.nhom_15_dulichtphcm.data.db.SaigonTravelDatabaseHelper;
import com.example.nhom_15_dulichtphcm.data.model.Attraction;
import com.example.nhom_15_dulichtphcm.data.model.Permission;
import com.example.nhom_15_dulichtphcm.data.model.ResultBox;
import com.example.nhom_15_dulichtphcm.data.model.UserAccount;
import com.example.nhom_15_dulichtphcm.util.Localized;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Travel Journal & Saigon Postcard manager backed by SQLite table travel_journals.
 */
public final class TravelJournalManager {

    public static final String[] MOOD_EMOJIS = {"😍", "🤩", "😋", "😌", "🥳", "🌅"};

    private static final List<SaigonTravelDatabaseHelper.JournalRow> journals = new ArrayList<>();

    private TravelJournalManager() {}

    public static synchronized void initialize(Context context) {
        if (context != null) {
            SaigonTravelDatabaseHelper.getInstance(context);
        }
        reloadActiveScope();
    }

    public static synchronized void reloadActiveScope() {
        journals.clear();
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            journals.addAll(db.getJournals(AuthManager.getActiveUserScope()));
        }
    }

    public static synchronized void resetForTesting() {
        journals.clear();
    }

    public static synchronized List<SaigonTravelDatabaseHelper.JournalRow> getJournals() {
        return Collections.unmodifiableList(new ArrayList<>(journals));
    }

    public static synchronized SaigonTravelDatabaseHelper.JournalRow getJournalForAttraction(String attractionId) {
        if (attractionId == null) return null;
        for (SaigonTravelDatabaseHelper.JournalRow j : journals) {
            if (attractionId.equals(j.attractionId)) {
                return j;
            }
        }
        return null;
    }

    public static synchronized ResultBox<SaigonTravelDatabaseHelper.JournalRow> saveJournal(
            Attraction attraction,
            String moodEmoji,
            String note,
            String visitDate
    ) {
        ResultBox<Void> perm = AuthManager.requirePermission(Permission.SAVE_PERSONAL_COLLECTION);
        if (!perm.isSuccess()) {
            return ResultBox.failure(perm.getError());
        }
        if (attraction == null) {
            return ResultBox.failureMessage("Invalid attraction");
        }
        String cleanNote = note != null ? note.trim() : "";
        if (cleanNote.length() < 3) {
            return ResultBox.failureMessage(Localized.tr(
                    "Vui lòng viết ít nhất 3 ký tự kỷ niệm",
                    "Please write at least 3 characters for your memory"));
        }
        String cleanMood = (moodEmoji != null && !moodEmoji.isEmpty()) ? moodEmoji : "😍";
        String cleanDate = (visitDate != null && !visitDate.trim().isEmpty())
                ? visitDate.trim()
                : new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        SaigonTravelDatabaseHelper.JournalRow row = new SaigonTravelDatabaseHelper.JournalRow(
                attraction.id,
                attraction.nameVi,
                cleanMood,
                cleanNote,
                cleanDate,
                System.currentTimeMillis()
        );

        journals.removeIf(j -> attraction.id.equals(j.attractionId));
        journals.add(0, row);

        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            db.upsertJournal(AuthManager.getActiveUserScope(), row);
        }
        return ResultBox.success(row);
    }

    public static synchronized void deleteJournal(String attractionId) {
        if (attractionId == null) return;
        journals.removeIf(j -> attractionId.equals(j.attractionId));
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            db.deleteJournal(AuthManager.getActiveUserScope(), attractionId);
        }
    }

    public static String formatPostcardText(Attraction attraction, SaigonTravelDatabaseHelper.JournalRow journal) {
        UserAccount u = AuthManager.getCurrentUser();
        String author = u != null ? u.fullName : Localized.tr("Lữ khách Sài Gòn", "Saigon Traveler");
        return "📮 BƯU THIẾP KỶ NIỆM DU LỊCH — EXPLORE SAIGON 🇻🇳\n"
                + "━━━━━━━━━━━━━━━━━━━━━━━━━━\n"
                + "📍 Điểm đến: " + attraction.nameVi + " (" + attraction.nameEn + ")\n"
                + "🗺️ Khu vực: " + attraction.district + " • " + attraction.province + "\n"
                + "📅 Ngày ghé thăm: " + journal.visitDate + "   |   Cảm xúc: " + journal.moodEmoji + "\n\n"
                + "❝ " + journal.note + " ❞\n\n"
                + "✍️ Người gửi: " + author + "\n"
                + "━━━━━━━━━━━━━━━━━━━━━━━━━━";
    }
}
