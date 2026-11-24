package com.example.nhom_15_dulichtphcm.ui.screens;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
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
import com.example.nhom_15_dulichtphcm.data.ai.GeminiService;
import com.example.nhom_15_dulichtphcm.data.local.AuthManager;
import com.example.nhom_15_dulichtphcm.data.local.ReviewManager;
import com.example.nhom_15_dulichtphcm.data.model.Attraction;
import com.example.nhom_15_dulichtphcm.data.model.AttractionCategory;
import com.example.nhom_15_dulichtphcm.data.model.AuditLogEntry;
import com.example.nhom_15_dulichtphcm.data.model.FoodCategory;
import com.example.nhom_15_dulichtphcm.data.model.Permission;
import com.example.nhom_15_dulichtphcm.data.model.PlaceReview;
import com.example.nhom_15_dulichtphcm.data.model.PublicUtility;
import com.example.nhom_15_dulichtphcm.data.model.ResultBox;
import com.example.nhom_15_dulichtphcm.data.model.SpecialtyFood;
import com.example.nhom_15_dulichtphcm.data.model.UserAccount;
import com.example.nhom_15_dulichtphcm.data.model.UserRole;
import com.example.nhom_15_dulichtphcm.data.model.UtilityCategory;
import com.example.nhom_15_dulichtphcm.data.repository.AttractionRepository;
import com.example.nhom_15_dulichtphcm.data.repository.FoodRepository;
import com.example.nhom_15_dulichtphcm.data.repository.UtilityRepository;
import com.example.nhom_15_dulichtphcm.ui.AppNavigator;
import com.example.nhom_15_dulichtphcm.ui.components.ChipViewFactory;
import com.example.nhom_15_dulichtphcm.ui.components.CompactHeaderHelper;
import com.example.nhom_15_dulichtphcm.ui.components.DialogHelper;
import com.example.nhom_15_dulichtphcm.util.AvatarImageManager;
import com.example.nhom_15_dulichtphcm.util.ImageLoader;
import com.example.nhom_15_dulichtphcm.util.Localized;
import com.example.nhom_15_dulichtphcm.util.SearchUtils;
import com.example.nhom_15_dulichtphcm.util.SeedImageResolver;
import com.example.nhom_15_dulichtphcm.util.ThemeManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Pure Java + XML Visual Admin & Content Management Console backed by SQLite saigon_travel.db,
 * featuring rich photo thumbnails, 2D vector illustrations, live KPI cards, instant search,
 * and illustrated CMS upload/preset dialogs.
 */
public final class AdminDashboardFragment extends Fragment {

    private static final int[] ATTRACTION_PRESET_DRAWABLES = {
            R.drawable.attraction_notre_dame,
            R.drawable.attraction_ben_thanh_market,
            R.drawable.attraction_independence_palace,
            R.drawable.attraction_landmark_81,
            R.drawable.attraction_thu_thiem_bason,
            R.drawable.attraction_saigon_opera_house,
            R.drawable.attraction_central_post_office,
            R.drawable.attraction_jade_emperor_pagoda,
            R.drawable.attraction_dai_nam,
            R.drawable.attraction_tuong_chua_kito,
            R.drawable.attraction_bach_dinh,
            R.drawable.attraction_bai_sau_vung_tau
    };

    private static final int[] FOOD_PRESET_DRAWABLES = {
            R.drawable.food_com_tam,
            R.drawable.food_banh_mi,
            R.drawable.food_pho_saigon,
            R.drawable.food_hu_tieu,
            R.drawable.food_banh_xeo,
            R.drawable.food_banh_khot,
            R.drawable.food_oc_saigon,
            R.drawable.food_ca_phe_vot,
            R.drawable.food_banh_beo_bi
    };

    private static final String[] PROVINCE_OPTIONS = {
            "TP. Hồ Chí Minh",
            "Bình Dương",
            "Bà Rịa - Vũng Tàu"
    };

    private int selectedTab = 0; // 0=Attractions, 1=Foods, 2=Utilities, 3=Reviews, 4=Users, 5=AuditLogs
    private String searchQuery = "";
    private View rootView;
    private LinearLayout tabsBox;
    private TextView tvStats;
    private TextView btnCreate;
    private RecyclerView rv;
    private View boxEmpty;
    private TextView tvEmptyTitle;
    private TextView tvEmptySub;

    private String pendingCmsImagePrefix = "cms";
    private Consumer<String> pendingCmsImageSavedCallback = null;

    private final ActivityResultLauncher<String> cmsGalleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null || !isAdded()) return;
                String savedPath = ImageLoader.saveCmsIllustrationFromUri(requireContext(), pendingCmsImagePrefix, uri);
                if (savedPath == null) {
                    Toast.makeText(requireContext(),
                            Localized.tr("Không thể đọc ảnh vừa chọn", "Failed to load selected image"),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (pendingCmsImageSavedCallback != null) {
                    pendingCmsImageSavedCallback.accept(savedPath);
                }
            });

    private final ActivityResultLauncher<Void> cmsCameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap == null || !isAdded()) return;
                String savedPath = ImageLoader.saveCmsIllustrationFromBitmap(requireContext(), pendingCmsImagePrefix, bitmap);
                if (savedPath == null) {
                    Toast.makeText(requireContext(),
                            Localized.tr("Không thể lưu ảnh chụp", "Failed to save captured photo"),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (pendingCmsImageSavedCallback != null) {
                    pendingCmsImageSavedCallback.accept(savedPath);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
        tabsBox = rootView.findViewById(R.id.container_admin_tabs);
        tvStats = rootView.findViewById(R.id.tv_admin_stats);
        btnCreate = rootView.findViewById(R.id.btn_admin_create);
        rv = rootView.findViewById(R.id.rv_admin_items);
        boxEmpty = rootView.findViewById(R.id.box_admin_empty);
        tvEmptyTitle = rootView.findViewById(R.id.tv_admin_empty_title);
        tvEmptySub = rootView.findViewById(R.id.tv_admin_empty_sub);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        CompactHeaderHelper.bindSubscreenToolbar(
                rootView,
                Localized.tr("🛡️ Trung tâm Quản trị & CMS", "🛡️ Visual Admin & CMS Console"),
                getNavigator(),
                AuthManager.hasPermission(Permission.CONFIGURE_SYSTEM) ? "⚙️ Cloud AI" : null,
                this::showCloudConfigDialog
        );

        // Bind 4 Interactive KPI Tiles on the Hero Banner
        rootView.findViewById(R.id.kpi_tile_attractions).setOnClickListener(v -> switchTab(0));
        rootView.findViewById(R.id.kpi_tile_foods).setOnClickListener(v -> switchTab(1));
        rootView.findViewById(R.id.kpi_tile_reviews).setOnClickListener(v -> switchTab(3));
        rootView.findViewById(R.id.kpi_tile_users).setOnClickListener(v -> {
            if (AuthManager.hasPermission(Permission.MANAGE_USERS)) {
                switchTab(4);
            } else {
                Toast.makeText(requireContext(),
                        Localized.tr("Chỉ Quản trị viên (Admin) mới quản lý danh sách Tài khoản",
                                "Only Administrators can manage user accounts"),
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Instant Search Bar
        EditText etSearch = rootView.findViewById(R.id.et_admin_search);
        TextView btnClearSearch = rootView.findViewById(R.id.btn_admin_search_clear);
        etSearch.setHint(Localized.tr(
                "Tìm nhanh tên địa điểm, món ăn, tài khoản...",
                "Quick filter places, dishes, users, logs..."));
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s != null ? s.toString().trim() : "";
                btnClearSearch.setVisibility(searchQuery.isEmpty() ? View.GONE : View.VISIBLE);
                renderActiveTabListOnly();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        btnClearSearch.setOnClickListener(v -> etSearch.setText(""));

        renderDashboard();
        return rootView;
    }

    private void switchTab(int idx) {
        if (selectedTab == idx) return;
        selectedTab = idx;
        renderDashboard();
    }

    private void showCloudConfigDialog() {
        float d = getResources().getDisplayMetrics().density;
        LinearLayout wrap = new LinearLayout(requireContext());
        wrap.setOrientation(LinearLayout.VERTICAL);
        int padH = (int) (20 * d);
        int padV = (int) (12 * d);
        wrap.setPadding(padH, padV, padH, (int) (4 * d));

        EditText input = new EditText(requireContext());
        input.setHint(Localized.tr("Nhập Gemini API Key (tùy chọn)", "Enter Gemini API Key (optional)"));
        input.setText(GeminiService.getUserApiKey());
        input.setBackgroundResource(R.drawable.bg_search_box);
        input.setPadding((int) (12 * d), (int) (10 * d), (int) (12 * d), (int) (10 * d));
        wrap.addView(input);

        new AlertDialog.Builder(requireContext())
                .setTitle(Localized.tr("⚙️ Cấu hình Trợ lý Đám mây (SQLite app_settings)", "⚙️ Cloud AI Key (SQLite app_settings)"))
                .setView(wrap)
                .setPositiveButton(Localized.tr("Lưu khóa", "Save Key"), (dlg, w) -> {
                    GeminiService.saveApiKey(input.getText().toString());
                    Toast.makeText(requireContext(),
                            Localized.tr("Đã lưu vào bảng app_settings!", "Saved to SQLite app_settings!"),
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(Localized.tr("Đóng", "Close"), null)
                .show();
    }

    private void bindHeroBannerAndKpis() {
        if (!isAdded() || rootView == null) return;
        UserAccount cur = AuthManager.getCurrentUser();
        UserRole role = AuthManager.getCurrentRole();

        TextView tvRolePill = rootView.findViewById(R.id.tv_admin_hero_role_pill);
        TextView tvGreeting = rootView.findViewById(R.id.tv_admin_hero_greeting);
        TextView tvSub = rootView.findViewById(R.id.tv_admin_hero_subtitle);

        tvRolePill.setText(role == UserRole.ADMIN
                ? Localized.tr("🛡️ QUẢN TRỊ VIÊN TOÀN QUYỀN (RBAC)", "🛡️ FULL ADMINISTRATOR ACCESS")
                : Localized.tr("✍️ BIÊN TẬP VIÊN NỘI DUNG (CMS)", "✍️ CONTENT EDITOR CONSOLE"));
        String adminName = cur != null ? cur.fullName : Localized.tr("Quản trị viên", "Administrator");
        tvGreeting.setText(Localized.tr("Xin chào, " + adminName, "Welcome, " + adminName));
        tvSub.setText(Localized.tr(
                "Quản lý hình ảnh minh họa, địa danh, ẩm thực & phân quyền trên SQLite",
                "Visual media, heritage places, cuisine & RBAC management on SQLite"));

        int attCount = AttractionRepository.getAttractions().size();
        int foodCount = FoodRepository.getSpecialtyFoods().size();
        int revCount = ReviewManager.getReviews().size();
        int userCount = AuthManager.getUsers().size();

        ((TextView) rootView.findViewById(R.id.tv_kpi_attractions_val)).setText(String.valueOf(attCount));
        ((TextView) rootView.findViewById(R.id.tv_kpi_attractions_lbl)).setText(Localized.tr("🏛️ Địa điểm", "🏛️ Places"));
        ((TextView) rootView.findViewById(R.id.tv_kpi_foods_val)).setText(String.valueOf(foodCount));
        ((TextView) rootView.findViewById(R.id.tv_kpi_foods_lbl)).setText(Localized.tr("🍜 Ẩm thực", "🍜 Cuisine"));
        ((TextView) rootView.findViewById(R.id.tv_kpi_reviews_val)).setText(String.valueOf(revCount));
        ((TextView) rootView.findViewById(R.id.tv_kpi_reviews_lbl)).setText(Localized.tr("⭐ Đánh giá", "⭐ Reviews"));
        ((TextView) rootView.findViewById(R.id.tv_kpi_users_val)).setText(String.valueOf(userCount));
        ((TextView) rootView.findViewById(R.id.tv_kpi_users_lbl)).setText(Localized.tr("👥 Tài khoản", "👥 Users"));
    }

    private void renderDashboard() {
        if (!isAdded() || rootView == null) return;
        bindHeroBannerAndKpis();

        tabsBox.removeAllViews();
        boolean isAdmin = AuthManager.hasPermission(Permission.MANAGE_USERS);

        int attCount = AttractionRepository.getAttractions().size();
        int foodCount = FoodRepository.getSpecialtyFoods().size();
        int utilCount = UtilityRepository.getUtilities().size();
        int revCount = ReviewManager.getReviews().size();
        int userCount = AuthManager.getUsers().size();
        int logCount = AuthManager.getAuditLogs().size();

        String[] labels = isAdmin
                ? new String[]{
                Localized.tr("🏛️ Địa điểm (" + attCount + ")", "🏛️ Places (" + attCount + ")"),
                Localized.tr("🍜 Ẩm thực (" + foodCount + ")", "🍜 Foods (" + foodCount + ")"),
                Localized.tr("🚇 Tiện ích (" + utilCount + ")", "🚇 Utilities (" + utilCount + ")"),
                Localized.tr("⭐ Kiểm duyệt (" + revCount + ")", "⭐ Reviews (" + revCount + ")"),
                Localized.tr("👥 Tài khoản (" + userCount + ")", "👥 Users (" + userCount + ")"),
                Localized.tr("📋 Nhật ký (" + logCount + ")", "📋 Logs (" + logCount + ")")
        }
                : new String[]{
                Localized.tr("🏛️ Địa điểm (" + attCount + ")", "🏛️ Places (" + attCount + ")"),
                Localized.tr("🍜 Ẩm thực (" + foodCount + ")", "🍜 Foods (" + foodCount + ")"),
                Localized.tr("🚇 Tiện ích (" + utilCount + ")", "🚇 Utilities (" + utilCount + ")"),
                Localized.tr("⭐ Kiểm duyệt (" + revCount + ")", "⭐ Reviews (" + revCount + ")")
        };

        if (!isAdmin && selectedTab >= labels.length) {
            selectedTab = 0;
        }

        for (int i = 0; i < labels.length; i++) {
            final int idx = i;
            tabsBox.addView(ChipViewFactory.createChip(
                    requireContext(),
                    labels[i],
                    selectedTab == idx,
                    0,
                    () -> {
                        selectedTab = idx;
                        renderDashboard();
                    }
            ));
        }

        renderActiveTabListOnly();
    }

    private void renderActiveTabListOnly() {
        if (!isAdded() || rootView == null) return;
        boolean isAdmin = AuthManager.hasPermission(Permission.MANAGE_USERS);
        if (selectedTab == 0) renderAttractionsCms();
        else if (selectedTab == 1) renderFoodsCms();
        else if (selectedTab == 2) renderUtilitiesCms();
        else if (selectedTab == 3) renderReviewsModeration();
        else if (selectedTab == 4 && isAdmin) renderUsersAdmin();
        else renderAuditLogs();
    }

    private void updateEmptyState(int visibleCount) {
        if (boxEmpty == null || rv == null) return;
        if (visibleCount == 0) {
            rv.setVisibility(View.GONE);
            boxEmpty.setVisibility(View.VISIBLE);
            tvEmptyTitle.setText(searchQuery.isEmpty()
                    ? Localized.tr("Chưa có dữ liệu trong mục này", "No items in this section yet")
                    : Localized.tr("Không tìm thấy kết quả cho \"" + searchQuery + "\"",
                            "No matches for \"" + searchQuery + "\""));
            tvEmptySub.setText(Localized.tr(
                    "Thử từ khóa khác hoặc bấm nút '+ Thêm' ở góc phải để bổ sung mới.",
                    "Try another keyword or tap '+ Add' above to create a new record."));
        } else {
            rv.setVisibility(View.VISIBLE);
            boxEmpty.setVisibility(View.GONE);
        }
    }

    // =========================================================================
    // TAB 0: ATTRACTIONS (Illustrated Photo Thumbnails + Visual Editor)
    // =========================================================================

    private void renderAttractionsCms() {
        List<Attraction> all = AttractionRepository.getAttractions();
        List<Attraction> filtered = new ArrayList<>();
        for (Attraction a : all) {
            String hay = a.nameVi + " " + a.nameEn + " " + a.district + " " + a.province + " " + a.category.displayNameVi;
            if (SearchUtils.matchesAllTokens(hay, searchQuery)) {
                filtered.add(a);
            }
        }

        tvStats.setText(Localized.tr(
                "🏛️ Hiển thị " + filtered.size() + "/" + all.size() + " địa điểm du lịch",
                "🏛️ Showing " + filtered.size() + "/" + all.size() + " attractions"));
        btnCreate.setVisibility(View.VISIBLE);
        btnCreate.setText(Localized.tr("➕ Thêm địa điểm", "➕ Add Place"));
        btnCreate.setOnClickListener(v -> showEditAttractionDialog(null));

        boolean canDelete = AuthManager.hasPermission(Permission.DELETE_CONTENT);
        List<CmsRow> rows = new ArrayList<>();
        for (Attraction a : filtered) {
            CmsRow row = CmsRow.forAttraction(
                    a,
                    Localized.tr(a.category.displayNameVi, a.category.displayName),
                    a.ticketPrice != null && !a.ticketPrice.isEmpty() ? a.ticketPrice : "Miễn phí",
                    Localized.tr(a.nameVi, a.nameEn),
                    "📍 " + a.district + " • " + a.province
                            + (a.openingHours != null && !a.openingHours.isEmpty() ? " • 🕒 " + a.openingHours : ""),
                    () -> {
                        AppNavigator nav = getNavigator();
                        if (nav != null) nav.openAttractionDetail(a.id);
                    },
                    () -> showEditAttractionDialog(a),
                    canDelete ? () -> confirmDelete(
                            Localized.tr("Xóa địa điểm \"" + a.nameVi + "\"?", "Delete \"" + a.nameEn + "\"?"),
                            () -> {
                                AttractionRepository.deleteAttraction(a.id);
                                renderDashboard();
                            }) : null
            );
            rows.add(row);
        }
        rv.setAdapter(new GenericCmsAdapter(rows));
        updateEmptyState(rows.size());
    }

    private void showEditAttractionDialog(@Nullable Attraction existing) {
        if (!isAdded()) return;
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_admin_cms_editor, null);
        AlertDialog dialog = DialogHelper.showCustomCardDialog(requireContext(), dialogView);

        TextView tvTitle = dialogView.findViewById(R.id.tv_cms_editor_title);
        ImageView ivPreview = dialogView.findViewById(R.id.iv_cms_editor_preview);
        TextView tvPreviewBadge = dialogView.findViewById(R.id.tv_cms_preview_badge);
        TextView btnUpload = dialogView.findViewById(R.id.btn_cms_upload_gallery);
        TextView btnCamera = dialogView.findViewById(R.id.btn_cms_take_camera);
        LinearLayout boxPresets = dialogView.findViewById(R.id.container_cms_image_presets);
        EditText etImageUrl = dialogView.findViewById(R.id.et_cms_image_url);
        LinearLayout boxCats = dialogView.findViewById(R.id.container_cms_categories);
        View boxProvSection = dialogView.findViewById(R.id.box_cms_province_section);
        LinearLayout boxProvs = dialogView.findViewById(R.id.container_cms_provinces);
        EditText etVi = dialogView.findViewById(R.id.et_cms_name_vi);
        EditText etEn = dialogView.findViewById(R.id.et_cms_name_en);
        EditText etDistrict = dialogView.findViewById(R.id.et_cms_district);
        EditText etPrice = dialogView.findViewById(R.id.et_cms_extra_meta);
        EditText etSummary = dialogView.findViewById(R.id.et_cms_summary);
        TextView btnCancel = dialogView.findViewById(R.id.btn_cms_cancel);
        TextView btnSave = dialogView.findViewById(R.id.btn_cms_save);

        tvTitle.setText(existing != null
                ? Localized.tr("✏️ Chỉnh sửa Địa điểm & Ảnh minh họa", "✏️ Edit Place & Illustration")
                : Localized.tr("➕ Thêm Địa điểm Mới kèm Minh họa", "➕ Add Illustrated Attraction"));
        etPrice.setHint(Localized.tr("🎫 Giá vé (VD: Miễn phí)", "🎫 Ticket price"));
        etSummary.setHint(Localized.tr("📝 Giới thiệu ngắn / Điểm đặc sắc...", "📝 Short highlight summary..."));
        boxProvSection.setVisibility(View.VISIBLE);

        final AttractionCategory[] selectedCat = {
                existing != null ? existing.category : AttractionCategory.CULTURAL
        };
        final String[] selectedProvince = {
                existing != null && existing.province != null && !existing.province.isEmpty()
                        ? existing.province : PROVINCE_OPTIONS[0]
        };
        final int[] selectedResId = {
                existing != null
                        ? SeedImageResolver.resolveAttractionDrawable(requireContext().getResources(), existing)
                        : R.drawable.attraction_notre_dame
        };
        final String[] customSourceUrl = {
                existing != null && existing.imageUrl != null ? existing.imageUrl : ""
        };

        if (existing != null) {
            etVi.setText(existing.nameVi);
            etEn.setText(existing.nameEn);
            etDistrict.setText(existing.district);
            etPrice.setText(existing.ticketPrice);
            etSummary.setText(existing.shortSummary);
            if (customSourceUrl[0].startsWith("http") && !SeedImageResolver.isCuratedAttractionId(existing.id)) {
                etImageUrl.setText(customSourceUrl[0]);
            }
        } else {
            etDistrict.setText("Quận 1");
            etPrice.setText("Miễn phí");
            customSourceUrl[0] = ImageLoader.buildDrawableUri(getResources(), selectedResId[0]);
        }

        Runnable refreshPreview = () -> {
            String typedUrl = etImageUrl.getText().toString().trim();
            String effectiveUrl = !typedUrl.isEmpty() ? typedUrl : customSourceUrl[0];
            ImageLoader.load(ivPreview, selectedResId[0], effectiveUrl);
            if (effectiveUrl != null && (effectiveUrl.startsWith("/") || effectiveUrl.startsWith("file://"))) {
                tvPreviewBadge.setText(Localized.tr("📸 Đang dùng ảnh tải lên từ thiết bị", "📸 Custom uploaded device photo"));
            } else if (!typedUrl.isEmpty()) {
                tvPreviewBadge.setText(Localized.tr("🌐 Đang dùng ảnh minh họa từ URL", "🌐 Remote URL illustration"));
            } else {
                tvPreviewBadge.setText(Localized.tr("🖼️ Ảnh minh họa hiển thị trên ứng dụng", "🖼️ Active app illustration"));
            }
        };

        Runnable renderPresetThumbnails = new Runnable() {
            @Override
            public void run() {
                boxPresets.removeAllViews();
                for (int resId : ATTRACTION_PRESET_DRAWABLES) {
                    String uriToken = ImageLoader.buildDrawableUri(getResources(), resId);
                    boolean isSelected = (customSourceUrl[0] != null && customSourceUrl[0].equals(uriToken))
                            || ((customSourceUrl[0] == null || !customSourceUrl[0].startsWith("/"))
                            && etImageUrl.getText().toString().trim().isEmpty()
                            && selectedResId[0] == resId);
                    boxPresets.addView(buildPresetThumbnailTile(resId, isSelected, () -> {
                        selectedResId[0] = resId;
                        customSourceUrl[0] = uriToken;
                        etImageUrl.setText("");
                        refreshPreview.run();
                        run();
                    }));
                }
            }
        };

        Runnable renderCategoryChips = new Runnable() {
            @Override
            public void run() {
                boxCats.removeAllViews();
                for (AttractionCategory cat : AttractionCategory.values()) {
                    if (cat == AttractionCategory.ALL) continue;
                    boxCats.addView(ChipViewFactory.createChip(
                            requireContext(),
                            Localized.tr(cat.displayNameVi, cat.displayName),
                            selectedCat[0] == cat,
                            0,
                            () -> {
                                selectedCat[0] = cat;
                                run();
                            }
                    ));
                }
            }
        };

        Runnable renderProvinceChips = new Runnable() {
            @Override
            public void run() {
                boxProvs.removeAllViews();
                for (String prov : PROVINCE_OPTIONS) {
                    boxProvs.addView(ChipViewFactory.createChip(
                            requireContext(),
                            prov,
                            prov.equalsIgnoreCase(selectedProvince[0]),
                            0,
                            () -> {
                                selectedProvince[0] = prov;
                                run();
                            }
                    ));
                }
            }
        };

        refreshPreview.run();
        renderPresetThumbnails.run();
        renderCategoryChips.run();
        renderProvinceChips.run();

        btnUpload.setOnClickListener(v -> launchCmsGallery("place", path -> {
            customSourceUrl[0] = path;
            etImageUrl.setText("");
            refreshPreview.run();
            renderPresetThumbnails.run();
        }));
        btnCamera.setOnClickListener(v -> launchCmsCamera("place", path -> {
            customSourceUrl[0] = path;
            etImageUrl.setText("");
            refreshPreview.run();
            renderPresetThumbnails.run();
        }));
        etImageUrl.addTextChangedListener(new SimpleWatcher(refreshPreview));

        dialogView.findViewById(R.id.btn_cms_editor_close).setOnClickListener(v -> dialog.dismiss());
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String vi = etVi.getText().toString().trim();
            if (vi.isEmpty()) {
                Toast.makeText(requireContext(),
                        Localized.tr("Vui lòng nhập tên địa điểm Tiếng Việt", "Please enter Vietnamese name"),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            String en = etEn.getText().toString().trim();
            if (en.isEmpty()) en = vi;
            String district = etDistrict.getText().toString().trim();
            if (district.isEmpty()) district = "Quận 1";
            String price = etPrice.getText().toString().trim();
            if (price.isEmpty()) price = "Miễn phí";
            String summary = etSummary.getText().toString().trim();
            if (summary.isEmpty()) summary = "Điểm tham quan nổi bật được quản lý qua Trung tâm CMS SQLite.";

            String typedUrl = etImageUrl.getText().toString().trim();
            String finalImageUrl = !typedUrl.isEmpty() ? typedUrl : customSourceUrl[0];

            Attraction item = new Attraction(
                    existing != null ? existing.id : "cms_" + UUID.randomUUID().toString().substring(0, 6),
                    en,
                    vi,
                    selectedCat[0],
                    finalImageUrl,
                    selectedResId[0],
                    district,
                    existing != null ? existing.address : district + ", " + selectedProvince[0],
                    existing != null ? existing.latitude : 10.7750,
                    existing != null ? existing.longitude : 106.7000,
                    price,
                    existing != null ? existing.openingHours : "08:00 - 21:00",
                    summary,
                    existing != null ? existing.history : summary,
                    existing != null ? existing.culture : "Không gian trải nghiệm văn hóa - du lịch đặc sắc.",
                    existing != null ? existing.foreignerTips : "Nên ghé thăm vào buổi sáng hoặc chiều mát để chụp ảnh đẹp nhất.",
                    selectedProvince[0]
            );
            ResultBox<Attraction> res = AttractionRepository.upsertAttraction(item);
            if (!res.isSuccess()) {
                Toast.makeText(requireContext(), res.getErrorMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            Toast.makeText(requireContext(),
                    Localized.tr("✅ Đã lưu địa điểm & hình ảnh minh họa!", "✅ Saved attraction & illustration!"),
                    Toast.LENGTH_SHORT).show();
            renderDashboard();
        });
    }

    // =========================================================================
    // TAB 1: SPECIALTY FOODS (Illustrated Dish Photos + Visual Editor)
    // =========================================================================

    private void renderFoodsCms() {
        List<SpecialtyFood> all = FoodRepository.getSpecialtyFoods();
        List<SpecialtyFood> filtered = new ArrayList<>();
        for (SpecialtyFood f : all) {
            String hay = f.nameVi + " " + f.nameEn + " " + f.category.labelVi + " " + f.shortDescription;
            if (SearchUtils.matchesAllTokens(hay, searchQuery)) {
                filtered.add(f);
            }
        }

        tvStats.setText(Localized.tr(
                "🍜 Hiển thị " + filtered.size() + "/" + all.size() + " món đặc sản",
                "🍜 Showing " + filtered.size() + "/" + all.size() + " specialty dishes"));
        btnCreate.setVisibility(View.VISIBLE);
        btnCreate.setText(Localized.tr("➕ Thêm món ngon", "➕ Add Dish"));
        btnCreate.setOnClickListener(v -> showEditFoodDialog(null));

        boolean canDelete = AuthManager.hasPermission(Permission.DELETE_CONTENT);
        List<CmsRow> rows = new ArrayList<>();
        for (SpecialtyFood f : filtered) {
            rows.add(CmsRow.forFood(
                    f,
                    Localized.tr(f.category.labelVi, f.category.label),
                    "🏪 " + f.restaurants.size() + Localized.tr(" quán", " spots"),
                    f.nameVi + " (" + f.nameEn + ")",
                    f.shortDescription,
                    () -> {
                        AppNavigator nav = getNavigator();
                        if (nav != null) nav.openFoodDetail(f.id);
                    },
                    () -> showEditFoodDialog(f),
                    canDelete ? () -> confirmDelete(
                            Localized.tr("Xóa món \"" + f.nameVi + "\"?", "Delete \"" + f.nameEn + "\"?"),
                            () -> {
                                FoodRepository.deleteFood(f.id);
                                renderDashboard();
                            }) : null
            ));
        }
        rv.setAdapter(new GenericCmsAdapter(rows));
        updateEmptyState(rows.size());
    }

    private void showEditFoodDialog(@Nullable SpecialtyFood existing) {
        if (!isAdded()) return;
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_admin_cms_editor, null);
        AlertDialog dialog = DialogHelper.showCustomCardDialog(requireContext(), dialogView);

        TextView tvTitle = dialogView.findViewById(R.id.tv_cms_editor_title);
        ImageView ivPreview = dialogView.findViewById(R.id.iv_cms_editor_preview);
        TextView tvPreviewBadge = dialogView.findViewById(R.id.tv_cms_preview_badge);
        TextView btnUpload = dialogView.findViewById(R.id.btn_cms_upload_gallery);
        TextView btnCamera = dialogView.findViewById(R.id.btn_cms_take_camera);
        LinearLayout boxPresets = dialogView.findViewById(R.id.container_cms_image_presets);
        EditText etImageUrl = dialogView.findViewById(R.id.et_cms_image_url);
        LinearLayout boxCats = dialogView.findViewById(R.id.container_cms_categories);
        View boxProvSection = dialogView.findViewById(R.id.box_cms_province_section);
        View rowMeta = dialogView.findViewById(R.id.row_cms_meta_fields);
        EditText etVi = dialogView.findViewById(R.id.et_cms_name_vi);
        EditText etEn = dialogView.findViewById(R.id.et_cms_name_en);
        EditText etHowToEat = dialogView.findViewById(R.id.et_cms_district);
        EditText etOrigin = dialogView.findViewById(R.id.et_cms_extra_meta);
        EditText etDesc = dialogView.findViewById(R.id.et_cms_summary);
        TextView btnCancel = dialogView.findViewById(R.id.btn_cms_cancel);
        TextView btnSave = dialogView.findViewById(R.id.btn_cms_save);

        tvTitle.setText(existing != null
                ? Localized.tr("✏️ Chỉnh sửa Món Đặc sản & Ảnh minh họa", "✏️ Edit Dish & Illustration")
                : Localized.tr("➕ Thêm Món Đặc sản kèm Minh họa", "➕ Add Illustrated Specialty Dish"));
        boxProvSection.setVisibility(View.GONE);
        rowMeta.setVisibility(View.VISIBLE);
        etHowToEat.setHint(Localized.tr("🥢 Cách thưởng thức ngon", "🥢 How to eat"));
        etOrigin.setHint(Localized.tr("🌟 Nguyên liệu chính", "🌟 Main ingredient"));
        etDesc.setHint(Localized.tr("📝 Mô tả hương vị hấp dẫn...", "📝 Flavor description..."));

        final FoodCategory[] selectedCat = {
                existing != null ? existing.category : FoodCategory.STREET_SNACKS
        };
        final int[] selectedResId = {
                existing != null
                        ? SeedImageResolver.resolveFoodDrawable(requireContext().getResources(), existing)
                        : R.drawable.food_com_tam
        };
        final String[] customSourceUrl = {
                existing != null && existing.imageUrl != null ? existing.imageUrl : ""
        };

        if (existing != null) {
            etVi.setText(existing.nameVi);
            etEn.setText(existing.nameEn);
            etHowToEat.setText(existing.howToEatLikeALocal);
            etOrigin.setText(!existing.ingredients.isEmpty() ? existing.ingredients.get(0) : "");
            etDesc.setText(existing.shortDescription);
            if (customSourceUrl[0].startsWith("http") && !SeedImageResolver.isCuratedFoodId(existing.id)) {
                etImageUrl.setText(customSourceUrl[0]);
            }
        } else {
            customSourceUrl[0] = ImageLoader.buildDrawableUri(getResources(), selectedResId[0]);
        }

        Runnable refreshPreview = () -> {
            String typedUrl = etImageUrl.getText().toString().trim();
            String effectiveUrl = !typedUrl.isEmpty() ? typedUrl : customSourceUrl[0];
            ImageLoader.load(ivPreview, selectedResId[0], effectiveUrl);
            if (effectiveUrl != null && (effectiveUrl.startsWith("/") || effectiveUrl.startsWith("file://"))) {
                tvPreviewBadge.setText(Localized.tr("📸 Đang dùng ảnh món ăn tải từ máy", "📸 Custom uploaded dish photo"));
            } else {
                tvPreviewBadge.setText(Localized.tr("🍜 Ảnh minh họa món ăn trên thực đơn", "🍜 Active dish illustration"));
            }
        };

        Runnable renderPresetThumbnails = new Runnable() {
            @Override
            public void run() {
                boxPresets.removeAllViews();
                for (int resId : FOOD_PRESET_DRAWABLES) {
                    String uriToken = ImageLoader.buildDrawableUri(getResources(), resId);
                    boolean isSelected = (customSourceUrl[0] != null && customSourceUrl[0].equals(uriToken))
                            || ((customSourceUrl[0] == null || !customSourceUrl[0].startsWith("/"))
                            && etImageUrl.getText().toString().trim().isEmpty()
                            && selectedResId[0] == resId);
                    boxPresets.addView(buildPresetThumbnailTile(resId, isSelected, () -> {
                        selectedResId[0] = resId;
                        customSourceUrl[0] = uriToken;
                        etImageUrl.setText("");
                        refreshPreview.run();
                        run();
                    }));
                }
            }
        };

        Runnable renderCategoryChips = new Runnable() {
            @Override
            public void run() {
                boxCats.removeAllViews();
                for (FoodCategory cat : FoodCategory.values()) {
                    if (cat == FoodCategory.ALL) continue;
                    boxCats.addView(ChipViewFactory.createChip(
                            requireContext(),
                            Localized.tr(cat.labelVi, cat.label),
                            selectedCat[0] == cat,
                            0,
                            () -> {
                                selectedCat[0] = cat;
                                run();
                            }
                    ));
                }
            }
        };

        refreshPreview.run();
        renderPresetThumbnails.run();
        renderCategoryChips.run();

        btnUpload.setOnClickListener(v -> launchCmsGallery("food", path -> {
            customSourceUrl[0] = path;
            etImageUrl.setText("");
            refreshPreview.run();
            renderPresetThumbnails.run();
        }));
        btnCamera.setOnClickListener(v -> launchCmsCamera("food", path -> {
            customSourceUrl[0] = path;
            etImageUrl.setText("");
            refreshPreview.run();
            renderPresetThumbnails.run();
        }));
        etImageUrl.addTextChangedListener(new SimpleWatcher(refreshPreview));

        dialogView.findViewById(R.id.btn_cms_editor_close).setOnClickListener(v -> dialog.dismiss());
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String vi = etVi.getText().toString().trim();
            if (vi.isEmpty()) {
                Toast.makeText(requireContext(),
                        Localized.tr("Vui lòng nhập tên món ăn Tiếng Việt", "Please enter dish name"),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            String en = etEn.getText().toString().trim();
            if (en.isEmpty()) en = vi;
            String desc = etDesc.getText().toString().trim();
            if (desc.isEmpty()) desc = "Món ngon trứ danh mang đậm phong vị ẩm thực Sài Gòn.";
            String howToEat = etHowToEat.getText().toString().trim();
            if (howToEat.isEmpty()) howToEat = "Thưởng thức nóng cùng nước chấm pha chế gia truyền và rau sống.";
            String ing = etOrigin.getText().toString().trim();

            String typedUrl = etImageUrl.getText().toString().trim();
            String finalImageUrl = !typedUrl.isEmpty() ? typedUrl : customSourceUrl[0];

            List<String> ingredients = existing != null && !existing.ingredients.isEmpty()
                    ? existing.ingredients
                    : Collections.singletonList(!ing.isEmpty() ? ing : "Nguyên liệu tươi bản địa");
            if (!ing.isEmpty() && (existing == null || existing.ingredients.isEmpty())) {
                ingredients = Collections.singletonList(ing);
            }

            SpecialtyFood food = new SpecialtyFood(
                    existing != null ? existing.id : "food_cms_" + UUID.randomUUID().toString().substring(0, 6),
                    en,
                    vi,
                    selectedCat[0],
                    finalImageUrl,
                    selectedResId[0],
                    desc,
                    existing != null ? existing.storyAndHistory : "Tinh hoa ẩm thực đường phố phương Nam.",
                    howToEat,
                    ingredients,
                    existing != null ? existing.restaurants : Collections.emptyList()
            );
            ResultBox<SpecialtyFood> res = FoodRepository.upsertFood(food);
            if (!res.isSuccess()) {
                Toast.makeText(requireContext(), res.getErrorMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            Toast.makeText(requireContext(),
                    Localized.tr("✅ Đã lưu món đặc sản & hình ảnh minh họa!", "✅ Saved dish & illustration!"),
                    Toast.LENGTH_SHORT).show();
            renderDashboard();
        });
    }

    // =========================================================================
    // TAB 2: PUBLIC UTILITIES (Color-Coded Emblem Tiles + Full Edit Support)
    // =========================================================================

    private void renderUtilitiesCms() {
        List<PublicUtility> all = UtilityRepository.getUtilities();
        List<PublicUtility> filtered = new ArrayList<>();
        for (PublicUtility u : all) {
            String hay = u.nameVi + " " + u.nameEn + " " + u.district + " " + u.category.labelVi + " " + u.address;
            if (SearchUtils.matchesAllTokens(hay, searchQuery)) {
                filtered.add(u);
            }
        }

        tvStats.setText(Localized.tr(
                "🚇 Hiển thị " + filtered.size() + "/" + all.size() + " điểm tiện ích công cộng",
                "🚇 Showing " + filtered.size() + "/" + all.size() + " public utilities"));
        btnCreate.setVisibility(View.VISIBLE);
        btnCreate.setText(Localized.tr("➕ Thêm tiện ích", "➕ Add Utility"));
        btnCreate.setOnClickListener(v -> showEditUtilityDialog(null));

        boolean canDelete = AuthManager.hasPermission(Permission.DELETE_CONTENT);
        List<CmsRow> rows = new ArrayList<>();
        for (PublicUtility u : filtered) {
            int accentHex = resolveUtilityAccentColor(u.category);
            rows.add(CmsRow.forEmblem(
                    u.category.iconEmoji,
                    u.operatingHours != null && !u.operatingHours.isEmpty() ? u.operatingHours : "24/7",
                    accentHex,
                    Localized.tr(u.category.labelVi, u.category.labelEn),
                    u.price != null && !u.price.isEmpty() ? u.price : "Miễn phí",
                    Localized.tr(u.nameVi, u.nameEn),
                    "📍 " + u.district + " • " + u.address
                            + (u.phone != null && !u.phone.isEmpty() ? " • ☎️ " + u.phone : ""),
                    Localized.tr("📍 Bản đồ", "📍 Map"),
                    () -> {
                        AppNavigator nav = getNavigator();
                        if (nav != null) nav.openMapAt(u.latitude, u.longitude, u.nameVi);
                    },
                    () -> showEditUtilityDialog(u),
                    canDelete ? () -> confirmDelete(
                            Localized.tr("Xóa tiện ích \"" + u.nameVi + "\"?", "Delete \"" + u.nameEn + "\"?"),
                            () -> {
                                UtilityRepository.deleteUtility(u.id);
                                renderDashboard();
                            }) : null
            ));
        }
        rv.setAdapter(new GenericCmsAdapter(rows));
        updateEmptyState(rows.size());
    }

    private void showEditUtilityDialog(@Nullable PublicUtility existing) {
        if (!isAdded()) return;
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_admin_cms_editor, null);
        AlertDialog dialog = DialogHelper.showCustomCardDialog(requireContext(), dialogView);

        TextView tvTitle = dialogView.findViewById(R.id.tv_cms_editor_title);
        View boxImageSection = dialogView.findViewById(R.id.box_cms_image_section);
        View boxProvSection = dialogView.findViewById(R.id.box_cms_province_section);
        LinearLayout boxCats = dialogView.findViewById(R.id.container_cms_categories);
        EditText etVi = dialogView.findViewById(R.id.et_cms_name_vi);
        EditText etEn = dialogView.findViewById(R.id.et_cms_name_en);
        EditText etDistrict = dialogView.findViewById(R.id.et_cms_district);
        EditText etHours = dialogView.findViewById(R.id.et_cms_extra_meta);
        EditText etAddress = dialogView.findViewById(R.id.et_cms_summary);
        TextView btnCancel = dialogView.findViewById(R.id.btn_cms_cancel);
        TextView btnSave = dialogView.findViewById(R.id.btn_cms_save);

        boxImageSection.setVisibility(View.GONE);
        boxProvSection.setVisibility(View.GONE);
        tvTitle.setText(existing != null
                ? Localized.tr("✏️ Sửa Tiện ích Công cộng", "✏️ Edit Public Utility")
                : Localized.tr("➕ Thêm Tiện ích Công cộng", "➕ Add Public Utility"));
        etHours.setHint(Localized.tr("🕒 Giờ mở (VD: 24/7)", "🕒 Hours (e.g. 24/7)"));
        etAddress.setHint(Localized.tr("📍 Địa chỉ chi tiết & Ghi chú hỗ trợ...", "📍 Detailed address & notes..."));

        final UtilityCategory[] selectedCat = {
                existing != null ? existing.category : UtilityCategory.TOURIST_INFO_SIM
        };

        if (existing != null) {
            etVi.setText(existing.nameVi);
            etEn.setText(existing.nameEn);
            etDistrict.setText(existing.district);
            etHours.setText(existing.operatingHours);
            etAddress.setText(existing.address);
        } else {
            etDistrict.setText("Quận 1");
            etHours.setText("24/7");
        }

        Runnable renderCategoryChips = new Runnable() {
            @Override
            public void run() {
                boxCats.removeAllViews();
                for (UtilityCategory cat : UtilityCategory.values()) {
                    if (cat == UtilityCategory.ALL) continue;
                    boxCats.addView(ChipViewFactory.createChip(
                            requireContext(),
                            cat.iconEmoji + " " + Localized.tr(cat.labelVi, cat.labelEn),
                            selectedCat[0] == cat,
                            0,
                            () -> {
                                selectedCat[0] = cat;
                                run();
                            }
                    ));
                }
            }
        };
        renderCategoryChips.run();

        dialogView.findViewById(R.id.btn_cms_editor_close).setOnClickListener(v -> dialog.dismiss());
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String vi = etVi.getText().toString().trim();
            if (vi.isEmpty()) {
                Toast.makeText(requireContext(),
                        Localized.tr("Vui lòng nhập tên tiện ích", "Please enter utility name"),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            String en = etEn.getText().toString().trim();
            if (en.isEmpty()) en = vi;
            String dist = etDistrict.getText().toString().trim();
            if (dist.isEmpty()) dist = "Quận 1";
            String hours = etHours.getText().toString().trim();
            if (hours.isEmpty()) hours = "24/7";
            String addr = etAddress.getText().toString().trim();
            if (addr.isEmpty()) addr = "Trung tâm TP. Hồ Chí Minh";

            PublicUtility u = new PublicUtility(
                    existing != null ? existing.id : "util_cms_" + UUID.randomUUID().toString().substring(0, 6),
                    en,
                    vi,
                    selectedCat[0],
                    dist,
                    addr,
                    existing != null ? existing.latitude : 10.775,
                    existing != null ? existing.longitude : 106.701,
                    hours,
                    existing != null ? existing.price : "Miễn phí",
                    existing != null ? existing.phone : "1087",
                    en,
                    vi,
                    existing != null ? existing.tips : "Điểm tiện ích hỗ trợ du khách trong hành trình."
            );
            ResultBox<PublicUtility> res = UtilityRepository.upsertUtility(u);
            if (!res.isSuccess()) {
                Toast.makeText(requireContext(), res.getErrorMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            Toast.makeText(requireContext(),
                    Localized.tr("✅ Đã lưu thông tin tiện ích!", "✅ Saved public utility!"),
                    Toast.LENGTH_SHORT).show();
            renderDashboard();
        });
    }

    // =========================================================================
    // TAB 3: REVIEWS MODERATION (Shows Actual Photo of Reviewed Place/Dish!)
    // =========================================================================

    private void renderReviewsModeration() {
        List<PlaceReview> all = ReviewManager.getReviews();
        List<PlaceReview> filtered = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        for (PlaceReview r : all) {
            Attraction targetAtt = AttractionRepository.getAttractionById(r.targetId);
            SpecialtyFood targetFood = targetAtt == null ? FoodRepository.getFoodById(r.targetId) : null;
            String targetTitle = targetAtt != null ? targetAtt.nameVi
                    : (targetFood != null ? targetFood.nameVi : r.targetId);
            String hay = r.authorName + " " + r.comment + " " + targetTitle + " " + r.targetId;
            if (SearchUtils.matchesAllTokens(hay, searchQuery)) {
                filtered.add(r);
            }
        }

        tvStats.setText(Localized.tr(
                "⭐ Kiểm duyệt " + filtered.size() + "/" + all.size() + " đánh giá cộng đồng",
                "⭐ Moderating " + filtered.size() + "/" + all.size() + " community reviews"));
        btnCreate.setVisibility(View.GONE);

        List<CmsRow> rows = new ArrayList<>();
        for (PlaceReview r : filtered) {
            Attraction targetAtt = AttractionRepository.getAttractionById(r.targetId);
            SpecialtyFood targetFood = targetAtt == null ? FoodRepository.getFoodById(r.targetId) : null;
            String placeName = targetAtt != null
                    ? Localized.tr(targetAtt.nameVi, targetAtt.nameEn)
                    : (targetFood != null ? Localized.tr(targetFood.nameVi, targetFood.nameEn) : r.targetId);

            String stars = buildStarString(r.rating);
            Runnable onPreview = targetAtt != null
                    ? () -> {
                        AppNavigator nav = getNavigator();
                        if (nav != null) nav.openAttractionDetail(targetAtt.id);
                    }
                    : (targetFood != null ? () -> {
                        AppNavigator nav = getNavigator();
                        if (nav != null) nav.openFoodDetail(targetFood.id);
                    } : null);

            rows.add(CmsRow.forReview(
                    targetAtt,
                    targetFood,
                    "★ " + r.rating + "/5",
                    "📍 " + placeName,
                    sdf.format(new Date(r.createdAt)),
                    r.authorName + "  " + stars,
                    "“" + r.comment + "”",
                    onPreview,
                    () -> confirmDelete(
                            Localized.tr("Gỡ bình luận của " + r.authorName + "?", "Remove review by " + r.authorName + "?"),
                            () -> {
                                ReviewManager.deleteReview(r.id);
                                renderDashboard();
                            })
            ));
        }
        rv.setAdapter(new GenericCmsAdapter(rows));
        updateEmptyState(rows.size());
    }

    // =========================================================================
    // TAB 4: USERS MANAGEMENT (Circular Uploaded Avatars + RBAC Controls)
    // =========================================================================

    private void renderUsersAdmin() {
        List<UserAccount> all = AuthManager.getUsers();
        List<UserAccount> filtered = new ArrayList<>();
        for (UserAccount u : all) {
            String hay = u.fullName + " " + u.username + " " + u.email + " " + u.role.titleVi + " " + u.authProvider.badgeLabel;
            if (SearchUtils.matchesAllTokens(hay, searchQuery)) {
                filtered.add(u);
            }
        }

        tvStats.setText(Localized.tr(
                "👥 Quản lý " + filtered.size() + "/" + all.size() + " tài khoản người dùng",
                "👥 Managing " + filtered.size() + "/" + all.size() + " user accounts"));
        btnCreate.setVisibility(View.VISIBLE);
        btnCreate.setText(Localized.tr("➕ Tạo tài khoản", "➕ New User"));
        btnCreate.setOnClickListener(v -> showCreateUserDialog());

        final float d = getResources().getDisplayMetrics().density;
        final int ringColor = ContextCompat.getColor(requireContext(), R.color.saigon_red);

        rv.setAdapter(new RecyclerView.Adapter<UserVH>() {
            @NonNull
            @Override
            public UserVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new UserVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull UserVH h, int position) {
                UserAccount u = filtered.get(position);
                AvatarImageManager.bindAvatarViews(
                        h.ivAvatarPhoto,
                        h.tvAvatarEmoji,
                        u.avatarPhotoPath,
                        u.avatarEmoji,
                        (int) (50 * d * 2),
                        ringColor,
                        2f * d
                );
                h.tvStatusDot.setText(u.isLocked ? "🔒" : "🟢");
                h.tvTitle.setText(u.fullName + (u.isLocked ? Localized.tr(" (Đã khóa)", " (Locked)") : ""));
                h.tvBadge.setText(roleEmoji(u.role) + " " + Localized.tr(u.role.shortVi, u.role.shortEn));
                h.tvProviderBadge.setText("🔗 " + u.authProvider.badgeLabel);
                h.tvEmail.setText("@" + u.username + (u.email.isEmpty() ? "" : " • " + u.email));

                h.btnRole.setText(Localized.tr("🔄 Phân quyền", "🔄 Role"));
                h.btnRole.setOnClickListener(v -> {
                    UserRole[] roles = {UserRole.MEMBER, UserRole.EDITOR, UserRole.ADMIN};
                    String[] rLabels = {
                            "🧳 " + Localized.tr("Thành viên (Member)", "Tourist Member"),
                            "✍️ " + Localized.tr("Biên tập viên (Editor)", "Content Editor"),
                            "🛡️ " + Localized.tr("Quản trị viên (Admin)", "System Administrator")
                    };
                    new AlertDialog.Builder(requireContext())
                            .setTitle(Localized.tr("Đổi vai trò cho @" + u.username, "Change role for @" + u.username))
                            .setItems(rLabels, (dlg, w) -> {
                                ResultBox<UserAccount> res = AuthManager.changeUserRole(u.id, roles[w]);
                                if (!res.isSuccess()) {
                                    Toast.makeText(requireContext(), res.getErrorMessage(), Toast.LENGTH_SHORT).show();
                                }
                                renderDashboard();
                            })
                            .show();
                });

                h.btnLock.setText(u.isLocked
                        ? Localized.tr("🔓 Mở khóa", "🔓 Unlock")
                        : Localized.tr("🔒 Khóa", "🔒 Lock"));
                h.btnLock.setOnClickListener(v -> {
                    ResultBox<UserAccount> res = AuthManager.toggleUserLock(u.id);
                    if (!res.isSuccess()) {
                        Toast.makeText(requireContext(), res.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                    renderDashboard();
                });

                h.btnDel.setText(Localized.tr("🗑️ Xóa", "🗑️ Delete"));
                h.btnDel.setOnClickListener(v -> confirmDelete(
                        Localized.tr("Xóa tài khoản @" + u.username + "?", "Delete account @" + u.username + "?"),
                        () -> {
                            ResultBox<Void> res = AuthManager.deleteUser(u.id);
                            if (!res.isSuccess()) {
                                Toast.makeText(requireContext(), res.getErrorMessage(), Toast.LENGTH_SHORT).show();
                            }
                            renderDashboard();
                        }
                ));
            }

            @Override
            public int getItemCount() {
                return filtered.size();
            }
        });
        updateEmptyState(filtered.size());
    }

    private void showCreateUserDialog() {
        if (!isAdded()) return;
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_admin_create_user, null);
        AlertDialog dialog = DialogHelper.showCustomCardDialog(requireContext(), dialogView);

        LinearLayout boxRoles = dialogView.findViewById(R.id.container_admin_new_user_roles);
        EditText etName = dialogView.findViewById(R.id.et_admin_new_fullname);
        EditText etUser = dialogView.findViewById(R.id.et_admin_new_username);
        EditText etEmail = dialogView.findViewById(R.id.et_admin_new_email);
        EditText etPass = dialogView.findViewById(R.id.et_admin_new_password);
        TextView btnCancel = dialogView.findViewById(R.id.btn_admin_new_user_cancel);
        TextView btnSubmit = dialogView.findViewById(R.id.btn_admin_new_user_submit);

        final UserRole[] selectedRole = {UserRole.EDITOR};
        UserRole[] selectable = {UserRole.EDITOR, UserRole.MEMBER, UserRole.ADMIN};

        Runnable renderRoleChips = new Runnable() {
            @Override
            public void run() {
                boxRoles.removeAllViews();
                for (UserRole r : selectable) {
                    boxRoles.addView(ChipViewFactory.createChip(
                            requireContext(),
                            roleEmoji(r) + " " + Localized.tr(r.shortVi, r.shortEn),
                            selectedRole[0] == r,
                            0,
                            () -> {
                                selectedRole[0] = r;
                                run();
                            }
                    ));
                }
            }
        };
        renderRoleChips.run();

        dialogView.findViewById(R.id.btn_admin_new_user_close).setOnClickListener(v -> dialog.dismiss());
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSubmit.setOnClickListener(v -> {
            ResultBox<UserAccount> res = AuthManager.createUserByAdmin(
                    etName.getText().toString(),
                    etUser.getText().toString(),
                    etEmail.getText().toString(),
                    etPass.getText().toString(),
                    selectedRole[0]
            );
            if (!res.isSuccess()) {
                Toast.makeText(requireContext(), res.getErrorMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            Toast.makeText(requireContext(),
                    Localized.tr("✅ Đã tạo tài khoản mới thành công!", "✅ Created new account!"),
                    Toast.LENGTH_SHORT).show();
            renderDashboard();
        });
    }

    // =========================================================================
    // TAB 5: SECURITY AUDIT LOGS (Illustrated Role Emblem Tiles)
    // =========================================================================

    private void renderAuditLogs() {
        List<AuditLogEntry> all = AuthManager.getAuditLogs();
        List<AuditLogEntry> filtered = new ArrayList<>();
        for (AuditLogEntry e : all) {
            String hay = e.actorUsername + " " + e.actionVi + " " + e.actionEn + " " + e.actorRole.shortVi;
            if (SearchUtils.matchesAllTokens(hay, searchQuery)) {
                filtered.add(e);
            }
        }

        tvStats.setText(Localized.tr(
                "📋 Nhật ký bảo mật SQLite: " + filtered.size() + "/" + all.size() + " sự kiện",
                "📋 Security audit logs: " + filtered.size() + "/" + all.size() + " events"));
        btnCreate.setVisibility(View.GONE);

        SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.US);
        SimpleDateFormat dateFmt = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US);
        List<CmsRow> rows = new ArrayList<>();
        for (AuditLogEntry e : filtered) {
            int roleColor = e.actorRole == UserRole.ADMIN ? 0xFF4F46E5
                    : (e.actorRole == UserRole.EDITOR ? 0xFF0D9488 : 0xFFD97706);
            rows.add(CmsRow.forEmblem(
                    roleEmoji(e.actorRole),
                    timeFmt.format(new Date(e.timestamp)),
                    roleColor,
                    Localized.tr(e.actorRole.shortVi, e.actorRole.shortEn),
                    dateFmt.format(new Date(e.timestamp)),
                    Localized.tr(e.actionVi, e.actionEn),
                    "👤 @" + e.actorUsername,
                    null,
                    null,
                    null,
                    null
            ));
        }
        rv.setAdapter(new GenericCmsAdapter(rows));
        updateEmptyState(rows.size());
    }

    // =========================================================================
    // HELPERS & RECYCLERVIEW ADAPTERS
    // =========================================================================

    private View buildPresetThumbnailTile(int drawableResId, boolean selected, Runnable onSelect) {
        float d = getResources().getDisplayMetrics().density;
        int w = (int) (72 * d);
        int h = (int) (52 * d);

        FrameLayout outer = new FrameLayout(requireContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(w, h);
        lp.setMarginEnd((int) (8 * d));
        outer.setLayoutParams(lp);
        int pad = selected ? (int) (2.5f * d) : (int) (1 * d);
        outer.setPadding(pad, pad, pad, pad);

        GradientDrawable ring = new GradientDrawable();
        ring.setShape(GradientDrawable.RECTANGLE);
        ring.setCornerRadius(11f * d);
        ring.setColor(ContextCompat.getColor(requireContext(), R.color.surface_subtle));
        ring.setStroke(
                selected ? (int) (2.5f * d) : (int) Math.max(1, d),
                selected
                        ? ContextCompat.getColor(requireContext(), R.color.saigon_teal)
                        : ContextCompat.getColor(requireContext(), R.color.card_stroke)
        );
        outer.setBackground(ring);

        FrameLayout clipBox = new FrameLayout(requireContext());
        clipBox.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        clipBox.setBackgroundResource(R.drawable.bg_admin_thumb_frame);
        clipBox.setClipToOutline(true);

        ImageView iv = new ImageView(requireContext());
        iv.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setImageResource(drawableResId);
        clipBox.addView(iv);

        if (selected) {
            TextView check = new TextView(requireContext());
            FrameLayout.LayoutParams clp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            check.setLayoutParams(clp);
            check.setBackgroundResource(R.drawable.bg_dark_overlay_pill);
            check.setPadding((int) (5 * d), (int) (1 * d), (int) (5 * d), (int) (1 * d));
            check.setText("✓");
            check.setTextColor(Color.WHITE);
            check.setTextSize(10f);
            clipBox.addView(check);
        }

        outer.addView(clipBox);
        outer.setClickable(true);
        outer.setFocusable(true);
        outer.setOnClickListener(v -> onSelect.run());
        return outer;
    }

    private void launchCmsGallery(String prefix, Consumer<String> onSaved) {
        pendingCmsImagePrefix = prefix;
        pendingCmsImageSavedCallback = onSaved;
        try {
            cmsGalleryLauncher.launch("image/*");
        } catch (Exception e) {
            Toast.makeText(requireContext(),
                    Localized.tr("Không tìm thấy trình chọn ảnh trên thiết bị", "Image picker unavailable"),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void launchCmsCamera(String prefix, Consumer<String> onSaved) {
        pendingCmsImagePrefix = prefix;
        pendingCmsImageSavedCallback = onSaved;
        try {
            cmsCameraLauncher.launch(null);
        } catch (Exception e) {
            Toast.makeText(requireContext(),
                    Localized.tr("Không thể mở Camera trên thiết bị này", "Camera unavailable"),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDelete(String message, Runnable onConfirmed) {
        if (!isAdded()) return;
        new AlertDialog.Builder(requireContext())
                .setTitle(Localized.tr("⚠️ Xác nhận thao tác", "⚠️ Confirm Action"))
                .setMessage(message)
                .setPositiveButton(Localized.tr("Đồng ý xóa", "Delete"), (d, w) -> onConfirmed.run())
                .setNegativeButton(Localized.tr("Hủy", "Cancel"), null)
                .show();
    }

    private static String roleEmoji(UserRole role) {
        if (role == UserRole.ADMIN) return "🛡️";
        if (role == UserRole.EDITOR) return "✍️";
        if (role == UserRole.MEMBER) return "🧳";
        return "👤";
    }

    private static String buildStarString(int rating) {
        int clamped = Math.max(1, Math.min(5, rating));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(i < clamped ? '★' : '☆');
        }
        return sb.toString();
    }

    private static int resolveUtilityAccentColor(UtilityCategory cat) {
        if (cat == null) return 0xFF0D9488;
        switch (cat) {
            case MEDICAL:
            case EMERGENCY_HOTLINE:
                return 0xFFDC2626;
            case TRANSIT_METRO:
                return 0xFF0284C7;
            case CURRENCY_EXCHANGE:
                return 0xFFD97706;
            case TOURIST_INFO_SIM:
                return 0xFF4F46E5;
            case RESTROOM:
            default:
                return 0xFF0D9488;
        }
    }

    private static final class SimpleWatcher implements TextWatcher {
        private final Runnable onChange;

        SimpleWatcher(Runnable onChange) {
            this.onChange = onChange;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (onChange != null) onChange.run();
        }

        @Override
        public void afterTextChanged(Editable s) {}
    }

    private static final class CmsRow {
        final Attraction attraction;
        final SpecialtyFood food;
        final boolean usePhotoThumbnail;
        final String photoTag;
        final String emblemIcon;
        final String emblemCaption;
        final int emblemColorHex;
        final String badgeText;
        final String metaRightText;
        final String title;
        final String subtitle;
        final String previewLabel;
        final Runnable onPreview;
        final Runnable onEdit;
        final Runnable onDelete;

        private CmsRow(
                Attraction attraction,
                SpecialtyFood food,
                boolean usePhotoThumbnail,
                String photoTag,
                String emblemIcon,
                String emblemCaption,
                int emblemColorHex,
                String badgeText,
                String metaRightText,
                String title,
                String subtitle,
                String previewLabel,
                Runnable onPreview,
                Runnable onEdit,
                Runnable onDelete
        ) {
            this.attraction = attraction;
            this.food = food;
            this.usePhotoThumbnail = usePhotoThumbnail;
            this.photoTag = photoTag;
            this.emblemIcon = emblemIcon;
            this.emblemCaption = emblemCaption;
            this.emblemColorHex = emblemColorHex;
            this.badgeText = badgeText;
            this.metaRightText = metaRightText;
            this.title = title;
            this.subtitle = subtitle;
            this.previewLabel = previewLabel;
            this.onPreview = onPreview;
            this.onEdit = onEdit;
            this.onDelete = onDelete;
        }

        static CmsRow forAttraction(
                Attraction a,
                String badge,
                String meta,
                String title,
                String sub,
                Runnable onPreview,
                Runnable onEdit,
                Runnable onDelete
        ) {
            return new CmsRow(a, null, true, "🏛️", "", "", 0, badge, meta, title, sub,
                    Localized.tr("👁️ Xem", "👁️ View"), onPreview, onEdit, onDelete);
        }

        static CmsRow forFood(
                SpecialtyFood f,
                String badge,
                String meta,
                String title,
                String sub,
                Runnable onPreview,
                Runnable onEdit,
                Runnable onDelete
        ) {
            return new CmsRow(null, f, true, "🍜", "", "", 0, badge, meta, title, sub,
                    Localized.tr("👁️ Xem", "👁️ View"), onPreview, onEdit, onDelete);
        }

        static CmsRow forReview(
                Attraction a,
                SpecialtyFood f,
                String ratingTag,
                String badge,
                String meta,
                String title,
                String sub,
                Runnable onPreview,
                Runnable onDelete
        ) {
            boolean hasPhoto = (a != null || f != null);
            return new CmsRow(a, f, hasPhoto, ratingTag, "⭐", ratingTag, 0xFFD97706, badge, meta, title, sub,
                    Localized.tr("👁️ Mục gốc", "👁️ Open"), onPreview, null, onDelete);
        }

        static CmsRow forEmblem(
                String icon,
                String caption,
                int colorHex,
                String badge,
                String meta,
                String title,
                String sub,
                String previewLabel,
                Runnable onPreview,
                Runnable onEdit,
                Runnable onDelete
        ) {
            return new CmsRow(null, null, false, "", icon, caption, colorHex, badge, meta, title, sub,
                    previewLabel, onPreview, onEdit, onDelete);
        }
    }

    private final class GenericCmsAdapter extends RecyclerView.Adapter<GenericCmsAdapter.VH> {
        private final List<CmsRow> rows;

        GenericCmsAdapter(List<CmsRow> rows) {
            this.rows = rows;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_entry, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            CmsRow r = rows.get(position);
            if (r.usePhotoThumbnail) {
                h.framePhoto.setVisibility(View.VISIBLE);
                h.boxEmblem.setVisibility(View.GONE);
                if (r.attraction != null) {
                    ImageLoader.loadAttraction(h.ivPhoto, r.attraction);
                } else if (r.food != null) {
                    ImageLoader.loadFood(h.ivPhoto, r.food);
                } else {
                    h.ivPhoto.setImageResource(R.drawable.attraction_ben_thanh_market);
                }
                h.tvPhotoTag.setText(r.photoTag);
            } else {
                h.framePhoto.setVisibility(View.GONE);
                h.boxEmblem.setVisibility(View.VISIBLE);
                h.tvEmblemIcon.setText(r.emblemIcon);
                h.tvEmblemCaption.setText(r.emblemCaption);

                boolean dark = ThemeManager.isDarkActive(h.itemView.getContext());
                float d = h.itemView.getResources().getDisplayMetrics().density;
                GradientDrawable tileBg = new GradientDrawable();
                tileBg.setShape(GradientDrawable.RECTANGLE);
                tileBg.setCornerRadius(14f * d);
                int alphaFill = dark ? 48 : 26;
                int alphaStroke = dark ? 110 : 75;
                tileBg.setColor(Color.argb(alphaFill, Color.red(r.emblemColorHex), Color.green(r.emblemColorHex), Color.blue(r.emblemColorHex)));
                tileBg.setStroke((int) Math.max(1, d), Color.argb(alphaStroke, Color.red(r.emblemColorHex), Color.green(r.emblemColorHex), Color.blue(r.emblemColorHex)));
                h.boxEmblem.setBackground(tileBg);
                h.tvEmblemCaption.setTextColor(dark ? 0xFFE2E8F0 : r.emblemColorHex);
            }

            h.tvBadge.setText(r.badgeText);
            h.tvMeta.setText(r.metaRightText);
            h.tvTitle.setText(r.title);
            h.tvSub.setText(r.subtitle);

            boolean hasAnyAction = (r.onPreview != null || r.onEdit != null || r.onDelete != null);
            h.rowActions.setVisibility(hasAnyAction ? View.VISIBLE : View.GONE);

            if (r.onPreview != null) {
                h.btnPreview.setVisibility(View.VISIBLE);
                h.btnPreview.setText(r.previewLabel != null ? r.previewLabel : "👁️ Xem");
                h.btnPreview.setOnClickListener(v -> r.onPreview.run());
                h.itemView.setOnClickListener(v -> r.onPreview.run());
            } else if (r.onEdit != null) {
                h.btnPreview.setVisibility(View.GONE);
                h.itemView.setOnClickListener(v -> r.onEdit.run());
            } else {
                h.btnPreview.setVisibility(View.GONE);
                h.itemView.setOnClickListener(null);
            }

            h.btnEdit.setVisibility(r.onEdit != null ? View.VISIBLE : View.GONE);
            h.btnEdit.setText(Localized.tr("✏️ Sửa", "✏️ Edit"));
            h.btnEdit.setOnClickListener(v -> {
                if (r.onEdit != null) r.onEdit.run();
            });

            h.btnDelete.setVisibility(r.onDelete != null ? View.VISIBLE : View.GONE);
            h.btnDelete.setText(Localized.tr("🗑️ Xóa", "🗑️ Delete"));
            h.btnDelete.setOnClickListener(v -> {
                if (r.onDelete != null) r.onDelete.run();
            });
        }

        @Override
        public int getItemCount() {
            return rows.size();
        }

        final class VH extends RecyclerView.ViewHolder {
            final View framePhoto;
            final ImageView ivPhoto;
            final TextView tvPhotoTag;
            final View boxEmblem;
            final TextView tvEmblemIcon;
            final TextView tvEmblemCaption;
            final TextView tvBadge;
            final TextView tvMeta;
            final TextView tvTitle;
            final TextView tvSub;
            final View rowActions;
            final TextView btnPreview;
            final TextView btnEdit;
            final TextView btnDelete;

            VH(@NonNull View v) {
                super(v);
                framePhoto = v.findViewById(R.id.frame_admin_entry_photo);
                ivPhoto = v.findViewById(R.id.iv_admin_entry_photo);
                tvPhotoTag = v.findViewById(R.id.tv_admin_entry_photo_tag);
                boxEmblem = v.findViewById(R.id.box_admin_entry_emblem);
                tvEmblemIcon = v.findViewById(R.id.tv_admin_entry_emblem_icon);
                tvEmblemCaption = v.findViewById(R.id.tv_admin_entry_emblem_caption);
                tvBadge = v.findViewById(R.id.tv_admin_entry_badge);
                tvMeta = v.findViewById(R.id.tv_admin_entry_meta);
                tvTitle = v.findViewById(R.id.tv_admin_entry_title);
                tvSub = v.findViewById(R.id.tv_admin_entry_sub);
                rowActions = v.findViewById(R.id.row_admin_entry_actions);
                btnPreview = v.findViewById(R.id.btn_admin_entry_preview);
                btnEdit = v.findViewById(R.id.btn_admin_entry_edit);
                btnDelete = v.findViewById(R.id.btn_admin_entry_delete);
            }
        }
    }

    private static final class UserVH extends RecyclerView.ViewHolder {
        final TextView tvAvatarEmoji;
        final ImageView ivAvatarPhoto;
        final TextView tvStatusDot;
        final TextView tvTitle;
        final TextView tvBadge;
        final TextView tvProviderBadge;
        final TextView tvEmail;
        final TextView btnRole;
        final TextView btnLock;
        final TextView btnDel;

        UserVH(@NonNull View v) {
            super(v);
            tvAvatarEmoji = v.findViewById(R.id.tv_user_avatar_emoji);
            ivAvatarPhoto = v.findViewById(R.id.iv_user_avatar_photo);
            tvStatusDot = v.findViewById(R.id.tv_user_status_indicator);
            tvTitle = v.findViewById(R.id.tv_user_title);
            tvBadge = v.findViewById(R.id.tv_user_role_badge);
            tvProviderBadge = v.findViewById(R.id.tv_user_provider_badge);
            tvEmail = v.findViewById(R.id.tv_user_email);
            btnRole = v.findViewById(R.id.btn_user_change_role);
            btnLock = v.findViewById(R.id.btn_user_toggle_lock);
            btnDel = v.findViewById(R.id.btn_user_delete);
        }
    }

    private AppNavigator getNavigator() {
        return getActivity() instanceof AppNavigator ? (AppNavigator) getActivity() : null;
    }
}
