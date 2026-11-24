package com.example.nhom_15_dulichtphcm.ui.screens;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.nhom_15_dulichtphcm.R;
import com.example.nhom_15_dulichtphcm.data.model.DriverPhraseCard;
import com.example.nhom_15_dulichtphcm.data.model.RideEstimate;
import com.example.nhom_15_dulichtphcm.data.model.RideProvider;
import com.example.nhom_15_dulichtphcm.data.repository.RideRepository;
import com.example.nhom_15_dulichtphcm.ui.AppNavigator;
import com.example.nhom_15_dulichtphcm.ui.components.ChipViewFactory;
import com.example.nhom_15_dulichtphcm.ui.components.CompactHeaderHelper;
import com.example.nhom_15_dulichtphcm.util.Localized;
import com.example.nhom_15_dulichtphcm.util.RideLauncher;
import com.example.nhom_15_dulichtphcm.util.SpeechHelper;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Pure Java + XML Ride-Hailing & Taxi Fare Comparison screen (Old Style 3-Tab Arrangement).
 */
public final class RideBookingFragment extends Fragment {

    private static final String ARG_DEST_NAME = "dest_name";
    private static final String ARG_DEST_LAT = "dest_lat";
    private static final String ARG_DEST_LNG = "dest_lng";

    public static RideBookingFragment newInstance(String destName, double destLat, double destLng) {
        RideBookingFragment f = new RideBookingFragment();
        Bundle b = new Bundle();
        b.putString(ARG_DEST_NAME, destName);
        b.putDouble(ARG_DEST_LAT, destLat);
        b.putDouble(ARG_DEST_LNG, destLng);
        f.setArguments(b);
        return f;
    }

    private int currentSubTab = 0; // 0 = Apps, 1 = Estimator, 2 = Driver Cards
    private int selectedPickupIndex = 0;
    private double destLat = 10.7798;
    private double destLng = 106.6990;
    private View rootView;
    private EditText etDestination;
    private TextView tabApps;
    private TextView tabEstimator;
    private TextView tabCards;
    private View panelApps;
    private View panelEstimator;
    private View panelCards;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_ride_booking, container, false);
        etDestination = rootView.findViewById(R.id.et_ride_destination);
        tabApps = rootView.findViewById(R.id.tab_ride_apps);
        tabEstimator = rootView.findViewById(R.id.tab_ride_estimator);
        tabCards = rootView.findViewById(R.id.tab_ride_cards);
        panelApps = rootView.findViewById(R.id.panel_ride_apps);
        panelEstimator = rootView.findViewById(R.id.panel_ride_estimator);
        panelCards = rootView.findViewById(R.id.panel_ride_cards);

        String initDest = "Nhà thờ Đức Bà Sài Gòn";
        if (getArguments() != null) {
            initDest = getArguments().getString(ARG_DEST_NAME, initDest);
            destLat = getArguments().getDouble(ARG_DEST_LAT, destLat);
            destLng = getArguments().getDouble(ARG_DEST_LNG, destLng);
        }
        etDestination.setText(initDest);

        CompactHeaderHelper.bindSubscreenToolbar(
                rootView,
                Localized.tr("Đặt xe tại Sài Gòn 🚖", "Book a Ride in Saigon 🚖"),
                getNavigator(),
                null,
                null
        );

        tabApps.setOnClickListener(v -> {
            currentSubTab = 0;
            updateTabVisibility();
        });
        tabEstimator.setOnClickListener(v -> {
            currentSubTab = 1;
            updateTabVisibility();
        });
        tabCards.setOnClickListener(v -> {
            currentSubTab = 2;
            updateTabVisibility();
        });

        updateTabVisibility();
        renderDynamicSections();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (rootView != null && isAdded()) {
            renderDynamicSections();
        }
    }

    private void updateTabVisibility() {
        if (!isAdded()) return;
        tabApps.setText(Localized.tr("🚗 Ứng dụng & Taxi", "🚗 Ride Apps & Taxis"));
        tabEstimator.setText(Localized.tr("⏱️ Tính giá cước", "⏱️ Fare Estimator"));
        tabCards.setText(Localized.tr("🗣️ Thẻ Tài xế", "🗣️ Driver Cards"));

        TextView[] tabs = {tabApps, tabEstimator, tabCards};
        View[] panels = {panelApps, panelEstimator, panelCards};
        int darkText = ContextCompat.getColor(requireContext(), R.color.text_primary);
        for (int i = 0; i < tabs.length; i++) {
            boolean sel = i == currentSubTab;
            tabs[i].setBackgroundResource(sel ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
            tabs[i].setTextColor(sel ? Color.WHITE : darkText);
            tabs[i].setTypeface(null, sel ? Typeface.BOLD : Typeface.NORMAL);
            panels[i].setVisibility(sel ? View.VISIBLE : View.GONE);
        }
    }

    private void renderDynamicSections() {
        LinearLayout pickupChips = rootView.findViewById(R.id.container_pickup_chips);
        TextView tvSummary = rootView.findViewById(R.id.tv_ride_distance_summary);
        LinearLayout providersBox = rootView.findViewById(R.id.container_ride_providers);
        LinearLayout estimatesBox = rootView.findViewById(R.id.container_fare_estimates);
        LinearLayout phrasesBox = rootView.findViewById(R.id.container_driver_phrases);
        TextView tvSafety = rootView.findViewById(R.id.tv_ride_safety_rules);

        pickupChips.removeAllViews();
        for (int i = 0; i < RideRepository.popularPickupPoints.size(); i++) {
            final int idx = i;
            RideRepository.LocationPoint pt = RideRepository.popularPickupPoints.get(i);
            pickupChips.addView(ChipViewFactory.createChip(
                    requireContext(),
                    Localized.tr(pt.nameVi, pt.nameEn),
                    idx == selectedPickupIndex,
                    0,
                    () -> {
                        selectedPickupIndex = idx;
                        renderDynamicSections();
                    }
            ));
        }

        RideRepository.LocationPoint pickup = RideRepository.popularPickupPoints.get(selectedPickupIndex);
        double km = RideRepository.calculateDistanceKm(pickup.lat, pickup.lng, destLat, destLng);
        int estMins = Math.max(5, (int) Math.round(km * 3.2 + 3.0));
        tvSummary.setText(String.format(
                Locale.US,
                Localized.tr(
                        "📏 QUÃNG ĐƯỜNG ƯỚC TÍNH: %.1f km   •   ⏱️ GIỜ GIAO THÔNG: ~%d phút",
                        "📏 ESTIMATED DISTANCE: %.1f km   •   ⏱️ SAIGON TRAFFIC TIME: ~%d mins"
                ),
                km, estMins
        ));

        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        providersBox.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        int minBike = Integer.MAX_VALUE, maxBike = 0;
        int minCar4 = Integer.MAX_VALUE, maxCar4 = 0;
        int minCar7 = Integer.MAX_VALUE, maxCar7 = 0;

        for (RideProvider p : RideRepository.providers) {
            RideEstimate est = RideRepository.estimateFare(p, km);
            if (est.bikeFareVnd > 0) {
                minBike = Math.min(minBike, est.bikeFareVnd);
                maxBike = Math.max(maxBike, est.bikeFareVnd);
            }
            minCar4 = Math.min(minCar4, est.car4FareVnd);
            maxCar4 = Math.max(maxCar4, est.car4FareVnd);
            minCar7 = Math.min(minCar7, est.car7FareVnd);
            maxCar7 = Math.max(maxCar7, est.car7FareVnd);

            View card = inflater.inflate(R.layout.item_ride_provider, providersBox, false);
            TextView tvName = card.findViewById(R.id.tv_provider_name);
            TextView tvBadge = card.findViewById(R.id.tv_provider_badge);
            TextView tvDesc = card.findViewById(R.id.tv_provider_desc);
            TextView tvFares = card.findViewById(R.id.tv_provider_fares);
            TextView btnOpen = card.findViewById(R.id.btn_provider_open);
            TextView btnHotline = card.findViewById(R.id.btn_provider_hotline);

            tvName.setText(p.iconEmoji + " " + p.name);
            tvBadge.setText("⚡ " + p.badge);
            tvDesc.setText(Localized.tr(p.descriptionVi, p.descriptionEn));

            String bikePart = est.bikeFareVnd > 0 ? "🛵 Bike: " + nf.format(est.bikeFareVnd) + "₫  •  " : "";
            tvFares.setText(bikePart + "🚗 4-seat: " + nf.format(est.car4FareVnd) + "₫  •  🚐 7-seat: " + nf.format(est.car7FareVnd) + "₫");

            boolean installed = RideLauncher.isRideAppInstalled(requireContext(), p);
            btnOpen.setText(installed
                    ? Localized.tr("🚀 Mở ứng dụng ngay", "🚀 Open App")
                    : Localized.tr("📥 Tải trên CH Play", "📥 Get on Google Play"));
            btnOpen.setOnClickListener(v ->
                    RideLauncher.openRideProvider(requireContext(), p, etDestination.getText().toString()));

            if (p.hotline != null) {
                btnHotline.setVisibility(View.VISIBLE);
                btnHotline.setText("📞 " + p.hotline);
                btnHotline.setOnClickListener(v -> RideLauncher.callHotline(requireContext(), p.hotline));
            } else {
                btnHotline.setVisibility(View.GONE);
            }
            providersBox.addView(card);
        }

        // Populate 3 Old-Style Vehicle Tier Estimate Cards in Panel 2
        if (estimatesBox != null) {
            estimatesBox.removeAllViews();
            if (maxBike > 0) {
                estimatesBox.addView(buildVehicleEstimateCard(
                        "🛵 " + Localized.tr("Xe ôm công nghệ (1 khách)", "Motorbike Ride (1 passenger)"),
                        nf.format(minBike) + "₫ – " + nf.format(maxBike) + "₫",
                        Localized.tr("Nhanh nhất giờ cao điểm • Có nón bảo hiểm", "Fastest in traffic • Helmet provided"),
                        0xFF00796B
                ));
            }
            estimatesBox.addView(buildVehicleEstimateCard(
                    "🚗 " + Localized.tr("Xe hơi 4 chỗ máy lạnh", "Standard 4-Seat AC Car"),
                    nf.format(minCar4) + "₫ – " + nf.format(maxCar4) + "₫",
                    Localized.tr("GrabCar • Xanh SM EV • BeCar • Vinasun", "GrabCar • Xanh SM EV • BeCar • Vinasun"),
                    0xFF1D4ED8
            ));
            estimatesBox.addView(buildVehicleEstimateCard(
                    "🚐 " + Localized.tr("Xe 7 chỗ gia đình & hành lý", "7-Seat SUV (Groups & Luggage)"),
                    nf.format(minCar7) + "₫ – " + nf.format(maxCar7) + "₫",
                    Localized.tr("Phù hợp 4–6 khách hoặc đi sân bay Tân Sơn Nhất", "Ideal for 4–6 travelers or airport transfers"),
                    0xFFD84315
            ));
        }

        phrasesBox.removeAllViews();
        SpeechHelper speech = getNavigator() != null ? getNavigator().getSpeechHelper() : null;
        for (DriverPhraseCard dp : RideRepository.driverPhrases) {
            View pRow = inflater.inflate(R.layout.item_driver_phrase, phrasesBox, false);
            TextView tvVi = pRow.findViewById(R.id.tv_driver_vi);
            TextView tvEn = pRow.findViewById(R.id.tv_driver_en);
            TextView btnSpeak = pRow.findViewById(R.id.btn_driver_speak);
            TextView btnCopy = pRow.findViewById(R.id.btn_driver_copy);

            tvEn.setText(dp.english + "  •  💡 " + dp.tip);
            tvVi.setText(dp.vietnamese);
            btnSpeak.setOnClickListener(v -> {
                if (speech != null) speech.speakVietnamese(dp.vietnamese);
            });
            btnCopy.setOnClickListener(v -> RideLauncher.copyText(requireContext(), dp.vietnamese));
            phrasesBox.addView(pRow);
        }

        StringBuilder rules = new StringBuilder();
        for (String r : Localized.isVietnameseUi() ? RideRepository.safetyRulesVi : RideRepository.safetyRulesEn) {
            if (rules.length() > 0) rules.append("\n\n");
            rules.append("• ").append(r);
        }
        tvSafety.setText(rules.toString());
    }

    private View buildVehicleEstimateCard(String title, String priceRange, String note, int accentColor) {
        float d = getResources().getDisplayMetrics().density;
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card_rounded);
        int pad = (int) (14 * d);
        card.setPadding(pad, pad, pad, pad);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        lp.bottomMargin = (int) (10 * d);
        card.setLayoutParams(lp);

        TextView tvTitle = new TextView(requireContext());
        tvTitle.setText(title);
        tvTitle.setTextSize(14.5f);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        card.addView(tvTitle);

        TextView tvPrice = new TextView(requireContext());
        tvPrice.setText(priceRange);
        tvPrice.setTextSize(18f);
        tvPrice.setTypeface(null, Typeface.BOLD);
        tvPrice.setTextColor(accentColor);
        card.addView(tvPrice);

        TextView tvNote = new TextView(requireContext());
        tvNote.setText(note);
        tvNote.setTextSize(12f);
        tvNote.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        card.addView(tvNote);

        return card;
    }

    private AppNavigator getNavigator() {
        return getActivity() instanceof AppNavigator ? (AppNavigator) getActivity() : null;
    }
}
