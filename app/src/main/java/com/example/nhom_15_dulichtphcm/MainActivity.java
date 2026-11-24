package com.example.nhom_15_dulichtphcm;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.nhom_15_dulichtphcm.data.ai.GeminiService;
import com.example.nhom_15_dulichtphcm.data.db.SaigonTravelDatabaseHelper;
import com.example.nhom_15_dulichtphcm.data.local.AuthManager;
import com.example.nhom_15_dulichtphcm.data.local.FavoritesManager;
import com.example.nhom_15_dulichtphcm.data.local.PackingListManager;
import com.example.nhom_15_dulichtphcm.data.local.ReviewManager;
import com.example.nhom_15_dulichtphcm.data.local.TicketWalletManager;
import com.example.nhom_15_dulichtphcm.data.local.TravelJournalManager;
import com.example.nhom_15_dulichtphcm.data.local.TravelPassportManager;
import com.example.nhom_15_dulichtphcm.data.local.TripExpenseManager;
import com.example.nhom_15_dulichtphcm.data.local.TripPlanManager;
import com.example.nhom_15_dulichtphcm.data.repository.AttractionRepository;
import com.example.nhom_15_dulichtphcm.data.repository.FoodRepository;
import com.example.nhom_15_dulichtphcm.data.repository.UtilityRepository;
import com.example.nhom_15_dulichtphcm.ui.AppNavigator;
import com.example.nhom_15_dulichtphcm.ui.components.DialogHelper;
import com.example.nhom_15_dulichtphcm.ui.screens.AdminDashboardFragment;
import com.example.nhom_15_dulichtphcm.ui.screens.AiAssistantFragment;
import com.example.nhom_15_dulichtphcm.ui.screens.AttractionDetailFragment;
import com.example.nhom_15_dulichtphcm.ui.screens.AttractionListFragment;
import com.example.nhom_15_dulichtphcm.ui.screens.AuthProfileFragment;
import com.example.nhom_15_dulichtphcm.ui.screens.CityMapFragment;
import com.example.nhom_15_dulichtphcm.ui.screens.DiscoveryQuizFragment;
import com.example.nhom_15_dulichtphcm.ui.screens.FavoritesFragment;
import com.example.nhom_15_dulichtphcm.ui.screens.FoodDetailFragment;
import com.example.nhom_15_dulichtphcm.ui.screens.FoodListFragment;
import com.example.nhom_15_dulichtphcm.ui.screens.MetroWaterbusFragment;
import com.example.nhom_15_dulichtphcm.ui.screens.PhrasesFragment;
import com.example.nhom_15_dulichtphcm.ui.screens.PublicUtilitiesFragment;
import com.example.nhom_15_dulichtphcm.ui.screens.RideBookingFragment;
import com.example.nhom_15_dulichtphcm.ui.screens.SafetyHubFragment;
import com.example.nhom_15_dulichtphcm.ui.screens.TravelToolkitFragment;
import com.example.nhom_15_dulichtphcm.ui.screens.TripPlannerFragment;
import com.example.nhom_15_dulichtphcm.util.AppStrings;
import com.example.nhom_15_dulichtphcm.util.LanguageManager;
import com.example.nhom_15_dulichtphcm.util.SpeechHelper;
import com.example.nhom_15_dulichtphcm.util.ThemeManager;

/**
 * Pure Java MainActivity hosting XML layouts (res/layout/activity_main.xml)
 * and initializing SQLite database saigon_travel.db (SQLiteOpenHelper).
 */
public class MainActivity extends AppCompatActivity implements AppNavigator {

    private int currentTabIndex = 0;
    private SpeechHelper speechHelper;

    private View fabGuide;
    private View bottomNavBar;
    private TextView tvFabLabel;
    private View navPillAttractions;
    private View navPillFood;
    private View navPillUtilities;
    private View navPillPhrases;
    private View navPillMap;
    private TextView tvNavAttractions;
    private TextView tvNavFood;
    private TextView tvNavUtilities;
    private TextView tvNavPhrases;
    private TextView tvNavMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Initialize SQLite & Night/Light mode BEFORE super.onCreate() to avoid mid-launch recreate() race
        SaigonTravelDatabaseHelper.getInstance(this).getWritableDatabase();
        ThemeManager.initialize(this);
        LanguageManager.initialize(this);

        super.onCreate(savedInstanceState);

        AuthManager.initialize(this);
        ReviewManager.initialize(this);
        FavoritesManager.initialize(this);
        TripPlanManager.initialize(this);
        TravelPassportManager.initialize(this);
        TripExpenseManager.initialize(this);
        TicketWalletManager.initialize(this);
        TravelJournalManager.initialize(this);
        PackingListManager.initialize(this);
        AttractionRepository.initialize(this);
        FoodRepository.initialize(this);
        UtilityRepository.initialize(this);
        GeminiService.initialize(this);

        speechHelper = new SpeechHelper(this);

        setContentView(R.layout.activity_main);

        View rootMain = findViewById(R.id.root_main);
        ViewCompat.setOnApplyWindowInsetsListener(rootMain, (v, windowInsets) -> {
            Insets sysBars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            Insets ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            int bottomInset = Math.max(sysBars.bottom, ime.bottom);
            v.setPadding(sysBars.left, sysBars.top, sysBars.right, bottomInset);
            return windowInsets;
        });

        fabGuide = findViewById(R.id.fab_travel_guide);
        bottomNavBar = findViewById(R.id.bottom_nav_bar);
        tvFabLabel = findViewById(R.id.tv_fab_guide_label);
        navPillAttractions = findViewById(R.id.nav_pill_attractions);
        navPillFood = findViewById(R.id.nav_pill_food);
        navPillUtilities = findViewById(R.id.nav_pill_utilities);
        navPillPhrases = findViewById(R.id.nav_pill_phrases);
        navPillMap = findViewById(R.id.nav_pill_map);
        tvNavAttractions = findViewById(R.id.tv_nav_attractions);
        tvNavFood = findViewById(R.id.tv_nav_food);
        tvNavUtilities = findViewById(R.id.tv_nav_utilities);
        tvNavPhrases = findViewById(R.id.tv_nav_phrases);
        tvNavMap = findViewById(R.id.tv_nav_map);

        findViewById(R.id.nav_tab_attractions).setOnClickListener(v -> selectMainTab(0));
        findViewById(R.id.nav_tab_food).setOnClickListener(v -> selectMainTab(1));
        findViewById(R.id.nav_tab_utilities).setOnClickListener(v -> selectMainTab(2));
        findViewById(R.id.nav_tab_phrases).setOnClickListener(v -> selectMainTab(3));
        findViewById(R.id.nav_tab_map).setOnClickListener(v -> selectMainTab(4));
        fabGuide.setOnClickListener(v -> openAiAssistant());

        getSupportFragmentManager().addOnBackStackChangedListener(this::updateChromeVisibility);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                FragmentManager fm = getSupportFragmentManager();
                if (fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack();
                } else if (currentTabIndex != 0) {
                    selectMainTab(0);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        updateBottomNavLabels();
        if (savedInstanceState == null) {
            selectMainTab(0);
        } else {
            updateChromeVisibility();
        }
    }

    private void selectMainTab(int index) {
        currentTabIndex = index;
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        Fragment tabFragment;
        switch (index) {
            case 1:
                tabFragment = new FoodListFragment();
                break;
            case 2:
                tabFragment = new PublicUtilitiesFragment();
                break;
            case 3:
                tabFragment = new PhrasesFragment();
                break;
            case 4:
                tabFragment = new CityMapFragment();
                break;
            case 0:
            default:
                tabFragment = new AttractionListFragment();
                break;
        }

        fm.beginTransaction()
                .replace(R.id.fragment_container, tabFragment)
                .commit();
        updateBottomNavLabels();
        updateChromeVisibility();
    }

    private void pushSubScreen(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void updateChromeVisibility() {
        boolean isRootTab = getSupportFragmentManager().getBackStackEntryCount() == 0;
        fabGuide.setVisibility(isRootTab ? View.VISIBLE : View.GONE);
        if (bottomNavBar != null) {
            bottomNavBar.setVisibility(isRootTab ? View.VISIBLE : View.GONE);
        }
    }

    private void updateBottomNavLabels() {
        tvFabLabel.setText(AppStrings.get("ai_assistant_fab"));
        tvNavAttractions.setText(AppStrings.get("tab_places"));
        tvNavFood.setText(AppStrings.get("tab_food"));
        tvNavUtilities.setText(AppStrings.get("tab_utilities"));
        tvNavPhrases.setText(AppStrings.get("tab_phrases"));
        tvNavMap.setText(AppStrings.get("tab_map"));

        TextView[] labels = {tvNavAttractions, tvNavFood, tvNavUtilities, tvNavPhrases, tvNavMap};
        View[] pills = {navPillAttractions, navPillFood, navPillUtilities, navPillPhrases, navPillMap};
        int activeColor = ContextCompat.getColor(this, R.color.saigon_red);
        int inactiveColor = ContextCompat.getColor(this, R.color.text_secondary);
        for (int i = 0; i < labels.length; i++) {
            boolean sel = i == currentTabIndex;
            labels[i].setTextColor(sel ? activeColor : inactiveColor);
            labels[i].setTypeface(null, sel ? Typeface.BOLD : Typeface.NORMAL);
            if (pills[i] != null) {
                pills[i].setBackgroundResource(sel ? R.drawable.bg_nav_active_pill : 0);
            }
        }
    }

    @Override
    public void openAttractionDetail(String attractionId) {
        pushSubScreen(AttractionDetailFragment.newInstance(attractionId));
    }

    @Override
    public void openFoodDetail(String foodId) {
        pushSubScreen(FoodDetailFragment.newInstance(foodId));
    }

    @Override
    public void openMapAt(double lat, double lng, String title) {
        currentTabIndex = 4;
        updateBottomNavLabels();
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, CityMapFragment.newInstance(lat, lng, title))
                .commit();
        updateChromeVisibility();
    }

    @Override
    public void openRideBooking(String destName, double destLat, double destLng) {
        pushSubScreen(RideBookingFragment.newInstance(destName, destLat, destLng));
    }

    @Override
    public void openAiAssistant() {
        pushSubScreen(new AiAssistantFragment());
    }

    @Override
    public void openFavorites() {
        pushSubScreen(new FavoritesFragment());
    }

    @Override
    public void openTripPlanner() {
        pushSubScreen(new TripPlannerFragment());
    }

    @Override
    public void openTravelToolkit() {
        pushSubScreen(new TravelToolkitFragment());
    }

    @Override
    public void openMetroGuide() {
        pushSubScreen(new MetroWaterbusFragment());
    }

    @Override
    public void openSafetyHub() {
        pushSubScreen(new SafetyHubFragment());
    }

    @Override
    public void openDiscoveryQuiz() {
        pushSubScreen(new DiscoveryQuizFragment());
    }

    @Override
    public void openAuthProfile() {
        pushSubScreen(new AuthProfileFragment());
    }

    @Override
    public void openAdminDashboard() {
        pushSubScreen(new AdminDashboardFragment());
    }

    @Override
    public void navigateBack() {
        getOnBackPressedDispatcher().onBackPressed();
    }

    @Override
    public void showPermissionRequiredDialog(String message) {
        DialogHelper.showPermissionRequired(this, message, this);
    }

    @Override
    public void showLanguagePickerDialog() {
        DialogHelper.showLanguageSelector(this, () -> {
            updateBottomNavLabels();
            selectMainTab(currentTabIndex);
        });
    }

    @Override
    public SpeechHelper getSpeechHelper() {
        return speechHelper;
    }

    @Override
    protected void onDestroy() {
        if (speechHelper != null) {
            speechHelper.shutdown();
        }
        super.onDestroy();
    }
}
