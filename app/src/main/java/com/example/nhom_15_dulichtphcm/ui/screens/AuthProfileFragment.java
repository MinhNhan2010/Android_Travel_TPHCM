package com.example.nhom_15_dulichtphcm.ui.screens;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.nhom_15_dulichtphcm.R;
import com.example.nhom_15_dulichtphcm.data.local.AuthManager;
import com.example.nhom_15_dulichtphcm.data.local.ExplorerRank;
import com.example.nhom_15_dulichtphcm.data.local.FavoritesManager;
import com.example.nhom_15_dulichtphcm.data.local.ReviewManager;
import com.example.nhom_15_dulichtphcm.data.local.TicketWalletManager;
import com.example.nhom_15_dulichtphcm.data.local.TravelPassportManager;
import com.example.nhom_15_dulichtphcm.data.local.TripPlanManager;
import com.example.nhom_15_dulichtphcm.data.local.UserDataBackupManager;
import com.example.nhom_15_dulichtphcm.data.model.AuditLogEntry;
import com.example.nhom_15_dulichtphcm.data.model.AuthProvider;
import com.example.nhom_15_dulichtphcm.data.model.Permission;
import com.example.nhom_15_dulichtphcm.data.model.ResultBox;
import com.example.nhom_15_dulichtphcm.data.model.UserAccount;
import com.example.nhom_15_dulichtphcm.data.model.UserRole;
import com.example.nhom_15_dulichtphcm.data.repository.AttractionRepository;
import com.example.nhom_15_dulichtphcm.data.repository.FoodRepository;
import com.example.nhom_15_dulichtphcm.ui.AppNavigator;
import com.example.nhom_15_dulichtphcm.ui.components.AvatarCropperView;
import com.example.nhom_15_dulichtphcm.ui.components.CompactHeaderHelper;
import com.example.nhom_15_dulichtphcm.ui.components.DialogHelper;
import com.example.nhom_15_dulichtphcm.ui.components.ExperienceDialogs;
import com.example.nhom_15_dulichtphcm.util.AppLanguage;
import com.example.nhom_15_dulichtphcm.util.AvatarImageManager;
import com.example.nhom_15_dulichtphcm.util.LanguageManager;
import com.example.nhom_15_dulichtphcm.util.Localized;
import com.example.nhom_15_dulichtphcm.util.ThemeManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Pure Java + XML Account, Personal Travel Stats, Appearance (Light/Dark) & RBAC Hub
 * persisted 100% in SQLite (saigon_travel.db: users, audit_logs, app_settings).
 * Restores the original Google (Android AccountManager chooser + Gmail dialog fallback)
 * and Social Network (Facebook, Zalo, Apple ID) sign-in & registration experience.
 */
public final class AuthProfileFragment extends Fragment {

    private static final String[] AVATAR_EMOJIS = {
            "🧳", "🎒", "🍜", "📸", "🌴", "☕", "🛵", "🏙️", "🌊", "🧑‍💻", "🌸", "😎"
    };

    private boolean isRegisterMode = false;
    private boolean isPasswordVisible = false;
    private boolean isSwitchAccountExpanded = false;
    private boolean hasAutoFilledRememberedOnce = false;
    private String localFormError = null;
    private String pendingRegistrationAvatarPath = "";
    private String pendingAvatarTargetUserId = "pending";
    private Consumer<String> pendingAvatarPickedCallback = null;
    private View rootView;

    private final ActivityResultLauncher<String> avatarGalleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (!isAdded() || uri == null) return;
                Bitmap decoded = AvatarImageManager.decodeSampledBitmapFromUri(requireContext(), uri);
                if (decoded == null) {
                    Toast.makeText(requireContext(),
                            Localized.tr("Không thể đọc tệp hình ảnh đã chọn", "Unable to decode selected image"),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                Consumer<String> cb = pendingAvatarPickedCallback;
                pendingAvatarPickedCallback = null;
                showAvatarCropDialog(decoded, pendingAvatarTargetUserId, cb);
            });

    private final ActivityResultLauncher<Void> avatarCameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (!isAdded() || bitmap == null) return;
                Consumer<String> cb = pendingAvatarPickedCallback;
                pendingAvatarPickedCallback = null;
                showAvatarCropDialog(bitmap, pendingAvatarTargetUserId, cb);
            });

    /**
     * Bộ chọn tài khoản Google (Gmail) hệ thống có sẵn trên thiết bị Android (không cần quyền nhạy cảm).
     * Nếu máy ảo/thiết bị chưa đăng nhập Google hoặc người dùng đóng bộ chọn -> mở hộp thoại nhập Gmail nhanh.
     */
    private final ActivityResultLauncher<Intent> googleAccountPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (!isAdded()) return;
                String chosenEmail = null;
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    chosenEmail = result.getData().getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                }

                if (chosenEmail != null && !chosenEmail.trim().isEmpty()) {
                    String cleanEmail = chosenEmail.trim();
                    ResultBox<UserAccount> res = AuthManager.signInOrRegisterSocial(
                            AuthProvider.GOOGLE,
                            cleanEmail,
                            "",
                            cleanEmail.toLowerCase(Locale.ROOT)
                    );
                    if (res.isSuccess() && res.getOrNull() != null) {
                        UserAccount acc = res.getOrNull();
                        localFormError = null;
                        isSwitchAccountExpanded = false;
                        Toast.makeText(
                                requireContext(),
                                Localized.tr("Đã đăng nhập Gmail: " + acc.email,
                                        "Signed in with Gmail: " + acc.email),
                                Toast.LENGTH_SHORT
                        ).show();
                        renderState();
                    } else {
                        localFormError = res.getErrorMessage();
                        Toast.makeText(requireContext(), res.getErrorMessage(), Toast.LENGTH_LONG).show();
                        renderState();
                    }
                } else {
                    // Máy ảo hoặc thiết bị chưa đăng nhập Google -> mở hộp thoại nhập Gmail nhanh đúng phong cách gốc
                    showSocialAuthDialog(AuthProvider.GOOGLE);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_auth_profile, container, false);

        bindToolbar();

        // Profile Card & Avatar Upload Actions
        View.OnClickListener avatarClick = v -> showAvatarActionChooser();
        rootView.findViewById(R.id.box_profile_avatar).setOnClickListener(avatarClick);
        rootView.findViewById(R.id.tv_profile_avatar).setOnClickListener(avatarClick);
        rootView.findViewById(R.id.iv_profile_avatar_photo).setOnClickListener(avatarClick);
        rootView.findViewById(R.id.badge_profile_camera).setOnClickListener(avatarClick);
        rootView.findViewById(R.id.btn_upload_avatar).setOnClickListener(avatarClick);
        rootView.findViewById(R.id.btn_edit_profile).setOnClickListener(v -> showEditProfileDialog());
        rootView.findViewById(R.id.btn_change_password).setOnClickListener(v -> showChangePasswordDialog());

        // Optional Avatar Upload during Registration
        View.OnClickListener regAvatarClick = v -> showRegistrationAvatarChooser();
        rootView.findViewById(R.id.row_register_avatar_upload).setOnClickListener(regAvatarClick);
        rootView.findViewById(R.id.btn_reg_pick_photo).setOnClickListener(regAvatarClick);

        rootView.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            AuthManager.logout();
            isSwitchAccountExpanded = false;
            isRegisterMode = false;
            localFormError = null;
            hasAutoFilledRememberedOnce = false;
            Toast.makeText(requireContext(),
                    Localized.tr("Đã đăng xuất về chế độ Khách vãng lai", "Signed out to Guest mode"),
                    Toast.LENGTH_SHORT).show();
            renderState();
        });

        View.OnClickListener openAdminAction = v -> {
            AppNavigator nav = getNavigator();
            if (nav != null) nav.openAdminDashboard();
        };
        rootView.findViewById(R.id.btn_open_admin_console).setOnClickListener(openAdminAction);
        rootView.findViewById(R.id.card_admin_portal_showcase).setOnClickListener(openAdminAction);

        // Interactive Travel Stats Tiles
        rootView.findViewById(R.id.tile_stat_saved).setOnClickListener(v -> {
            AppNavigator nav = getNavigator();
            if (nav != null) nav.openFavorites();
        });
        rootView.findViewById(R.id.tile_stat_trip).setOnClickListener(v -> {
            AppNavigator nav = getNavigator();
            if (nav != null) nav.openTripPlanner();
        });
        rootView.findViewById(R.id.tile_stat_passport).setOnClickListener(v -> {
            AppNavigator nav = getNavigator();
            if (nav != null) nav.openTravelToolkit();
        });
        rootView.findViewById(R.id.tile_stat_reviews).setOnClickListener(v -> {
            UserAccount cur = AuthManager.getCurrentUser();
            int count = cur != null ? ReviewManager.countByUser(cur.id) : 0;
            Toast.makeText(requireContext(),
                    Localized.tr(
                            "Bạn đã đóng góp " + count + " đánh giá địa điểm & ẩm thực!",
                            "You have contributed " + count + " place & food reviews!"),
                    Toast.LENGTH_SHORT).show();
        });

        // Wallet, Quiz & Backup Hub
        rootView.findViewById(R.id.btn_profile_ticket_wallet).setOnClickListener(v ->
                ExperienceDialogs.showTicketWalletDialog(requireContext(), this::renderState));
        rootView.findViewById(R.id.btn_profile_discovery_quiz).setOnClickListener(v -> {
            AppNavigator nav = getNavigator();
            if (nav != null) nav.openDiscoveryQuiz();
        });
        rootView.findViewById(R.id.btn_profile_backup_report).setOnClickListener(v ->
                ExperienceDialogs.showBackupAndReportDialog(requireContext(), this::renderState));

        // Theme (Light / Dark / System) & Language
        rootView.findViewById(R.id.btn_theme_light).setOnClickListener(v -> {
            ThemeManager.setThemeMode(requireContext(), ThemeManager.ThemeMode.LIGHT);
            renderState();
        });
        rootView.findViewById(R.id.btn_theme_dark).setOnClickListener(v -> {
            ThemeManager.setThemeMode(requireContext(), ThemeManager.ThemeMode.DARK);
            renderState();
        });
        rootView.findViewById(R.id.btn_theme_system).setOnClickListener(v -> {
            ThemeManager.setThemeMode(requireContext(), ThemeManager.ThemeMode.SYSTEM);
            renderState();
        });
        rootView.findViewById(R.id.btn_profile_language).setOnClickListener(v -> {
            DialogHelper.showLanguageSelector(requireContext(), () -> {
                bindToolbar();
                renderState();
            });
        });

        // Collapsible Switch Account Toggle
        rootView.findViewById(R.id.btn_toggle_switch_account).setOnClickListener(v -> {
            isSwitchAccountExpanded = !isSwitchAccountExpanded;
            renderState();
        });

        // Login / Register Form & Password Eye Toggle
        rootView.findViewById(R.id.tab_mode_login).setOnClickListener(v -> {
            isRegisterMode = false;
            localFormError = null;
            renderState();
        });
        rootView.findViewById(R.id.tab_mode_register).setOnClickListener(v -> {
            isRegisterMode = true;
            localFormError = null;
            renderState();
        });

        EditText etFull = rootView.findViewById(R.id.et_auth_fullname);
        EditText etUser = rootView.findViewById(R.id.et_auth_username);
        EditText etEmail = rootView.findViewById(R.id.et_auth_email);
        EditText etPass = rootView.findViewById(R.id.et_auth_password);

        TextWatcher clearErrorWatcher = new SimpleErrorClearWatcher(() -> {
            if (localFormError != null) {
                localFormError = null;
                TextView tvErr = rootView != null ? rootView.findViewById(R.id.tv_auth_error) : null;
                if (tvErr != null) tvErr.setVisibility(View.GONE);
            }
        });
        etFull.addTextChangedListener(clearErrorWatcher);
        etUser.addTextChangedListener(clearErrorWatcher);
        etEmail.addTextChangedListener(clearErrorWatcher);
        etPass.addTextChangedListener(clearErrorWatcher);

        TextView btnEye = rootView.findViewById(R.id.btn_auth_eye);
        btnEye.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            applyPasswordVisibility(etPass, isPasswordVisible);
            btnEye.setText(isPasswordVisible ? "🙈" : "👁️");
        });

        CheckBox cbRemember = rootView.findViewById(R.id.cb_remember_login);
        TextView tvRememberLbl = rootView.findViewById(R.id.tv_remember_login_label);
        cbRemember.setChecked(AuthManager.isRememberMeEnabled());
        cbRemember.setOnCheckedChangeListener((btn, checked) -> AuthManager.setRememberMeEnabled(checked));
        tvRememberLbl.setOnClickListener(v -> cbRemember.setChecked(!cbRemember.isChecked()));

        rootView.findViewById(R.id.btn_auth_submit).setOnClickListener(v -> handleSubmit());

        // Đăng nhập / Đăng ký bằng Google (gọi Android Google Account Picker trước, fallback hộp thoại nhập Gmail)
        // & 3 mạng xã hội phổ biến (Facebook, Zalo, Apple ID) theo đúng phong cách gốc
        rootView.findViewById(R.id.btn_social_google).setOnClickListener(v -> launchGoogleSignInFlow());
        rootView.findViewById(R.id.btn_social_facebook).setOnClickListener(v -> showSocialAuthDialog(AuthProvider.FACEBOOK));
        rootView.findViewById(R.id.btn_social_zalo).setOnClickListener(v -> showSocialAuthDialog(AuthProvider.ZALO));
        rootView.findViewById(R.id.btn_social_apple).setOnClickListener(v -> showSocialAuthDialog(AuthProvider.APPLE));

        // 4-Tier RBAC Quick Demo Switcher
        rootView.findViewById(R.id.btn_demo_admin).setOnClickListener(v -> switchDemoRole(UserRole.ADMIN));
        rootView.findViewById(R.id.btn_demo_editor).setOnClickListener(v -> switchDemoRole(UserRole.EDITOR));
        rootView.findViewById(R.id.btn_demo_member).setOnClickListener(v -> switchDemoRole(UserRole.MEMBER));
        rootView.findViewById(R.id.btn_demo_guest).setOnClickListener(v -> switchDemoRole(UserRole.GUEST));

        renderState();
        return rootView;
    }

    private void bindToolbar() {
        if (!isAdded() || rootView == null) return;
        boolean darkActive = ThemeManager.isDarkActive(requireContext());
        String themeActionLabel = darkActive
                ? Localized.tr("☀️ Sáng", "☀️ Light")
                : Localized.tr("🌙 Tối", "🌙 Dark");
        CompactHeaderHelper.bindSubscreenToolbar(
                rootView,
                Localized.tr("🔐 Tài khoản & Phân quyền (RBAC)", "🔐 Account & Role Permissions"),
                getNavigator(),
                themeActionLabel,
                () -> ThemeManager.toggleLightDark(requireContext())
        );
    }

    private void switchDemoRole(UserRole role) {
        AuthManager.quickDemoLogin(role);
        isSwitchAccountExpanded = false;
        localFormError = null;
        renderState();
    }

    private void handleSubmit() {
        EditText etFull = rootView.findViewById(R.id.et_auth_fullname);
        EditText etUser = rootView.findViewById(R.id.et_auth_username);
        EditText etEmail = rootView.findViewById(R.id.et_auth_email);
        EditText etPass = rootView.findViewById(R.id.et_auth_password);
        CheckBox cbRemember = rootView.findViewById(R.id.cb_remember_login);
        boolean remember = cbRemember == null || cbRemember.isChecked();

        ResultBox<UserAccount> res;
        if (isRegisterMode) {
            res = AuthManager.register(
                    etFull.getText().toString(),
                    etUser.getText().toString(),
                    etEmail.getText().toString(),
                    etPass.getText().toString(),
                    remember
            );
        } else {
            res = AuthManager.login(
                    etUser.getText().toString(),
                    etPass.getText().toString(),
                    remember
            );
        }

        if (!res.isSuccess()) {
            localFormError = res.getErrorMessage();
            renderState();
            Toast.makeText(requireContext(), res.getErrorMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
        localFormError = null;
        etPass.setText("");
        isSwitchAccountExpanded = false;
        UserAccount acc = res.getOrNull();
        if (acc != null) {
            if (isRegisterMode && !pendingRegistrationAvatarPath.isEmpty()) {
                AuthManager.updateCurrentUserAvatarPhoto(pendingRegistrationAvatarPath);
                pendingRegistrationAvatarPath = "";
            }
            Toast.makeText(requireContext(),
                    isRegisterMode
                            ? Localized.tr("Đăng ký thành công tài khoản @" + acc.username + "!",
                                    "Registered @" + acc.username + "!")
                            : Localized.tr("Xin chào " + acc.fullName + " (" + acc.role.shortVi + ")!",
                                    "Welcome " + acc.fullName + "!"),
                    Toast.LENGTH_SHORT).show();
        }
        renderState();
    }

    /**
     * Khởi chạy Android Google Account Picker hệ thống (AccountManager.newChooseAccountIntent);
     * nếu thiết bị không hỗ trợ, tự động mở hộp thoại nhập Gmail gốc.
     */
    private void launchGoogleSignInFlow() {
        if (!isAdded()) return;
        boolean launched = false;
        try {
            @SuppressWarnings("deprecation")
            Intent intent = AccountManager.newChooseAccountIntent(
                    null,
                    null,
                    new String[]{"com.google"},
                    null,
                    null,
                    null,
                    null
            );
            googleAccountPickerLauncher.launch(intent);
            launched = true;
        } catch (Exception ignored) {
            launched = false;
        }
        if (!launched) {
            showSocialAuthDialog(AuthProvider.GOOGLE);
        }
    }

    /**
     * Hộp thoại Đăng nhập / Đăng ký qua Google (Gmail), Facebook, Zalo, Apple ID theo đúng luồng cũ:
     * - Hiển thị các tài khoản thực sự đã liên kết trước đó trên máy (1-tap từ SQLite) nếu có.
     * - Form nhập trực tiếp Email / Số điện thoại Zalo / Tên tài khoản + Họ tên hiển thị (tùy chọn).
     * - Tự động tạo tài khoản Du khách Thành viên (MEMBER) hoặc đăng nhập lại vào phiên cũ.
     */
    private void showSocialAuthDialog(AuthProvider provider) {
        if (!isAdded()) return;
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_social_oauth, null);
        AlertDialog dialog = DialogHelper.showCustomCardDialog(requireContext(), dialogView);

        TextView tvBadge = dialogView.findViewById(R.id.tv_oauth_brand_badge);
        TextView tvTitle = dialogView.findViewById(R.id.tv_oauth_title);
        View boxSavedSection = dialogView.findViewById(R.id.box_oauth_saved_section);
        TextView tvSavedLbl = dialogView.findViewById(R.id.tv_oauth_saved_label);
        LinearLayout boxAccounts = dialogView.findViewById(R.id.container_oauth_accounts);
        TextView btnPickDeviceGoogle = dialogView.findViewById(R.id.btn_oauth_pick_device_google);
        TextView tvSub = dialogView.findViewById(R.id.tv_oauth_subtitle);
        TextView tvLblId = dialogView.findViewById(R.id.tv_lbl_oauth_identifier);
        EditText etIdentifier = dialogView.findViewById(R.id.et_oauth_identifier);
        TextView tvLblFullName = dialogView.findViewById(R.id.tv_lbl_oauth_fullname);
        EditText etFullName = dialogView.findViewById(R.id.et_oauth_fullname);
        TextView tvError = dialogView.findViewById(R.id.tv_oauth_error);
        TextView btnCancel = dialogView.findViewById(R.id.btn_oauth_cancel);
        TextView btnSubmit = dialogView.findViewById(R.id.btn_oauth_submit);

        boolean darkActive = ThemeManager.isDarkActive(requireContext());
        int accentBrandColor = resolveReadableBrandColor(provider, darkActive);
        int solidBtnColor = (provider == AuthProvider.APPLE && darkActive) ? 0xFF334155 : provider.brandColorHex;

        float d = getResources().getDisplayMetrics().density;

        // Brand emblem circle styling
        GradientDrawable badgeCircle = new GradientDrawable();
        badgeCircle.setShape(GradientDrawable.OVAL);
        badgeCircle.setColor(solidBtnColor);
        tvBadge.setBackground(badgeCircle);
        tvBadge.setText(getProviderSymbol(provider));

        // Brand submit button styling
        GradientDrawable submitBg = new GradientDrawable();
        submitBg.setShape(GradientDrawable.RECTANGLE);
        submitBg.setCornerRadius(10f * d);
        submitBg.setColor(solidBtnColor);
        if (provider == AuthProvider.APPLE && darkActive) {
            submitBg.setStroke((int) Math.max(1, d), 0xFF64748B);
        }
        btnSubmit.setBackground(submitBg);

        tvTitle.setText(isRegisterMode
                ? Localized.tr("Đăng ký qua " + provider.localizedName(),
                        "Register with " + provider.localizedName())
                : Localized.tr("Đăng nhập " + provider.localizedName(),
                        "Sign in with " + provider.localizedName()));

        tvSub.setText(Localized.tr(
                "Xác thực nhanh bằng " + provider.badgeLabel + ": tài khoản mới tự động nhận quyền Du khách Thành viên.",
                "Fast " + provider.badgeLabel + " auth: new accounts automatically receive Tourist Member access."));

        String idLabel = getProviderFieldLabel(provider);
        tvLblId.setText(idLabel);
        etIdentifier.setHint(getProviderHint(provider));
        if (provider == AuthProvider.ZALO) {
            etIdentifier.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        } else {
            etIdentifier.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        }

        String nameLabel = Localized.tr("Họ và tên hiển thị (tùy chọn)", "Display name (optional)");
        tvLblFullName.setText(nameLabel);
        etFullName.setHint(nameLabel);

        btnCancel.setText(Localized.tr("Hủy", "Cancel"));
        btnSubmit.setText(Localized.tr("Tiếp tục", "Continue"));

        dialogView.findViewById(R.id.btn_oauth_close).setOnClickListener(v -> dialog.dismiss());
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Tùy chọn gọi lại Bộ chọn Google trên thiết bị ngay từ hộp thoại Google
        if (provider == AuthProvider.GOOGLE) {
            btnPickDeviceGoogle.setVisibility(View.VISIBLE);
            btnPickDeviceGoogle.setText(Localized.tr(
                    "📱 Chọn tài khoản Google có sẵn trên thiết bị",
                    "📱 Choose Google account from this device"));
            GradientDrawable pickBg = new GradientDrawable();
            pickBg.setShape(GradientDrawable.RECTANGLE);
            pickBg.setCornerRadius(10f * d);
            pickBg.setColor(withAlpha(provider.brandColorHex, darkActive ? 0.16f : 0.08f));
            pickBg.setStroke((int) Math.max(1, d), withAlpha(provider.brandColorHex, 0.40f));
            btnPickDeviceGoogle.setBackground(pickBg);
            btnPickDeviceGoogle.setTextColor(accentBrandColor);
            btnPickDeviceGoogle.setOnClickListener(v -> {
                dialog.dismiss();
                launchGoogleSignInFlow();
            });
        } else {
            btnPickDeviceGoogle.setVisibility(View.GONE);
        }

        // Chỉ hiển thị danh sách tài khoản thực tế đã từng liên kết trên máy (từ SQLite users)
        List<UserAccount> savedAccounts = AuthManager.getSavedSocialAccounts(provider);
        if (!savedAccounts.isEmpty()) {
            boxSavedSection.setVisibility(View.VISIBLE);
            tvSavedLbl.setText(Localized.tr(
                    "Tài khoản đã liên kết trên máy (chạm để vào ngay):",
                    "Linked accounts on this device (1-tap):"));
            for (UserAccount acc : savedAccounts) {
                boxAccounts.addView(createSavedSocialAccountRow(acc, provider, accentBrandColor, darkActive, () -> {
                    ResultBox<UserAccount> res = AuthManager.signInOrRegisterSocial(
                            provider,
                            acc.email,
                            acc.fullName,
                            acc.providerAccountId
                    );
                    if (!res.isSuccess() || res.getOrNull() == null) {
                        tvError.setText("⚠️ " + res.getErrorMessage());
                        tvError.setVisibility(View.VISIBLE);
                        return;
                    }
                    onSocialAuthenticated(dialog, provider, res.getOrNull());
                }));
            }
        } else {
            boxSavedSection.setVisibility(View.GONE);
        }

        TextWatcher clearDialogErr = new SimpleErrorClearWatcher(() -> tvError.setVisibility(View.GONE));
        etIdentifier.addTextChangedListener(clearDialogErr);
        etFullName.addTextChangedListener(clearDialogErr);

        btnSubmit.setOnClickListener(v -> {
            String identifier = etIdentifier.getText().toString();
            String fullName = etFullName.getText().toString();
            ResultBox<UserAccount> res = AuthManager.signInOrRegisterSocial(
                    provider,
                    identifier,
                    fullName
            );
            if (!res.isSuccess() || res.getOrNull() == null) {
                tvError.setText("⚠️ " + res.getErrorMessage());
                tvError.setVisibility(View.VISIBLE);
                return;
            }
            onSocialAuthenticated(dialog, provider, res.getOrNull());
        });
    }

    private void onSocialAuthenticated(AlertDialog dialog, AuthProvider provider, UserAccount account) {
        dialog.dismiss();
        localFormError = null;
        isSwitchAccountExpanded = false;
        Toast.makeText(
                requireContext(),
                Localized.tr(
                        "Xin chào " + account.fullName + " (" + provider.badgeLabel + ")!",
                        "Signed in as " + account.fullName + " (" + provider.badgeLabel + ")!"
                ),
                Toast.LENGTH_SHORT
        ).show();
        renderState();
    }

    private View createSavedSocialAccountRow(
            UserAccount acc,
            AuthProvider provider,
            int accentBrandColor,
            boolean darkActive,
            Runnable onPick
    ) {
        float d = getResources().getDisplayMetrics().density;
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        GradientDrawable rowBg = new GradientDrawable();
        rowBg.setShape(GradientDrawable.RECTANGLE);
        rowBg.setCornerRadius(10f * d);
        rowBg.setColor(withAlpha(accentBrandColor, darkActive ? 0.16f : 0.08f));
        rowBg.setStroke((int) Math.max(1, d), withAlpha(accentBrandColor, darkActive ? 0.42f : 0.30f));
        row.setBackground(rowBg);

        int padH = (int) (10 * d);
        int padV = (int) (8 * d);
        row.setPadding(padH, padV, padH, padV);
        row.setClickable(true);
        row.setFocusable(true);

        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowLp.bottomMargin = (int) (6 * d);
        row.setLayoutParams(rowLp);

        if (acc.hasAvatarPhoto()) {
            ImageView ivSaved = new ImageView(requireContext());
            int sz = (int) (28 * d);
            ivSaved.setLayoutParams(new LinearLayout.LayoutParams(sz, sz));
            android.graphics.Bitmap bmp = AvatarImageManager.loadCircularBitmap(
                    acc.avatarPhotoPath, sz * 2, accentBrandColor, 2f * d);
            if (bmp != null) {
                ivSaved.setImageBitmap(bmp);
                row.addView(ivSaved);
            } else {
                TextView avatar = new TextView(requireContext());
                avatar.setText(acc.avatarEmoji);
                avatar.setTextSize(18f);
                row.addView(avatar);
            }
        } else {
            TextView avatar = new TextView(requireContext());
            avatar.setText(acc.avatarEmoji);
            avatar.setTextSize(18f);
            row.addView(avatar);
        }

        LinearLayout mid = new LinearLayout(requireContext());
        mid.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams midLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        midLp.setMarginStart((int) (8 * d));
        mid.setLayoutParams(midLp);

        TextView tvName = new TextView(requireContext());
        tvName.setText(acc.fullName);
        tvName.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        tvName.setTextSize(13f);
        tvName.setTypeface(null, Typeface.BOLD);
        mid.addView(tvName);

        TextView tvEmail = new TextView(requireContext());
        tvEmail.setText(acc.email);
        tvEmail.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        tvEmail.setTextSize(11f);
        mid.addView(tvEmail);

        row.addView(mid);

        TextView arrow = new TextView(requireContext());
        arrow.setText("→");
        arrow.setTextSize(14f);
        arrow.setTypeface(null, Typeface.BOLD);
        arrow.setTextColor(accentBrandColor);
        row.addView(arrow);

        row.setOnClickListener(v -> onPick.run());
        return row;
    }

    /**
     * Tạo kiểu cho nút Google toàn chiều ngang + 3 nút Facebook / Zalo / Apple đúng bản gốc,
     * đồng thời hiển thị sắc nét trên cả chế độ Sáng & Tối.
     */
    private void styleOriginalSocialButtons() {
        if (!isAdded() || rootView == null) return;
        boolean darkActive = ThemeManager.isDarkActive(requireContext());
        float d = getResources().getDisplayMetrics().density;
        int strokePx = (int) Math.max(1, Math.round(d));

        // 1. Nút Google / Gmail nổi bật
        View btnGoogle = rootView.findViewById(R.id.btn_social_google);
        TextView tvGMono = rootView.findViewById(R.id.tv_google_monogram);
        TextView tvGLabel = rootView.findViewById(R.id.tv_btn_google_label);

        GradientDrawable gCircle = new GradientDrawable();
        gCircle.setShape(GradientDrawable.OVAL);
        gCircle.setColor(AuthProvider.GOOGLE.brandColorHex);
        tvGMono.setBackground(gCircle);

        GradientDrawable gBtnBg = new GradientDrawable();
        gBtnBg.setShape(GradientDrawable.RECTANGLE);
        gBtnBg.setCornerRadius(12f * d);
        gBtnBg.setColor(ContextCompat.getColor(requireContext(), R.color.surface_white));
        gBtnBg.setStroke(strokePx, withAlpha(AuthProvider.GOOGLE.brandColorHex, darkActive ? 0.65f : 0.50f));
        btnGoogle.setBackground(gBtnBg);
        tvGLabel.setText(Localized.tr("Tiếp tục với Google (Gmail)", "Continue with Google (Gmail)"));

        // 2. Hàng 3 nút mạng xã hội: Facebook, Zalo, Apple
        styleSocialChipButton(
                rootView.findViewById(R.id.btn_social_facebook),
                rootView.findViewById(R.id.tv_fb_monogram),
                rootView.findViewById(R.id.tv_btn_fb_label),
                AuthProvider.FACEBOOK,
                darkActive,
                d,
                strokePx
        );
        styleSocialChipButton(
                rootView.findViewById(R.id.btn_social_zalo),
                rootView.findViewById(R.id.tv_zalo_monogram),
                rootView.findViewById(R.id.tv_btn_zalo_label),
                AuthProvider.ZALO,
                darkActive,
                d,
                strokePx
        );
        styleSocialChipButton(
                rootView.findViewById(R.id.btn_social_apple),
                rootView.findViewById(R.id.tv_apple_monogram),
                rootView.findViewById(R.id.tv_btn_apple_label),
                AuthProvider.APPLE,
                darkActive,
                d,
                strokePx
        );
    }

    private void styleSocialChipButton(
            View container,
            TextView monogramView,
            TextView labelView,
            AuthProvider provider,
            boolean darkActive,
            float density,
            int strokePx
    ) {
        int readableColor = resolveReadableBrandColor(provider, darkActive);
        int circleBgColor = (provider == AuthProvider.APPLE && darkActive)
                ? 0xFF334155
                : provider.brandColorHex;

        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(circleBgColor);
        monogramView.setBackground(circle);

        GradientDrawable chipBg = new GradientDrawable();
        chipBg.setShape(GradientDrawable.RECTANGLE);
        chipBg.setCornerRadius(12f * density);
        chipBg.setColor(withAlpha(readableColor, darkActive ? 0.16f : 0.10f));
        chipBg.setStroke(strokePx, withAlpha(readableColor, darkActive ? 0.45f : 0.35f));
        container.setBackground(chipBg);

        labelView.setText(provider.badgeLabel);
        labelView.setTextColor(readableColor);
    }

    private int resolveReadableBrandColor(AuthProvider provider, boolean darkActive) {
        if (!darkActive) {
            return provider.brandColorHex;
        }
        switch (provider) {
            case APPLE:
                return 0xFFE2E8F0;
            case FACEBOOK:
                return 0xFF60A5FA;
            case ZALO:
                return 0xFF38BDF8;
            case GOOGLE:
                return 0xFFF87171;
            case LOCAL:
            default:
                return provider.brandColorHex;
        }
    }

    private static int withAlpha(int colorHex, float alpha) {
        int a = Math.max(0, Math.min(255, Math.round(alpha * 255f)));
        return Color.argb(a, Color.red(colorHex), Color.green(colorHex), Color.blue(colorHex));
    }

    private void launchGalleryAvatarPicker(String targetUserId, Consumer<String> onSaved) {
        if (!isAdded()) return;
        pendingAvatarTargetUserId = (targetUserId != null && !targetUserId.isEmpty()) ? targetUserId : "pending";
        pendingAvatarPickedCallback = onSaved;
        try {
            avatarGalleryLauncher.launch("image/*");
        } catch (Exception e) {
            Toast.makeText(requireContext(),
                    Localized.tr("Thiết bị không có ứng dụng chọn ảnh", "No image picker available on device"),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void launchCameraAvatarCapture(String targetUserId, Consumer<String> onSaved) {
        if (!isAdded()) return;
        pendingAvatarTargetUserId = (targetUserId != null && !targetUserId.isEmpty()) ? targetUserId : "pending";
        pendingAvatarPickedCallback = onSaved;
        try {
            avatarCameraLauncher.launch(null);
        } catch (Exception e) {
            Toast.makeText(requireContext(),
                    Localized.tr("Không thể mở Camera trên thiết bị này", "Camera is unavailable on this device"),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void applyDirectUserAvatarPhoto(String newPath) {
        ResultBox<UserAccount> res = AuthManager.updateCurrentUserAvatarPhoto(newPath);
        if (!res.isSuccess()) {
            Toast.makeText(requireContext(), res.getErrorMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(requireContext(),
                Localized.tr("✨ Đã cập nhật ảnh đại diện thành công!", "✨ Profile photo updated!"),
                Toast.LENGTH_SHORT).show();
        renderState();
    }

    /**
     * Interactive Circular Crop, Pan, Pinch/Slider Zoom & 90° Rotate Dialog.
     */
    private void showAvatarCropDialog(Bitmap rawBitmap, String targetUserId, Consumer<String> onSaved) {
        if (!isAdded() || rawBitmap == null) return;
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_crop_avatar, null);
        AlertDialog dialog = DialogHelper.showCustomCardDialog(requireContext(), dialogView);

        TextView tvTitle = dialogView.findViewById(R.id.tv_crop_dialog_title);
        TextView tvHint = dialogView.findViewById(R.id.tv_crop_dialog_hint);
        AvatarCropperView cropper = dialogView.findViewById(R.id.avatar_cropper_view);
        SeekBar seekZoom = dialogView.findViewById(R.id.seek_crop_zoom);
        TextView tvZoomLbl = dialogView.findViewById(R.id.tv_crop_zoom_label);
        TextView btnZoomOut = dialogView.findViewById(R.id.btn_crop_zoom_out);
        TextView btnZoomIn = dialogView.findViewById(R.id.btn_crop_zoom_in);
        TextView btnRotate = dialogView.findViewById(R.id.btn_crop_rotate);
        TextView btnReset = dialogView.findViewById(R.id.btn_crop_reset);
        TextView btnCancel = dialogView.findViewById(R.id.btn_crop_cancel);
        TextView btnApply = dialogView.findViewById(R.id.btn_crop_apply);

        tvTitle.setText(Localized.tr("✂️ Căn chỉnh Ảnh đại diện", "✂️ Adjust & Crop Avatar"));
        tvHint.setText(Localized.tr(
                "Kéo ảnh để căn khung mặt • Kéo thanh trượt hoặc chụm 2 ngón tay để thu phóng",
                "Drag photo to frame • Use slider or 2-finger pinch to zoom"));
        btnRotate.setText(Localized.tr("🔄 Xoay 90°", "🔄 Rotate 90°"));
        btnReset.setText(Localized.tr("🎯 Đặt lại", "🎯 Reset"));
        btnCancel.setText(Localized.tr("Hủy", "Cancel"));
        btnApply.setText(Localized.tr("✅ Lưu ảnh đã căn chỉnh", "✅ Save Cropped Photo"));

        final boolean[] updatingFromCropper = {false};
        cropper.setOnZoomChangedListener(zoom -> {
            updatingFromCropper[0] = true;
            int prog = Math.round((zoom - cropper.getMinZoom()) * 100f);
            seekZoom.setProgress(Math.max(0, Math.min(seekZoom.getMax(), prog)));
            tvZoomLbl.setText(String.format(Locale.US, "%.1fx", zoom));
            updatingFromCropper[0] = false;
        });
        cropper.setImageBitmap(rawBitmap);

        seekZoom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (updatingFromCropper[0]) return;
                float z = cropper.getMinZoom() + (progress / 100f);
                cropper.setZoomFactor(z);
                tvZoomLbl.setText(String.format(Locale.US, "%.1fx", cropper.getZoomFactor()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnZoomOut.setOnClickListener(v -> seekZoom.setProgress(Math.max(0, seekZoom.getProgress() - 35)));
        btnZoomIn.setOnClickListener(v -> seekZoom.setProgress(Math.min(seekZoom.getMax(), seekZoom.getProgress() + 35)));
        btnRotate.setOnClickListener(v -> cropper.rotate90Clockwise());
        btnReset.setOnClickListener(v -> {
            cropper.resetTransform();
            seekZoom.setProgress(0);
            tvZoomLbl.setText("1.0x");
        });

        dialogView.findViewById(R.id.btn_crop_close).setOnClickListener(v -> dialog.dismiss());
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnApply.setOnClickListener(v -> {
            Bitmap exported = cropper.exportCroppedSquareBitmap(512);
            String savedPath = AvatarImageManager.saveAvatarFromBitmap(requireContext(), targetUserId, exported);
            if (savedPath == null) {
                Toast.makeText(requireContext(),
                        Localized.tr("Không thể lưu ảnh đã căn chỉnh", "Failed to save cropped photo"),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            if (onSaved != null) {
                onSaved.accept(savedPath);
            }
        });
    }

    private void reopenExistingAvatarInCropper(String existingPath, String targetUserId, Consumer<String> onSaved) {
        Bitmap existing = AvatarImageManager.decodeSampledBitmapFromFile(existingPath);
        if (existing == null) {
            Toast.makeText(requireContext(),
                    Localized.tr("Không tìm thấy tệp ảnh gốc để căn chỉnh lại", "Original image file not found"),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        showAvatarCropDialog(existing, targetUserId, onSaved);
    }

    private void showAvatarActionChooser() {
        if (!isAdded()) return;
        UserAccount cur = AuthManager.getCurrentUser();
        if (cur == null) {
            isRegisterMode = true;
            renderState();
            showRegistrationAvatarChooser();
            return;
        }

        final boolean hasExistingPhoto = cur.hasAvatarPhoto();
        List<String> options = new ArrayList<>();
        options.add(Localized.tr("🖼️ Tải ảnh lên từ Thư viện & Căn chỉnh", "🖼️ Upload Photo from Gallery & Crop"));
        options.add(Localized.tr("📸 Chụp ảnh mới bằng Camera & Căn chỉnh", "📸 Take Camera Photo & Crop"));
        if (hasExistingPhoto) {
            options.add(Localized.tr("✂️ Căn chỉnh lại ảnh hiện tại (Phóng to / Xoay)", "✂️ Re-adjust / Crop Current Photo"));
        }
        options.add(Localized.tr("🎭 Chọn biểu tượng Emoji & Sửa hồ sơ", "🎭 Pick Emoji Emblem & Edit Profile"));
        if (hasExistingPhoto) {
            options.add(Localized.tr("🗑️ Gỡ ảnh đại diện hiện tại", "🗑️ Remove Current Photo"));
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(Localized.tr("📷 Ảnh đại diện tài khoản @" + cur.username, "📷 Profile Picture (@" + cur.username + ")"))
                .setItems(options.toArray(new String[0]), (dlg, which) -> {
                    if (which == 0) {
                        launchGalleryAvatarPicker(cur.id, this::applyDirectUserAvatarPhoto);
                    } else if (which == 1) {
                        launchCameraAvatarCapture(cur.id, this::applyDirectUserAvatarPhoto);
                    } else if (hasExistingPhoto && which == 2) {
                        reopenExistingAvatarInCropper(cur.avatarPhotoPath, cur.id, this::applyDirectUserAvatarPhoto);
                    } else if ((hasExistingPhoto && which == 3) || (!hasExistingPhoto && which == 2)) {
                        showEditProfileDialog();
                    } else if (hasExistingPhoto && which == 4) {
                        AvatarImageManager.deleteAvatarFile(cur.avatarPhotoPath);
                        AuthManager.updateCurrentUserAvatarPhoto("");
                        Toast.makeText(requireContext(),
                                Localized.tr("Đã gỡ ảnh đại diện, chuyển về biểu tượng Emoji", "Removed custom photo"),
                                Toast.LENGTH_SHORT).show();
                        renderState();
                    }
                })
                .setNegativeButton(Localized.tr("Đóng", "Close"), null)
                .show();
    }

    private void showRegistrationAvatarChooser() {
        if (!isAdded()) return;
        final boolean hasPending = !pendingRegistrationAvatarPath.isEmpty();
        List<String> options = new ArrayList<>();
        options.add(Localized.tr("🖼️ Tải ảnh lên từ Thư viện & Căn chỉnh", "🖼️ Upload Photo & Crop"));
        options.add(Localized.tr("📸 Chụp ảnh bằng Camera & Căn chỉnh", "📸 Take Photo & Crop"));
        if (hasPending) {
            options.add(Localized.tr("✂️ Căn chỉnh lại ảnh vừa chọn", "✂️ Re-adjust Selected Photo"));
            options.add(Localized.tr("🗑️ Bỏ chọn ảnh vừa tải", "🗑️ Clear Selected Photo"));
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(Localized.tr("📷 Chọn ảnh đại diện đăng ký", "📷 Registration Profile Photo"))
                .setItems(options.toArray(new String[0]), (dlg, which) -> {
                    if (which == 0) {
                        launchGalleryAvatarPicker("reg", path -> {
                            pendingRegistrationAvatarPath = path;
                            renderState();
                        });
                    } else if (which == 1) {
                        launchCameraAvatarCapture("reg", path -> {
                            pendingRegistrationAvatarPath = path;
                            renderState();
                        });
                    } else if (hasPending && which == 2) {
                        reopenExistingAvatarInCropper(pendingRegistrationAvatarPath, "reg", path -> {
                            pendingRegistrationAvatarPath = path;
                            renderState();
                        });
                    } else if (hasPending && which == 3) {
                        AvatarImageManager.deleteAvatarFile(pendingRegistrationAvatarPath);
                        pendingRegistrationAvatarPath = "";
                        renderState();
                    }
                })
                .setNegativeButton(Localized.tr("Đóng", "Close"), null)
                .show();
    }

    /**
     * Modern Rounded Edit Profile + Photo Upload & Avatar Emoji Picker Dialog persisted to SQLite table users.
     */
    private void showEditProfileDialog() {
        UserAccount cur = AuthManager.getCurrentUser();
        if (cur == null || !isAdded()) return;

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null);
        AlertDialog dialog = DialogHelper.showCustomCardDialog(requireContext(), dialogView);

        TextView tvDialogTitle = dialogView.findViewById(R.id.tv_edit_dialog_title);
        View boxPreview = dialogView.findViewById(R.id.box_edit_avatar_preview);
        TextView tvAvatarPreview = dialogView.findViewById(R.id.tv_edit_avatar_preview);
        ImageView ivAvatarPhoto = dialogView.findViewById(R.id.iv_edit_avatar_photo);
        TextView tvAvatarLabel = dialogView.findViewById(R.id.tv_edit_avatar_label);
        TextView tvMeta = dialogView.findViewById(R.id.tv_edit_profile_meta);
        TextView btnUploadPhoto = dialogView.findViewById(R.id.btn_edit_upload_photo);
        TextView btnTakePhoto = dialogView.findViewById(R.id.btn_edit_take_photo);
        TextView btnRemovePhoto = dialogView.findViewById(R.id.btn_edit_remove_photo);
        LinearLayout boxAvatars = dialogView.findViewById(R.id.container_avatar_picker);
        TextView tvLblName = dialogView.findViewById(R.id.tv_lbl_edit_name);
        TextView tvLblEmail = dialogView.findViewById(R.id.tv_lbl_edit_email);
        EditText etName = dialogView.findViewById(R.id.et_edit_profile_name);
        EditText etEmail = dialogView.findViewById(R.id.et_edit_profile_email);
        TextView btnCancel = dialogView.findViewById(R.id.btn_edit_profile_cancel);
        TextView btnSave = dialogView.findViewById(R.id.btn_edit_profile_save);

        tvDialogTitle.setText(Localized.tr("✏️ Chỉnh sửa Hồ sơ Cá nhân", "✏️ Edit Personal Profile"));
        tvAvatarLabel.setText(Localized.tr("Ảnh đại diện hoặc biểu tượng du lịch", "Profile Photo or Travel Emblem"));
        tvMeta.setText("@" + cur.username + " • " + Localized.tr(cur.role.shortVi, cur.role.shortEn)
                + " • " + cur.authProvider.badgeLabel);
        btnUploadPhoto.setText(Localized.tr("🖼️ Tải ảnh lên", "🖼️ Upload Photo"));
        btnTakePhoto.setText(Localized.tr("📸 Chụp ảnh", "📸 Camera"));
        tvLblName.setText(Localized.tr("Họ và tên hiển thị", "Display Name"));
        tvLblEmail.setText(Localized.tr("Email liên hệ", "Contact Email"));
        btnCancel.setText(Localized.tr("Hủy", "Cancel"));
        btnSave.setText(Localized.tr("💾 Lưu thay đổi", "💾 Save Changes"));

        final String[] selectedEmoji = {cur.avatarEmoji};
        final String[] selectedPhotoPath = {cur.avatarPhotoPath};
        etName.setText(cur.fullName);
        etEmail.setText(cur.email);

        float d = getResources().getDisplayMetrics().density;
        int ringColor = ContextCompat.getColor(requireContext(), R.color.saigon_red);

        Runnable refreshAvatarPreview = () -> {
            boolean hasPhoto = AvatarImageManager.bindAvatarViews(
                    ivAvatarPhoto,
                    tvAvatarPreview,
                    selectedPhotoPath[0],
                    selectedEmoji[0],
                    (int) (56 * d * 2),
                    ringColor,
                    2.5f * d
            );
            btnRemovePhoto.setVisibility(hasPhoto ? View.VISIBLE : View.GONE);
        };

        Runnable renderEmojiChips = new Runnable() {
            @Override
            public void run() {
                boxAvatars.removeAllViews();
                for (String em : AVATAR_EMOJIS) {
                    TextView chip = new TextView(requireContext());
                    int sz = (int) (38 * d);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(sz, sz);
                    lp.setMarginEnd((int) (6 * d));
                    chip.setLayoutParams(lp);
                    chip.setGravity(Gravity.CENTER);
                    chip.setText(em);
                    chip.setTextSize(18f);
                    boolean active = selectedPhotoPath[0].isEmpty() && em.equals(selectedEmoji[0]);
                    chip.setBackgroundResource(active ? R.drawable.bg_profile_avatar_ring : R.drawable.bg_oauth_button);
                    chip.setOnClickListener(v -> {
                        selectedEmoji[0] = em;
                        selectedPhotoPath[0] = "";
                        refreshAvatarPreview.run();
                        run();
                    });
                    boxAvatars.addView(chip);
                }
            }
        };

        refreshAvatarPreview.run();
        renderEmojiChips.run();

        View.OnClickListener pickGalleryAction = v -> launchGalleryAvatarPicker(cur.id, path -> {
            selectedPhotoPath[0] = path;
            refreshAvatarPreview.run();
            renderEmojiChips.run();
        });
        boxPreview.setOnClickListener(v -> {
            if (!selectedPhotoPath[0].isEmpty()) {
                reopenExistingAvatarInCropper(selectedPhotoPath[0], cur.id, path -> {
                    selectedPhotoPath[0] = path;
                    refreshAvatarPreview.run();
                    renderEmojiChips.run();
                });
            } else {
                pickGalleryAction.onClick(v);
            }
        });
        btnUploadPhoto.setOnClickListener(pickGalleryAction);

        btnTakePhoto.setOnClickListener(v -> launchCameraAvatarCapture(cur.id, path -> {
            selectedPhotoPath[0] = path;
            refreshAvatarPreview.run();
            renderEmojiChips.run();
        }));

        btnRemovePhoto.setOnClickListener(v -> {
            selectedPhotoPath[0] = "";
            refreshAvatarPreview.run();
            renderEmojiChips.run();
        });

        dialogView.findViewById(R.id.btn_edit_profile_close).setOnClickListener(v -> dialog.dismiss());
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            if (selectedPhotoPath[0].isEmpty() && cur.hasAvatarPhoto()) {
                AvatarImageManager.deleteAvatarFile(cur.avatarPhotoPath);
            }
            ResultBox<UserAccount> res = AuthManager.updateCurrentUserProfile(
                    etName.getText().toString(),
                    etEmail.getText().toString(),
                    selectedEmoji[0],
                    selectedPhotoPath[0]
            );
            if (!res.isSuccess()) {
                Toast.makeText(requireContext(), res.getErrorMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            Toast.makeText(requireContext(),
                    Localized.tr("Đã cập nhật hồ sơ thành công", "Profile updated"),
                    Toast.LENGTH_SHORT).show();
            renderState();
        });
    }

    /**
     * Modern Rounded Change Password Dialog (PBKDF2-HMAC-SHA256) persisted to SQLite table users.
     */
    private void showChangePasswordDialog() {
        UserAccount cur = AuthManager.getCurrentUser();
        if (cur == null || !isAdded()) return;

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null);
        AlertDialog dialog = DialogHelper.showCustomCardDialog(requireContext(), dialogView);

        TextView tvTitle = dialogView.findViewById(R.id.tv_pwd_dialog_title);
        TextView tvSub = dialogView.findViewById(R.id.tv_pwd_dialog_subtitle);
        View boxCurrent = dialogView.findViewById(R.id.box_pwd_current);
        TextView tvLblCur = dialogView.findViewById(R.id.tv_lbl_pwd_current);
        TextView tvLblNew = dialogView.findViewById(R.id.tv_lbl_pwd_new);
        TextView tvLblConf = dialogView.findViewById(R.id.tv_lbl_pwd_confirm);
        EditText etCur = dialogView.findViewById(R.id.et_pwd_current);
        EditText etNew = dialogView.findViewById(R.id.et_pwd_new);
        EditText etConf = dialogView.findViewById(R.id.et_pwd_confirm);
        TextView btnToggleEye = dialogView.findViewById(R.id.btn_pwd_toggle_eye);
        TextView btnCancel = dialogView.findViewById(R.id.btn_pwd_cancel);
        TextView btnSave = dialogView.findViewById(R.id.btn_pwd_save);

        boolean isLocal = cur.authProvider == AuthProvider.LOCAL;
        boxCurrent.setVisibility(isLocal ? View.VISIBLE : View.GONE);

        tvTitle.setText(Localized.tr("🔑 Đổi mật khẩu bảo mật", "🔑 Change Security Password"));
        tvSub.setText(isLocal
                ? Localized.tr("Băm một chiều PBKDF2-HMAC-SHA256 trên SQLite", "PBKDF2-HMAC-SHA256 hashed in SQLite")
                : Localized.tr("Thiết lập mật khẩu nội bộ cho tài khoản " + cur.authProvider.badgeLabel,
                        "Set a local password for your " + cur.authProvider.badgeLabel + " account"));
        tvLblCur.setText(Localized.tr("Mật khẩu hiện tại", "Current Password"));
        tvLblNew.setText(Localized.tr("Mật khẩu mới (>= 6 ký tự)", "New Password (min 6 chars)"));
        tvLblConf.setText(Localized.tr("Xác nhận mật khẩu mới", "Confirm New Password"));
        btnCancel.setText(Localized.tr("Hủy", "Cancel"));
        btnSave.setText(Localized.tr("🔐 Cập nhật mật khẩu", "🔐 Update Password"));

        final boolean[] showPwd = {false};
        btnToggleEye.setOnClickListener(v -> {
            showPwd[0] = !showPwd[0];
            applyPasswordVisibility(etCur, showPwd[0]);
            applyPasswordVisibility(etNew, showPwd[0]);
            applyPasswordVisibility(etConf, showPwd[0]);
            btnToggleEye.setText(showPwd[0]
                    ? Localized.tr("🙈 Ẩn mật khẩu", "🙈 Hide Passwords")
                    : Localized.tr("👁️ Hiện mật khẩu", "👁️ Show Passwords"));
        });

        dialogView.findViewById(R.id.btn_pwd_close).setOnClickListener(v -> dialog.dismiss());
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String newPass = etNew.getText().toString();
            String confPass = etConf.getText().toString();
            if (!newPass.equals(confPass)) {
                Toast.makeText(requireContext(),
                        Localized.tr("Mật khẩu xác nhận không khớp!", "Confirmation password does not match!"),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            ResultBox<UserAccount> res = AuthManager.changeCurrentUserPassword(
                    etCur.getText().toString(),
                    newPass
            );
            if (!res.isSuccess()) {
                Toast.makeText(requireContext(), res.getErrorMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            Toast.makeText(requireContext(),
                    Localized.tr("Đã đổi mật khẩu bảo mật thành công!", "Password updated successfully!"),
                    Toast.LENGTH_SHORT).show();
            renderState();
        });
    }

    private void applyPasswordVisibility(EditText editText, boolean visible) {
        if (editText == null) return;
        int cursor = editText.getSelectionEnd();
        editText.setInputType(visible
                ? (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                : (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
        editText.setTypeface(Typeface.DEFAULT);
        if (cursor >= 0 && cursor <= editText.getText().length()) {
            editText.setSelection(cursor);
        }
    }

    private void renderState() {
        if (!isAdded() || rootView == null) return;
        bindToolbar();

        UserAccount user = AuthManager.getCurrentUser();
        UserRole role = AuthManager.getCurrentRole();

        // 1. Hero Profile Card
        TextView tvAvatar = rootView.findViewById(R.id.tv_profile_avatar);
        ImageView ivAvatarPhoto = rootView.findViewById(R.id.iv_profile_avatar_photo);
        TextView badgeCamera = rootView.findViewById(R.id.badge_profile_camera);
        TextView tvRoleBadge = rootView.findViewById(R.id.tv_profile_role_badge);
        TextView tvProviderBadge = rootView.findViewById(R.id.tv_profile_provider_badge);
        TextView tvTitle = rootView.findViewById(R.id.tv_auth_status_title);
        TextView tvRoleDesc = rootView.findViewById(R.id.tv_auth_role_desc);
        TextView tvJoined = rootView.findViewById(R.id.tv_profile_joined);
        View rowActions = rootView.findViewById(R.id.row_logged_in_actions);
        TextView btnUploadAvatar = rootView.findViewById(R.id.btn_upload_avatar);
        TextView btnEditProfile = rootView.findViewById(R.id.btn_edit_profile);
        TextView btnChangePwd = rootView.findViewById(R.id.btn_change_password);
        TextView btnConsole = rootView.findViewById(R.id.btn_open_admin_console);
        TextView btnLogout = rootView.findViewById(R.id.btn_logout);

        float d = getResources().getDisplayMetrics().density;
        int ringColor = ContextCompat.getColor(requireContext(), R.color.saigon_red);

        ExplorerRank rank = TravelPassportManager.currentRank();
        String scholarBadge = UserDataBackupManager.hasCulturalScholarBadge()
                ? "  •  🎓 " + Localized.tr("Học Giả Sài Gòn", "Cultural Scholar")
                : "";
        String rankLabel = rank.badgeEmoji + " " + Localized.tr(rank.titleVi, rank.titleEn) + scholarBadge;

        if (user != null) {
            AvatarImageManager.bindAvatarViews(
                    ivAvatarPhoto,
                    tvAvatar,
                    user.avatarPhotoPath,
                    user.avatarEmoji,
                    (int) (64 * d * 2),
                    ringColor,
                    2.5f * d
            );
            badgeCamera.setVisibility(View.VISIBLE);
            tvRoleBadge.setText(getRoleEmoji(role) + " " + Localized.tr(role.shortVi, role.shortEn));
            if (user.authProvider != AuthProvider.LOCAL) {
                tvProviderBadge.setVisibility(View.VISIBLE);
                tvProviderBadge.setText(user.authProvider.badgeLabel);
            } else {
                tvProviderBadge.setVisibility(View.VISIBLE);
                tvProviderBadge.setText("🔗 " + user.authProvider.badgeLabel);
            }
            tvTitle.setText(user.fullName);
            tvRoleDesc.setText("@" + user.username + (user.email.isEmpty() ? "" : " • " + user.email));
            String dateStr = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(new Date(user.createdAt));
            tvJoined.setText("📅 " + Localized.tr("Tham gia ", "Joined ") + dateStr + "  •  " + rankLabel);

            rowActions.setVisibility(View.VISIBLE);
            btnUploadAvatar.setText(Localized.tr("📷 Đổi ảnh", "📷 Photo"));
            btnEditProfile.setText(Localized.tr("✏️ Sửa hồ sơ", "✏️ Profile"));
            btnChangePwd.setText(Localized.tr("🔑 Mật khẩu", "🔑 Password"));
            btnConsole.setText(role == UserRole.ADMIN
                    ? Localized.tr("🛡️ Trung tâm Quản trị & RBAC", "🛡️ Admin & RBAC Console")
                    : Localized.tr("✍️ Trang Biên tập Nội dung (CMS)", "✍️ Content Editor Console"));
            btnLogout.setText(Localized.tr("🚪 Đăng xuất", "🚪 Sign Out"));
            btnConsole.setVisibility(role.canAccessConsole() ? View.VISIBLE : View.GONE);
        } else {
            AvatarImageManager.bindAvatarViews(ivAvatarPhoto, tvAvatar, "", "👤", (int) (64 * d * 2), ringColor, 2.5f * d);
            badgeCamera.setVisibility(View.VISIBLE);
            tvRoleBadge.setText("👤 " + Localized.tr("Khách vãng lai", "Guest Mode"));
            tvProviderBadge.setVisibility(View.GONE);
            tvTitle.setText(Localized.tr("Chưa đăng nhập tài khoản", "Browsing as Guest"));
            tvRoleDesc.setText(Localized.tr(
                    "Đăng nhập hoặc đăng ký nhanh với Google / Mạng xã hội để lưu Yêu thích, Lịch trình & Hộ chiếu số.",
                    "Sign in or quick-register with Google / Social accounts to save Favorites, Trips & Passport."));
            tvJoined.setText(rankLabel);
            rowActions.setVisibility(View.GONE);
        }

        View cardAdminPortal = rootView.findViewById(R.id.card_admin_portal_showcase);
        if (role.canAccessConsole()) {
            cardAdminPortal.setVisibility(View.VISIBLE);
            int attTotal = AttractionRepository.getAttractions().size();
            int foodTotal = FoodRepository.getSpecialtyFoods().size();
            int userTotal = AuthManager.getUsers().size();
            ((TextView) rootView.findViewById(R.id.tv_admin_portal_badge)).setText(
                    role == UserRole.ADMIN
                            ? Localized.tr("🛡️ QUẢN TRỊ HỆ THỐNG & CMS HÌNH ẢNH", "🛡️ ADMIN PORTAL & VISUAL CMS")
                            : Localized.tr("✍️ TRANG BIÊN TẬP HÌNH ẢNH & NỘI DUNG", "✍️ VISUAL CONTENT EDITOR"));
            ((TextView) rootView.findViewById(R.id.tv_admin_portal_title)).setText(
                    Localized.tr("Bảng Quản lý Dữ liệu & Minh họa", "Visual Management Dashboard"));
            ((TextView) rootView.findViewById(R.id.tv_admin_portal_desc)).setText(
                    Localized.tr(
                            attTotal + " địa điểm • " + foodTotal + " món ăn • " + userTotal + " tài khoản (kèm ảnh minh họa)",
                            attTotal + " places • " + foodTotal + " dishes • " + userTotal + " users (visual CMS)"));
            ((TextView) rootView.findViewById(R.id.tv_admin_portal_cta)).setText(
                    Localized.tr("🚀 Mở giao diện Quản lý ngay ›", "🚀 Open Management Console ›"));
        } else {
            cardAdminPortal.setVisibility(View.GONE);
        }

        // 2. Personal Travel Stats
        TextView tvStatsHeader = rootView.findViewById(R.id.tv_section_stats_title);
        tvStatsHeader.setText(Localized.tr("📊 Hành trình & Thống kê Cá nhân", "📊 Personal Travel Activity"));

        int favCount = FavoritesManager.getTotalCount();
        int tripCount = TripPlanManager.getStopIds().size();
        int stampCount = TravelPassportManager.getStamps().size();
        int reviewCount = user != null ? ReviewManager.countByUser(user.id) : 0;

        ((TextView) rootView.findViewById(R.id.tv_stat_saved_val)).setText(String.valueOf(favCount));
        ((TextView) rootView.findViewById(R.id.tv_stat_saved_lbl)).setText(Localized.tr("Đã lưu ›", "Saved ›"));
        ((TextView) rootView.findViewById(R.id.tv_stat_trip_val)).setText(String.valueOf(tripCount));
        ((TextView) rootView.findViewById(R.id.tv_stat_trip_lbl)).setText(Localized.tr("Điểm lịch trình ›", "Trip Stops ›"));
        ((TextView) rootView.findViewById(R.id.tv_stat_passport_val)).setText(String.valueOf(stampCount));
        ((TextView) rootView.findViewById(R.id.tv_stat_passport_lbl)).setText(
                rank.badgeEmoji + " " + Localized.tr("Hộ chiếu số ›", "Passport ›"));
        ((TextView) rootView.findViewById(R.id.tv_stat_reviews_val)).setText(String.valueOf(reviewCount));
        ((TextView) rootView.findViewById(R.id.tv_stat_reviews_lbl)).setText(Localized.tr("Đánh giá đã đăng", "Reviews Posted"));

        // 2.5 Wallet, Quiz & SQLite Backup
        int ticketCount = TicketWalletManager.getTicketCount();
        ((TextView) rootView.findViewById(R.id.tv_section_wallet_backup_title)).setText(
                Localized.tr("🎫 Ví Vé Điện tử QR & Sao lưu Dữ liệu SQLite", "🎫 QR E-Ticket Wallet & SQLite Backup"));
        ((TextView) rootView.findViewById(R.id.btn_profile_ticket_wallet)).setText(
                Localized.tr("🎫 Ví Vé QR (" + ticketCount + ")", "🎫 QR Wallet (" + ticketCount + ")"));
        ((TextView) rootView.findViewById(R.id.btn_profile_discovery_quiz)).setText(
                UserDataBackupManager.hasCulturalScholarBadge()
                        ? Localized.tr("🎓 Học Giả (" + UserDataBackupManager.getBestQuizScore() + "/10)",
                                "🎓 Scholar (" + UserDataBackupManager.getBestQuizScore() + "/10)")
                        : Localized.tr("🎯 Đố vui Văn hóa", "🎯 Heritage Quiz"));
        ((TextView) rootView.findViewById(R.id.btn_profile_backup_report)).setText(
                Localized.tr("💾 Sao lưu / Khôi phục JSON & Xuất Hồ sơ", "💾 Backup / Restore JSON & Trip Report"));

        // 3. Appearance (Light / Dark / System) & Language
        ((TextView) rootView.findViewById(R.id.tv_section_appearance_title)).setText(
                Localized.tr("🌗 Giao diện Sáng / Tối & Ngôn ngữ", "🌗 Theme (Light / Dark) & Language"));

        TextView btnLight = rootView.findViewById(R.id.btn_theme_light);
        TextView btnDark = rootView.findViewById(R.id.btn_theme_dark);
        TextView btnSystem = rootView.findViewById(R.id.btn_theme_system);
        btnLight.setText(Localized.tr("☀️ Sáng", "☀️ Light"));
        btnDark.setText(Localized.tr("🌙 Tối", "🌙 Dark"));
        btnSystem.setText(Localized.tr("📱 Hệ thống", "📱 System"));

        ThemeManager.ThemeMode currentTheme = ThemeManager.getCurrentMode();
        styleSelectableChip(btnLight, currentTheme == ThemeManager.ThemeMode.LIGHT);
        styleSelectableChip(btnDark, currentTheme == ThemeManager.ThemeMode.DARK);
        styleSelectableChip(btnSystem, currentTheme == ThemeManager.ThemeMode.SYSTEM);

        AppLanguage lang = LanguageManager.getCurrentLanguage();
        ((TextView) rootView.findViewById(R.id.tv_profile_lang_label)).setText(
                Localized.tr("🌐 Ngôn ngữ hiển thị", "🌐 App Language"));
        ((TextView) rootView.findViewById(R.id.tv_profile_lang_value)).setText(
                lang.flag + " " + lang.displayName + " ▾");

        // 4. Collapsible Switch Account & Original Social/Local Auth Card
        TextView btnToggleSwitch = rootView.findViewById(R.id.btn_toggle_switch_account);
        View cardAuthForm = rootView.findViewById(R.id.card_auth_form);
        if (user != null) {
            btnToggleSwitch.setVisibility(View.VISIBLE);
            btnToggleSwitch.setText(isSwitchAccountExpanded
                    ? Localized.tr("🔼 Thu gọn khung Chuyển đổi Tài khoản", "🔼 Collapse Account Switcher")
                    : Localized.tr("🔄 Chuyển đổi / Đăng nhập tài khoản khác ▾", "🔄 Switch or Add Another Account ▾"));
            cardAuthForm.setVisibility(isSwitchAccountExpanded ? View.VISIBLE : View.GONE);
        } else {
            btnToggleSwitch.setVisibility(View.GONE);
            cardAuthForm.setVisibility(View.VISIBLE);
        }

        TextView tabLogin = rootView.findViewById(R.id.tab_mode_login);
        TextView tabReg = rootView.findViewById(R.id.tab_mode_register);
        EditText etFull = rootView.findViewById(R.id.et_auth_fullname);
        EditText etUser = rootView.findViewById(R.id.et_auth_username);
        EditText etEmail = rootView.findViewById(R.id.et_auth_email);
        EditText etPass = rootView.findViewById(R.id.et_auth_password);
        TextView tvError = rootView.findViewById(R.id.tv_auth_error);
        TextView tvDemoHint = rootView.findViewById(R.id.tv_auth_demo_hint);
        TextView btnSubmit = rootView.findViewById(R.id.btn_auth_submit);
        TextView tvOAuthDiv = rootView.findViewById(R.id.tv_oauth_divider_label);

        tabLogin.setText(Localized.tr("Đăng nhập", "Sign In"));
        tabReg.setText(Localized.tr("Đăng ký Du khách", "Register"));
        styleSelectableChip(tabLogin, !isRegisterMode);
        styleSelectableChip(tabReg, isRegisterMode);

        etFull.setHint(Localized.tr("Họ và tên", "Full name"));
        etUser.setHint(isRegisterMode
                ? Localized.tr("Tên đăng nhập (ví dụ: nam_saigon)", "Username (e.g. nam_saigon)")
                : Localized.tr("Tên đăng nhập hoặc Email", "Username or Email"));
        etEmail.setHint("Email");
        etPass.setHint(Localized.tr("Mật khẩu", "Password"));
        tvDemoHint.setText(Localized.tr(
                "💡 Tài khoản mẫu: admin/admin123 • guide/guide123 • tourist/user123",
                "💡 Demo logins: admin/admin123 • guide/guide123 • tourist/user123"));

        if (localFormError != null && !localFormError.isEmpty()) {
            tvError.setText("⚠️ " + localFormError);
            tvError.setVisibility(View.VISIBLE);
        } else {
            tvError.setVisibility(View.GONE);
        }

        tvOAuthDiv.setText(isRegisterMode
                ? Localized.tr("  Hoặc đăng ký nhanh bằng  ", "  Or quick register with  ")
                : Localized.tr("  Hoặc đăng nhập bằng  ", "  Or sign in with  "));

        View rowRegAvatar = rootView.findViewById(R.id.row_register_avatar_upload);
        ImageView ivRegPhoto = rootView.findViewById(R.id.iv_reg_avatar_photo);
        TextView tvRegEmoji = rootView.findViewById(R.id.tv_reg_avatar_emoji);
        TextView tvRegLabel = rootView.findViewById(R.id.tv_reg_avatar_label);
        TextView btnRegPick = rootView.findViewById(R.id.btn_reg_pick_photo);
        rowRegAvatar.setVisibility(isRegisterMode ? View.VISIBLE : View.GONE);
        if (isRegisterMode) {
            boolean hasRegPhoto = AvatarImageManager.bindAvatarViews(
                    ivRegPhoto, tvRegEmoji, pendingRegistrationAvatarPath, "🧳",
                    (int) (42 * d * 2), ringColor, 2f * d
            );
            tvRegLabel.setText(hasRegPhoto
                    ? Localized.tr("✅ Đã chọn ảnh đại diện cá nhân", "✅ Custom profile photo ready")
                    : Localized.tr("Ảnh đại diện cá nhân (Tùy chọn)", "Profile photo (Optional)"));
            btnRegPick.setText(hasRegPhoto
                    ? Localized.tr("🔄 Đổi ảnh", "🔄 Change")
                    : Localized.tr("📤 Tải ảnh lên", "📤 Upload"));
        }

        CheckBox cbRemember = rootView.findViewById(R.id.cb_remember_login);
        TextView tvRememberLbl = rootView.findViewById(R.id.tv_remember_login_label);
        cbRemember.setChecked(AuthManager.isRememberMeEnabled());
        tvRememberLbl.setText(isRegisterMode
                ? Localized.tr("Lưu thông tin đăng nhập sau khi đăng ký", "Save login credentials after registration")
                : Localized.tr("Lưu thông tin đăng nhập cho lần sau", "Save login info for next time"));

        renderSavedLoginRows(etUser, etPass, cbRemember, ringColor, d);

        if (!isRegisterMode && user == null && !hasAutoFilledRememberedOnce) {
            AuthManager.SavedLoginItem primary = AuthManager.getPrimarySavedLogin();
            if (primary != null && etUser.getText().toString().trim().isEmpty()) {
                etUser.setText(primary.username);
                etPass.setText(primary.savedPassword);
                cbRemember.setChecked(true);
            }
            hasAutoFilledRememberedOnce = true;
        }

        etFull.setVisibility(isRegisterMode ? View.VISIBLE : View.GONE);
        etEmail.setVisibility(isRegisterMode ? View.VISIBLE : View.GONE);
        tvDemoHint.setVisibility(isRegisterMode ? View.GONE : View.VISIBLE);
        btnSubmit.setText(isRegisterMode
                ? Localized.tr("Tạo tài khoản Du khách", "Create Tourist Account")
                : Localized.tr("Đăng nhập ngay", "Sign In"));

        styleOriginalSocialButtons();

        // 5. 4-Tier RBAC Switcher, Permission Matrix & Audit Logs
        ((TextView) rootView.findViewById(R.id.tv_section_rbac_title)).setText(
                Localized.tr("⚡ Chuyển nhanh 4 Vai trò Mẫu (1 chạm)", "⚡ Quick 4-Tier Role Switcher (Demo)"));
        styleSelectableChip(rootView.findViewById(R.id.btn_demo_admin), role == UserRole.ADMIN);
        styleSelectableChip(rootView.findViewById(R.id.btn_demo_editor), role == UserRole.EDITOR);
        styleSelectableChip(rootView.findViewById(R.id.btn_demo_member), role == UserRole.MEMBER);
        styleSelectableChip(rootView.findViewById(R.id.btn_demo_guest), role == UserRole.GUEST);

        ((TextView) rootView.findViewById(R.id.tv_lbl_permissions_header)).setText(
                Localized.tr("🔑 Bảng Quyền hạn của " + role.titleVi + " (" + role.permissions.size() + " quyền):",
                        "🔑 Permissions for " + role.titleEn + " (" + role.permissions.size() + " granted):"));
        renderPermissionRows(role);

        ((TextView) rootView.findViewById(R.id.tv_lbl_recent_audits)).setText(
                Localized.tr("📋 Hoạt động Bảo mật Gần đây (SQLite audit_logs)",
                        "📋 Recent Security Activity (SQLite audit_logs)"));
        renderRecentAudits();
    }

    private void renderSavedLoginRows(
            EditText etUser,
            EditText etPass,
            CheckBox cbRemember,
            int ringColor,
            float d
    ) {
        View boxSection = rootView.findViewById(R.id.box_saved_logins_section);
        TextView tvLbl = rootView.findViewById(R.id.tv_saved_logins_label);
        LinearLayout container = rootView.findViewById(R.id.container_saved_logins);
        if (boxSection == null || container == null) return;

        container.removeAllViews();
        List<AuthManager.SavedLoginItem> savedList = AuthManager.getSavedLogins();
        if (isRegisterMode || savedList.isEmpty()) {
            boxSection.setVisibility(View.GONE);
            return;
        }

        boxSection.setVisibility(View.VISIBLE);
        tvLbl.setText(Localized.tr(
                "💾 Tài khoản đã lưu đăng nhập (chạm để vào ngay):",
                "💾 Saved login accounts (1-tap sign in):"));

        boolean darkActive = ThemeManager.isDarkActive(requireContext());
        int accent = ContextCompat.getColor(requireContext(), R.color.saigon_teal);

        for (AuthManager.SavedLoginItem item : savedList) {
            UserAccount acc = item.account;
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);

            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setCornerRadius(10f * d);
            bg.setColor(ContextCompat.getColor(requireContext(), R.color.surface_white));
            bg.setStroke((int) Math.max(1, d), withAlpha(accent, darkActive ? 0.45f : 0.30f));
            row.setBackground(bg);

            int ph = (int) (10 * d);
            int pv = (int) (7 * d);
            row.setPadding(ph, pv, ph, pv);
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rowLp.bottomMargin = (int) (6 * d);
            row.setLayoutParams(rowLp);

            int avatarPx = (int) (32 * d);
            Bitmap bmp = acc.hasAvatarPhoto()
                    ? AvatarImageManager.loadCircularBitmap(acc.avatarPhotoPath, avatarPx * 2, ringColor, 2f * d)
                    : null;
            if (bmp != null) {
                ImageView iv = new ImageView(requireContext());
                iv.setLayoutParams(new LinearLayout.LayoutParams(avatarPx, avatarPx));
                iv.setImageBitmap(bmp);
                row.addView(iv);
            } else {
                TextView tvEm = new TextView(requireContext());
                tvEm.setLayoutParams(new LinearLayout.LayoutParams(avatarPx, avatarPx));
                tvEm.setBackgroundResource(R.drawable.bg_profile_avatar_ring);
                tvEm.setGravity(Gravity.CENTER);
                tvEm.setText(acc.avatarEmoji);
                tvEm.setTextSize(15f);
                row.addView(tvEm);
            }

            LinearLayout mid = new LinearLayout(requireContext());
            mid.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams midLp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            midLp.setMarginStart((int) (9 * d));
            mid.setLayoutParams(midLp);

            TextView tvName = new TextView(requireContext());
            tvName.setText(acc.fullName);
            tvName.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            tvName.setTextSize(12.5f);
            tvName.setTypeface(null, Typeface.BOLD);
            mid.addView(tvName);

            TextView tvSub = new TextView(requireContext());
            tvSub.setText("@" + item.username + " • " + Localized.tr(acc.role.shortVi, acc.role.shortEn));
            tvSub.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            tvSub.setTextSize(11f);
            mid.addView(tvSub);

            row.addView(mid);

            TextView btnLoginFast = new TextView(requireContext());
            btnLoginFast.setText(Localized.tr("Vào ngay →", "Sign in →"));
            btnLoginFast.setTextColor(accent);
            btnLoginFast.setTextSize(11.5f);
            btnLoginFast.setTypeface(null, Typeface.BOLD);
            btnLoginFast.setPadding((int) (6 * d), (int) (4 * d), (int) (6 * d), (int) (4 * d));
            row.addView(btnLoginFast);

            TextView btnForget = new TextView(requireContext());
            int rmSz = (int) (26 * d);
            LinearLayout.LayoutParams rmLp = new LinearLayout.LayoutParams(rmSz, rmSz);
            rmLp.setMarginStart((int) (4 * d));
            btnForget.setLayoutParams(rmLp);
            btnForget.setBackgroundResource(R.drawable.bg_lavender_pill);
            btnForget.setGravity(Gravity.CENTER);
            btnForget.setText("✕");
            btnForget.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            btnForget.setTextSize(11f);
            btnForget.setOnClickListener(v -> {
                AuthManager.removeSavedLogin(acc.id);
                if (item.username.equalsIgnoreCase(etUser.getText().toString().trim())) {
                    etUser.setText("");
                    etPass.setText("");
                }
                Toast.makeText(requireContext(),
                        Localized.tr("Đã xóa thông tin đăng nhập đã lưu của @" + item.username,
                                "Removed saved login for @" + item.username),
                        Toast.LENGTH_SHORT).show();
                renderState();
            });
            row.addView(btnForget);

            row.setOnClickListener(v -> {
                ResultBox<UserAccount> fastRes = AuthManager.loginWithSavedAccount(acc.id);
                if (fastRes.isSuccess() && fastRes.getOrNull() != null) {
                    localFormError = null;
                    etPass.setText("");
                    isSwitchAccountExpanded = false;
                    Toast.makeText(requireContext(),
                            Localized.tr("Xin chào " + acc.fullName + "!", "Welcome back " + acc.fullName + "!"),
                            Toast.LENGTH_SHORT).show();
                    renderState();
                } else {
                    etUser.setText(item.username);
                    etPass.setText("");
                    cbRemember.setChecked(true);
                    localFormError = fastRes.getErrorMessage();
                    renderState();
                }
            });

            container.addView(row);
        }
    }

    private void renderPermissionRows(UserRole role) {
        LinearLayout box = rootView.findViewById(R.id.container_role_permissions);
        if (box == null) return;
        box.removeAllViews();
        float d = getResources().getDisplayMetrics().density;

        for (Permission p : role.permissions) {
            TextView pill = new TextView(requireContext());
            pill.setText("✓  " + Localized.tr(p.titleVi, p.titleEn));
            pill.setBackgroundResource(R.drawable.bg_stat_tile);
            pill.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            pill.setTextSize(12f);
            int ph = (int) (10 * d);
            int pv = (int) (6 * d);
            pill.setPadding(ph, pv, ph, pv);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = (int) (5 * d);
            pill.setLayoutParams(lp);
            box.addView(pill);
        }
    }

    private void renderRecentAudits() {
        LinearLayout box = rootView.findViewById(R.id.container_recent_audits);
        if (box == null) return;
        box.removeAllViews();

        List<AuditLogEntry> logs = AuthManager.getAuditLogs();
        int max = Math.min(3, logs.size());
        SimpleDateFormat fmt = new SimpleDateFormat("HH:mm dd/MM", Locale.getDefault());
        float d = getResources().getDisplayMetrics().density;

        for (int i = 0; i < max; i++) {
            AuditLogEntry log = logs.get(i);
            TextView row = new TextView(requireContext());
            String time = fmt.format(new Date(log.timestamp));
            row.setText("• [" + time + "] @" + log.actorUsername + ": "
                    + Localized.tr(log.actionVi, log.actionEn));
            row.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            row.setTextSize(11.5f);
            row.setPadding(0, (int) (3 * d), 0, (int) (3 * d));
            box.addView(row);
        }
    }

    private void styleSelectableChip(TextView chip, boolean selected) {
        if (chip == null) return;
        chip.setBackgroundResource(selected ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chip.setTextColor(ContextCompat.getColor(requireContext(),
                selected ? R.color.white : R.color.text_primary));
    }

    private String getRoleEmoji(UserRole role) {
        if (role == UserRole.ADMIN) return "🛡️";
        if (role == UserRole.EDITOR) return "✍️";
        if (role == UserRole.MEMBER) return "🧳";
        return "👤";
    }

    private String getProviderSymbol(AuthProvider provider) {
        if (provider == AuthProvider.GOOGLE) return "G";
        if (provider == AuthProvider.FACEBOOK) return "f";
        if (provider == AuthProvider.ZALO) return "Z";
        if (provider == AuthProvider.APPLE) return "A";
        return "🔐";
    }

    private String getProviderFieldLabel(AuthProvider provider) {
        if (provider == AuthProvider.GOOGLE) {
            return Localized.tr("Địa chỉ Gmail (vd: ten@gmail.com)", "Gmail address (e.g. name@gmail.com)");
        }
        if (provider == AuthProvider.ZALO) {
            return Localized.tr("Số điện thoại Zalo hoặc Email", "Zalo phone number or Email");
        }
        if (provider == AuthProvider.FACEBOOK) {
            return Localized.tr("Email hoặc Tên tài khoản Facebook", "Facebook Email or Handle");
        }
        if (provider == AuthProvider.APPLE) {
            return Localized.tr("Email Apple ID", "Apple ID Email");
        }
        return "Email";
    }

    private String getProviderHint(AuthProvider provider) {
        if (provider == AuthProvider.GOOGLE) {
            return Localized.tr("ten@gmail.com", "name@gmail.com");
        }
        if (provider == AuthProvider.ZALO) {
            return Localized.tr("0909123456 hoặc email@...", "0909123456 or email@...");
        }
        if (provider == AuthProvider.FACEBOOK) {
            return Localized.tr("ten.tai.khoan hoặc email@...", "username or email@...");
        }
        if (provider == AuthProvider.APPLE) {
            return Localized.tr("ten@icloud.com", "name@icloud.com");
        }
        return "name@example.com";
    }

    private AppNavigator getNavigator() {
        return getActivity() instanceof AppNavigator ? (AppNavigator) getActivity() : null;
    }

    private static final class SimpleErrorClearWatcher implements TextWatcher {
        private final Runnable onChanged;

        SimpleErrorClearWatcher(Runnable onChanged) {
            this.onChanged = onChanged;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (onChanged != null) onChanged.run();
        }

        @Override
        public void afterTextChanged(Editable s) {}
    }
}
