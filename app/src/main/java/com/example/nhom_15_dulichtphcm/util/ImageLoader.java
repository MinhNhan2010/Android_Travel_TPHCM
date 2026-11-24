package com.example.nhom_15_dulichtphcm.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;

import com.example.nhom_15_dulichtphcm.R;
import com.example.nhom_15_dulichtphcm.data.model.Attraction;
import com.example.nhom_15_dulichtphcm.data.model.SpecialtyFood;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Pure Java image loader supporting validated local Drawable photos, CMS-uploaded internal storage
 * files, named drawable URIs ({@code drawable://entry_name}), and async URL bitmap caching,
 * immune to AAPT2 {@code R.drawable.*} integer resource ID shifts across app upgrades.
 */
public final class ImageLoader {

    private static final String DRAWABLE_URI_PREFIX = "drawable://";
    private static final int CMS_MAX_EXPORT_WIDTH_PX = 1080;

    private static final LruCache<String, Bitmap> MEMORY_CACHE = new LruCache<String, Bitmap>(24 * 1024) {
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            return Math.max(1, bitmap.getAllocationByteCount() / 1024);
        }
    };

    private static final ExecutorService IO_POOL = Executors.newFixedThreadPool(3);
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private ImageLoader() {}

    public static void loadAttraction(ImageView imageView, Attraction attraction) {
        if (imageView == null) return;
        if (attraction == null) {
            setSafeDrawable(imageView, R.drawable.attraction_ben_thanh_market, R.drawable.attraction_ben_thanh_market);
            return;
        }
        if (tryLoadCustomCmsSource(imageView, attraction.imageUrl)) {
            return;
        }
        int resolvedRes = SeedImageResolver.resolveAttractionDrawable(imageView.getResources(), attraction);
        if (!SeedImageResolver.isCuratedAttractionId(attraction.id) && isHttpUrl(attraction.imageUrl)) {
            loadRemoteWithFallback(imageView, attraction.imageUrl.trim(), resolvedRes);
            return;
        }
        setSafeDrawable(imageView, resolvedRes, R.drawable.attraction_ben_thanh_market);
    }

    public static void loadFood(ImageView imageView, SpecialtyFood food) {
        if (imageView == null) return;
        if (food == null) {
            setSafeDrawable(imageView, R.drawable.food_com_tam, R.drawable.food_com_tam);
            return;
        }
        if (tryLoadCustomCmsSource(imageView, food.imageUrl)) {
            return;
        }
        int resolvedRes = SeedImageResolver.resolveFoodDrawable(imageView.getResources(), food);
        if (!SeedImageResolver.isCuratedFoodId(food.id) && isHttpUrl(food.imageUrl)) {
            loadRemoteWithFallback(imageView, food.imageUrl.trim(), resolvedRes);
            return;
        }
        setSafeDrawable(imageView, resolvedRes, R.drawable.food_com_tam);
    }

    public static void load(ImageView imageView, int imageResId, String imageUrl) {
        if (imageView == null) return;
        if (tryLoadCustomCmsSource(imageView, imageUrl)) {
            return;
        }
        if (SeedImageResolver.isValidPhotoDrawable(imageView.getResources(), imageResId, false)) {
            setSafeDrawable(imageView, imageResId, R.drawable.attraction_ben_thanh_market);
            return;
        }
        if (isHttpUrl(imageUrl)) {
            loadRemoteWithFallback(imageView, imageUrl.trim(), R.drawable.attraction_ben_thanh_market);
            return;
        }
        setSafeDrawable(imageView, R.drawable.attraction_ben_thanh_market, R.drawable.attraction_ben_thanh_market);
    }

    /**
     * Imports a photo from Gallery Uri into {@code files/cms_images/} and returns its file path.
     */
    public static String saveCmsIllustrationFromUri(Context context, String prefix, Uri sourceUri) {
        Bitmap decoded = AvatarImageManager.decodeSampledBitmapFromUri(context, sourceUri);
        if (decoded == null) return null;
        return saveCmsIllustrationFromBitmap(context, prefix, decoded);
    }

    /**
     * Saves a Bitmap (from Camera or Gallery) into {@code files/cms_images/} and returns its file path.
     */
    public static String saveCmsIllustrationFromBitmap(Context context, String prefix, Bitmap source) {
        if (context == null || source == null || source.isRecycled()) return null;
        try {
            File dir = new File(context.getFilesDir(), "cms_images");
            if (!dir.exists() && !dir.mkdirs()) return null;

            Bitmap export = source;
            if (source.getWidth() > CMS_MAX_EXPORT_WIDTH_PX) {
                int targetH = Math.max(1, Math.round(source.getHeight() * (CMS_MAX_EXPORT_WIDTH_PX / (float) source.getWidth())));
                export = Bitmap.createScaledBitmap(source, CMS_MAX_EXPORT_WIDTH_PX, targetH, true);
            }

            String safePrefix = (prefix != null && !prefix.trim().isEmpty())
                    ? prefix.trim().replaceAll("[^a-zA-Z0-9_\\-]", "_")
                    : "cms";
            File outFile = new File(dir, safePrefix + "_" + System.currentTimeMillis() + ".jpg");
            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                if (!export.compress(Bitmap.CompressFormat.JPEG, 88, fos)) {
                    return null;
                }
                fos.flush();
            }
            if (export != source && !export.isRecycled()) {
                export.recycle();
            }
            return outFile.getAbsolutePath();
        } catch (Exception ignored) {
            return null;
        }
    }

    public static String buildDrawableUri(Resources res, int drawableResId) {
        if (res == null || drawableResId == 0) return "";
        try {
            String entry = res.getResourceEntryName(drawableResId);
            if (entry != null && !entry.isEmpty()) {
                return DRAWABLE_URI_PREFIX + entry;
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    private static boolean tryLoadCustomCmsSource(ImageView imageView, String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) return false;
        String clean = imageUrl.trim();

        // 1. Named built-in drawable URI (drawable://attraction_... or drawable://food_...)
        if (clean.startsWith(DRAWABLE_URI_PREFIX)) {
            String entryName = clean.substring(DRAWABLE_URI_PREFIX.length()).trim();
            if (!entryName.isEmpty()) {
                Resources res = imageView.getResources();
                String pkg = imageView.getContext().getPackageName();
                int resId = res.getIdentifier(entryName, "drawable", pkg);
                if (resId != 0 && SeedImageResolver.isValidPhotoDrawable(res, resId, false)) {
                    setSafeDrawable(imageView, resId, R.drawable.attraction_ben_thanh_market);
                    return true;
                }
            }
            return false;
        }

        // 2. Local CMS-uploaded file path (/data/... or file:///...)
        String filePath = clean.startsWith("file://") ? clean.substring(7) : clean;
        if (filePath.startsWith("/") || filePath.contains(":\\")) {
            File f = new File(filePath);
            if (f.exists() && f.isFile() && f.length() > 0) {
                String cacheKey = "local:" + f.getAbsolutePath() + "@" + f.lastModified();
                Bitmap cached = MEMORY_CACHE.get(cacheKey);
                if (cached != null && !cached.isRecycled()) {
                    imageView.setTag(null);
                    imageView.setImageBitmap(cached);
                    return true;
                }
                Bitmap decoded = AvatarImageManager.decodeSampledBitmapFromFile(f.getAbsolutePath());
                if (decoded != null) {
                    MEMORY_CACHE.put(cacheKey, decoded);
                    imageView.setTag(null);
                    imageView.setImageBitmap(decoded);
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isHttpUrl(String url) {
        if (url == null) return false;
        String trimmed = url.trim().toLowerCase(Locale.ROOT);
        return trimmed.startsWith("http://") || trimmed.startsWith("https://");
    }

    private static void setSafeDrawable(ImageView imageView, int resId, int fallbackResId) {
        imageView.setTag(null);
        try {
            imageView.setImageResource(resId);
        } catch (Resources.NotFoundException e) {
            try {
                imageView.setImageResource(fallbackResId);
            } catch (Resources.NotFoundException ignored) {
            }
        }
    }

    private static void loadRemoteWithFallback(ImageView imageView, final String cleanUrl, int placeholderResId) {
        Bitmap cached = MEMORY_CACHE.get(cleanUrl);
        if (cached != null && !cached.isRecycled()) {
            imageView.setTag(cleanUrl);
            imageView.setImageBitmap(cached);
            return;
        }

        setSafeDrawable(imageView, placeholderResId, R.drawable.attraction_ben_thanh_market);
        imageView.setTag(cleanUrl);
        IO_POOL.execute(() -> {
            Bitmap downloaded = downloadBitmap(cleanUrl);
            if (downloaded != null) {
                MEMORY_CACHE.put(cleanUrl, downloaded);
                MAIN_HANDLER.post(() -> {
                    Object currentTag = imageView.getTag();
                    if (cleanUrl.equals(currentTag)) {
                        imageView.setImageBitmap(downloaded);
                    }
                });
            }
        });
    }

    private static Bitmap downloadBitmap(String urlString) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(6000);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("User-Agent", "ExploreSaigon/2.0 Android");
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (InputStream is = conn.getInputStream()) {
                    return BitmapFactory.decodeStream(is);
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (conn != null) conn.disconnect();
        }
        return null;
    }
}
