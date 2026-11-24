package com.example.nhom_15_dulichtphcm.data.repository;

import android.content.Context;

import com.example.nhom_15_dulichtphcm.data.db.SaigonTravelDatabaseHelper;
import com.example.nhom_15_dulichtphcm.data.local.AuthManager;
import com.example.nhom_15_dulichtphcm.data.model.Permission;
import com.example.nhom_15_dulichtphcm.data.model.Phrase;
import com.example.nhom_15_dulichtphcm.data.model.PhraseCategory;
import com.example.nhom_15_dulichtphcm.data.model.ResultBox;
import com.example.nhom_15_dulichtphcm.util.Localized;
import com.example.nhom_15_dulichtphcm.util.SearchUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * SQLite-backed repository for Survival Vietnamese Phrases and user-bookmarked/custom phrases.
 */
public final class PhraseRepository {

    private final SaigonTravelDatabaseHelper db;
    private final List<Phrase> fallbackPhrases;
    private final Set<String> fallbackSavedIds = new HashSet<>();

    public PhraseRepository(Context context) {
        this.db = context != null ? SaigonTravelDatabaseHelper.getInstance(context) : SaigonTravelDatabaseHelper.peekInstance();
        this.fallbackPhrases = new ArrayList<>(PhraseSeedData.createDefaultPhrases());
    }

    public static List<Phrase> getDefaultPhrases() {
        return Collections.unmodifiableList(PhraseSeedData.createDefaultPhrases());
    }

    public List<Phrase> getAllPhrases() {
        if (db != null) {
            return db.getAllPhrases(AuthManager.getActiveUserScope());
        }
        List<Phrase> out = new ArrayList<>(fallbackPhrases.size());
        for (Phrase p : fallbackPhrases) {
            out.add(p.withSaved(fallbackSavedIds.contains(p.id)));
        }
        return out;
    }

    public ResultBox<Boolean> toggleSaved(Phrase phrase) {
        ResultBox<Void> perm = AuthManager.requirePermission(Permission.SAVE_PERSONAL_COLLECTION);
        if (!perm.isSuccess()) {
            return ResultBox.failure(perm.getError());
        }
        if (phrase == null) {
            return ResultBox.failureMessage("Invalid phrase");
        }
        boolean nextSaved = !phrase.isSaved;
        if (db != null) {
            db.setPhraseSaved(AuthManager.getActiveUserScope(), phrase.id, nextSaved);
        } else {
            if (nextSaved) fallbackSavedIds.add(phrase.id);
            else fallbackSavedIds.remove(phrase.id);
        }
        return ResultBox.success(nextSaved);
    }

    public ResultBox<Phrase> addCustomPhrase(
            String english,
            String vietnamese,
            String pronunciation,
            String explanation,
            PhraseCategory category
    ) {
        ResultBox<Void> perm = AuthManager.requirePermission(Permission.MANAGE_OWN_PHRASES);
        if (!perm.isSuccess()) {
            return ResultBox.failure(perm.getError());
        }
        String en = english != null ? english.trim() : "";
        String vi = vietnamese != null ? vietnamese.trim() : "";
        if (en.isEmpty() || vi.isEmpty()) {
            return ResultBox.failureMessage(Localized.tr(
                    "Vui lòng nhập cả câu Tiếng Anh và Tiếng Việt",
                    "Please enter both English and Vietnamese text"));
        }
        Phrase custom = new Phrase(
                "custom_" + UUID.randomUUID().toString().substring(0, 8),
                en,
                vi,
                SearchUtils.normalize(vi),
                pronunciation != null && !pronunciation.trim().isEmpty() ? pronunciation.trim() : vi,
                explanation != null && !explanation.trim().isEmpty()
                        ? explanation.trim()
                        : Localized.tr("Câu giao tiếp cá nhân do bạn tự thêm.", "Custom phrase added to your personal phrasebook."),
                category != null ? category : PhraseCategory.GREETING,
                true,
                true
        );
        if (db != null) {
            db.insertCustomPhrase(custom);
            db.setPhraseSaved(AuthManager.getActiveUserScope(), custom.id, true);
        } else {
            fallbackPhrases.add(0, custom);
            fallbackSavedIds.add(custom.id);
        }
        return ResultBox.success(custom);
    }

    public ResultBox<Void> deleteCustomPhrase(String id) {
        ResultBox<Void> perm = AuthManager.requirePermission(Permission.MANAGE_OWN_PHRASES);
        if (!perm.isSuccess()) {
            return ResultBox.failure(perm.getError());
        }
        if (db != null) {
            db.deleteCustomPhrase(id);
        } else {
            fallbackPhrases.removeIf(p -> p.id.equals(id) && p.isCustom);
            fallbackSavedIds.remove(id);
        }
        return ResultBox.success(null);
    }
}
