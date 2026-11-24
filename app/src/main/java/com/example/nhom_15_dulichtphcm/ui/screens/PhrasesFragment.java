package com.example.nhom_15_dulichtphcm.ui.screens;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom_15_dulichtphcm.R;
import com.example.nhom_15_dulichtphcm.data.local.AuthManager;
import com.example.nhom_15_dulichtphcm.data.model.Permission;
import com.example.nhom_15_dulichtphcm.data.model.Phrase;
import com.example.nhom_15_dulichtphcm.data.model.PhraseCategory;
import com.example.nhom_15_dulichtphcm.data.model.ResultBox;
import com.example.nhom_15_dulichtphcm.data.repository.PhraseRepository;
import com.example.nhom_15_dulichtphcm.ui.AppNavigator;
import com.example.nhom_15_dulichtphcm.ui.adapters.PhraseAdapter;
import com.example.nhom_15_dulichtphcm.ui.components.ChipViewFactory;
import com.example.nhom_15_dulichtphcm.ui.components.CompactHeaderHelper;
import com.example.nhom_15_dulichtphcm.util.Localized;
import com.example.nhom_15_dulichtphcm.util.SearchUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure Java + XML Survival Vietnamese Phrases screen backed by SQLite tables phrases & saved_phrases (Old Style).
 */
public final class PhrasesFragment extends Fragment {

    private PhraseCategory selectedCategory = PhraseCategory.ALL;
    private boolean savedOnly = false;
    private String searchQuery = "";
    private PhraseRepository repository;
    private View rootView;
    private LinearLayout categoryContainer;
    private EditText etSearch;
    private PhraseAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_phrases, container, false);
        repository = new PhraseRepository(requireContext());
        categoryContainer = rootView.findViewById(R.id.container_phrase_categories);
        etSearch = rootView.findViewById(R.id.et_search_phrases);
        RecyclerView rv = rootView.findViewById(R.id.rv_phrases);

        AppNavigator nav = getNavigator();
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PhraseAdapter(
                nav,
                phrase -> {
                    ResultBox<Boolean> res = repository.toggleSaved(phrase);
                    if (!res.isSuccess()) {
                        if (nav != null) nav.showPermissionRequiredDialog(res.getErrorMessage());
                        return;
                    }
                    renderList();
                },
                phrase -> {
                    repository.deleteCustomPhrase(phrase.id);
                    renderList();
                }
        );
        rv.setAdapter(adapter);

        rootView.findViewById(R.id.btn_add_custom_phrase).setOnClickListener(v -> showAddPhraseDialog());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s != null ? s.toString() : "";
                renderList();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        renderAll();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        renderAll();
    }

    private void showAddPhraseDialog() {
        AppNavigator nav = getNavigator();
        if (!AuthManager.hasPermission(Permission.MANAGE_OWN_PHRASES)) {
            if (nav != null) nav.showPermissionRequiredDialog(null);
            return;
        }
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_phrase, null);
        EditText etEn = dialogView.findViewById(R.id.et_custom_phrase_en);
        EditText etVi = dialogView.findViewById(R.id.et_custom_phrase_vi);
        EditText etPron = dialogView.findViewById(R.id.et_custom_phrase_pron);
        EditText etNote = dialogView.findViewById(R.id.et_custom_phrase_note);

        new AlertDialog.Builder(requireContext())
                .setTitle(Localized.tr("➕ Thêm câu giao tiếp cá nhân", "➕ Add Custom Phrase"))
                .setView(dialogView)
                .setPositiveButton(Localized.tr("Lưu", "Save"), (d, w) -> {
                    ResultBox<Phrase> res = repository.addCustomPhrase(
                            etEn.getText().toString(),
                            etVi.getText().toString(),
                            etPron.getText().toString(),
                            etNote.getText().toString(),
                            selectedCategory == PhraseCategory.ALL ? PhraseCategory.GREETING : selectedCategory
                    );
                    if (!res.isSuccess()) {
                        Toast.makeText(requireContext(), res.getErrorMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    renderList();
                })
                .setNegativeButton(Localized.tr("Hủy", "Cancel"), null)
                .show();
    }

    private void renderAll() {
        if (rootView == null || !isAdded()) return;
        CompactHeaderHelper.bindMainHeader(
                rootView,
                Localized.tr("Sổ tay Giao tiếp Tiếng Việt", "Vietnamese Phrasebook"),
                Localized.tr("Phát âm chuẩn • Đọc chậm • Thẻ đưa người bản địa", "Essential local phrases with voice TTS"),
                getNavigator()
        );
        if (etSearch != null) {
            etSearch.setHint(Localized.tr(
                    "🔍 Tìm câu giao tiếp Tiếng Việt hoặc Tiếng Anh...",
                    "🔍 Search English or Vietnamese phrases..."
            ));
        }

        categoryContainer.removeAllViews();
        categoryContainer.addView(ChipViewFactory.createChip(
                requireContext(),
                Localized.tr("❤️ Yêu thích", "❤️ Favorites"),
                savedOnly,
                0,
                () -> {
                    savedOnly = !savedOnly;
                    renderAll();
                }
        ));

        for (PhraseCategory cat : PhraseCategory.values()) {
            categoryContainer.addView(ChipViewFactory.createChip(
                    requireContext(),
                    cat.emoji + " " + Localized.tr(cat.displayNameVi, cat.displayName),
                    !savedOnly && cat == selectedCategory,
                    0,
                    () -> {
                        savedOnly = false;
                        selectedCategory = cat;
                        renderAll();
                    }
            ));
        }
        renderList();
    }

    private void renderList() {
        if (!isAdded()) return;
        List<Phrase> all = repository.getAllPhrases();
        List<Phrase> filtered = new ArrayList<>();
        for (Phrase p : all) {
            if (savedOnly && !p.isSaved) continue;
            if (!savedOnly && selectedCategory != PhraseCategory.ALL && p.category != selectedCategory) continue;
            if (!searchQuery.trim().isEmpty()) {
                String haystack = p.vietnamese + " " + p.vietnameseUnaccented + " " + p.english + " " + p.explanation;
                if (!SearchUtils.matchesAllTokens(haystack, searchQuery)) continue;
            }
            filtered.add(p);
        }
        adapter.submitList(filtered);
    }

    private AppNavigator getNavigator() {
        return getActivity() instanceof AppNavigator ? (AppNavigator) getActivity() : null;
    }
}
