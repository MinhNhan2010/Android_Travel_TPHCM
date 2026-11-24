package com.example.nhom_15_dulichtphcm.ui.screens;

import android.Manifest;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom_15_dulichtphcm.R;
import com.example.nhom_15_dulichtphcm.data.model.Attraction;
import com.example.nhom_15_dulichtphcm.data.model.AttractionCategory;
import com.example.nhom_15_dulichtphcm.data.repository.AttractionRepository;
import com.example.nhom_15_dulichtphcm.data.repository.RideRepository;
import com.example.nhom_15_dulichtphcm.ui.AppNavigator;
import com.example.nhom_15_dulichtphcm.ui.adapters.AttractionAdapter;
import com.example.nhom_15_dulichtphcm.ui.components.AppIcons2D;
import com.example.nhom_15_dulichtphcm.ui.components.ChipViewFactory;
import com.example.nhom_15_dulichtphcm.ui.components.CompactHeaderHelper;
import com.example.nhom_15_dulichtphcm.util.AppStrings;
import com.example.nhom_15_dulichtphcm.util.Localized;
import com.example.nhom_15_dulichtphcm.util.LocationHelper;
import com.example.nhom_15_dulichtphcm.util.SearchUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Pure Java + XML screen displaying attractions across Greater Ho Chi Minh City from SQLite.
 */
public final class AttractionListFragment extends Fragment {

    private String selectedRegion = "ALL";
    private AttractionCategory selectedCategory = AttractionCategory.ALL;
    private String searchQuery = "";
    private boolean sortByNearMe = false;
    private Double gpsLat = null;
    private Double gpsLng = null;

    private View rootView;
    private LinearLayout regionContainer;
    private LinearLayout categoryContainer;
    private TextView tvCount;
    private TextView tvEmpty;
    private TextView btnNearMe;
    private TextView tvAiBannerTitle;
    private TextView tvAiBannerSubtitle;
    private EditText etSearch;
    private AttractionAdapter adapter;

    private final ActivityResultLauncher<String[]> locationPermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                fetchGpsAndSort();
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_attraction_list, container, false);
        AppNavigator nav = getNavigator();

        regionContainer = rootView.findViewById(R.id.container_region_chips);
        categoryContainer = rootView.findViewById(R.id.container_category_chips);
        tvCount = rootView.findViewById(R.id.tv_attractions_count);
        tvEmpty = rootView.findViewById(R.id.tv_empty_attractions);
        btnNearMe = rootView.findViewById(R.id.btn_near_me);
        tvAiBannerTitle = rootView.findViewById(R.id.tv_ai_banner_title);
        tvAiBannerSubtitle = rootView.findViewById(R.id.tv_ai_banner_subtitle);
        etSearch = rootView.findViewById(R.id.et_search_attractions);
        RecyclerView rv = rootView.findViewById(R.id.rv_attractions);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AttractionAdapter(nav, this::refreshHeaderOnly);
        rv.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s != null ? s.toString() : "";
                renderList();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnNearMe.setOnClickListener(v -> {
            if (sortByNearMe) {
                sortByNearMe = false;
                gpsLat = null;
                gpsLng = null;
                renderAll();
            } else {
                if (LocationHelper.hasPermission(requireContext())) {
                    fetchGpsAndSort();
                } else {
                    locationPermLauncher.launch(new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    });
                }
            }
        });

        renderAll();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        renderAll();
    }

    private void fetchGpsAndSort() {
        if (!isAdded()) return;
        LocationHelper.fetchCurrentLocation(requireContext(), loc -> {
            if (!isAdded()) return;
            if (loc != null && LocationHelper.isNearHcmc(loc.getLatitude(), loc.getLongitude())) {
                gpsLat = loc.getLatitude();
                gpsLng = loc.getLongitude();
            } else {
                gpsLat = LocationHelper.DEMO_LAT;
                gpsLng = LocationHelper.DEMO_LNG;
                Toast.makeText(requireContext(),
                        Localized.tr("Đang tính khoảng cách từ trung tâm Chợ Bến Thành (Q1)", "Sorting from Ben Thanh Market center"),
                        Toast.LENGTH_SHORT).show();
            }
            sortByNearMe = true;
            renderAll();
        });
    }

    private void refreshHeaderOnly() {
        if (rootView == null || !isAdded()) return;
        CompactHeaderHelper.bindMainHeader(
                rootView,
                AppStrings.get("app_title"),
                AppStrings.get("app_subtitle"),
                getNavigator()
        );
        if (tvAiBannerTitle != null) {
            tvAiBannerTitle.setText(Localized.tr("🤖 Trợ lý Du lịch AI ✨", "🤖 AI Travel Guide ✨"));
        }
        if (tvAiBannerSubtitle != null) {
            tvAiBannerSubtitle.setText(Localized.tr(
                    "Cần lịch trình hay gợi ý quán ngon? Hỏi Cẩm nang AI ngay ✨",
                    "Need itineraries or food tips? Ask AI Travel Guide ✨"
            ));
        }
        if (etSearch != null) {
            etSearch.setHint(Localized.tr(
                    "🔍 Tìm địa điểm, quận, lịch sử...",
                    "🔍 Search landmarks, districts, history..."
            ));
        }
    }

    private void renderAll() {
        if (rootView == null || !isAdded()) return;
        refreshHeaderOnly();
        btnNearMe.setBackgroundResource(sortByNearMe ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        btnNearMe.setTextColor(requireContext().getColor(sortByNearMe ? R.color.white : R.color.text_primary));
        btnNearMe.setTypeface(null, sortByNearMe ? Typeface.BOLD : Typeface.NORMAL);
        btnNearMe.setText(Localized.tr("📍 Gần tôi", "📍 Near me"));

        // Region chips
        regionContainer.removeAllViews();
        String[][] regions = new String[][]{
                {"ALL", Localized.tr("🌐 Tất cả vùng", "🌐 All Regions")},
                {"HCM", Localized.tr("🏙️ TP. Hồ Chí Minh", "🏙️ Ho Chi Minh City")},
                {"BD", "🌳 Bình Dương"},
                {"VT", "🌊 Vũng Tàu"}
        };
        for (String[] r : regions) {
            final String code = r[0];
            regionContainer.addView(ChipViewFactory.createChip(
                    requireContext(),
                    r[1],
                    code.equals(selectedRegion),
                    AppIcons2D.regionIconRes(code),
                    () -> {
                        selectedRegion = code;
                        renderAll();
                    }
            ));
        }

        // Category chips
        categoryContainer.removeAllViews();
        for (AttractionCategory cat : AttractionCategory.values()) {
            categoryContainer.addView(ChipViewFactory.createChip(
                    requireContext(),
                    Localized.tr(cat.displayNameVi, cat.displayName),
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
        List<Attraction> all = AttractionRepository.getAttractions();
        List<Attraction> filtered = new ArrayList<>();

        for (Attraction a : all) {
            if (!"ALL".equals(selectedRegion)) {
                if ("HCM".equals(selectedRegion) && !SearchUtils.fuzzyContains(a.province, "Hồ Chí Minh") && !SearchUtils.fuzzyContains(a.province, "TP.HCM")) {
                    continue;
                }
                if ("BD".equals(selectedRegion) && !SearchUtils.fuzzyContains(a.province, "Bình Dương")) {
                    continue;
                }
                if ("VT".equals(selectedRegion) && !SearchUtils.fuzzyContains(a.province, "Vũng Tàu")) {
                    continue;
                }
            }
            if (selectedCategory != AttractionCategory.ALL && a.category != selectedCategory) {
                continue;
            }
            if (!searchQuery.trim().isEmpty()) {
                String haystack = a.nameVi + " " + a.nameEn + " " + a.district + " " + a.province + " " + a.shortSummary;
                if (!SearchUtils.matchesAllTokens(haystack, searchQuery)) {
                    continue;
                }
            }
            filtered.add(a);
        }

        if (sortByNearMe && gpsLat != null && gpsLng != null) {
            filtered.sort(Comparator.comparingDouble(a ->
                    RideRepository.calculateDistanceKm(gpsLat, gpsLng, a.latitude, a.longitude)));
        }

        tvCount.setText(Localized.tr(
                "📍 " + filtered.size() + " điểm tham quan nổi bật",
                "📍 " + filtered.size() + " featured landmarks"));
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        adapter.submitList(filtered, gpsLat, gpsLng);
    }

    private AppNavigator getNavigator() {
        return getActivity() instanceof AppNavigator ? (AppNavigator) getActivity() : null;
    }
}
