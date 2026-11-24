package com.example.nhom_15_dulichtphcm.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom_15_dulichtphcm.R;
import com.example.nhom_15_dulichtphcm.data.local.FavoritesManager;
import com.example.nhom_15_dulichtphcm.data.model.Restaurant;
import com.example.nhom_15_dulichtphcm.data.model.ResultBox;
import com.example.nhom_15_dulichtphcm.data.model.SpecialtyFood;
import com.example.nhom_15_dulichtphcm.ui.AppNavigator;
import com.example.nhom_15_dulichtphcm.util.ImageLoader;
import com.example.nhom_15_dulichtphcm.util.Localized;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Pure Java RecyclerView.Adapter binding SpecialtyFood rows from item_food.xml (Old Style).
 */
public final class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.VH> {

    private final List<SpecialtyFood> items = new ArrayList<>();
    private final AppNavigator navigator;
    private final Runnable onStateChanged;

    public FoodAdapter(AppNavigator navigator, Runnable onStateChanged) {
        this.navigator = navigator;
        this.onStateChanged = onStateChanged;
    }

    public void submitList(List<SpecialtyFood> newList) {
        this.items.clear();
        if (newList != null) this.items.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        SpecialtyFood item = items.get(position);
        ImageLoader.loadFood(h.ivPhoto, item);

        h.tvCategory.setText(Localized.tr(item.category.labelVi, item.category.label));
        h.tvName.setText(Localized.tr(item.nameVi, item.nameEn));
        h.tvSubtitle.setText(Localized.tr(item.nameEn, item.nameVi));
        h.tvDesc.setText(item.shortDescription);

        // Compute top rating & heritage highlight from restaurants
        double bestRating = 4.8;
        String heritageLabel = "";
        String samplePrice = "";
        for (Restaurant r : item.restaurants) {
            if (r.rating > bestRating) bestRating = r.rating;
            if (heritageLabel.isEmpty() && r.highlight != null && !r.highlight.isEmpty()) {
                heritageLabel = r.highlight;
            }
            if (samplePrice.isEmpty() && r.priceRange != null && !r.priceRange.isEmpty()) {
                samplePrice = r.priceRange;
            }
        }
        h.tvRating.setText(String.format(Locale.US, "⭐ %.1f", bestRating));
        if (!heritageLabel.isEmpty()) {
            h.tvHeritage.setVisibility(View.VISIBLE);
            h.tvHeritage.setText("🏆 " + heritageLabel);
        } else {
            h.tvHeritage.setVisibility(View.VISIBLE);
            h.tvHeritage.setText(Localized.tr("🏆 Đặc sản trứ danh", "🏆 Iconic Local Dish"));
        }

        // Populate Lavender Ingredient Chips
        h.ingredientsContainer.removeAllViews();
        float d = h.itemView.getResources().getDisplayMetrics().density;
        int maxIng = Math.min(4, item.ingredients.size());
        for (int i = 0; i < maxIng; i++) {
            TextView chip = new TextView(h.itemView.getContext());
            chip.setText(item.ingredients.get(i));
            chip.setTextSize(11f);
            chip.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.text_primary));
            chip.setBackgroundResource(R.drawable.bg_lavender_pill);
            int padH = (int) (8 * d);
            int padV = (int) (3.5f * d);
            chip.setPadding(padH, padV, padH, padV);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            lp.setMarginEnd((int) (6 * d));
            chip.setLayoutParams(lp);
            h.ingredientsContainer.addView(chip);
        }

        int restCount = item.restaurants.size();
        String priceSuffix = samplePrice.isEmpty() ? "" : " • " + samplePrice;
        h.tvRestaurants.setText(Localized.tr(
                "🏪 " + restCount + " Quán ngon bản địa" + priceSuffix,
                "🏪 " + restCount + " Famous Eateries on Map" + priceSuffix));
        h.tvViewEateries.setText(Localized.tr("Xem quán →", "View eateries →"));

        boolean fav = FavoritesManager.isFoodFavorite(item.id);
        h.btnFav.setText(fav ? "❤️" : "🤍");
        h.btnFav.setOnClickListener(v -> {
            ResultBox<Boolean> res = FavoritesManager.toggleFood(item.id);
            if (!res.isSuccess()) {
                if (navigator != null) navigator.showPermissionRequiredDialog(res.getErrorMessage());
                return;
            }
            notifyItemChanged(h.getAdapterPosition());
            if (onStateChanged != null) onStateChanged.run();
        });

        h.itemView.setOnClickListener(v -> {
            if (navigator != null) navigator.openFoodDetail(item.id);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class VH extends RecyclerView.ViewHolder {
        final ImageView ivPhoto;
        final TextView tvCategory;
        final TextView tvRating;
        final TextView tvHeritage;
        final TextView btnFav;
        final TextView tvName;
        final TextView tvSubtitle;
        final TextView tvDesc;
        final LinearLayout ingredientsContainer;
        final TextView tvRestaurants;
        final TextView tvViewEateries;

        VH(@NonNull View v) {
            super(v);
            ivPhoto = v.findViewById(R.id.iv_food_photo);
            tvCategory = v.findViewById(R.id.tv_food_category_badge);
            tvRating = v.findViewById(R.id.tv_food_rating_badge);
            tvHeritage = v.findViewById(R.id.tv_food_heritage_badge);
            btnFav = v.findViewById(R.id.btn_food_favorite);
            tvName = v.findViewById(R.id.tv_food_name);
            tvSubtitle = v.findViewById(R.id.tv_food_subtitle);
            tvDesc = v.findViewById(R.id.tv_food_desc);
            ingredientsContainer = v.findViewById(R.id.container_food_ingredients);
            tvRestaurants = v.findViewById(R.id.tv_food_restaurants_count);
            tvViewEateries = v.findViewById(R.id.tv_food_view_eateries);
        }
    }
}
