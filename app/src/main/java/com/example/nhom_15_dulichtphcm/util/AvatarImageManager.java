package com.example.nhom_15_dulichtphcm.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.Uri;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Pure Java manager for importing, center-cropping, persisting, and rendering circular
 * user profile avatar photos stored inside internal app storage (files/avatars/) and SQLite.
 */
public final class AvatarImageManager {

    private static final int AVATAR_EXPORT_SIZE_PX = 512;
    private static final int MAX_DECODE_DIMENSION_PX = 1280;

    private static final LruCache<String, Bitmap> CIRCLE_CACHE = new LruCache<String, Bitmap>(6 * 1024) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return Math.max(1, value.getAllocationByteCount() / 1024);
        }
    };

    private AvatarImageManager() {}

    /**
     * Decodes a high-resolution sampled Bitmap (up to ~1280px) from a Uri for interactive cropping.
     */
    public static Bitmap decodeSampledBitmapFromUri(Context context, Uri sourceUri) {
        if (context == null || sourceUri == null) return null;
        try {
            byte[] rawBytes = readAllBytesFromUri(context, sourceUri);
            if (rawBytes == null || rawBytes.length == 0) return null;

            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.length, bounds);
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null;

            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, MAX_DECODE_DIMENSION_PX);
            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.length, opts);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Decodes an existing avatar file from disk so the user can re-adjust / re-crop it anytime.
     */
    public static Bitmap decodeSampledBitmapFromFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) return null;
        try {
            File f = new File(filePath.trim());
            if (!f.exists() || !f.isFile()) return null;
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(f.getAbsolutePath(), bounds);
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null;

            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, MAX_DECODE_DIMENSION_PX);
            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return BitmapFactory.decodeFile(f.getAbsolutePath(), opts);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Imports an image from a content:// or file:// Uri, safely downsamples and center-crops it
     * to 512x512, writes it to internal app storage, and returns the saved absolute file path.
     */
    public static String saveAvatarFromUri(Context context, String userScopeId, Uri sourceUri) {
        if (context == null || sourceUri == null) return null;
        try {
            byte[] rawBytes = readAllBytesFromUri(context, sourceUri);
            if (rawBytes == null || rawBytes.length == 0) return null;

            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.length, bounds);
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null;

            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, MAX_DECODE_DIMENSION_PX);
            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;

            Bitmap decoded = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.length, opts);
            if (decoded == null) return null;

            return persistSquareAvatarBitmap(context, userScopeId, decoded);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Saves a camera preview Bitmap as a 512x512 JPEG in internal app storage and returns its path.
     */
    public static String saveAvatarFromBitmap(Context context, String userScopeId, Bitmap sourceBitmap) {
        if (context == null || sourceBitmap == null || sourceBitmap.isRecycled()) return null;
        try {
            return persistSquareAvatarBitmap(context, userScopeId, sourceBitmap);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String persistSquareAvatarBitmap(Context context, String userScopeId, Bitmap source) throws Exception {
        String safeOwner = (userScopeId != null && !userScopeId.trim().isEmpty())
                ? userScopeId.trim().replaceAll("[^a-zA-Z0-9_\\-]", "_")
                : "pending";

        File avatarDir = new File(context.getFilesDir(), "avatars");
        if (!avatarDir.exists() && !avatarDir.mkdirs()) {
            return null;
        }

        Bitmap square = centerCropSquare(source, AVATAR_EXPORT_SIZE_PX);
        String fileName = "avatar_" + safeOwner + "_" + System.currentTimeMillis() + ".jpg";
        File outFile = new File(avatarDir, fileName);

        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            if (!square.compress(Bitmap.CompressFormat.JPEG, 90, fos)) {
                return null;
            }
            fos.flush();
        }

        if (square != source && !square.isRecycled()) {
            square.recycle();
        }

        // Remove older avatar files belonging to the same user to keep storage clean
        if (!"pending".equals(safeOwner)) {
            cleanupPreviousUserAvatars(avatarDir, "avatar_" + safeOwner + "_", outFile.getName());
        }
        CIRCLE_CACHE.evictAll();
        return outFile.getAbsolutePath();
    }

    public static void deleteAvatarFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) return;
        try {
            File f = new File(filePath.trim());
            if (f.exists() && f.isFile()) {
                //noinspection ResultOfMethodCallIgnored
                f.delete();
            }
            CIRCLE_CACHE.evictAll();
        } catch (Exception ignored) {
        }
    }

    /**
     * Loads and caches an anti-aliased circular Bitmap with an optional stroke ring.
     */
    public static Bitmap loadCircularBitmap(String filePath, int diameterPx, int strokeColor, float strokeWidthPx) {
        if (filePath == null || filePath.trim().isEmpty()) return null;
        int size = Math.max(32, diameterPx);
        String cleanPath = filePath.trim();
        File f = new File(cleanPath);
        if (!f.exists() || !f.isFile() || f.length() <= 0) return null;

        String cacheKey = cleanPath + "@" + size + "@" + strokeColor + "@" + f.lastModified();
        Bitmap cached = CIRCLE_CACHE.get(cacheKey);
        if (cached != null && !cached.isRecycled()) {
            return cached;
        }

        try {
            Bitmap src = BitmapFactory.decodeFile(cleanPath);
            if (src == null) return null;

            Bitmap out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(out);

            BitmapShader shader = new BitmapShader(src, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            Matrix matrix = new Matrix();
            float scale = Math.max((float) size / src.getWidth(), (float) size / src.getHeight());
            float dx = (size - src.getWidth() * scale) * 0.5f;
            float dy = (size - src.getHeight() * scale) * 0.5f;
            matrix.setScale(scale, scale);
            matrix.postTranslate(dx, dy);
            shader.setLocalMatrix(matrix);

            Paint imgPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
            imgPaint.setShader(shader);

            float radius = size * 0.5f;
            float inset = strokeWidthPx > 0f ? strokeWidthPx * 0.5f : 0f;
            canvas.drawCircle(radius, radius, Math.max(1f, radius - inset), imgPaint);

            if (strokeWidthPx > 0f && strokeColor != 0) {
                Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                ringPaint.setStyle(Paint.Style.STROKE);
                ringPaint.setStrokeWidth(strokeWidthPx);
                ringPaint.setColor(strokeColor);
                canvas.drawCircle(radius, radius, Math.max(1f, radius - inset), ringPaint);
            }

            CIRCLE_CACHE.put(cacheKey, out);
            return out;
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Binds either a circular uploaded photo (into photoView) or an emoji badge (into emojiView).
     */
    public static boolean bindAvatarViews(
            ImageView photoView,
            TextView emojiView,
            String photoPath,
            String fallbackEmoji,
            int diameterPx,
            int strokeColor,
            float strokeWidthPx
    ) {
        Bitmap circle = loadCircularBitmap(photoPath, diameterPx, strokeColor, strokeWidthPx);
        if (circle != null && photoView != null) {
            photoView.setImageBitmap(circle);
            photoView.setVisibility(View.VISIBLE);
            if (emojiView != null) {
                emojiView.setVisibility(View.GONE);
            }
            return true;
        }
        if (photoView != null) {
            photoView.setImageDrawable(null);
            photoView.setVisibility(View.GONE);
        }
        if (emojiView != null) {
            emojiView.setVisibility(View.VISIBLE);
            emojiView.setText((fallbackEmoji != null && !fallbackEmoji.isEmpty()) ? fallbackEmoji : "🧳");
        }
        return false;
    }

    private static Bitmap centerCropSquare(Bitmap src, int targetSize) {
        int w = src.getWidth();
        int h = src.getHeight();
        int edge = Math.min(w, h);
        int x = (w - edge) / 2;
        int y = (h - edge) / 2;
        Bitmap cropped = Bitmap.createBitmap(src, x, y, edge, edge);
        if (edge == targetSize) {
            return cropped;
        }
        Bitmap scaled = Bitmap.createScaledBitmap(cropped, targetSize, targetSize, true);
        if (scaled != cropped && cropped != src && !cropped.isRecycled()) {
            cropped.recycle();
        }
        return scaled;
    }

    private static int calculateInSampleSize(int width, int height, int maxDim) {
        int inSampleSize = 1;
        while ((width / inSampleSize) > maxDim || (height / inSampleSize) > maxDim) {
            inSampleSize *= 2;
        }
        return Math.max(1, inSampleSize);
    }

    private static byte[] readAllBytesFromUri(Context context, Uri uri) {
        try (InputStream is = context.getContentResolver().openInputStream(uri);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            if (is == null) return null;
            byte[] buf = new byte[8192];
            int n;
            int total = 0;
            final int maxBytes = 20 * 1024 * 1024; // 20 MB safety guard
            while ((n = is.read(buf)) != -1) {
                total += n;
                if (total > maxBytes) return null;
                bos.write(buf, 0, n);
            }
            return bos.toByteArray();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static void cleanupPreviousUserAvatars(File dir, String prefix, String keepFileName) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            String name = f.getName();
            if (name.startsWith(prefix) && !name.equals(keepFileName)) {
                //noinspection ResultOfMethodCallIgnored
                f.delete();
            }
        }
    }
}
