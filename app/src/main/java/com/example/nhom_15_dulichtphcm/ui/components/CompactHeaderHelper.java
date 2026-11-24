package com.example.nhom_15_dulichtphcm.ui.components;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.nhom_15_dulichtphcm.R;
import com.example.nhom_15_dulichtphcm.data.local.AuthManager;
import com.example.nhom_15_dulichtphcm.data.local.FavoritesManager;
import com.example.nhom_15_dulichtphcm.data.local.TripPlanManager;
import com.example.nhom_15_dulichtphcm.data.model.UserAccount;
import com.example.nhom_15_dulichtphcm.data.model.UserRole;
import com.example.nhom_15_dulichtphcm.ui.AppNavigator;
import com.example.nhom_15_dulichtphcm.util.AppLanguage;
import com.example.nhom_15_dulichtphcm.util.AvatarImageManager;
import com.example.nhom_15_dulichtphcm.util.LanguageManager;
import com.example.nhom_15_dulichtphcm.util.Localized;
import com.example.nhom_15_dulichtphcm.util.ThemeManager;

/**
 * Binds XML view_compact_header.xml, Home quick-action feature cards, and view_subscreen_header.xml to live SQLite state.
 */
public final class CompactHeaderHelper {

    private CompactHeaderHelper() {}

    public static void bindMainHeader(View root, String title, String subtitle, AppNavigator nav) {
        if (root == null) return;
        TextView tvTitle = root.findViewById(R.id.tv_header_title);
        TextView tvSubtitle = root.findViewById(R.id.tv_header_subtitle);
        View btnAi = root.findViewById(R.id.btn_header_ai);
        TextView btnAdmin = root.findViewById(R.id.btn_header_admin);
        View btnAccount = root.findViewById(R.id.btn_header_account);
        ImageView ivRole = root.findViewById(R.id.iv_header_role);
        TextView tvRole = root.findViewById(R.id.tv_header_role);
        TextView btnTheme = root.findViewById(R.id.btn_header_theme);
        TextView btnLanguage = root.findViewById(R.id.btn_header_language);

        View btnAiBanner = root.findViewById(R.id.card_ai_banner);
        View btnFav = root.findViewById(R.id.btn_quick_favorites);
        TextView tvFavTitle = root.findViewById(R.id.tv_quick_favorites_title);
        TextView tvFav = root.findViewById(R.id.tv_quick_favorites);
        View btnTrip = root.findViewById(R.id.btn_quick_trip);
        TextView tvTripTitle = root.findViewById(R.id.tv_quick_trip_title);
        TextView tvTrip = root.findViewById(R.id.tv_quick_trip);
        View btnToolkit = root.findViewById(R.id.btn_quick_toolkit);
        TextView tvToolkitTitle = root.findViewById(R.id.tv_quick_toolkit_title);
        TextView tvToolkit = root.findViewById(R.id.tv_quick_toolkit);
        View btnDiscovery = root.findViewById(R.id.btn_quick_discovery);
        TextView tvDiscoveryTitle = root.findViewById(R.id.tv_quick_discovery_title);
        TextView tvDiscovery = root.findViewById(R.id.tv_quick_discovery);
        View btnMetro = root.findViewById(R.id.btn_quick_metro);
        TextView tvMetroTitle = root.findViewById(R.id.tv_quick_metro_title);
        TextView tvMetro = root.findViewById(R.id.tv_quick_metro);
        View btnSafety = root.findViewById(R.id.btn_quick_safety);
        TextView tvSafetyTitle = root.findViewById(R.id.tv_quick_safety_title);
        TextView tvSafety = root.findViewById(R.id.tv_quick_safety);
        View btnRide = root.findViewById(R.id.btn_quick_ride);
        TextView tvRideTitle = root.findViewById(R.id.tv_quick_ride_title);
        TextView tvRide = root.findViewById(R.id.tv_quick_ride);

        if (tvTitle != null && title != null) tvTitle.setText(title);
        if (tvSubtitle != null && subtitle != null) tvSubtitle.setText(subtitle);

        if (btnAi != null) {
            btnAi.setOnClickListener(v -> {
                if (nav != null) nav.openAiAssistant();
            });
        }
        if (btnAiBanner != null) {
            btnAiBanner.setOnClickListener(v -> {
                if (nav != null) nav.openAiAssistant();
            });
        }

        UserAccount user = AuthManager.getCurrentUser();
        UserRole role = AuthManager.getCurrentRole();

        if (ivRole != null) {
            android.graphics.Bitmap avatarBmp = (user != null && user.hasAvatarPhoto())
                    ? AvatarImageManager.loadCircularBitmap(user.avatarPhotoPath, 64, 0, 0f)
                    : null;
            if (avatarBmp != null) {
                ivRole.setImageBitmap(avatarBmp);
            } else {
                ivRole.setImageResource(AppIcons2D.roleIconRes(role));
            }
        }
        if (tvRole != null) {
            String roleText = user != null
                    ? user.username
                    : Localized.tr("Đăng nhập", "Sign In");
            tvRole.setText(roleText);
        }

        if (btnAdmin != null) {
            boolean canConsole = role.canAccessConsole();
            btnAdmin.setVisibility(canConsole ? View.VISIBLE : View.GONE);
            btnAdmin.setOnClickListener(v -> {
                if (nav != null) nav.openAdminDashboard();
            });
        }

        if (btnAccount != null) {
            btnAccount.setOnClickListener(v -> {
                if (nav != null) nav.openAuthProfile();
            });
        }

        if (btnTheme != null) {
            boolean darkActive = ThemeManager.isDarkActive(root.getContext());
            btnTheme.setText(darkActive ? "☀️" : "🌙");
            btnTheme.setOnClickListener(v -> ThemeManager.toggleLightDark(v.getContext()));
            btnTheme.setOnLongClickListener(v -> {
                DialogHelper.showThemeSelector(v.getContext());
                return true;
            });
        }

        AppLanguage lang = LanguageManager.getCurrentLanguage();
        if (btnLanguage != null) {
            btnLanguage.setText(lang.flag + " " + lang.code.toUpperCase());
            btnLanguage.setOnClickListener(v -> {
                if (nav != null) nav.showLanguagePickerDialog();
            });
        }

        int favCount = FavoritesManager.getTotalCount();
        int tripCount = TripPlanManager.getStopIds().size();

        if (tvFavTitle != null) {
            tvFavTitle.setText(Localized.tr("Đã lưu", "Saved"));
        }
        if (tvFav != null) {
            tvFav.setText(Localized.tr(favCount + " mục đã lưu", favCount + " items"));
        }
        if (tvTripTitle != null) {
            tvTripTitle.setText(Localized.tr("Lịch trình", "My Trip"));
        }
        if (tvTrip != null) {
            tvTrip.setText(Localized.tr(tripCount + " điểm • tối ưu", tripCount + " stops • optimize"));
        }
        if (tvToolkitTitle != null) {
            tvToolkitTitle.setText(Localized.tr("Tiện ích", "Toolkit"));
        }
        if (tvToolkit != null) {
            tvToolkit.setText(Localized.tr("Thời tiết • Tỷ giá • Hành lý", "Weather • FX • Packing"));
        }
        if (tvDiscoveryTitle != null) {
            tvDiscoveryTitle.setText(Localized.tr("Ăn gì • Đi đâu", "Eat & Go"));
        }
        if (tvDiscovery != null) {
            tvDiscovery.setText(Localized.tr("Vòng quay • Đố vui", "Spinner • Heritage Quiz"));
        }
        if (tvMetroTitle != null) {
            tvMetroTitle.setText(Localized.tr("Metro Số 1", "Metro Line 1"));
        }
        if (tvMetro != null) {
            tvMetro.setText(Localized.tr("14 ga • Waterbus", "14 stops • Waterbus"));
        }
        if (tvSafetyTitle != null) {
            tvSafetyTitle.setText(Localized.tr("SOS & Dị ứng", "SOS & Allergy"));
        }
        if (tvSafety != null) {
            tvSafety.setText(Localized.tr("113/115 • Thẻ bếp", "113/115 • Chef Card"));
        }
        if (tvRideTitle != null) {
            tvRideTitle.setText(Localized.tr("Cẩm nang Đặt xe & Taxi Du lịch 🚖", "Tourist Ride-Booking & Taxi Guide 🚖"));
        }
        if (tvRide != null) {
            tvRide.setText(Localized.tr(
                    "Grab, Xanh SM EV, Be • Ước tính cước & thẻ giao tiếp tài xế",
                    "Grab, Xanh SM EV, Be • Fare calculator & driver flashcards"
            ));
        }

        if (btnFav != null) btnFav.setOnClickListener(v -> { if (nav != null) nav.openFavorites(); });
        if (btnTrip != null) btnTrip.setOnClickListener(v -> { if (nav != null) nav.openTripPlanner(); });
        if (btnRide != null) btnRide.setOnClickListener(v -> {
            if (nav != null) nav.openRideBooking("Chợ Bến Thành", 10.7725, 106.6980);
        });
        if (btnToolkit != null) btnToolkit.setOnClickListener(v -> { if (nav != null) nav.openTravelToolkit(); });
        if (btnDiscovery != null) btnDiscovery.setOnClickListener(v -> { if (nav != null) nav.openDiscoveryQuiz(); });
        if (btnMetro != null) btnMetro.setOnClickListener(v -> { if (nav != null) nav.openMetroGuide(); });
        if (btnSafety != null) btnSafety.setOnClickListener(v -> { if (nav != null) nav.openSafetyHub(); });
    }

    public static void bindSubscreenToolbar(
            View root,
            String title,
            AppNavigator nav,
            String actionLabel,
            Runnable onAction
    ) {
        if (root == null) return;
        View btnBack = root.findViewById(R.id.btn_toolbar_back);
        TextView tvTitle = root.findViewById(R.id.tv_toolbar_title);
        TextView btnAction = root.findViewById(R.id.btn_toolbar_action);

        if (tvTitle != null && title != null) {
            tvTitle.setText(title);
        }
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (nav != null) nav.navigateBack();
            });
        }
        if (btnAction != null) {
            if (actionLabel != null && onAction != null) {
                btnAction.setVisibility(View.VISIBLE);
                btnAction.setText(actionLabel);
                btnAction.setOnClickListener(v -> onAction.run());
            } else {
                btnAction.setVisibility(View.GONE);
            }
        }
    }
}
