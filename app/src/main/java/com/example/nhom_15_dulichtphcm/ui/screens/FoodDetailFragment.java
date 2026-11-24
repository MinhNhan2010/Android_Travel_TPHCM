package com.example.nhom_15_dulichtphcm.ui.screens;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.nhom_15_dulichtphcm.R;
import com.example.nhom_15_dulichtphcm.data.local.FavoritesManager;
import com.example.nhom_15_dulichtphcm.data.model.Restaurant;
import com.example.nhom_15_dulichtphcm.data.model.ResultBox;
import com.example.nhom_15_dulichtphcm.data.model.SpecialtyFood;
import com.example.nhom_15_dulichtphcm.data.repository.FoodRepository;
import com.example.nhom_15_dulichtphcm.ui.AppNavigator;
import com.example.nhom_15_dulichtphcm.util.ImageLoader;
import com.example.nhom_15_dulichtphcm.util.Localized;
import com.example.nhom_15_dulichtphcm.util.MapLauncher;
import com.example.nhom_15_dulichtphcm.util.SpeechHelper;

import java.util.Locale;

/**
 * Pure Java + XML detail screen for SpecialtyFood and its curated Restaurant list.
 */
public final class FoodDetailFragment extends Fragment {

    private static final String ARG_ID = "food_id";

    public static FoodDetailFragment newInstance(String foodId) {
        FoodDetailFragment f = new FoodDetailFragment();
        Bundle b = new Bundle();
        b.putString(ARG_ID, foodId);
        f.setArguments(b);
        return f;
    }

    private SpecialtyFood food;
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_food_detail, container, false);
        String id = getArguments() != null ? getArguments().getString(ARG_ID) : null;
        food = FoodRepository.getFoodById(id);
        if (food == null && !FoodRepository.getSpecialtyFoods().isEmpty()) {
            food = FoodRepository.getSpecialtyFoods().get(0);
        }
        if (food != null) {
            bindContent();
        }
        return rootView;
    }

    private void bindContent() {
        AppNavigator nav = getNavigator();
        boolean fav = FavoritesManager.isFoodFavorite(food.id);

        TextView tvToolbarTitle = rootView.findViewById(R.id.tv_toolbar_title);
        TextView tvToolbarSub = rootView.findViewById(R.id.tv_food_toolbar_sub);
        TextView btnFavTop = rootView.findViewById(R.id.btn_toolbar_action);
        TextView btnShare = rootView.findViewById(R.id.btn_food_share);

        tvToolbarTitle.setText(Localized.tr(food.nameVi, food.nameEn));
        tvToolbarSub.setText(Localized.tr(food.category.labelVi, food.category.label));
        btnFavTop.setText(fav ? "❤️" : "🤍");

        rootView.findViewById(R.id.btn_toolbar_back).setOnClickListener(v -> {
            if (nav != null) nav.navigateBack();
        });

        btnFavTop.setOnClickListener(v -> {
            ResultBox<Boolean> res = FavoritesManager.toggleFood(food.id);
            if (!res.isSuccess()) {
                if (nav != null) nav.showPermissionRequiredDialog(res.getErrorMessage());
                return;
            }
            bindContent();
        });

        btnShare.setOnClickListener(v -> MapLauncher.shareText(
                requireContext(),
                food.nameVi,
                food.nameVi + " (" + food.nameEn + ")\n" + food.shortDescription));

        ImageView ivHero = rootView.findViewById(R.id.iv_food_detail_hero);
        ImageLoader.loadFood(ivHero, food);

        TextView tvHeroBadge = rootView.findViewById(R.id.tv_food_hero_badge);
        TextView tvName = rootView.findViewById(R.id.tv_food_detail_name);
        TextView tvSub = rootView.findViewById(R.id.tv_food_detail_sub);
        TextView tvStory = rootView.findViewById(R.id.tv_food_story);
        TextView tvHow = rootView.findViewById(R.id.tv_food_how_to_eat);
        TextView btnPronounce = rootView.findViewById(R.id.btn_food_pronounce);
        LinearLayout containerIng = rootView.findViewById(R.id.container_food_ingredients);
        LinearLayout containerRest = rootView.findViewById(R.id.container_food_restaurants);

        tvHeroBadge.setText(food.nameVi);
        tvName.setText(food.nameEn + " (" + food.nameVi + ")");
        tvSub.setText(food.shortDescription);
        tvStory.setText(food.storyAndHistory);
        tvHow.setText(food.howToEatLikeALocal);

        View.OnClickListener openPhotos = v -> MapLauncher.openPhotoSearch(requireContext(), food.nameVi);
        rootView.findViewById(R.id.btn_food_google_photos).setOnClickListener(openPhotos);
        rootView.findViewById(R.id.btn_food_google_gallery).setOnClickListener(openPhotos);

        btnPronounce.setOnClickListener(v -> {
            SpeechHelper sh = nav != null ? nav.getSpeechHelper() : null;
            if (sh != null) {
                sh.speakVietnamese("Cho tôi một phần " + food.nameVi);
            }
        });

        TextView btnFoodAudio = rootView.findViewById(R.id.btn_food_audio_guide);
        TextView btnFoodAllergy = rootView.findViewById(R.id.btn_food_allergy_card);
        btnFoodAudio.setText(Localized.tr("🎧 Thuyết minh Ẩm thực", "🎧 Culinary Audio Guide"));
        btnFoodAllergy.setText(Localized.tr("⚠️ Thẻ Dị ứng Bếp", "⚠️ Allergy Chef Card"));

        btnFoodAudio.setOnClickListener(v -> {
            SpeechHelper sh = nav != null ? nav.getSpeechHelper() : null;
            if (sh == null) return;
            if (sh.isSpeaking()) {
                sh.stopSpeaking();
                btnFoodAudio.setText(Localized.tr("🎧 Thuyết minh Ẩm thực", "🎧 Culinary Audio Guide"));
            } else {
                String viScript = food.nameVi + ". " + food.shortDescription + " Câu chuyện ẩm thực: "
                        + food.storyAndHistory + " Cách thưởng thức đúng điệu: " + food.howToEatLikeALocal;
                String enScript = food.nameEn + ". " + food.shortDescription + " " + food.storyAndHistory
                        + " How to enjoy: " + food.howToEatLikeALocal;
                sh.speakGuide(viScript, enScript, 1.0f);
                btnFoodAudio.setText(Localized.tr("⏹️ Dừng thuyết minh", "⏹️ Stop Audio"));
            }
        });

        btnFoodAllergy.setOnClickListener(v -> {
            if (nav != null) nav.openSafetyHub();
        });

        containerIng.removeAllViews();
        float d = getResources().getDisplayMetrics().density;
        for (String ing : food.ingredients) {
            TextView pill = new TextView(requireContext());
            pill.setText(ing);
            pill.setBackgroundResource(R.drawable.bg_lavender_pill);
            pill.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            pill.setTextSize(12.5f);
            int ph = (int) (12 * d);
            int pv = (int) (7 * d);
            pill.setPadding(ph, pv, ph, pv);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = (int) (6 * d);
            pill.setLayoutParams(lp);
            containerIng.addView(pill);
        }

        containerRest.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (Restaurant r : food.restaurants) {
            View row = inflater.inflate(R.layout.item_restaurant, containerRest, false);
            TextView rName = row.findViewById(R.id.tv_rest_name);
            TextView rRating = row.findViewById(R.id.tv_rest_rating);
            TextView rAddr = row.findViewById(R.id.tv_rest_address);
            TextView rMeta = row.findViewById(R.id.tv_rest_meta);
            TextView rHigh = row.findViewById(R.id.tv_rest_highlight);
            TextView btnMap = row.findViewById(R.id.btn_rest_map);
            TextView btnRide = row.findViewById(R.id.btn_rest_ride);

            rName.setText(r.name);
            rRating.setText(String.format(Locale.US, "★ %.1f", r.rating));
            rAddr.setText("📍 " + r.address + " (" + r.district + ")");
            rMeta.setText("🕒 " + r.openingHours + "  •  💰 " + r.priceRange);
            rHigh.setText("✨ " + r.highlight);

            btnMap.setOnClickListener(v -> {
                if (nav != null) nav.openMapAt(r.latitude, r.longitude, r.name);
            });
            btnRide.setOnClickListener(v -> {
                if (nav != null) nav.openRideBooking(r.name, r.latitude, r.longitude);
            });
            containerRest.addView(row);
        }
    }

    @Override
    public void onDestroyView() {
        AppNavigator nav = getNavigator();
        if (nav != null && nav.getSpeechHelper() != null) {
            nav.getSpeechHelper().stopSpeaking();
        }
        super.onDestroyView();
    }

    private AppNavigator getNavigator() {
        return getActivity() instanceof AppNavigator ? (AppNavigator) getActivity() : null;
    }
}
