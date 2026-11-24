package com.example.nhom_15_dulichtphcm.ui.screens;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom_15_dulichtphcm.R;
import com.example.nhom_15_dulichtphcm.data.local.FavoritesManager;
import com.example.nhom_15_dulichtphcm.data.model.Attraction;
import com.example.nhom_15_dulichtphcm.data.model.SpecialtyFood;
import com.example.nhom_15_dulichtphcm.data.repository.AttractionRepository;
import com.example.nhom_15_dulichtphcm.data.repository.FoodRepository;
import com.example.nhom_15_dulichtphcm.ui.AppNavigator;
import com.example.nhom_15_dulichtphcm.ui.adapters.AttractionAdapter;
import com.example.nhom_15_dulichtphcm.ui.adapters.FoodAdapter;
import com.example.nhom_15_dulichtphcm.ui.components.CompactHeaderHelper;
import com.example.nhom_15_dulichtphcm.util.Localized;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Pure Java + XML Saved Favorites screen backed by SQLite table favorites (Old Style).
 */
public final class FavoritesFragment extends Fragment {

    private boolean showingAttractions = true;
    private View rootView;
    private TextView tabAttractions;
    private TextView tabFoods;
    private View indAttractions;
    private View indFoods;
    private TextView tvTipBanner;
    private RecyclerView rv;
    private TextView tvEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_favorites, container, false);
        tabAttractions = rootView.findViewById(R.id.tab_fav_attractions);
        tabFoods = rootView.findViewById(R.id.tab_fav_foods);
        indAttractions = rootView.findViewById(R.id.ind_fav_attractions);
        indFoods = rootView.findViewById(R.id.ind_fav_foods);
        tvTipBanner = rootView.findViewById(R.id.card_fav_tip_banner);
        rv = rootView.findViewById(R.id.rv_favorites);
        tvEmpty = rootView.findViewById(R.id.tv_empty_favorites);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        View wrapAttr = rootView.findViewById(R.id.wrap_tab_fav_attractions);
        View wrapFood = rootView.findViewById(R.id.wrap_tab_fav_foods);
        View.OnClickListener selectAttr = v -> {
            showingAttractions = true;
            renderContent();
        };
        View.OnClickListener selectFood = v -> {
            showingAttractions = false;
            renderContent();
        };
        if (wrapAttr != null) wrapAttr.setOnClickListener(selectAttr);
        tabAttractions.setOnClickListener(selectAttr);
        if (wrapFood != null) wrapFood.setOnClickListener(selectFood);
        tabFoods.setOnClickListener(selectFood);

        if (tvTipBanner != null) {
            tvTipBanner.setOnClickListener(v -> {
                AppNavigator nav = getNavigator();
                if (nav != null) nav.openTripPlanner();
            });
        }

        CompactHeaderHelper.bindSubscreenToolbar(
                rootView,
                Localized.tr("Bộ sưu tập Đã lưu ❤️", "My Saved Collection ❤️"),
                getNavigator(),
                null,
                null
        );

        renderContent();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        renderContent();
    }

    private void renderContent() {
        if (!isAdded() || rootView == null) return;
        Set<String> attrIds = FavoritesManager.getAttractionIds();
        Set<String> foodIds = FavoritesManager.getFoodIds();

        tabAttractions.setText(Localized.tr("📍 Địa điểm (" + attrIds.size() + ")", "📍 Places (" + attrIds.size() + ")"));
        tabFoods.setText(Localized.tr("🍜 Món ngon (" + foodIds.size() + ")", "🍜 Dishes (" + foodIds.size() + ")"));
        if (tvTipBanner != null) {
            tvTipBanner.setText(Localized.tr(
                    "💡 Chạm 📅 để thêm địa điểm vào Lịch trình — tự động tối ưu lộ trình →",
                    "💡 Tap 📅 to add places to My Trip — auto-optimize route →"
            ));
        }

        int activeRed = requireContext().getColor(R.color.saigon_red);
        int inactiveGray = requireContext().getColor(R.color.text_secondary);
        tabAttractions.setTextColor(showingAttractions ? activeRed : inactiveGray);
        tabFoods.setTextColor(!showingAttractions ? activeRed : inactiveGray);
        if (indAttractions != null) indAttractions.setBackgroundColor(showingAttractions ? activeRed : Color.TRANSPARENT);
        if (indFoods != null) indFoods.setBackgroundColor(!showingAttractions ? activeRed : Color.TRANSPARENT);

        if (showingAttractions) {
            List<Attraction> favList = new ArrayList<>();
            for (Attraction a : AttractionRepository.getAttractions()) {
                if (attrIds.contains(a.id)) favList.add(a);
            }
            AttractionAdapter attrAdapter = new AttractionAdapter(getNavigator(), this::renderContent);
            rv.setAdapter(attrAdapter);
            attrAdapter.submitList(favList, null, null);
            tvEmpty.setVisibility(favList.isEmpty() ? View.VISIBLE : View.GONE);
        } else {
            List<SpecialtyFood> favFoods = new ArrayList<>();
            for (SpecialtyFood f : FoodRepository.getSpecialtyFoods()) {
                if (foodIds.contains(f.id)) favFoods.add(f);
            }
            FoodAdapter foodAdapter = new FoodAdapter(getNavigator(), this::renderContent);
            rv.setAdapter(foodAdapter);
            foodAdapter.submitList(favFoods);
            tvEmpty.setVisibility(favFoods.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private AppNavigator getNavigator() {
        return getActivity() instanceof AppNavigator ? (AppNavigator) getActivity() : null;
    }
}
