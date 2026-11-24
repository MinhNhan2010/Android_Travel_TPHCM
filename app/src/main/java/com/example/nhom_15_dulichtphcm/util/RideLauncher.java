package com.example.nhom_15_dulichtphcm.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import com.example.nhom_15_dulichtphcm.data.model.RideProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure Java launcher for Ride-Hailing & Taxi apps:
 * 1. Opens the installed app directly when present on the device (via Android 11+ <queries>).
 * 2. Otherwise opens Google Play Store (CH Play - com.android.vending) directly — NEVER hijacked
 *    by third-party market schemes such as TapTap.
 * 3. Falls back to the official https://play.google.com/store/apps/details URL if Google Play
 *    is disabled or absent on non-GMS emulators.
 */
public final class RideLauncher {

    public static final String GOOGLE_PLAY_PACKAGE = "com.android.vending";
    private static final String PLAY_STORE_WEB_PREFIX = "https://play.google.com/store/apps/details?id=";

    private RideLauncher() {}

    public static List<String> getCandidatePackages(RideProvider provider) {
        List<String> pkgs = new ArrayList<>();
        if (provider == null) return pkgs;
        if (provider.packageName != null && !provider.packageName.trim().isEmpty()) {
            pkgs.add(provider.packageName.trim());
        }
        if ("mai_linh".equals(provider.id)) {
            if (!pkgs.contains("vn.mailinh.taxi")) pkgs.add("vn.mailinh.taxi");
        } else if ("vinasun".equals(provider.id)) {
            if (!pkgs.contains("com.vinasun.app")) pkgs.add("com.vinasun.app");
        }
        return pkgs;
    }

    public static boolean isRideAppInstalled(Context context, RideProvider provider) {
        if (context == null || provider == null) return false;
        PackageManager pm = context.getPackageManager();
        for (String pkg : getCandidatePackages(provider)) {
            try {
                if (pm.getLaunchIntentForPackage(pkg) != null) {
                    return true;
                }
                pm.getPackageInfo(pkg, 0);
                return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    public static String buildPlayStoreWebUrl(String packageName) {
        String cleanPkg = (packageName != null && !packageName.trim().isEmpty())
                ? packageName.trim()
                : "com.gsm.customer";
        return PLAY_STORE_WEB_PREFIX + cleanPkg;
    }

    public static void openRideProvider(Context context, RideProvider provider, String destName) {
        if (context == null || provider == null) return;
        if (destName != null && !destName.trim().isEmpty()) {
            copyText(context, destName.trim());
        }

        PackageManager pm = context.getPackageManager();

        // 1. Ưu tiên số 1: Nếu máy đã cài sẵn ứng dụng đặt xe -> Mở thẳng vào ứng dụng đó
        for (String pkg : getCandidatePackages(provider)) {
            try {
                Intent launchIntent = pm.getLaunchIntentForPackage(pkg);
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(launchIntent);
                    return;
                }
            } catch (Exception ignored) {
            }
        }

        // 2. Ưu tiên số 2: Chưa cài app -> Ép mở trực tiếp Cửa hàng Google Play (CH Play: com.android.vending)
        // Ghim cứng setPackage("com.android.vending") để KHÔNG BAO GIỜ bị TapTap hay kho app lạ chặn bắt scheme market://
        String targetPkg = provider.packageName != null ? provider.packageName.trim() : "";
        if (!targetPkg.isEmpty()) {
            try {
                Intent playStoreIntent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + targetPkg)
                );
                playStoreIntent.setPackage(GOOGLE_PLAY_PACKAGE);
                playStoreIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(playStoreIntent);
                return;
            } catch (Exception ignored) {
            }

            // 3. Dự phòng cho máy ảo không có CH Play: mở trang Google Play chính thức trên trình duyệt (https://play.google.com/...)
            try {
                Intent playWebIntent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(buildPlayStoreWebUrl(targetPkg))
                );
                playWebIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                playWebIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(playWebIntent);
                return;
            } catch (Exception ignored) {
            }
        }

        // 4. Dự phòng cuối cùng: mở trang chủ chính thức của hãng xe
        try {
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(provider.webUrl));
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(webIntent);
        } catch (Exception e) {
            Toast.makeText(context,
                    Localized.tr("Không mở được CH Play hoặc ứng dụng gọi xe", "Could not open Google Play or ride app"),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public static void callHotline(Context context, String phoneNumber) {
        if (context == null || phoneNumber == null || phoneNumber.trim().isEmpty()) return;
        try {
            String clean = phoneNumber.replaceAll("[^0-9+]", "");
            Intent dial = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + clean));
            dial.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(dial);
        } catch (Exception e) {
            Toast.makeText(context,
                    Localized.tr("Không mở được trình gọi điện", "Could not open dialer"),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public static void copyText(Context context, String text) {
        if (context == null || text == null) return;
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText("Address", text));
            Toast.makeText(context,
                    Localized.tr("Đã chép địa chỉ điểm đến!", "Copied destination address!"),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
