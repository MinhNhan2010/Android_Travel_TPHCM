package com.example.nhom_15_dulichtphcm.ui.screens;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom_15_dulichtphcm.R;
import com.example.nhom_15_dulichtphcm.data.model.AdministrativeDistrict;
import com.example.nhom_15_dulichtphcm.data.model.Attraction;
import com.example.nhom_15_dulichtphcm.data.model.LatLngPoint;
import com.example.nhom_15_dulichtphcm.data.model.PublicUtility;
import com.example.nhom_15_dulichtphcm.data.model.Restaurant;
import com.example.nhom_15_dulichtphcm.data.model.SpecialtyFood;
import com.example.nhom_15_dulichtphcm.data.remote.TravelDataService;
import com.example.nhom_15_dulichtphcm.data.repository.AdministrativeDistrictRepository;
import com.example.nhom_15_dulichtphcm.data.repository.AttractionRepository;
import com.example.nhom_15_dulichtphcm.data.repository.FoodRepository;
import com.example.nhom_15_dulichtphcm.data.repository.UtilityRepository;
import com.example.nhom_15_dulichtphcm.ui.AppNavigator;
import com.example.nhom_15_dulichtphcm.ui.components.MapHtmlBuilder;
import com.example.nhom_15_dulichtphcm.util.AppLanguage;
import com.example.nhom_15_dulichtphcm.util.LanguageManager;
import com.example.nhom_15_dulichtphcm.util.Localized;
import com.example.nhom_15_dulichtphcm.util.LocationHelper;
import com.example.nhom_15_dulichtphcm.util.MapLauncher;
import com.example.nhom_15_dulichtphcm.util.ThemeManager;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Pure Java + XML Modern Interactive City Map (CityMapFragment) matching the original CityMapScreen layout:
 * - Dedicated Map Header ("Bản đồ Du lịch Hiện đại", synced pins counter, Auto/Manual sync toggle & Refresh)
 * - 6 Layer Filter Chips (Tất cả, Mới cập nhật, Địa điểm, Quán ăn, Tiện ích, Địa giới 2025)
 * - 3 Region Quick-Fly Pills (TP. Hồ Chí Minh, Bình Dương, Vũng Tàu)
 * - Rounded 16dp Leaflet + MarkerCluster WebView with 4 Tile Modes (Google Maps / Giao thông / Vệ tinh / Ban đêm),
 *   Top-Right Pin Legend, and Bottom-Right GPS My-Location button
 * - Bottom Selected Place / District Detail Card + Horizontal Quick-Pick Place Carousel.
 */
public final class CityMapFragment extends Fragment {

    private static final String ARG_LAT = "focus_lat";
    private static final String ARG_LNG = "focus_lng";
    private static final String ARG_TITLE = "focus_title";

    public static CityMapFragment newInstance(double lat, double lng, String title) {
        CityMapFragment f = new CityMapFragment();
        Bundle b = new Bundle();
        b.putDouble(ARG_LAT, lat);
        b.putDouble(ARG_LNG, lng);
        b.putString(ARG_TITLE, title);
        f.setArguments(b);
        return f;
    }

    private String filterMode = "ALL";
    private String tileStyle = "voyager";
    private double centerLat = 10.7769;
    private double centerLng = 106.7009;
    private int zoomLevel = 13;

    private Double userGpsLat = null;
    private Double userGpsLng = null;
    private boolean isSyncingPlaces = false;
    private String syncStatusNote = null;
    private String activeRouteSummary = null;

    private final List<UnifiedMapPlace> allPlaces = new ArrayList<>();
    private final List<UnifiedMapPlace> filteredPlaces = new ArrayList<>();
    private UnifiedMapPlace selectedPlace = null;
    private AdministrativeDistrict selectedDistrict = null;

    private View rootView;
    private WebView webView;
    private TextView tvHeaderTitle;
    private TextView tvSyncStatus;
    private TextView btnAutoSync;
    private TextView btnSyncNow;
    private LinearLayout chipsContainer;
    private TextView tvFlyLabel;
    private TextView tvLegend;
    private TextView btnTileVoyager;
    private TextView btnTileTraffic;
    private TextView btnTileSatellite;
    private TextView btnTileDark;
    private ImageView ivMyGps;

    private View cardSelection;
    private TextView tvSelIcon;
    private TextView tvSelTitle;
    private TextView tvSelSub;
    private TextView tvSelAddress;
    private TextView tvSelCoords;
    private TextView btnRouteHere;
    private TextView btnGoogleNav;
    private TextView btnOpenDetail;

    private View cardDistrict;
    private TextView tvDistTitle;
    private TextView tvDistSub;
    private TextView tvDistZoneBadge;
    private TextView tvDistMergerBox;
    private TextView tvDistStats;

    private TextView tvCarouselLabel;
    private RecyclerView rvQuickPlaces;
    private QuickChipAdapter quickChipAdapter;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<String[]> gpsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), r -> locateUserGps());

    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_city_map, container, false);

        Double initialFocusLat = null;
        Double initialFocusLng = null;
        if (getArguments() != null && getArguments().containsKey(ARG_LAT)) {
            centerLat = getArguments().getDouble(ARG_LAT, 10.7769);
            centerLng = getArguments().getDouble(ARG_LNG, 106.7009);
            initialFocusLat = centerLat;
            initialFocusLng = centerLng;
            zoomLevel = 16;
        }

        tvHeaderTitle = rootView.findViewById(R.id.tv_map_header_title);
        tvSyncStatus = rootView.findViewById(R.id.tv_map_sync_status);
        btnAutoSync = rootView.findViewById(R.id.btn_map_auto_sync);
        btnSyncNow = rootView.findViewById(R.id.btn_map_sync);
        chipsContainer = rootView.findViewById(R.id.container_map_chips);
        tvFlyLabel = rootView.findViewById(R.id.tv_map_fly_label);

        View mapViewport = rootView.findViewById(R.id.container_map_viewport);
        float d = getResources().getDisplayMetrics().density;
        mapViewport.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), 16f * d);
            }
        });
        mapViewport.setClipToOutline(true);

        webView = rootView.findViewById(R.id.webview_city_map);
        tvLegend = rootView.findViewById(R.id.tv_map_legend);
        btnTileVoyager = rootView.findViewById(R.id.btn_tile_voyager);
        btnTileTraffic = rootView.findViewById(R.id.btn_tile_traffic);
        btnTileSatellite = rootView.findViewById(R.id.btn_tile_satellite);
        btnTileDark = rootView.findViewById(R.id.btn_tile_dark);
        ivMyGps = rootView.findViewById(R.id.iv_map_my_gps);

        cardSelection = rootView.findViewById(R.id.card_map_selection);
        tvSelIcon = rootView.findViewById(R.id.tv_map_sel_icon);
        tvSelTitle = rootView.findViewById(R.id.tv_map_sel_title);
        tvSelSub = rootView.findViewById(R.id.tv_map_sel_subtitle);
        tvSelAddress = rootView.findViewById(R.id.tv_map_sel_address);
        tvSelCoords = rootView.findViewById(R.id.tv_map_sel_coords);
        btnRouteHere = rootView.findViewById(R.id.btn_map_route_here);
        btnGoogleNav = rootView.findViewById(R.id.btn_map_google_nav);
        btnOpenDetail = rootView.findViewById(R.id.btn_map_open_detail);

        cardDistrict = rootView.findViewById(R.id.card_map_district);
        tvDistTitle = rootView.findViewById(R.id.tv_dist_title);
        tvDistSub = rootView.findViewById(R.id.tv_dist_subtitle);
        tvDistZoneBadge = rootView.findViewById(R.id.tv_dist_zone_badge);
        tvDistMergerBox = rootView.findViewById(R.id.tv_dist_merger_box);
        tvDistStats = rootView.findViewById(R.id.tv_dist_stats);

        tvCarouselLabel = rootView.findViewById(R.id.tv_map_carousel_label);
        rvQuickPlaces = rootView.findViewById(R.id.rv_map_quick_places);
        rvQuickPlaces.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        quickChipAdapter = new QuickChipAdapter();
        rvQuickPlaces.setAdapter(quickChipAdapter);

        // Configure WebView
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(true);
        ws.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new MapBridge(), "AndroidBridge");

        // Auto-sync toggle & manual sync
        btnAutoSync.setOnClickListener(v -> {
            boolean next = !AttractionRepository.isAutoSyncEnabled();
            AttractionRepository.setAutoSyncPreference(next);
            renderControlsAndBottomPanel();
            if (next) {
                triggerLiveSync("ALL", true);
            }
        });
        btnSyncNow.setOnClickListener(v -> triggerLiveSync("ALL", true));

        // Region Quick-Fly buttons
        rootView.findViewById(R.id.btn_fly_hcm).setOnClickListener(v -> onRegionFlyClicked("TP. Hồ Chí Minh", 10.7769, 106.7009, 13));
        rootView.findViewById(R.id.btn_fly_bd).setOnClickListener(v -> onRegionFlyClicked("Bình Dương", 10.9806, 106.6542, 12));
        rootView.findViewById(R.id.btn_fly_vt).setOnClickListener(v -> onRegionFlyClicked("Vũng Tàu", 10.3460, 107.0844, 13));

        // 4 Tile Style Switcher buttons
        btnTileVoyager.setOnClickListener(v -> switchTileStyle("voyager"));
        btnTileTraffic.setOnClickListener(v -> switchTileStyle("traffic"));
        btnTileSatellite.setOnClickListener(v -> switchTileStyle("satellite"));
        btnTileDark.setOnClickListener(v -> switchTileStyle("dark"));

        // GPS button
        rootView.findViewById(R.id.btn_map_my_gps).setOnClickListener(v -> {
            if (LocationHelper.hasPermission(requireContext())) {
                locateUserGps();
            } else {
                gpsLauncher.launch(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                });
            }
        });

        // Copy GPS coordinates
        rootView.findViewById(R.id.btn_map_copy_coords).setOnClickListener(v -> {
            if (selectedPlace == null) return;
            String coordText = String.format(Locale.US, "%.5f, %.5f", selectedPlace.latitude, selectedPlace.longitude);
            ClipboardManager cm = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText("GPS Coordinates", coordText));
                Toast.makeText(requireContext(),
                        Localized.tr("Đã chép tọa độ GPS: " + coordText, "Copied GPS: " + coordText),
                        Toast.LENGTH_SHORT).show();
            }
        });

        // 3 Action Buttons on Selected Place Card
        styleActionButtons();
        btnRouteHere.setOnClickListener(v -> drawOsrmRouteToSelection());
        btnGoogleNav.setOnClickListener(v -> {
            if (selectedPlace != null) {
                MapLauncher.openInGoogleMaps(requireContext(),
                        selectedPlace.latitude, selectedPlace.longitude, selectedPlace.title);
            }
        });
        btnOpenDetail.setOnClickListener(v -> {
            if (selectedPlace == null) return;
            AppNavigator nav = getNavigator();
            if (nav == null) return;
            if ("attraction".equals(selectedPlace.kind)) {
                nav.openAttractionDetail(selectedPlace.rawId);
            } else if ("restaurant".equals(selectedPlace.kind) && selectedPlace.foodId != null) {
                nav.openFoodDetail(selectedPlace.foodId);
            } else {
                MapLauncher.openInGoogleMaps(requireContext(),
                        selectedPlace.latitude, selectedPlace.longitude, selectedPlace.title);
            }
        });

        List<AdministrativeDistrict> dists = AdministrativeDistrictRepository.getDistricts();
        if (!dists.isEmpty()) {
            selectedDistrict = dists.get(0);
        }

        rebuildPlacesData(initialFocusLat, initialFocusLng);
        renderControlsAndBottomPanel();
        reloadMapHtml();

        if (AttractionRepository.shouldAutoSyncNow() || AttractionRepository.getAutoSyncedCount() == 0) {
            triggerLiveSync("ALL", AttractionRepository.getAutoSyncedCount() == 0);
        }

        return rootView;
    }

    private void rebuildPlacesData(@Nullable Double focusLat, @Nullable Double focusLng) {
        allPlaces.clear();
        boolean isVi = LanguageManager.getCurrentLanguage() == AppLanguage.VIETNAMESE;

        for (Attraction a : AttractionRepository.getAttractions()) {
            boolean isNew = a.id != null && a.id.startsWith("sync_");
            allPlaces.add(new UnifiedMapPlace(
                    "attraction_" + a.id,
                    a.id,
                    null,
                    "attraction",
                    isVi ? a.nameVi : a.nameEn,
                    isVi ? (a.nameEn + " • " + a.district) : (a.nameVi + " • " + a.district),
                    a.address != null && !a.address.isEmpty() ? a.address : (a.district + ", " + a.province),
                    a.latitude,
                    a.longitude,
                    isNew
            ));
        }

        for (SpecialtyFood f : FoodRepository.getSpecialtyFoods()) {
            String foodTitle = isVi ? f.nameVi : f.nameEn;
            for (Restaurant r : f.restaurants) {
                allPlaces.add(new UnifiedMapPlace(
                        "restaurant_" + r.id,
                        r.id,
                        f.id,
                        "restaurant",
                        r.name,
                        foodTitle + " • " + r.district,
                        r.address,
                        r.latitude,
                        r.longitude,
                        false
                ));
            }
        }

        for (PublicUtility u : UtilityRepository.getUtilities()) {
            allPlaces.add(new UnifiedMapPlace(
                    "utility_" + u.id,
                    u.id,
                    null,
                    "utility",
                    isVi ? u.nameVi : u.nameEn,
                    u.category.iconEmoji + " " + u.district + " • " + u.price,
                    u.address,
                    u.latitude,
                    u.longitude,
                    false
            ));
        }

        applyFilterList();

        if (focusLat != null && focusLng != null) {
            for (UnifiedMapPlace p : allPlaces) {
                if (Math.abs(p.latitude - focusLat) < 0.0003 && Math.abs(p.longitude - focusLng) < 0.0003) {
                    selectedPlace = p;
                    break;
                }
            }
        }
        if (selectedPlace != null) {
            String prevKey = selectedPlace.uniqueKey;
            UnifiedMapPlace refreshed = null;
            for (UnifiedMapPlace p : allPlaces) {
                if (p.uniqueKey.equals(prevKey)) {
                    refreshed = p;
                    break;
                }
            }
            selectedPlace = refreshed;
        }
        if (selectedPlace == null && !filteredPlaces.isEmpty()) {
            selectedPlace = filteredPlaces.get(0);
        } else if (selectedPlace == null && !allPlaces.isEmpty()) {
            selectedPlace = allPlaces.get(0);
        }
    }

    private void applyFilterList() {
        filteredPlaces.clear();
        for (UnifiedMapPlace p : allPlaces) {
            if ("ALL".equals(filterMode)) {
                filteredPlaces.add(p);
            } else if ("NEW_UPDATES".equals(filterMode) && p.isNewlySynced) {
                filteredPlaces.add(p);
            } else if ("ATTRACTIONS".equals(filterMode) && "attraction".equals(p.kind)) {
                filteredPlaces.add(p);
            } else if ("RESTAURANTS".equals(filterMode) && "restaurant".equals(p.kind)) {
                filteredPlaces.add(p);
            } else if ("UTILITIES".equals(filterMode) && "utility".equals(p.kind)) {
                filteredPlaces.add(p);
            }
        }
    }

    private void triggerLiveSync(String regionKey, boolean force) {
        if (!isAdded() || isSyncingPlaces) return;
        if (!force && !AttractionRepository.shouldAutoSyncNow()) return;

        isSyncingPlaces = true;
        btnSyncNow.setText("⏳");
        tvSyncStatus.setText(Localized.tr("Đang đồng bộ địa điểm mới...", "Syncing latest places..."));

        TravelDataService.fetchLatestPlacesForRegionAsync(regionKey, res -> {
            if (!isAdded()) return;
            isSyncingPlaces = false;
            btnSyncNow.setText("↻");
            AttractionRepository.SyncMergeSummary summary =
                    AttractionRepository.mergeAutoSyncedPlaces(res.places, System.currentTimeMillis());

            if (summary.addedCount > 0) {
                syncStatusNote = Localized.tr(
                        "Đã thêm +" + summary.addedCount + " điểm mới • " + countTotalPins() + " ghim",
                        "Added +" + summary.addedCount + " new places • " + countTotalPins() + " pins");
                rebuildPlacesData(null, null);
                reloadMapHtml();
            } else {
                syncStatusNote = null;
                rebuildPlacesData(null, null);
            }
            renderControlsAndBottomPanel();
        });
    }

    private int countTotalPins() {
        int count = AttractionRepository.getAttractions().size()
                + UtilityRepository.getUtilities().size();
        for (SpecialtyFood f : FoodRepository.getSpecialtyFoods()) {
            count += f.restaurants.size();
        }
        return count;
    }

    private void onRegionFlyClicked(String regionName, double lat, double lng, int zoom) {
        centerLat = lat;
        centerLng = lng;
        zoomLevel = zoom;
        if ("DISTRICTS".equals(filterMode)) {
            for (AdministrativeDistrict d : AdministrativeDistrictRepository.getDistricts()) {
                if (("Bình Dương".equals(regionName) && d.nameVi.contains("Bình Dương"))
                        || ("Vũng Tàu".equals(regionName) && d.nameVi.contains("Vũng Tàu"))
                        || ("TP. Hồ Chí Minh".equals(regionName) && !d.nameVi.contains("Bình Dương") && !d.nameVi.contains("Vũng Tàu"))) {
                    selectedDistrict = d;
                    break;
                }
            }
            renderControlsAndBottomPanel();
            reloadMapHtml();
        } else {
            flyCamera(lat, lng, zoom);
            if (AttractionRepository.isAutoSyncEnabled()) {
                triggerLiveSync(regionName, false);
            }
        }
    }

    private void switchTileStyle(String nextStyle) {
        tileStyle = nextStyle;
        styleTileSwitcherButtons();
        if (webView != null) {
            webView.evaluateJavascript("setTileStyleJs('" + nextStyle + "');", null);
        }
    }

    private void styleActionButtons() {
        float d = getResources().getDisplayMetrics().density;
        GradientDrawable blueBg = new GradientDrawable();
        blueBg.setShape(GradientDrawable.RECTANGLE);
        blueBg.setCornerRadius(10f * d);
        blueBg.setColor(0xFF2563EB);
        btnRouteHere.setBackground(blueBg);
        btnRouteHere.setTextColor(Color.WHITE);

        GradientDrawable coralBg = new GradientDrawable();
        coralBg.setShape(GradientDrawable.RECTANGLE);
        coralBg.setCornerRadius(10f * d);
        coralBg.setColor(0xFFFF8A65);
        btnGoogleNav.setBackground(coralBg);
        btnGoogleNav.setTextColor(0xFF1E293B);

        GradientDrawable tealBg = new GradientDrawable();
        tealBg.setShape(GradientDrawable.RECTANGLE);
        tealBg.setCornerRadius(10f * d);
        tealBg.setColor(0xFF4DB6AC);
        btnOpenDetail.setBackground(tealBg);
        btnOpenDetail.setTextColor(0xFF0F172A);
    }

    private void styleAutoSyncPill() {
        boolean auto = AttractionRepository.isAutoSyncEnabled();
        boolean darkActive = ThemeManager.isDarkActive(requireContext());
        float d = getResources().getDisplayMetrics().density;

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(12f * d);
        if (auto) {
            bg.setColor(darkActive ? 0xFFECFDF5 : 0xFFECFDF5);
            bg.setStroke((int) Math.max(1, d), 0xFF10B981);
            btnAutoSync.setTextColor(0xFF059669);
            btnAutoSync.setText(Localized.tr("🔄 Tự động", "🔄 Auto"));
        } else {
            bg.setColor(ContextCompat.getColor(requireContext(), R.color.surface_subtle));
            bg.setStroke((int) Math.max(1, d), ContextCompat.getColor(requireContext(), R.color.input_stroke));
            btnAutoSync.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            btnAutoSync.setText(Localized.tr("🔄 Thủ công", "🔄 Manual"));
        }
        btnAutoSync.setBackground(bg);
    }

    private void styleTileSwitcherButtons() {
        TextView[] btns = {btnTileVoyager, btnTileTraffic, btnTileSatellite, btnTileDark};
        String[] keys = {"voyager", "traffic", "satellite", "dark"};
        String[] viLabels = {"Google Maps", "Giao thông", "Vệ tinh", "Ban đêm"};
        String[] enLabels = {"Google Maps", "Traffic", "Satellite", "Dark"};

        float d = getResources().getDisplayMetrics().density;
        for (int i = 0; i < btns.length; i++) {
            boolean active = keys[i].equals(tileStyle);
            btns[i].setText(Localized.tr(viLabels[i], enLabels[i]));
            if (active) {
                GradientDrawable activeBg = new GradientDrawable();
                activeBg.setShape(GradientDrawable.RECTANGLE);
                activeBg.setCornerRadius(10f * d);
                activeBg.setColor(0xFFFF8A65);
                btns[i].setBackground(activeBg);
                btns[i].setTextColor(Color.WHITE);
            } else {
                btns[i].setBackground(null);
                btns[i].setTextColor(0xFF334155);
            }
        }
    }

    private void renderLegendText() {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        if ("DISTRICTS".equals(filterMode)) {
            appendColoredLegendItem(sb, "● ", 0xFF7C4DFF, Localized.tr("TP. Thủ Đức   ", "Thu Duc   "), 0xFF7C4DFF);
            appendColoredLegendItem(sb, "● ", 0xFFE53935, Localized.tr("Trung tâm", "Central"), 0xFFE53935);
        } else {
            appendColoredLegendItem(sb, "● ", 0xFF2563EB, Localized.tr("Điểm đến   ", "Places   "), 0xFF2563EB);
            appendColoredLegendItem(sb, "● ", 0xFFEA580C, Localized.tr("Quán ăn   ", "Food   "), 0xFFEA580C);
            appendColoredLegendItem(sb, "● ", 0xFF10B981, Localized.tr("Mới", "New"), 0xFF059669);
        }
        tvLegend.setText(sb);
    }

    private void appendColoredLegendItem(SpannableStringBuilder sb, String dot, int dotColor, String text, int textColor) {
        int s1 = sb.length();
        sb.append(dot);
        sb.setSpan(new ForegroundColorSpan(dotColor), s1, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        int s2 = sb.length();
        sb.append(text);
        sb.setSpan(new ForegroundColorSpan(textColor), s2, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void renderFilterChips() {
        chipsContainer.removeAllViews();
        int syncedCount = AttractionRepository.getAutoSyncedCount();
        String newUpdatesLabel = syncedCount > 0
                ? Localized.tr("Mới cập nhật (" + syncedCount + ")", "New (" + syncedCount + ")")
                : Localized.tr("Mới cập nhật", "New Updates");

        String[][] filters = new String[][]{
                {"ALL", Localized.tr("Tất cả", "All")},
                {"NEW_UPDATES", newUpdatesLabel},
                {"ATTRACTIONS", Localized.tr("Địa điểm", "Places")},
                {"RESTAURANTS", Localized.tr("Quán ăn", "Eateries")},
                {"UTILITIES", Localized.tr("Tiện ích", "Utilities")},
                {"DISTRICTS", Localized.tr("Địa giới 2025", "2025 Boundaries")}
        };

        float d = getResources().getDisplayMetrics().density;
        for (String[] f : filters) {
            final String code = f[0];
            boolean selected = code.equals(filterMode);

            TextView chip = new TextView(requireContext());
            chip.setText(f[1]);
            chip.setTextSize(12f);
            chip.setTypeface(null, android.graphics.Typeface.BOLD);
            int ph = (int) (13 * d);
            int pv = (int) (6.5f * d);
            chip.setPadding(ph, pv, ph, pv);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd((int) (7 * d));
            chip.setLayoutParams(lp);

            if (selected) {
                GradientDrawable selBg = new GradientDrawable();
                selBg.setShape(GradientDrawable.RECTANGLE);
                selBg.setCornerRadius(999f);
                if ("NEW_UPDATES".equals(code)) {
                    selBg.setColor(0xFF059669);
                } else if ("DISTRICTS".equals(code)) {
                    selBg.setColor(0xFF7C4DFF);
                } else {
                    selBg.setColor(0xFFD84315);
                }
                chip.setBackground(selBg);
                chip.setTextColor(Color.WHITE);
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_unselected);
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            }

            chip.setOnClickListener(v -> {
                filterMode = code;
                activeRouteSummary = null;
                applyFilterList();
                if ("DISTRICTS".equals(code) && selectedDistrict != null) {
                    centerLat = selectedDistrict.centerLat;
                    centerLng = selectedDistrict.centerLng;
                    zoomLevel = 12;
                } else if (!filteredPlaces.isEmpty()) {
                    selectedPlace = filteredPlaces.get(0);
                    centerLat = selectedPlace.latitude;
                    centerLng = selectedPlace.longitude;
                    zoomLevel = "NEW_UPDATES".equals(code) ? 12 : 13;
                }
                renderControlsAndBottomPanel();
                reloadMapHtml();
            });
            chipsContainer.addView(chip);
        }
    }

    private void renderControlsAndBottomPanel() {
        if (!isAdded() || rootView == null) return;

        tvHeaderTitle.setText(Localized.tr("Bản đồ Du lịch Hiện đại", "Modern Tourism Map"));
        int synced = AttractionRepository.getAutoSyncedCount();
        int totalPins = allPlaces.size();
        if (!isSyncingPlaces) {
            tvSyncStatus.setText(syncStatusNote != null
                    ? syncStatusNote
                    : Localized.tr(
                            "Đã đồng bộ " + synced + " điểm mới • " + totalPins + " ghim",
                            "Synced " + synced + " new places • " + totalPins + " pins"));
        }

        styleAutoSyncPill();
        renderFilterChips();
        tvFlyLabel.setText(Localized.tr("Bay tới vùng:", "Fly to:"));
        renderLegendText();
        styleTileSwitcherButtons();

        ivMyGps.setColorFilter(userGpsLat != null ? 0xFF2563EB : 0xFF475569);

        if ("DISTRICTS".equals(filterMode)) {
            cardSelection.setVisibility(View.GONE);
            cardDistrict.setVisibility(View.VISIBLE);
            if (selectedDistrict != null) {
                tvDistTitle.setText(Localized.tr(selectedDistrict.nameVi, selectedDistrict.nameEn));
                tvDistSub.setText(selectedDistrict.nameEn + " • " + selectedDistrict.type.displayNameVi);
                tvDistZoneBadge.setText(Localized.tr(selectedDistrict.zone.labelVi, selectedDistrict.zone.labelEn));
                tvDistMergerBox.setText("ℹ️ " + selectedDistrict.mergerStatus);
                tvDistStats.setText(String.format(Locale.US,
                        "📐 %.1f km²   •   👥 %s   •   🏘️ %d %s",
                        selectedDistrict.areaKm2,
                        selectedDistrict.population,
                        selectedDistrict.wardCount,
                        Localized.tr("phường/xã", "wards")));
            }
            tvCarouselLabel.setText(Localized.tr(
                    "Chọn khu vực hành chính sau sáp nhập:",
                    "Select a post-merger administrative district:"));
        } else {
            cardDistrict.setVisibility(View.GONE);
            cardSelection.setVisibility(View.VISIBLE);
            if (selectedPlace != null) {
                if ("restaurant".equals(selectedPlace.kind)) {
                    tvSelIcon.setText("🍴");
                } else if ("utility".equals(selectedPlace.kind)) {
                    tvSelIcon.setText("🏥");
                } else {
                    tvSelIcon.setText("📍");
                }
                tvSelTitle.setText(selectedPlace.title);
                tvSelSub.setText(selectedPlace.subtitle);
                tvSelAddress.setText(selectedPlace.address);

                String coordLine = String.format(Locale.US,
                        "📍 %s: %.5f° N, %.5f° E",
                        Localized.tr("Tọa độ GPS", "GPS Coords"),
                        selectedPlace.latitude,
                        selectedPlace.longitude);
                if (activeRouteSummary != null) {
                    coordLine = coordLine + "  •  " + activeRouteSummary;
                }
                tvSelCoords.setText(coordLine);

                btnRouteHere.setText(Localized.tr("➤  Chỉ đường", "➤  Route"));
                btnGoogleNav.setText(Localized.tr("Mở Maps", "Open Maps"));
                btnOpenDetail.setText("utility".equals(selectedPlace.kind)
                        ? Localized.tr("Định vị", "Locate")
                        : Localized.tr("Chi tiết", "Details"));
            }
            tvCarouselLabel.setText(Localized.tr(
                    "Chạm vào địa điểm để xem vị trí trên bản đồ:",
                    "Tap any place below to locate on map:"));
        }

        quickChipAdapter.notifyDataSetChanged();
    }

    private void reloadMapHtml() {
        if (!isAdded() || webView == null) return;
        String html = MapHtmlBuilder.buildHtml(
                AttractionRepository.getAttractions(),
                FoodRepository.getSpecialtyFoods(),
                UtilityRepository.getUtilities(),
                AdministrativeDistrictRepository.getDistricts(),
                filterMode,
                tileStyle,
                centerLat,
                centerLng,
                zoomLevel,
                selectedDistrict != null ? selectedDistrict.id : null
        );
        webView.loadDataWithBaseURL("https://tile.openstreetmap.org/", html, "text/html", "UTF-8", null);
    }

    private void flyCamera(double lat, double lng, int zoom) {
        centerLat = lat;
        centerLng = lng;
        zoomLevel = zoom;
        if (webView != null) {
            webView.evaluateJavascript(
                    String.format(Locale.US, "flyToPoint(%.5f, %.5f, %d);", lat, lng, zoom),
                    null
            );
        }
    }

    private void locateUserGps() {
        LocationHelper.fetchCurrentLocation(requireContext(), loc -> {
            if (!isAdded()) return;
            boolean realNear = (loc != null && LocationHelper.isNearHcmc(loc.getLatitude(), loc.getLongitude()));
            double lat = realNear ? loc.getLatitude() : LocationHelper.DEMO_LAT;
            double lng = realNear ? loc.getLongitude() : LocationHelper.DEMO_LNG;
            if (!realNear) {
                Toast.makeText(requireContext(),
                        Localized.tr("Đang hiển thị vị trí trung tâm Quận 1 (Chợ Bến Thành)",
                                "Showing District 1 center (Ben Thanh)"),
                        Toast.LENGTH_SHORT).show();
            }
            userGpsLat = lat;
            userGpsLng = lng;
            ivMyGps.setColorFilter(0xFF2563EB);
            if (webView != null) {
                webView.evaluateJavascript(String.format(Locale.US, "showUserGps(%.5f, %.5f);", lat, lng), null);
            }
        });
    }

    private void drawOsrmRouteToSelection() {
        if (selectedPlace == null) return;
        double fromLat = userGpsLat != null ? userGpsLat : LocationHelper.DEMO_LAT;
        double fromLng = userGpsLng != null ? userGpsLng : LocationHelper.DEMO_LNG;

        btnRouteHere.setText(Localized.tr("⏳ Đang tính...", "⏳ Routing..."));
        TravelDataService.fetchOsrmRouteAsync(
                fromLat, fromLng,
                selectedPlace.latitude, selectedPlace.longitude,
                route -> {
                    if (!isAdded() || route == null) return;
                    JSONArray arr = new JSONArray();
                    for (LatLngPoint pt : route.geometry) {
                        JSONArray pair = new JSONArray();
                        try {
                            pair.put(pt.latitude);
                            pair.put(pt.longitude);
                            arr.put(pair);
                        } catch (Exception ignored) {}
                    }
                    String escaped = arr.toString().replace("'", "\\'");
                    if (webView != null) {
                        webView.evaluateJavascript("drawRoute('" + escaped + "');", null);
                    }
                    activeRouteSummary = String.format(Locale.US, "🛣️ %.1f km (~%d %s)",
                            route.distanceKm, route.durationMinutes, Localized.tr("phút", "min"));
                    renderControlsAndBottomPanel();
                }
        );
    }

    public final class MapBridge {
        @JavascriptInterface
        public void onMarkerClicked(String kind, String id) {
            mainHandler.post(() -> {
                if (!isAdded()) return;
                String targetKey = kind + "_" + id;
                for (int i = 0; i < filteredPlaces.size(); i++) {
                    UnifiedMapPlace p = filteredPlaces.get(i);
                    if (p.uniqueKey.equals(targetKey) || p.rawId.equals(id)) {
                        selectedPlace = p;
                        activeRouteSummary = null;
                        renderControlsAndBottomPanel();
                        rvQuickPlaces.smoothScrollToPosition(i);
                        return;
                    }
                }
                for (UnifiedMapPlace p : allPlaces) {
                    if (p.uniqueKey.equals(targetKey) || p.rawId.equals(id)) {
                        selectedPlace = p;
                        activeRouteSummary = null;
                        renderControlsAndBottomPanel();
                        return;
                    }
                }
            });
        }

        @JavascriptInterface
        public void onDistrictClicked(String districtId) {
            mainHandler.post(() -> {
                if (!isAdded()) return;
                AdministrativeDistrict d = AdministrativeDistrictRepository.getById(districtId);
                if (d != null) {
                    selectedDistrict = d;
                    renderControlsAndBottomPanel();
                }
            });
        }
    }

    private final class QuickChipAdapter extends RecyclerView.Adapter<QuickChipAdapter.Holder> {

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView tv = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_map_place_chip, parent, false);
            return new Holder(tv);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            if ("DISTRICTS".equals(filterMode)) {
                List<AdministrativeDistrict> dists = AdministrativeDistrictRepository.getDistricts();
                AdministrativeDistrict dist = dists.get(position);
                boolean active = selectedDistrict != null && selectedDistrict.id.equals(dist.id);
                holder.chip.setText("🏛️ " + Localized.tr(dist.nameVi, dist.nameEn));
                bindChipAppearance(holder.chip, active, 0xFF7C4DFF);
                holder.chip.setOnClickListener(v -> {
                    selectedDistrict = dist;
                    centerLat = dist.centerLat;
                    centerLng = dist.centerLng;
                    zoomLevel = 13;
                    renderControlsAndBottomPanel();
                    reloadMapHtml();
                });
            } else {
                UnifiedMapPlace place = filteredPlaces.get(position);
                boolean active = selectedPlace != null && selectedPlace.uniqueKey.equals(place.uniqueKey);
                String prefix = "restaurant".equals(place.kind) ? "🍴 "
                        : ("utility".equals(place.kind) ? "🏥 " : "📍 ");
                holder.chip.setText(prefix + place.title);
                bindChipAppearance(holder.chip, active, 0xFFC6411C);
                holder.chip.setOnClickListener(v -> {
                    selectedPlace = place;
                    activeRouteSummary = null;
                    renderControlsAndBottomPanel();
                    if (webView != null) {
                        webView.evaluateJavascript("clearRoute();", null);
                        webView.evaluateJavascript(String.format(Locale.US,
                                "focusPlaceKey('%s', %.5f, %.5f);",
                                place.uniqueKey, place.latitude, place.longitude), null);
                    }
                });
            }
        }

        private void bindChipAppearance(TextView chip, boolean active, int solidColor) {
            if (active) {
                GradientDrawable bg = new GradientDrawable();
                bg.setShape(GradientDrawable.RECTANGLE);
                bg.setCornerRadius(999f);
                bg.setColor(solidColor);
                chip.setBackground(bg);
                chip.setTextColor(Color.WHITE);
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_unselected);
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            }
        }

        @Override
        public int getItemCount() {
            if ("DISTRICTS".equals(filterMode)) {
                return AdministrativeDistrictRepository.getDistricts().size();
            }
            return filteredPlaces.size();
        }

        final class Holder extends RecyclerView.ViewHolder {
            final TextView chip;

            Holder(@NonNull TextView itemView) {
                super(itemView);
                this.chip = itemView;
            }
        }
    }

    private static final class UnifiedMapPlace {
        final String uniqueKey;
        final String rawId;
        @Nullable final String foodId;
        final String kind;
        final String title;
        final String subtitle;
        final String address;
        final double latitude;
        final double longitude;
        final boolean isNewlySynced;

        UnifiedMapPlace(
                String uniqueKey,
                String rawId,
                @Nullable String foodId,
                String kind,
                String title,
                String subtitle,
                String address,
                double latitude,
                double longitude,
                boolean isNewlySynced
        ) {
            this.uniqueKey = uniqueKey;
            this.rawId = rawId;
            this.foodId = foodId;
            this.kind = kind;
            this.title = title != null ? title : "";
            this.subtitle = subtitle != null ? subtitle : "";
            this.address = address != null ? address : "";
            this.latitude = latitude;
            this.longitude = longitude;
            this.isNewlySynced = isNewlySynced;
        }
    }

    private AppNavigator getNavigator() {
        return getActivity() instanceof AppNavigator ? (AppNavigator) getActivity() : null;
    }
}
