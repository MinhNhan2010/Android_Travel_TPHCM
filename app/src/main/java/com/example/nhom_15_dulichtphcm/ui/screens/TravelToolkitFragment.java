package com.example.nhom_15_dulichtphcm.ui.screens;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.nhom_15_dulichtphcm.R;
import com.example.nhom_15_dulichtphcm.data.db.SaigonTravelDatabaseHelper;
import com.example.nhom_15_dulichtphcm.data.local.CheckInStamp;
import com.example.nhom_15_dulichtphcm.data.local.ExpenseCategory;
import com.example.nhom_15_dulichtphcm.data.local.ExplorerRank;
import com.example.nhom_15_dulichtphcm.data.local.PackingListManager;
import com.example.nhom_15_dulichtphcm.data.local.TravelJournalManager;
import com.example.nhom_15_dulichtphcm.data.local.TravelPassportManager;
import com.example.nhom_15_dulichtphcm.data.local.TripExpense;
import com.example.nhom_15_dulichtphcm.data.local.TripExpenseManager;
import com.example.nhom_15_dulichtphcm.data.local.UserDataBackupManager;
import com.example.nhom_15_dulichtphcm.data.model.Attraction;
import com.example.nhom_15_dulichtphcm.data.model.ResultBox;
import com.example.nhom_15_dulichtphcm.data.remote.TravelDataService;
import com.example.nhom_15_dulichtphcm.data.repository.AttractionRepository;
import com.example.nhom_15_dulichtphcm.ui.AppNavigator;
import com.example.nhom_15_dulichtphcm.ui.components.ChipViewFactory;
import com.example.nhom_15_dulichtphcm.ui.components.ExperienceDialogs;
import com.example.nhom_15_dulichtphcm.util.Localized;
import com.example.nhom_15_dulichtphcm.util.MapLauncher;

import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Pure Java + XML All-in-One Travel Toolkit:
 * 1) Live Weather & Foreign Exchange Converter (SQLite cached)
 * 2) Digital Travel Passport & Explorer Ranks (SQLite passport_stamps)
 * 3) Group Budget & Expense Splitter (SQLite expenses)
 */
public final class TravelToolkitFragment extends Fragment {

    private int activeTab = 0;
    private int selectedCityIdx = 0;
    private String selectedCurrency = "USD";
    private ExpenseCategory selectedExpenseCat = ExpenseCategory.FOOD;
    private int groupSize = 2;
    private int presetAmountIdx = 1;
    private PackingListManager.PresetRegion selectedPackingPreset = PackingListManager.PresetRegion.SAIGON_CITY;
    private static final String[] PRESET_AMOUNTS = {"10", "100", "500", "1000"};

    private final Map<String, Double> activeRates = new LinkedHashMap<>(TravelDataService.fallbackRates);
    private String ratesSourceLabel = "📦 SQLite Offline Reference";

    private View rootView;
    private View panelLive;
    private View panelPassport;
    private View panelBudget;
    private View panelPacking;
    private TextView tabLive;
    private TextView tabPassport;
    private TextView tabBudget;
    private TextView tabPacking;
    private EditText etFxAmount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_travel_toolkit, container, false);
        panelLive = rootView.findViewById(R.id.panel_tk_live);
        panelPassport = rootView.findViewById(R.id.panel_tk_passport);
        panelBudget = rootView.findViewById(R.id.panel_tk_budget);
        panelPacking = rootView.findViewById(R.id.panel_tk_packing);
        tabLive = rootView.findViewById(R.id.tab_tk_live);
        tabPassport = rootView.findViewById(R.id.tab_tk_passport);
        tabBudget = rootView.findViewById(R.id.tab_tk_budget);
        tabPacking = rootView.findViewById(R.id.tab_tk_packing);
        etFxAmount = rootView.findViewById(R.id.et_fx_amount);

        TextView tvTitle = rootView.findViewById(R.id.tv_toolbar_title);
        TextView tvSub = rootView.findViewById(R.id.tv_toolkit_header_sub);
        tvTitle.setText(Localized.tr("Bộ Công cụ Du lịch 🧰", "Travel Toolkit 🧰"));
        tvSub.setText(Localized.tr(
                "Thời tiết • Tỷ giá • Hộ chiếu • Chi tiêu • Hành lý",
                "Weather • FX • Passport • Expenses • Packing"));

        tabLive.setText(Localized.tr("🌤️ Thời tiết", "🌤️ Weather"));
        tabPassport.setText(Localized.tr("🛂 Hộ chiếu", "🛂 Passport"));
        tabBudget.setText(Localized.tr("💰 Chi tiêu", "💰 Expenses"));
        tabPacking.setText(Localized.tr("🎒 Hành lý", "🎒 Packing"));

        rootView.findViewById(R.id.btn_toolbar_back).setOnClickListener(v -> {
            AppNavigator nav = getNavigator();
            if (nav != null) nav.navigateBack();
        });

        rootView.findViewById(R.id.btn_weather_refresh).setOnClickListener(v -> loadLiveWeatherAndRates());

        rootView.findViewById(R.id.btn_fx_preset_cycle).setOnClickListener(v -> {
            presetAmountIdx = (presetAmountIdx + 1) % PRESET_AMOUNTS.length;
            etFxAmount.setText(PRESET_AMOUNTS[presetAmountIdx]);
            etFxAmount.setSelection(etFxAmount.getText().length());
        });

        tabLive.setOnClickListener(v -> switchTab(0));
        tabPassport.setOnClickListener(v -> switchTab(1));
        tabBudget.setOnClickListener(v -> switchTab(2));
        tabPacking.setOnClickListener(v -> switchTab(3));

        etFxAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updateFxConversion(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        setupBudgetHandlers();
        setupPackingHandlers();
        switchTab(0);
        loadLiveWeatherAndRates();
        return rootView;
    }

    private void switchTab(int tab) {
        activeTab = tab;
        panelLive.setVisibility(tab == 0 ? View.VISIBLE : View.GONE);
        panelPassport.setVisibility(tab == 1 ? View.VISIBLE : View.GONE);
        panelBudget.setVisibility(tab == 2 ? View.VISIBLE : View.GONE);
        panelPacking.setVisibility(tab == 3 ? View.VISIBLE : View.GONE);

        styleTab(tabLive, tab == 0);
        styleTab(tabPassport, tab == 1);
        styleTab(tabBudget, tab == 2);
        styleTab(tabPacking, tab == 3);

        if (tab == 0) renderLivePanel();
        else if (tab == 1) renderPassportPanel();
        else if (tab == 2) renderBudgetPanel();
        else renderPackingPanel();
    }

    private void styleTab(TextView tv, boolean sel) {
        tv.setBackgroundResource(sel ? R.drawable.bg_chip_sky_selected : R.drawable.bg_chip_unselected);
        tv.setTextColor(requireContext().getColor(sel ? R.color.white : R.color.text_primary));
    }

    private void loadLiveWeatherAndRates() {
        TravelDataService.CityCoord city = TravelDataService.cities.get(selectedCityIdx);
        TravelDataService.fetchWeatherAsync(city, info -> {
            if (!isAdded() || rootView == null) return;
            TextView tvEmoji = rootView.findViewById(R.id.tv_weather_emoji);
            TextView tvHead = rootView.findViewById(R.id.tv_weather_headline);
            TextView tvDet = rootView.findViewById(R.id.tv_weather_details);
            TextView tvAdv = rootView.findViewById(R.id.tv_weather_advice);
            TextView tvSrc = rootView.findViewById(R.id.tv_weather_source);
            LinearLayout forecastBox = rootView.findViewById(R.id.container_weather_forecast);

            tvEmoji.setText(TravelDataService.weatherEmoji(info.weatherCode));
            tvHead.setText(String.format(Locale.US, "%.0f°C", info.temperatureC));
            tvDet.setText(String.format(Locale.US,
                    "%s • feels %.0f°\n💧 %d%%   💨 %.0f km/h   ☀️ UV %.1f",
                    TravelDataService.weatherLabel(info.weatherCode),
                    info.feelsLikeC, info.humidityPercent, info.windSpeedKmh, info.uvIndex));
            tvAdv.setText(TravelDataService.travelAdvice(info));
            tvSrc.setText(info.isLive
                    ? "Source: Open-Meteo (live)"
                    : "Source: Seasonal tropical baseline (offline)");

            forecastBox.removeAllViews();
            float d = getResources().getDisplayMetrics().density;
            for (int i = 0; i < info.forecast3Days.size(); i++) {
                TravelDataService.DailyForecast df = info.forecast3Days.get(i);
                LinearLayout dayCard = new LinearLayout(requireContext());
                dayCard.setOrientation(LinearLayout.VERTICAL);
                dayCard.setGravity(Gravity.CENTER_HORIZONTAL);
                dayCard.setBackgroundResource(R.drawable.bg_lavender_card);
                int padH = (int) (12 * d);
                int padV = (int) (10 * d);
                dayCard.setPadding(padH, padV, padH, padV);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        (int) (88 * d), ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.setMarginEnd((int) (8 * d));
                dayCard.setLayoutParams(lp);

                TextView tvDate = new TextView(requireContext());
                tvDate.setText(i == 0 ? Localized.tr("Hôm nay", "Today") : df.date);
                tvDate.setTextSize(11f);
                tvDate.setTypeface(null, Typeface.BOLD);
                tvDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
                dayCard.addView(tvDate);

                TextView tvIcon = new TextView(requireContext());
                tvIcon.setText(TravelDataService.weatherEmoji(df.weatherCode));
                tvIcon.setTextSize(20f);
                tvIcon.setPadding(0, (int) (4 * d), 0, (int) (4 * d));
                dayCard.addView(tvIcon);

                TextView tvMinMax = new TextView(requireContext());
                tvMinMax.setText(String.format(Locale.US, "%.0f° / %.0f°", df.maxTempC, df.minTempC));
                tvMinMax.setTextSize(11.5f);
                tvMinMax.setTypeface(null, Typeface.BOLD);
                tvMinMax.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
                dayCard.addView(tvMinMax);

                TextView tvRain = new TextView(requireContext());
                tvRain.setText("☔ " + df.rainChancePercent + "%");
                tvRain.setTextSize(10.5f);
                tvRain.setTextColor(ContextCompat.getColor(requireContext(), R.color.weather_city_blue));
                dayCard.addView(tvRain);

                forecastBox.addView(dayCard);
            }
        });

        TravelDataService.fetchRatesAsync(requireContext(), res -> {
            if (!isAdded() || rootView == null) return;
            activeRates.clear();
            activeRates.putAll(res.vndPerUnit);
            ratesSourceLabel = (res.isLive ? "🟢 Live rates (open.er-api.com • " : "📦 SQLite Cache (") + res.updatedDate + ")";
            updateFxConversion();
        });
    }

    private void renderLivePanel() {
        LinearLayout cityRow = rootView.findViewById(R.id.container_weather_cities);
        LinearLayout currRow = rootView.findViewById(R.id.container_currency_chips);

        cityRow.removeAllViews();
        String[] cityIcons = {"🏙️ ", "🌊 ", "🌳 ", "🌿 "};
        for (int i = 0; i < TravelDataService.cities.size(); i++) {
            final int idx = i;
            TravelDataService.CityCoord c = TravelDataService.cities.get(i);
            String prefix = cityIcons[i % cityIcons.length];
            cityRow.addView(ChipViewFactory.createSkyChip(
                    requireContext(),
                    prefix + Localized.tr(c.nameVi, c.nameEn),
                    idx == selectedCityIdx,
                    () -> {
                        selectedCityIdx = idx;
                        renderLivePanel();
                        loadLiveWeatherAndRates();
                    }
            ));
        }

        currRow.removeAllViews();
        for (String code : activeRates.keySet()) {
            String flag = TravelDataService.currencyFlags.getOrDefault(code, "💱");
            currRow.addView(ChipViewFactory.createFxChip(
                    requireContext(),
                    flag + " " + code,
                    code.equals(selectedCurrency),
                    () -> {
                        selectedCurrency = code;
                        renderLivePanel();
                    }
            ));
        }
        updateFxConversion();
    }

    private void updateFxConversion() {
        if (!isAdded() || rootView == null) return;
        TextView tvRes = rootView.findViewById(R.id.tv_fx_result);
        TextView tvNote = rootView.findViewById(R.id.tv_fx_rate_note);
        TextView tvSrc = rootView.findViewById(R.id.tv_fx_source_status);

        double amount = 100.0;
        try {
            amount = Double.parseDouble(etFxAmount.getText().toString().trim());
        } catch (Exception ignored) {}

        double rate = activeRates.getOrDefault(selectedCurrency, 25450.0);
        long vnd = Math.round(amount * rate);
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        tvRes.setText("= " + nf.format(vnd) + "₫");
        tvNote.setText("1 " + selectedCurrency + " = " + nf.format(Math.round(rate)) + "₫  •  (" + nf.format(amount) + " " + selectedCurrency + ")");
        if (tvSrc != null) {
            tvSrc.setText(ratesSourceLabel);
        }
    }

    private void renderPassportPanel() {
        TextView tvRank = rootView.findViewById(R.id.tv_passport_rank);
        TextView tvProg = rootView.findViewById(R.id.tv_passport_progress);
        TextView tvRegs = rootView.findViewById(R.id.tv_passport_regions);
        LinearLayout stampsBox = rootView.findViewById(R.id.container_passport_stamps);

        List<CheckInStamp> stamps = TravelPassportManager.getStamps();
        ExplorerRank cur = TravelPassportManager.currentRank();
        ExplorerRank next = TravelPassportManager.nextRank();
        int[] reg = TravelPassportManager.regionalCounts();

        String scholarSuffix = UserDataBackupManager.hasCulturalScholarBadge()
                ? "  •  🎓 " + Localized.tr("Học Giả Sài Gòn", "Cultural Scholar")
                : "";
        tvRank.setText(cur.badgeEmoji + " " + Localized.tr(cur.titleVi, cur.titleEn) + scholarSuffix);
        tvProg.setText(next != null
                ? Localized.tr("Đã đóng " + stamps.size() + " dấu • Còn " + (next.minStamps - stamps.size()) + " điểm nữa lên hạng " + next.titleVi,
                        stamps.size() + " stamps collected • " + (next.minStamps - stamps.size()) + " more to reach " + next.titleEn)
                : Localized.tr("🏆 Đã đạt danh hiệu cao quý nhất!", "🏆 Maximum Explorer Rank achieved!"));
        tvRegs.setText("🏙️ TP.HCM: " + reg[0] + "   •   🏺 Bình Dương: " + reg[1] + "   •   🌊 Vũng Tàu: " + reg[2]);

        stampsBox.removeAllViews();
        AppNavigator nav = getNavigator();
        for (Attraction a : AttractionRepository.getAttractions()) {
            boolean checked = TravelPassportManager.isCheckedIn(a.id);
            SaigonTravelDatabaseHelper.JournalRow journal = TravelJournalManager.getJournalForAttraction(a.id);
            View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_admin_entry, stampsBox, false);
            TextView tvT = row.findViewById(R.id.tv_admin_entry_title);
            TextView tvS = row.findViewById(R.id.tv_admin_entry_sub);
            TextView btnEdit = row.findViewById(R.id.btn_admin_entry_edit);
            TextView btnDel = row.findViewById(R.id.btn_admin_entry_delete);

            tvT.setText((checked ? "✅ " : "⬜ ") + Localized.tr(a.nameVi, a.nameEn));
            tvS.setText(a.province + " • " + a.district + (journal != null ? " • " + journal.moodEmoji + " Đã viết nhật ký" : ""));

            btnDel.setVisibility(View.VISIBLE);
            btnDel.setText(journal != null ? journal.moodEmoji : "📸");
            btnDel.setBackgroundResource(R.drawable.bg_oauth_button);
            btnDel.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            btnDel.setOnClickListener(v -> ExperienceDialogs.showTravelJournalDialog(
                    requireContext(), a, nav, this::renderPassportPanel));

            btnEdit.setText(checked ? "Đã đóng dấu" : "🛂 Check-in");
            btnEdit.setOnClickListener(v -> {
                ResultBox<Boolean> res = TravelPassportManager.toggleCheckIn(a);
                if (!res.isSuccess()) {
                    if (nav != null) nav.showPermissionRequiredDialog(res.getErrorMessage());
                    return;
                }
                renderPassportPanel();
            });
            stampsBox.addView(row);
        }
    }

    private void setupBudgetHandlers() {
        EditText etTitle = rootView.findViewById(R.id.et_expense_title);
        EditText etAmount = rootView.findViewById(R.id.et_expense_amount);

        rootView.findViewById(R.id.btn_group_minus).setOnClickListener(v -> {
            groupSize = Math.max(1, groupSize - 1);
            renderBudgetPanel();
        });
        rootView.findViewById(R.id.btn_group_plus).setOnClickListener(v -> {
            groupSize = Math.min(20, groupSize + 1);
            renderBudgetPanel();
        });
        rootView.findViewById(R.id.btn_edit_budget).setOnClickListener(v -> {
            EditText input = new EditText(requireContext());
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            input.setText(String.valueOf(TripExpenseManager.getBudgetVnd()));
            new AlertDialog.Builder(requireContext())
                    .setTitle(Localized.tr("Đặt hạn mức Ngân sách (VNĐ)", "Set Trip Budget (VND)"))
                    .setView(input)
                    .setPositiveButton("Lưu", (d, w) -> {
                        try {
                            long val = Long.parseLong(input.getText().toString().trim());
                            TripExpenseManager.setBudget(val);
                            renderBudgetPanel();
                        } catch (Exception ignored) {}
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        rootView.findViewById(R.id.btn_add_expense).setOnClickListener(v -> {
            long amt = 0L;
            try {
                amt = Long.parseLong(etAmount.getText().toString().trim());
            } catch (Exception ignored) {}
            ResultBox<TripExpense> res = TripExpenseManager.addExpense(
                    etTitle.getText().toString(), amt, selectedExpenseCat);
            if (!res.isSuccess()) {
                Toast.makeText(requireContext(), res.getErrorMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            etTitle.setText("");
            etAmount.setText("");
            renderBudgetPanel();
        });
    }

    private void renderBudgetPanel() {
        TextView tvSum = rootView.findViewById(R.id.tv_budget_summary);
        TextView tvRem = rootView.findViewById(R.id.tv_budget_remaining);
        TextView tvSplit = rootView.findViewById(R.id.tv_group_split);
        LinearLayout catBox = rootView.findViewById(R.id.container_expense_categories);
        LinearLayout listBox = rootView.findViewById(R.id.container_expenses_list);

        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        long budget = TripExpenseManager.getBudgetVnd();
        long spent = TripExpenseManager.getTotalSpentVnd();
        long rem = TripExpenseManager.getRemainingBudgetVnd();
        long perPerson = TripExpenseManager.splitPerPerson(groupSize);

        tvSum.setText("💰 Đã chi: " + nf.format(spent) + "đ  /  Ngân sách: " + nf.format(budget) + "đ");
        tvRem.setText((rem >= 0 ? "🟢 Còn dư: " : "🔴 Vượt hạn mức: ") + nf.format(Math.abs(rem)) + "đ");
        tvSplit.setText("👥 Chia nhóm " + groupSize + " người: " + nf.format(perPerson) + "đ / người");

        catBox.removeAllViews();
        for (ExpenseCategory c : ExpenseCategory.values()) {
            catBox.addView(ChipViewFactory.createChip(
                    requireContext(),
                    c.emoji + " " + Localized.tr(c.labelVi, c.labelEn),
                    c == selectedExpenseCat,
                    0,
                    () -> {
                        selectedExpenseCat = c;
                        renderBudgetPanel();
                    }
            ));
        }

        listBox.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (TripExpense e : TripExpenseManager.getExpenses()) {
            View row = inflater.inflate(R.layout.item_expense, listBox, false);
            TextView tvT = row.findViewById(R.id.tv_exp_title);
            TextView tvC = row.findViewById(R.id.tv_exp_cat);
            TextView tvA = row.findViewById(R.id.tv_exp_amount);
            TextView btnDel = row.findViewById(R.id.btn_exp_delete);

            tvT.setText(e.category.emoji + " " + e.title);
            tvC.setText(Localized.tr(e.category.labelVi, e.category.labelEn));
            tvA.setText(nf.format(e.amountVnd) + "đ");
            btnDel.setOnClickListener(v -> {
                TripExpenseManager.deleteExpense(e.id);
                renderBudgetPanel();
            });
            listBox.addView(row);
        }
    }

    private void setupPackingHandlers() {
        EditText etCustom = rootView.findViewById(R.id.et_packing_custom);
        rootView.findViewById(R.id.btn_packing_add).setOnClickListener(v -> {
            if (!PackingListManager.addCustomItem(selectedPackingPreset, etCustom.getText().toString())) {
                Toast.makeText(requireContext(),
                        Localized.tr("Vui lòng nhập tên vật dụng", "Please enter an item name"),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            etCustom.setText("");
            renderPackingPanel();
        });

        rootView.findViewById(R.id.btn_packing_share).setOnClickListener(v -> MapLauncher.shareText(
                requireContext(),
                "Packing Checklist",
                PackingListManager.formatChecklistSummary(selectedPackingPreset)
        ));
    }

    private void renderPackingPanel() {
        if (!isAdded() || rootView == null) return;
        LinearLayout presetBox = rootView.findViewById(R.id.container_packing_presets);
        TextView tvProg = rootView.findViewById(R.id.tv_packing_progress_label);
        ProgressBar pb = rootView.findViewById(R.id.pb_packing_progress);
        LinearLayout itemsBox = rootView.findViewById(R.id.container_packing_items);

        presetBox.removeAllViews();
        for (PackingListManager.PresetRegion pr : PackingListManager.PresetRegion.values()) {
            presetBox.addView(ChipViewFactory.createSkyChip(
                    requireContext(),
                    Localized.tr(pr.labelVi, pr.labelEn),
                    pr == selectedPackingPreset,
                    () -> {
                        selectedPackingPreset = pr;
                        PackingListManager.ensurePresetLoaded(pr);
                        renderPackingPanel();
                    }
            ));
        }

        List<SaigonTravelDatabaseHelper.PackingRow> list = PackingListManager.getItemsByPreset(selectedPackingPreset);
        int pct = PackingListManager.getCompletionPercent(selectedPackingPreset);
        tvProg.setText(Localized.tr(
                "📊 Đã soạn xong: " + pct + "% (" + list.size() + " món trong SQLite)",
                "📊 Packed: " + pct + "% (" + list.size() + " items in SQLite)"));
        pb.setProgress(pct);

        itemsBox.removeAllViews();
        float d = getResources().getDisplayMetrics().density;
        for (SaigonTravelDatabaseHelper.PackingRow r : list) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackgroundResource(R.drawable.bg_card_rounded);
            int p = (int) (12 * d);
            row.setPadding(p, p, p, p);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = (int) (7 * d);
            row.setLayoutParams(lp);

            TextView tvCheck = new TextView(requireContext());
            tvCheck.setText((r.isChecked ? "✅  " : "⬜  ") + Localized.tr(r.titleVi, r.titleEn));
            tvCheck.setTextColor(ContextCompat.getColor(requireContext(),
                    r.isChecked ? R.color.saigon_teal : R.color.text_primary));
            tvCheck.setTextSize(13.5f);
            tvCheck.setTypeface(null, r.isChecked ? Typeface.NORMAL : Typeface.BOLD);
            tvCheck.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            tvCheck.setOnClickListener(v -> {
                PackingListManager.toggleItem(r.id);
                renderPackingPanel();
            });
            row.addView(tvCheck);

            TextView btnDel = new TextView(requireContext());
            btnDel.setText("✕");
            btnDel.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_muted));
            btnDel.setTextSize(13f);
            btnDel.setPadding((int) (8 * d), (int) (4 * d), (int) (4 * d), (int) (4 * d));
            btnDel.setOnClickListener(v -> {
                PackingListManager.deleteItem(r.id);
                renderPackingPanel();
            });
            row.addView(btnDel);

            itemsBox.addView(row);
        }
    }

    private AppNavigator getNavigator() {
        return getActivity() instanceof AppNavigator ? (AppNavigator) getActivity() : null;
    }
}
