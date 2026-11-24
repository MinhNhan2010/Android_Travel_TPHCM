package com.example.nhom_15_dulichtphcm.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom_15_dulichtphcm.R;
import com.example.nhom_15_dulichtphcm.data.model.PublicUtility;
import com.example.nhom_15_dulichtphcm.ui.AppNavigator;
import com.example.nhom_15_dulichtphcm.util.Localized;
import com.example.nhom_15_dulichtphcm.util.MapLauncher;
import com.example.nhom_15_dulichtphcm.util.RideLauncher;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure Java RecyclerView.Adapter binding PublicUtility rows from item_utility.xml (Old Style).
 */
public final class UtilityAdapter extends RecyclerView.Adapter<UtilityAdapter.VH> {

    private final List<PublicUtility> items = new ArrayList<>();
    private final AppNavigator navigator;

    public UtilityAdapter(AppNavigator navigator) {
        this.navigator = navigator;
    }

    public void submitList(List<PublicUtility> newList) {
        this.items.clear();
        if (newList != null) this.items.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_utility, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        PublicUtility u = items.get(position);
        h.tvBadge.setText(u.category.iconEmoji + " " + Localized.tr(u.category.labelVi, u.category.labelEn));
        h.tvDistrict.setText(u.district);
        h.tvName.setText(Localized.tr(u.nameVi, u.nameEn));
        h.tvSubtitle.setText(Localized.tr(u.nameEn, u.nameVi));
        h.tvAddress.setText("📍 " + u.address);
        h.tvHoursPrice.setText("🕒 " + u.operatingHours + "   •   " + u.price);
        h.tvDesc.setText(Localized.tr(u.descriptionVi, u.descriptionEn));
        h.tvTips.setText("💡 " + u.tips);

        h.btnGoogleMaps.setText("🧭 Google Maps");
        h.btnGoogleMaps.setOnClickListener(v ->
                MapLauncher.openDirections(v.getContext(), u.latitude, u.longitude));

        h.btnMap.setText(Localized.tr("📍 Xem Bản đồ", "📍 View on Map"));
        h.btnMap.setOnClickListener(v -> {
            if (navigator != null) navigator.openMapAt(u.latitude, u.longitude, u.nameVi);
        });

        h.btnRide.setText(Localized.tr("🚕 Đặt xe đến đây 🚖", "🚕 Book Ride / Đặt xe đến đây 🚖"));
        h.btnRide.setOnClickListener(v -> {
            if (navigator != null) navigator.openRideBooking(u.nameVi, u.latitude, u.longitude);
        });

        if (u.phone != null && !u.phone.trim().isEmpty()) {
            h.btnCall.setVisibility(View.VISIBLE);
            h.btnCall.setText("📞 " + u.phone);
            h.btnCall.setOnClickListener(v -> RideLauncher.callHotline(v.getContext(), u.phone));
        } else {
            h.btnCall.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class VH extends RecyclerView.ViewHolder {
        final TextView tvBadge;
        final TextView tvDistrict;
        final TextView tvName;
        final TextView tvSubtitle;
        final TextView tvAddress;
        final TextView tvHoursPrice;
        final TextView tvDesc;
        final TextView tvTips;
        final TextView btnGoogleMaps;
        final TextView btnMap;
        final TextView btnRide;
        final TextView btnCall;

        VH(@NonNull View v) {
            super(v);
            tvBadge = v.findViewById(R.id.tv_util_badge);
            tvDistrict = v.findViewById(R.id.tv_util_district);
            tvName = v.findViewById(R.id.tv_util_name);
            tvSubtitle = v.findViewById(R.id.tv_util_subtitle);
            tvAddress = v.findViewById(R.id.tv_util_address);
            tvHoursPrice = v.findViewById(R.id.tv_util_hours_price);
            tvDesc = v.findViewById(R.id.tv_util_desc);
            tvTips = v.findViewById(R.id.tv_util_tips);
            btnGoogleMaps = v.findViewById(R.id.btn_util_google_maps);
            btnMap = v.findViewById(R.id.btn_util_map);
            btnRide = v.findViewById(R.id.btn_util_ride);
            btnCall = v.findViewById(R.id.btn_util_call);
        }
    }
}
