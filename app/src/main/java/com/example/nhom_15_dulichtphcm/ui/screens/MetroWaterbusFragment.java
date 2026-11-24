package com.example.nhom_15_dulichtphcm.ui.screens;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.nhom_15_dulichtphcm.R;
import com.example.nhom_15_dulichtphcm.data.model.Attraction;
import com.example.nhom_15_dulichtphcm.data.repository.AttractionRepository;
import com.example.nhom_15_dulichtphcm.data.repository.MetroTransitData;
import com.example.nhom_15_dulichtphcm.ui.AppNavigator;
import com.example.nhom_15_dulichtphcm.ui.components.CompactHeaderHelper;
import com.example.nhom_15_dulichtphcm.util.Localized;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Pure Java + XML Interactive HCMC Metro Line 1 (14 Stations) & Saigon Waterbus (5 Piers)
 * Fare / Time Calculator and Station-to-Landmark Connector.
 */
public final class MetroWaterbusFragment extends Fragment {

    private boolean isWaterbusMode = false;
    private int fromIdx = 0;
    private int toIdx = 4;
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_metro_waterbus, container, false);

        CompactHeaderHelper.bindSubscreenToolbar(
                rootView,
                Localized.tr("🚇 Tuyến Metro Số 1 & Waterbus", "🚇 Metro Line 1 & Waterbus"),
                getNavigator(),
                null,
                null
        );

        rootView.findViewById(R.id.tab_mode_metro).setOnClickListener(v -> {
            isWaterbusMode = false;
            fromIdx = 0;
            toIdx = 4;
            renderState();
        });

        rootView.findViewById(R.id.tab_mode_waterbus).setOnClickListener(v -> {
            isWaterbusMode = true;
            fromIdx = 0;
            toIdx = 2;
            renderState();
        });

        rootView.findViewById(R.id.btn_pick_from_station).setOnClickListener(v -> showStationPicker(true));
        rootView.findViewById(R.id.btn_pick_to_station).setOnClickListener(v -> showStationPicker(false));
        rootView.findViewById(R.id.btn_swap_stations).setOnClickListener(v -> {
            int tmp = fromIdx;
            fromIdx = toIdx;
            toIdx = tmp;
            renderState();
        });

        rootView.findViewById(R.id.btn_transit_open_dest_map).setOnClickListener(v -> {
            List<MetroTransitData.TransitStation> list = getActiveStations();
            MetroTransitData.TransitStation dest = list.get(Math.min(toIdx, list.size() - 1));
            AppNavigator nav = getNavigator();
            if (nav != null) {
                nav.openMapAt(dest.latitude, dest.longitude, Localized.tr(dest.nameVi, dest.nameEn));
            }
        });

        rootView.findViewById(R.id.btn_transit_open_landmark).setOnClickListener(v -> {
            List<MetroTransitData.TransitStation> list = getActiveStations();
            MetroTransitData.TransitStation dest = list.get(Math.min(toIdx, list.size() - 1));
            openLinkedLandmark(dest);
        });

        renderState();
        return rootView;
    }

    private List<MetroTransitData.TransitStation> getActiveStations() {
        return isWaterbusMode ? MetroTransitData.SAIGON_WATERBUS : MetroTransitData.METRO_LINE_1;
    }

    private void showStationPicker(boolean pickingFrom) {
        List<MetroTransitData.TransitStation> list = getActiveStations();
        String[] labels = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            MetroTransitData.TransitStation s = list.get(i);
            labels[i] = (i + 1) + ". " + Localized.tr(s.nameVi, s.nameEn) + " (" + s.zoneLabel + ")";
        }
        int checked = pickingFrom ? fromIdx : toIdx;
        new AlertDialog.Builder(requireContext())
                .setTitle(pickingFrom
                        ? Localized.tr("🟢 Chọn Ga / Bến xuất phát", "🟢 Select Departure Station")
                        : Localized.tr("🏁 Chọn Ga / Bến đích", "🏁 Select Arrival Station"))
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    if (pickingFrom) fromIdx = which;
                    else toIdx = which;
                    dialog.dismiss();
                    renderState();
                })
                .setNegativeButton(Localized.tr("Hủy", "Cancel"), null)
                .show();
    }

    private void openLinkedLandmark(MetroTransitData.TransitStation st) {
        AppNavigator nav = getNavigator();
        if (nav == null) return;
        Attraction linked = AttractionRepository.getAttractionById(st.linkedAttractionId);
        if (linked == null && !AttractionRepository.getAttractions().isEmpty()) {
            linked = AttractionRepository.getAttractions().get(0);
        }
        if (linked != null) {
            nav.openAttractionDetail(linked.id);
        } else {
            Toast.makeText(requireContext(),
                    Localized.tr(st.nearbyHighlightsVi, st.nearbyHighlightsEn),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void renderState() {
        if (!isAdded() || rootView == null) return;

        TextView tabMetro = rootView.findViewById(R.id.tab_mode_metro);
        TextView tabWaterbus = rootView.findViewById(R.id.tab_mode_waterbus);
        styleTab(tabMetro, !isWaterbusMode);
        styleTab(tabWaterbus, isWaterbusMode);

        List<MetroTransitData.TransitStation> list = getActiveStations();
        fromIdx = Math.max(0, Math.min(fromIdx, list.size() - 1));
        toIdx = Math.max(0, Math.min(toIdx, list.size() - 1));

        MetroTransitData.TransitStation fromSt = list.get(fromIdx);
        MetroTransitData.TransitStation toSt = list.get(toIdx);

        TextView btnFrom = rootView.findViewById(R.id.btn_pick_from_station);
        TextView btnTo = rootView.findViewById(R.id.btn_pick_to_station);
        TextView tvSummary = rootView.findViewById(R.id.tv_transit_route_summary);
        TextView tvFare = rootView.findViewById(R.id.tv_transit_fare_main);
        TextView tvPass = rootView.findViewById(R.id.tv_transit_pass_note);
        TextView tvLineHeader = rootView.findViewById(R.id.tv_transit_line_header);
        LinearLayout stationContainer = rootView.findViewById(R.id.container_transit_stations);

        btnFrom.setText("🟢 " + Localized.tr("Đi: ", "From: ") + Localized.tr(fromSt.nameVi, fromSt.nameEn) + " ▾");
        btnTo.setText("🏁 " + Localized.tr("Đến: ", "To: ") + Localized.tr(toSt.nameVi, toSt.nameEn) + " ▾");

        int stops = Math.abs(toIdx - fromIdx);
        int mins = MetroTransitData.calculateTravelMinutes(isWaterbusMode, fromIdx, toIdx);
        NumberFormat nf = NumberFormat.getInstance(Locale.US);

        if (stops == 0) {
            tvSummary.setText(Localized.tr("📍 Bạn đang chọn cùng 1 ga/bến", "📍 Same departure & arrival station"));
            tvFare.setText(Localized.tr("Hãy chọn Ga đến khác để tính giá vé & thời gian", "Pick a different destination station"));
        } else if (!isWaterbusMode) {
            int cash = MetroTransitData.calculateMetroCashFareVnd(fromIdx, toIdx);
            int cashless = MetroTransitData.calculateMetroCashlessFareVnd(fromIdx, toIdx);
            tvSummary.setText(Localized.tr(
                    "🚇 Qua " + stops + " nhà ga  •  ⏱️ Khoảng " + mins + " phút",
                    "🚇 " + stops + " stops  •  ⏱️ ~" + mins + " mins"));
            tvFare.setText(Localized.tr(
                    "💳 Thẻ/QR: " + nf.format(cashless) + "đ   •   💵 Tiền mặt: " + nf.format(cash) + "đ",
                    "💳 Cashless/QR: " + nf.format(cashless) + "đ   •   💵 Cash: " + nf.format(cash) + "đ"));
        } else {
            tvSummary.setText(Localized.tr(
                    "⛴️ Qua " + stops + " bến sông Sài Gòn  •  ⏱️ Khoảng " + mins + " phút",
                    "⛴️ " + stops + " river piers  •  ⏱️ ~" + mins + " mins"));
            tvFare.setText(Localized.tr(
                    "🎫 Đồng giá Saigon Waterbus: 15,000đ / lượt / hành khách",
                    "🎫 Flat Saigon Waterbus fare: 15,000 VND / one-way trip"));
        }

        tvPass.setText(!isWaterbusMode
                ? Localized.tr(
                        "✨ Vé ngày không giới hạn lượt: 40,000đ/ngày • Vé 3 ngày: 90,000đ • Hoạt động 05:00 – 22:00",
                        "✨ Unlimited Day Pass: 40,000 VND • 3-Day Pass: 90,000 VND • Runs 05:00 – 22:00")
                : Localized.tr(
                        "🌅 Mẹo: Nên đặt chuyến 16:45 – 17:30 từ Bến Bạch Đằng để ngắm hoàng hôn Landmark 81!",
                        "🌅 Tip: Take the 16:45 – 17:30 departure from Bach Dang Pier for golden-hour skyline views!"));

        tvLineHeader.setText(!isWaterbusMode
                ? Localized.tr("🚇 Sơ đồ 14 Ga Metro Số 1 (Bến Thành – Suối Tiên)", "🚇 All 14 Metro Line 1 Stations")
                : Localized.tr("⛴️ Sơ đồ 5 Bến Buýt Đường Sông (Bạch Đằng – Linh Đông)", "⛴️ All 5 Saigon Waterbus Piers"));

        stationContainer.removeAllViews();
        float d = getResources().getDisplayMetrics().density;
        int minSel = Math.min(fromIdx, toIdx);
        int maxSel = Math.max(fromIdx, toIdx);

        for (int i = 0; i < list.size(); i++) {
            final int idx = i;
            MetroTransitData.TransitStation st = list.get(i);
            boolean inRoute = stops > 0 && idx >= minSel && idx <= maxSel;
            boolean isEndpoint = (idx == fromIdx || idx == toIdx);

            LinearLayout card = new LinearLayout(requireContext());
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(isEndpoint ? R.drawable.bg_lavender_card : R.drawable.bg_card_rounded);
            int pad = (int) (13 * d);
            card.setPadding(pad, pad, pad, pad);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = (int) (8 * d);
            card.setLayoutParams(lp);

            LinearLayout top = new LinearLayout(requireContext());
            top.setOrientation(LinearLayout.HORIZONTAL);
            top.setGravity(Gravity.CENTER_VERTICAL);

            TextView badge = new TextView(requireContext());
            badge.setText(String.format(Locale.US, "%02d", i + 1));
            int bSz = (int) (28 * d);
            badge.setLayoutParams(new LinearLayout.LayoutParams(bSz, bSz));
            badge.setGravity(Gravity.CENTER);
            badge.setBackgroundResource(inRoute ? R.drawable.bg_chip_sky_selected : R.drawable.bg_oauth_button);
            badge.setTextColor(ContextCompat.getColor(requireContext(), inRoute ? R.color.white : R.color.text_primary));
            badge.setTextSize(11.5f);
            badge.setTypeface(null, Typeface.BOLD);
            top.addView(badge);

            LinearLayout titleCol = new LinearLayout(requireContext());
            titleCol.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            tLp.setMarginStart((int) (10 * d));
            titleCol.setLayoutParams(tLp);

            TextView tvName = new TextView(requireContext());
            String tag = idx == fromIdx ? " 🟢" : (idx == toIdx ? " 🏁" : "");
            tvName.setText(Localized.tr(st.nameVi, st.nameEn) + tag);
            tvName.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            tvName.setTextSize(14f);
            tvName.setTypeface(null, Typeface.BOLD);
            titleCol.addView(tvName);

            TextView tvZone = new TextView(requireContext());
            String structType = (!isWaterbusMode && i < 3)
                    ? Localized.tr("Ga ngầm • ", "Underground • ")
                    : (!isWaterbusMode ? Localized.tr("Ga trên cao • ", "Elevated • ") : Localized.tr("Bến thủy • ", "River Pier • "));
            tvZone.setText(structType + st.zoneLabel);
            tvZone.setTextColor(ContextCompat.getColor(requireContext(), R.color.saigon_teal));
            tvZone.setTextSize(11.5f);
            tvZone.setTypeface(null, Typeface.BOLD);
            titleCol.addView(tvZone);

            top.addView(titleCol);
            card.addView(top);

            TextView tvNearby = new TextView(requireContext());
            tvNearby.setText("🏛️ " + Localized.tr(st.nearbyHighlightsVi, st.nearbyHighlightsEn));
            tvNearby.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            tvNearby.setTextSize(12.5f);
            tvNearby.setPadding(0, (int) (6 * d), 0, (int) (8 * d));
            card.addView(tvNearby);

            LinearLayout btnRow = new LinearLayout(requireContext());
            btnRow.setOrientation(LinearLayout.HORIZONTAL);

            TextView btnSetFrom = createMiniActionButton(Localized.tr("🟢 Chọn Ga đi", "🟢 Set From"), d);
            btnSetFrom.setOnClickListener(v -> { fromIdx = idx; renderState(); });
            btnRow.addView(btnSetFrom);

            TextView btnSetTo = createMiniActionButton(Localized.tr("🏁 Chọn Ga đến", "🏁 Set To"), d);
            btnSetTo.setOnClickListener(v -> { toIdx = idx; renderState(); });
            btnRow.addView(btnSetTo);

            TextView btnMap = createMiniActionButton(Localized.tr("🗺️ Bản đồ", "🗺️ Map"), d);
            btnMap.setOnClickListener(v -> {
                AppNavigator nav = getNavigator();
                if (nav != null) nav.openMapAt(st.latitude, st.longitude, Localized.tr(st.nameVi, st.nameEn));
            });
            btnRow.addView(btnMap);

            card.addView(btnRow);
            stationContainer.addView(card);
        }
    }

    private TextView createMiniActionButton(String text, float d) {
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setBackgroundResource(R.drawable.bg_stat_tile);
        tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        tv.setTextSize(11f);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setPadding((int) (10 * d), (int) (6 * d), (int) (10 * d), (int) (6 * d));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMarginEnd((int) (6 * d));
        tv.setLayoutParams(lp);
        return tv;
    }

    private void styleTab(TextView tv, boolean active) {
        tv.setBackgroundResource(active ? R.drawable.bg_chip_sky_selected : R.drawable.bg_chip_unselected);
        tv.setTextColor(ContextCompat.getColor(requireContext(), active ? R.color.white : R.color.text_primary));
    }

    private AppNavigator getNavigator() {
        return getActivity() instanceof AppNavigator ? (AppNavigator) getActivity() : null;
    }
}
