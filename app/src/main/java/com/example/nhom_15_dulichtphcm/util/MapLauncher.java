package com.example.nhom_15_dulichtphcm.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import java.util.Locale;

public final class MapLauncher {

    private MapLauncher() {}

    public static void openInGoogleMaps(Context context, double lat, double lng, String label) {
        if (context == null) return;
        try {
            String safeLabel = Uri.encode(label != null ? label : "TP.HCM");
            Uri geoUri = Uri.parse(String.format(Locale.US, "geo:%f,%f?q=%f,%f(%s)", lat, lng, lat, lng, safeLabel));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, geoUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(mapIntent);
                return;
            }
            Uri browserUri = Uri.parse(String.format(Locale.US,
                    "https://www.google.com/maps/search/?api=1&query=%f,%f", lat, lng));
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, browserUri);
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(browserIntent);
        } catch (Exception e) {
            Toast.makeText(context, Localized.tr("Không mở được bản đồ", "Could not open map"), Toast.LENGTH_SHORT).show();
        }
    }

    public static void openDirections(Context context, double lat, double lng) {
        if (context == null) return;
        try {
            Uri navUri = Uri.parse(String.format(Locale.US, "google.navigation:q=%f,%f&mode=d", lat, lng));
            Intent intent = new Intent(Intent.ACTION_VIEW, navUri);
            intent.setPackage("com.google.android.apps.maps");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
                return;
            }
            Uri webUri = Uri.parse(String.format(Locale.US,
                    "https://www.google.com/maps/dir/?api=1&destination=%f,%f", lat, lng));
            Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(webIntent);
        } catch (Exception e) {
            Toast.makeText(context, Localized.tr("Không mở được chỉ đường", "Could not open directions"), Toast.LENGTH_SHORT).show();
        }
    }

    public static void copyCoordinates(Context context, double lat, double lng, String label) {
        if (context == null) return;
        String text = String.format(Locale.US, "%.5f, %.5f", lat, lng);
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText(label != null ? label : "Coordinates", text));
            Toast.makeText(context,
                    Localized.tr("Đã sao chép tọa độ: " + text, "Copied coordinates: " + text),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public static void openPhotoSearch(Context context, String query) {
        if (context == null || query == null) return;
        try {
            Uri uri = Uri.parse("https://www.google.com/search?tbm=isch&q=" + Uri.encode(query + " Sài Gòn"));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception ignored) {
        }
    }

    public static void shareText(Context context, String subject, String body) {
        if (context == null || body == null) return;
        try {
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject != null ? subject : "");
            sendIntent.putExtra(Intent.EXTRA_TEXT, body);
            Intent chooser = Intent.createChooser(sendIntent, subject != null ? subject : "Share");
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(chooser);
        } catch (Exception ignored) {
        }
    }
}
