package com.example.nhom_15_dulichtphcm.ui.screens;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom_15_dulichtphcm.R;
import com.example.nhom_15_dulichtphcm.data.model.PublicUtility;
import com.example.nhom_15_dulichtphcm.data.model.UtilityCategory;
import com.example.nhom_15_dulichtphcm.data.repository.UtilityRepository;
import com.example.nhom_15_dulichtphcm.ui.AppNavigator;
import com.example.nhom_15_dulichtphcm.ui.adapters.UtilityAdapter;
import com.example.nhom_15_dulichtphcm.ui.components.ChipViewFactory;
import com.example.nhom_15_dulichtphcm.ui.components.CompactHeaderHelper;
import com.example.nhom_15_dulichtphcm.util.AppStrings;
import com.example.nhom_15_dulichtphcm.util.Localized;
import com.example.nhom_15_dulichtphcm.util.RideLauncher;
import com.example.nhom_15_dulichtphcm.util.SearchUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure Java + XML Public Utilities screen backed by SQLite table utilities (Old Style).
 */
public final class PublicUtilitiesFragment extends Fragment {

    private UtilityCategory selectedCategory = UtilityCategory.ALL;
    private String searchQuery = "";
    private View rootView;
    private LinearLayout categoryContainer;
    private EditText etSearch;
    private UtilityAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_public_utilities, container, false);
        categoryContainer = rootView.findViewById(R.id.container_utility_categories);
        etSearch = rootView.findViewById(R.id.et_search_utilities);
        RecyclerView rv = rootView.findViewById(R.id.rv_utilities);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new UtilityAdapter(getNavigator());
        rv.setAdapter(adapter);

        rootView.findViewById(R.id.btn_sos_113).setOnClickListener(v -> RideLauncher.callHotline(requireContext(), "113"));
        rootView.findViewById(R.id.btn_sos_115).setOnClickListener(v -> RideLauncher.callHotline(requireContext(), "115"));
        rootView.findViewById(R.id.btn_sos_1087).setOnClickListener(v -> RideLauncher.callHotline(requireContext(), "0281087"));
        rootView.findViewById(R.id.btn_util_metro_guide).setOnClickListener(v -> {
            AppNavigator nav = getNavigator();
            if (nav != null) nav.openMetroGuide();
        });
        rootView.findViewById(R.id.btn_util_safety_hub).setOnClickListener(v -> {
            AppNavigator nav = getNavigator();
            if (nav != null) nav.openSafetyHub();
        });

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

    private void renderAll() {
        if (rootView == null || !isAdded()) return;
        CompactHeaderHelper.bindMainHeader(
                rootView,
                AppStrings.get("utilities_title"),
                Localized.tr("Metro số 1, bệnh viện quốc tế, lãnh sự quán & ATM", "Metro Line 1, restrooms, hospitals & embassies"),
                getNavigator()
        );
        if (etSearch != null) {
            etSearch.setHint(Localized.tr(
                    "🔍 Tìm nhà vệ sinh, ga Metro, ATM, bệnh viện...",
                    "🔍 Search restrooms, ATMs, Metro, hospitals..."
            ));
        }

        categoryContainer.removeAllViews();
        for (UtilityCategory cat : UtilityCategory.values()) {
            categoryContainer.addView(ChipViewFactory.createChip(
                    requireContext(),
                    cat.iconEmoji + " " + Localized.tr(cat.labelVi, cat.labelEn),
                    cat == selectedCategory,
                    0,
                    () -> {
                        selectedCategory = cat;
                        renderAll();
                    }
            ));
        }
        renderList();
    }

    private void renderList() {
        if (!isAdded()) return;
        List<PublicUtility> all = UtilityRepository.getUtilities();
        List<PublicUtility> filtered = new ArrayList<>();
        for (PublicUtility u : all) {
            if (selectedCategory != UtilityCategory.ALL && u.category != selectedCategory) continue;
            if (!searchQuery.trim().isEmpty()) {
                String haystack = u.nameVi + " " + u.nameEn + " " + u.district + " " + u.address + " " + u.descriptionVi;
                if (!SearchUtils.matchesAllTokens(haystack, searchQuery)) continue;
            }
            filtered.add(u);
        }
        adapter.submitList(filtered);
    }

    private AppNavigator getNavigator() {
        return getActivity() instanceof AppNavigator ? (AppNavigator) getActivity() : null;
    }
}
