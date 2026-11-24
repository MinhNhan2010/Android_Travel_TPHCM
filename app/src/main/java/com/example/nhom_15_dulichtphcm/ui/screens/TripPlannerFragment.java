package com.example.nhom_15_dulichtphcm.ui.screens;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.nhom_15_dulichtphcm.R;
import com.example.nhom_15_dulichtphcm.data.ai.TripOptimizer;
import com.example.nhom_15_dulichtphcm.data.local.AuthManager;
import com.example.nhom_15_dulichtphcm.data.local.TripPlanManager;
import com.example.nhom_15_dulichtphcm.data.model.Attraction;
import com.example.nhom_15_dulichtphcm.data.model.Permission;
import com.example.nhom_15_dulichtphcm.data.model.ResultBox;
import com.example.nhom_15_dulichtphcm.data.repository.AttractionRepository;
import com.example.nhom_15_dulichtphcm.ui.AppNavigator;
import com.example.nhom_15_dulichtphcm.util.ImageLoader;
import com.example.nhom_15_dulichtphcm.util.Localized;
import com.example.nhom_15_dulichtphcm.util.MapLauncher;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Pure Java + XML Smart Itinerary Planner backed by SQLite tables trip_stops & trip_settings.
 */
public final class TripPlannerFragment extends Fragment {

    private View rootView;
    private TextView tvEmptyIcon;
    private TextView tvSummaryTitle;
    private TextView tvStats;
    private TextView btnStartTime;
    private LinearLayout stopsContainer;
    private LinearLayout quickPicksContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_trip_planner, container, false);
        tvEmptyIcon = rootView.findViewById(R.id.tv_trip_empty_icon);
        tvSummaryTitle = rootView.findViewById(R.id.tv_trip_summary_title);
        tvStats = rootView.findViewById(R.id.tv_trip_summary_stats);
        btnStartTime = rootView.findViewById(R.id.btn_trip_start_time);
        stopsContainer = rootView.findViewById(R.id.container_trip_stops);
        quickPicksContainer = rootView.findViewById(R.id.container_trip_quick_picks);

        TextView tvToolbarTitle = rootView.findViewById(R.id.tv_toolbar_title);
        TextView tvHeaderSub = rootView.findViewById(R.id.tv_trip_header_sub);
        TextView btnAddPlace = rootView.findViewById(R.id.btn_trip_add_place);
        TextView btnClear = rootView.findViewById(R.id.btn_trip_clear);
        TextView tvTemplatesHeader = rootView.findViewById(R.id.tv_trip_templates_header);
        TextView tvPickHeader = rootView.findViewById(R.id.tv_trip_pick_places_header);

        tvToolbarTitle.setText(Localized.tr("Lịch trình Thông minh 🗓️", "Smart Trip Planner 🗓️"));
        tvHeaderSub.setText(Localized.tr(
                "Tối ưu lộ trình tự động • ước tính giờ & chi phí xe",
                "Auto route optimization • time & cost estimate"));
        btnAddPlace.setText(Localized.tr("+ Thêm điểm", "+ Add Place"));
        btnClear.setText(Localized.tr("🗑️ Xóa hết", "🗑️ Clear"));
        tvTemplatesHeader.setText(Localized.tr("⚡ Khởi tạo nhanh bằng mẫu lộ trình", "⚡ Quick start with a template"));
        tvPickHeader.setText(Localized.tr("➕ Hoặc tự chọn địa điểm yêu thích", "➕ Or pick places yourself"));

        rootView.findViewById(R.id.btn_toolbar_back).setOnClickListener(v -> {
            AppNavigator nav = getNavigator();
            if (nav != null) nav.navigateBack();
        });

        btnStartTime.setOnClickListener(v -> showStartTimePicker());
        btnAddPlace.setOnClickListener(v -> showAddPlacePicker());
        rootView.findViewById(R.id.btn_trip_share).setOnClickListener(v -> sharePlanText());
        btnClear.setOnClickListener(v -> {
            TripPlanManager.clear();
            renderPlan();
        });

        rootView.findViewById(R.id.btn_preset_classic).setOnClickListener(v -> applyPreset(Arrays.asList(
                "independence_palace", "notre_dame", "central_post_office", "war_remnants_museum", "ben_thanh_market"
        )));
        rootView.findViewById(R.id.btn_preset_skylines).setOnClickListener(v -> applyPreset(Arrays.asList(
                "jade_emperor_pagoda", "landmark_81", "thu_thiem_waterfront", "nguyen_hue_walking_street"
        )));
        rootView.findViewById(R.id.btn_preset_vungtau).setOnClickListener(v -> applyPreset(Arrays.asList(
                "tuong_chua_kito_vung_tau", "hai_dang_vung_tau", "bach_dinh_villa_blanche", "mui_nghinh_phong"
        )));
        rootView.findViewById(R.id.btn_preset_megacity).setOnClickListener(v -> applyPreset(Arrays.asList(
                "dai_nam_binh_duong", "chua_ba_binh_duong", "lang_gom_lai_thieu", "vuon_trai_cay_lai_thieu"
        )));

        renderPlan();
        return rootView;
    }

    private void applyPreset(List<String> ids) {
        if (!AuthManager.hasPermission(Permission.SAVE_PERSONAL_COLLECTION)) {
            AppNavigator nav = getNavigator();
            if (nav != null) nav.showPermissionRequiredDialog(null);
            return;
        }
        TripPlanManager.replaceOrder(ids);
        renderPlan();
    }

    private void showStartTimePicker() {
        final int[] times = {7 * 60 + 30, 8 * 60, 8 * 60 + 30, 9 * 60, 13 * 60 + 30};
        String[] labels = {"07:30 Sáng sớm", "08:00 Chuẩn du lịch", "08:30 Thong thả", "09:00 Khởi hành", "13:30 Lịch trình chiều"};
        new AlertDialog.Builder(requireContext())
                .setTitle(Localized.tr("Chọn giờ xuất phát", "Select Departure Time"))
                .setItems(labels, (d, w) -> {
                    TripPlanManager.updateStartMinutes(times[w]);
                    renderPlan();
                })
                .show();
    }

    private void showAddPlacePicker() {
        if (!AuthManager.hasPermission(Permission.SAVE_PERSONAL_COLLECTION)) {
            AppNavigator nav = getNavigator();
            if (nav != null) nav.showPermissionRequiredDialog(null);
            return;
        }
        List<Attraction> candidates = new ArrayList<>();
        for (Attraction a : AttractionRepository.getAttractions()) {
            if (!TripPlanManager.contains(a.id)) candidates.add(a);
        }
        if (candidates.isEmpty()) {
            Toast.makeText(requireContext(), "Đã thêm toàn bộ địa điểm!", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] names = new String[candidates.size()];
        for (int i = 0; i < candidates.size(); i++) {
            Attraction a = candidates.get(i);
            names[i] = a.nameVi + " (" + a.province + ")";
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(Localized.tr("➕ Chọn điểm thêm vào Lịch trình", "➕ Add Place to Itinerary"))
                .setItems(names, (d, w) -> {
                    TripPlanManager.add(candidates.get(w).id);
                    renderPlan();
                })
                .show();
    }

    private void sharePlanText() {
        List<Attraction> selected = resolveSelectedAttractions();
        if (selected.isEmpty()) {
            Toast.makeText(requireContext(), Localized.tr("Chưa có điểm nào để chia sẻ", "Itinerary is empty"), Toast.LENGTH_SHORT).show();
            return;
        }
        TripOptimizer.OptimizedPlan plan = TripOptimizer.buildPlan(selected, TripPlanManager.getStartMinutes());
        StringBuilder sb = new StringBuilder("🇻🇳 LỊCH TRÌNH KHÁM PHÁ TP.HCM MỞ RỘNG\n");
        for (TripOptimizer.TripStop s : plan.stops) {
            sb.append(s.orderNumber).append(". [").append(s.arrivalClock).append(" - ").append(s.departureClock).append("] ")
              .append(s.attraction.nameVi).append(" (").append(s.attraction.province).append(")\n");
        }
        MapLauncher.shareText(requireContext(), "Lịch trình Du lịch TP.HCM", sb.toString());
    }

    private List<Attraction> resolveSelectedAttractions() {
        List<Attraction> list = new ArrayList<>();
        for (String id : TripPlanManager.getStopIds()) {
            Attraction a = AttractionRepository.getAttractionById(id);
            if (a != null) list.add(a);
        }
        return list;
    }

    private void renderPlan() {
        if (!isAdded() || rootView == null) return;
        int startMins = TripPlanManager.getStartMinutes();
        btnStartTime.setText("🕗 " + TripOptimizer.formatClock(startMins));

        List<Attraction> selected = resolveSelectedAttractions();
        stopsContainer.removeAllViews();
        renderQuickPicksCarousel();

        if (selected.isEmpty()) {
            tvEmptyIcon.setVisibility(View.VISIBLE);
            tvSummaryTitle.setText(Localized.tr("Lịch trình của bạn đang trống", "Your trip is empty"));
            tvStats.setText(Localized.tr(
                    "Nhấn 📅 trên bất kỳ địa điểm nào hoặc chọn một mẫu bên dưới. Ứng dụng sẽ tự tìm lộ trình ngắn nhất, tính giờ tham quan và chi phí đặt xe.",
                    "Tap 📅 on any place or pick a template. The app finds the shortest route, schedules arrival times, and estimates ride costs."));
            return;
        }

        tvEmptyIcon.setVisibility(View.GONE);
        tvSummaryTitle.setText(Localized.tr(
                "✨ Lộ trình Đã tối ưu (Nearest-Neighbor)",
                "✨ Optimized Route (Nearest-Neighbor)"));

        List<TripOptimizer.DayPlan> days = TripOptimizer.buildMultiDayPlan(selected, startMins);
        TripOptimizer.OptimizedPlan totalPlan = TripOptimizer.buildPlan(selected, startMins);
        NumberFormat nf = NumberFormat.getInstance(Locale.US);

        tvStats.setText(String.format(Locale.US,
                "📍 %d stops (%d day%s)  •  🛣️ %.1f km  •  🚕 ~%s₫",
                selected.size(), days.size(), days.size() > 1 ? "s" : "",
                totalPlan.totalDistanceKm, nf.format(totalPlan.totalEstimatedTransportVnd)));

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        AppNavigator nav = getNavigator();

        for (TripOptimizer.DayPlan day : days) {
            TextView dayHeader = new TextView(requireContext());
            dayHeader.setText(Localized.tr("🌅 NGÀY " + day.dayNumber, "🌅 DAY " + day.dayNumber));
            dayHeader.setTextSize(14f);
            dayHeader.setTypeface(null, Typeface.BOLD);
            dayHeader.setTextColor(ContextCompat.getColor(requireContext(), R.color.ai_purple));
            dayHeader.setPadding(4, 12, 4, 8);
            stopsContainer.addView(dayHeader);

            for (TripOptimizer.TripStop stop : day.plan.stops) {
                View card = inflater.inflate(R.layout.item_trip_stop, stopsContainer, false);
                TextView tvOrder = card.findViewById(R.id.tv_stop_order);
                TextView tvClock = card.findViewById(R.id.tv_stop_clock);
                TextView tvName = card.findViewById(R.id.tv_stop_name);
                TextView tvLeg = card.findViewById(R.id.tv_stop_leg_info);
                TextView btnRemove = card.findViewById(R.id.btn_stop_remove);

                tvOrder.setText(String.valueOf(stop.orderNumber));
                tvClock.setText(stop.arrivalClock + " → " + stop.departureClock + " (" + stop.stayMinutes + "m)");
                tvName.setText(Localized.tr(stop.attraction.nameVi, stop.attraction.nameEn) + " • " + stop.attraction.province);
                if (stop.legDistanceKm > 0) {
                    tvLeg.setText(String.format(Locale.US,
                            "🚕 %.1f km (~%d min • ~%s₫)",
                            stop.legDistanceKm, stop.legTravelMinutes, nf.format(stop.estimatedTaxiVnd)));
                } else {
                    tvLeg.setText(Localized.tr("🏁 Điểm xuất phát đầu ngày", "🏁 Starting landmark of the day"));
                }

                btnRemove.setOnClickListener(v -> {
                    TripPlanManager.remove(stop.attraction.id);
                    renderPlan();
                });
                card.setOnClickListener(v -> {
                    if (nav != null) nav.openAttractionDetail(stop.attraction.id);
                });
                stopsContainer.addView(card);
            }
        }
    }

    private void renderQuickPicksCarousel() {
        if (quickPicksContainer == null) return;
        quickPicksContainer.removeAllViews();
        float d = getResources().getDisplayMetrics().density;
        int cardW = (int) (148 * d);
        int imgH = (int) (96 * d);

        List<Attraction> all = AttractionRepository.getAttractions();
        int limit = Math.min(10, all.size());
        for (int i = 0; i < limit; i++) {
            final Attraction a = all.get(i);
            boolean added = TripPlanManager.contains(a.id);

            LinearLayout miniCard = new LinearLayout(requireContext());
            miniCard.setOrientation(LinearLayout.VERTICAL);
            miniCard.setBackgroundResource(R.drawable.bg_card_rounded);
            LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(cardW, ViewGroup.LayoutParams.WRAP_CONTENT);
            cardLp.setMarginEnd((int) (10 * d));
            miniCard.setLayoutParams(cardLp);

            FrameLayout photoBox = new FrameLayout(requireContext());
            photoBox.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, imgH));

            ImageView iv = new ImageView(requireContext());
            iv.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            ImageLoader.loadAttraction(iv, a);
            photoBox.addView(iv);

            TextView badge = new TextView(requireContext());
            int bSize = (int) (28 * d);
            FrameLayout.LayoutParams badgeLp = new FrameLayout.LayoutParams(bSize, bSize, Gravity.TOP | Gravity.END);
            int m = (int) (6 * d);
            badgeLp.setMargins(m, m, m, m);
            badge.setLayoutParams(badgeLp);
            badge.setGravity(Gravity.CENTER);
            badge.setBackgroundResource(added ? R.drawable.bg_ride_green_circle : R.drawable.bg_ai_circle);
            badge.setText(added ? "✓" : "+");
            badge.setTextColor(Color.WHITE);
            badge.setTextSize(14f);
            badge.setTypeface(null, Typeface.BOLD);
            photoBox.addView(badge);

            miniCard.addView(photoBox);

            TextView tvTitle = new TextView(requireContext());
            int pad = (int) (8 * d);
            tvTitle.setPadding(pad, pad, pad, pad);
            tvTitle.setText(Localized.tr(a.nameVi, a.nameEn));
            tvTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            tvTitle.setTextSize(12f);
            tvTitle.setTypeface(null, Typeface.BOLD);
            tvTitle.setMaxLines(1);
            tvTitle.setEllipsize(TextUtils.TruncateAt.END);
            miniCard.addView(tvTitle);

            miniCard.setOnClickListener(v -> {
                ResultBox<Boolean> res = TripPlanManager.toggle(a.id);
                if (!res.isSuccess()) {
                    AppNavigator nav = getNavigator();
                    if (nav != null) nav.showPermissionRequiredDialog(res.getErrorMessage());
                    return;
                }
                renderPlan();
            });

            quickPicksContainer.addView(miniCard);
        }
    }

    private AppNavigator getNavigator() {
        return getActivity() instanceof AppNavigator ? (AppNavigator) getActivity() : null;
    }
}
