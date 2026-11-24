package com.example.nhom_15_dulichtphcm.ui.components;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.nhom_15_dulichtphcm.R;
import com.example.nhom_15_dulichtphcm.data.model.Phrase;
import com.example.nhom_15_dulichtphcm.ui.AppNavigator;
import com.example.nhom_15_dulichtphcm.util.AppLanguage;
import com.example.nhom_15_dulichtphcm.util.LanguageManager;
import com.example.nhom_15_dulichtphcm.util.Localized;
import com.example.nhom_15_dulichtphcm.util.SpeechHelper;
import com.example.nhom_15_dulichtphcm.util.ThemeManager;

/**
 * Reusable pure Java AlertDialog builders backed by XML layouts and SQLite settings.
 */
public final class DialogHelper {

    private DialogHelper() {}

    public static void showPermissionRequired(Context context, String message, AppNavigator nav) {
        if (context == null) return;
        new AlertDialog.Builder(context)
                .setTitle(Localized.tr("🔒 Yêu cầu Đăng nhập / Phân quyền", "🔒 Sign-in / Permission Required"))
                .setMessage(message != null && !message.isEmpty()
                        ? message
                        : Localized.tr(
                                "Tính năng cá nhân hóa này được bảo vệ bởi phân quyền RBAC trên SQLite. Vui lòng đăng nhập tài khoản Thành viên trở lên!",
                                "This personalized feature requires a signed-in Member account in SQLite."))
                .setPositiveButton(Localized.tr("Đăng nhập ngay", "Sign In"), (d, w) -> {
                    if (nav != null) nav.openAuthProfile();
                })
                .setNegativeButton(Localized.tr("Đóng", "Cancel"), null)
                .show();
    }

    public static void showLanguageSelector(Context context, Runnable onChanged) {
        if (context == null) return;
        AppLanguage[] langs = AppLanguage.values();
        String[] labels = new String[langs.length];
        int checked = 0;
        AppLanguage current = LanguageManager.getCurrentLanguage();
        for (int i = 0; i < langs.length; i++) {
            labels[i] = langs[i].flag + "  " + langs[i].displayName + " (" + langs[i].code.toUpperCase() + ")";
            if (langs[i] == current) checked = i;
        }
        new AlertDialog.Builder(context)
                .setTitle(Localized.tr("🌐 Chọn Ngôn ngữ Giao diện", "🌐 Choose App Language"))
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    LanguageManager.setLanguage(context, langs[which]);
                    dialog.dismiss();
                    if (onChanged != null) onChanged.run();
                })
                .setNegativeButton(Localized.tr("Hủy", "Cancel"), null)
                .show();
    }

    public static void showThemeSelector(Context context) {
        if (context == null) return;
        ThemeManager.ThemeMode[] modes = ThemeManager.ThemeMode.values();
        String[] labels = new String[]{
                Localized.tr("☀️  Chế độ Sáng (Màu truyền thống)", "☀️  Light Mode (Classic Warm Palette)"),
                Localized.tr("🌙  Chế độ Tối (Bảo vệ mắt ban đêm)", "🌙  Dark Mode (Night Comfort)"),
                Localized.tr("📱  Tự động theo Hệ thống", "📱  Follow System Setting")
        };
        int checked = 0;
        ThemeManager.ThemeMode current = ThemeManager.getCurrentMode();
        for (int i = 0; i < modes.length; i++) {
            if (modes[i] == current) {
                checked = i;
                break;
            }
        }
        new AlertDialog.Builder(context)
                .setTitle(Localized.tr("🌗 Giao diện Sáng / Tối", "🌗 Appearance & Theme"))
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    dialog.dismiss();
                    ThemeManager.setThemeMode(context, modes[which]);
                })
                .setNegativeButton(Localized.tr("Đóng", "Cancel"), null)
                .show();
    }

    public static AlertDialog showCustomCardDialog(Context context, View customCardView) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(customCardView)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();
        return dialog;
    }

    public static void showPhraseToLocalCard(Context context, Phrase phrase, SpeechHelper speechHelper) {
        if (context == null || phrase == null) return;
        View cardView = LayoutInflater.from(context).inflate(R.layout.dialog_show_to_local, null);
        TextView tvVi = cardView.findViewById(R.id.tv_local_card_vi);
        TextView tvEn = cardView.findViewById(R.id.tv_local_card_en);
        TextView btnSpeak = cardView.findViewById(R.id.btn_local_card_speak);

        tvVi.setText(phrase.vietnamese);
        tvEn.setText(phrase.english);
        btnSpeak.setOnClickListener(v -> {
            if (speechHelper != null) speechHelper.speakVietnamese(phrase.vietnamese);
        });

        new AlertDialog.Builder(context)
                .setView(cardView)
                .setPositiveButton(Localized.tr("Đóng", "Close"), null)
                .show();
    }
}
