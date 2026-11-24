package com.example.nhom_15_dulichtphcm.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom_15_dulichtphcm.R;
import com.example.nhom_15_dulichtphcm.data.local.FavoritesManager;
import com.example.nhom_15_dulichtphcm.data.local.TripPlanManager;
import com.example.nhom_15_dulichtphcm.data.model.Attraction;
import com.example.nhom_15_dulichtphcm.data.model.ResultBox;
import com.example.nhom_15_dulichtphcm.data.repository.RideRepository;
import com.example.nhom_15_dulichtphcm.ui.AppNavigator;
import com.example.nhom_15_dulichtphcm.util.ImageLoader;
import com.example.nhom_15_dulichtphcm.util.Localized;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Pure Java RecyclerView.Adapter binding Attraction rows from item_attraction.xml (Old Style).
 */
public final class AttractionAdapter extends RecyclerView.Adapter<AttractionAdapter.VH> {

    private final List<Attraction> items = new ArrayList<>();
    private final AppNavigator navigator;
    private final Runnable onStateChanged;
    private Double userLat = null;
    private Double userLng = null;

    public AttractionAdapter(AppNavigator navigator, Runnable onStateChanged) {
        this.navigator = navigator;
        this.onStateChanged = onStateChanged;
    }

    public void submitList(List<Attraction> newList, Double lat, Double lng) {
        this.items.clear();
        if (newList != null) this.items.addAll(newList);
        this.userLat = lat;
        this.userLng = lng;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attraction, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Attraction item = items.get(position);
        ImageLoader.loadAttraction(h.ivPhoto, item);

        String locationBadge = item.district != null && !item.district.isEmpty()
                ? "📍 " + item.district
                : "📍 " + item.province;
        h.tvProvince.setText(locationBadge);
        h.tvCategory.setText(Localized.tr(item.category.displayNameVi, item.category.displayName));
        h.tvName.setText(Localized.tr(item.nameVi, item.nameEn));
        h.tvSubtitle.setText(Localized.tr(item.nameEn, item.nameVi));
        h.tvSummary.setText(item.shortSummary);
        h.tvMeta.setText("🕒 " + item.openingHours + "  •  🎟️ " + item.ticketPrice);

        if (userLat != null && userLng != null) {
            double km = RideRepository.calculateDistanceKm(userLat, userLng, item.latitude, item.longitude);
            h.tvDistance.setVisibility(View.VISIBLE);
            h.tvDistance.setText(String.format(Locale.US, "📍 %.1f km", km));
        } else {
            h.tvDistance.setVisibility(View.GONE);
        }

        boolean fav = FavoritesManager.isAttractionFavorite(item.id);
        h.btnFav.setText(fav ? "❤️" : "🤍");
        h.btnFav.setOnClickListener(v -> {
            ResultBox<Boolean> res = FavoritesManager.toggleAttraction(item.id);
            if (!res.isSuccess()) {
                if (navigator != null) navigator.showPermissionRequiredDialog(res.getErrorMessage());
                return;
            }
            notifyItemChanged(h.getAdapterPosition());
            if (onStateChanged != null) onStateChanged.run();
        });

        boolean inTrip = TripPlanManager.contains(item.id);
        h.btnTrip.setText(inTrip ? "✅" : "📅");
        h.btnTrip.setOnClickListener(v -> {
            ResultBox<Boolean> res = TripPlanManager.toggle(item.id);
            if (!res.isSuccess()) {
                if (navigator != null) navigator.showPermissionRequiredDialog(res.getErrorMessage());
                return;
            }
            Toast.makeText(v.getContext(),
                    Boolean.TRUE.equals(res.getOrNull())
                            ? Localized.tr("Đã thêm vào Lịch trình!", "Added to Trip Itinerary!")
                            : Localized.tr("Đã gỡ khỏi Lịch trình", "Removed from Trip"),
                    Toast.LENGTH_SHORT).show();
            notifyItemChanged(h.getAdapterPosition());
            if (onStateChanged != null) onStateChanged.run();
        });

        h.itemView.setOnClickListener(v -> {
            if (navigator != null) navigator.openAttractionDetail(item.id);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class VH extends RecyclerView.ViewHolder {
        final ImageView ivPhoto;
        final TextView tvProvince;
        final TextView btnFav;
        final TextView tvCategory;
        final TextView tvDistance;
        final TextView tvName;
        final TextView tvSubtitle;
        final TextView tvSummary;
        final TextView tvMeta;
        final TextView btnTrip;

        VH(@NonNull View v) {
            super(v);
            ivPhoto = v.findViewById(R.id.iv_attraction_photo);
            tvProvince = v.findViewById(R.id.tv_attraction_province_badge);
            btnFav = v.findViewById(R.id.btn_item_favorite);
            tvCategory = v.findViewById(R.id.tv_attraction_category);
            tvDistance = v.findViewById(R.id.tv_attraction_distance);
            tvName = v.findViewById(R.id.tv_attraction_name);
            tvSubtitle = v.findViewById(R.id.tv_attraction_subtitle);
            tvSummary = v.findViewById(R.id.tv_attraction_summary);
            tvMeta = v.findViewById(R.id.tv_attraction_meta);
            btnTrip = v.findViewById(R.id.btn_item_add_trip);
        }
    }
}
