package com.example.nhom_15_dulichtphcm.ui.screens;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom_15_dulichtphcm.R;
import com.example.nhom_15_dulichtphcm.data.model.FoodCategory;
import com.example.nhom_15_dulichtphcm.data.model.Restaurant;
import com.example.nhom_15_dulichtphcm.data.model.SpecialtyFood;
import com.example.nhom_15_dulichtphcm.data.repository.FoodRepository;
import com.example.nhom_15_dulichtphcm.ui.AppNavigator;
import com.example.nhom_15_dulichtphcm.ui.adapters.FoodAdapter;
import com.example.nhom_15_dulichtphcm.ui.components.ChipViewFactory;
import com.example.nhom_15_dulichtphcm.ui.components.CompactHeaderHelper;
import com.example.nhom_15_dulichtphcm.util.Localized;
import com.example.nhom_15_dulichtphcm.util.SearchUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure Java + XML Specialty Food screen backed by SQLite tables foods & restaurants (Old Style).
 */
public final class FoodListFragment extends Fragment {

    private FoodCategory selectedCategory = FoodCategory.ALL;
    private String selectedDistrict = "ALL";
    private String searchQuery = "";
    private View rootView;
    private LinearLayout categoryContainer;
    private LinearLayout districtContainer;
    private TextView tvAiTitle;
    private TextView tvAiSubtitle;
    private EditText etSearch;
    private FoodAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_food_list, container, false);
        categoryContainer = rootView.findViewById(R.id.container_food_categories);
        districtContainer = rootView.findViewById(R.id.container_food_districts);
        tvAiTitle = rootView.findViewById(R.id.tv_food_ai_title);
        tvAiSubtitle = rootView.findViewById(R.id.tv_food_ai_subtitle);
        etSearch = rootView.findViewById(R.id.et_search_foods);
        RecyclerView rv = rootView.findViewById(R.id.rv_foods);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new FoodAdapter(getNavigator(), this::refreshHeader);
        rv.setAdapter(adapter);

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

    private void refreshHeader() {
        if (rootView == null || !isAdded()) return;
        CompactHeaderHelper.bindMainHeader(
                rootView,
                Localized.tr("🍜 Bản đồ Ẩm thực Sài Gòn", "🍜 Saigon Culinary Trail"),
                Localized.tr("Đặc sản bản địa & quán ngon chính gốc", "Authentic dishes & iconic local eateries"),
                getNavigator()
        );
        if (tvAiTitle != null) {
            tvAiTitle.setText(Localized.tr("✨ Gợi ý Ẩm thực từ AI", "✨ AI Culinary Concierge"));
        }
        if (tvAiSubtitle != null) {
            tvAiSubtitle.setText(Localized.tr(
                    "Hỏi AI quán ngon bản địa, món ăn đêm & giá chuẩn",
                    "Ask AI for authentic local eateries & street food trails"
            ));
        }
        if (etSearch != null) {
            etSearch.setHint(Localized.tr(
                    "🍜 Tìm món đặc sản, nguyên liệu, quán ăn...",
                    "🍜 Search dishes, ingredients, eateries..."
            ));
        }
    }

    private void renderAll() {
        if (rootView == null || !isAdded()) return;
        refreshHeader();

        categoryContainer.removeAllViews();
        for (FoodCategory cat : FoodCategory.values()) {
            categoryContainer.addView(ChipViewFactory.createChip(
                    requireContext(),
                    Localized.tr(cat.labelVi, cat.label),
                    cat == selectedCategory,
                    0,
                    () -> {
                        selectedCategory = cat;
                        renderAll();
                    }
            ));
        }

        if (districtContainer != null) {
            districtContainer.removeAllViews();
            String[][] districts = new String[][]{
                    {"ALL", Localized.tr("📍 Tất cả khu vực", "📍 All Districts")},
                    {"Quận 1", Localized.tr("Quận 1", "District 1")},
                    {"Quận 3", Localized.tr("Quận 3", "District 3")},
                    {"Quận 4", Localized.tr("Quận 4", "District 4")},
                    {"Quận 5", Localized.tr("Quận 5", "District 5")},
                    {"Bình Dương", "Bình Dương"},
                    {"Vũng Tàu", "Vũng Tàu"}
            };
            for (String[] d : districts) {
                final String key = d[0];
                districtContainer.addView(ChipViewFactory.createTealChip(
                        requireContext(),
                        d[1],
                        key.equals(selectedDistrict),
                        () -> {
                            selectedDistrict = key;
                            renderAll();
                        }
                ));
            }
        }

        renderList();
    }

    private void renderList() {
        if (!isAdded()) return;
        List<SpecialtyFood> all = FoodRepository.getSpecialtyFoods();
        List<SpecialtyFood> filtered = new ArrayList<>();
        for (SpecialtyFood f : all) {
            if (selectedCategory != FoodCategory.ALL && f.category != selectedCategory) continue;
            if (!"ALL".equals(selectedDistrict)) {
                boolean matchDist = false;
                for (Restaurant r : f.restaurants) {
                    String combined = r.district + " " + r.address;
                    if (SearchUtils.fuzzyContains(combined, selectedDistrict)
                            || ("Quận 1".equals(selectedDistrict) && SearchUtils.fuzzyContains(combined, "District 1"))
                            || ("Quận 3".equals(selectedDistrict) && SearchUtils.fuzzyContains(combined, "District 3"))
                            || ("Quận 4".equals(selectedDistrict) && SearchUtils.fuzzyContains(combined, "District 4"))
                            || ("Quận 5".equals(selectedDistrict) && SearchUtils.fuzzyContains(combined, "District 5"))) {
                        matchDist = true;
                        break;
                    }
                }
                if (!matchDist) continue;
            }
            if (!searchQuery.trim().isEmpty()) {
                String haystack = f.nameVi + " " + f.nameEn + " " + f.shortDescription + " " + String.join(" ", f.ingredients);
                if (!SearchUtils.matchesAllTokens(haystack, searchQuery)) continue;
            }
            filtered.add(f);
        }
        adapter.submitList(filtered);
    }

    private AppNavigator getNavigator() {
        return getActivity() instanceof AppNavigator ? (AppNavigator) getActivity() : null;
    }
}
