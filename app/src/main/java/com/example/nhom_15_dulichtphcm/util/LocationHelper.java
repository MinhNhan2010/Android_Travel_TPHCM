package com.example.nhom_15_dulichtphcm.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.function.Consumer;

public final class LocationHelper {

    public static final double DEMO_LAT = 10.7725;
    public static final double DEMO_LNG = 106.6980;

    private LocationHelper() {}

    public static boolean hasPermission(Context context) {
        if (context == null) return false;
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isNearHcmc(double lat, double lng) {
        return lat >= 10.1 && lat <= 11.4 && lng >= 106.2 && lng <= 107.4;
    }

    @SuppressLint("MissingPermission")
    public static void fetchCurrentLocation(Context context, Consumer<Location> callback) {
        if (callback == null) return;
        if (context == null || !hasPermission(context)) {
            callback.accept(null);
            return;
        }
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (lm == null) {
            callback.accept(null);
            return;
        }

        Location bestLastKnown = null;
        try {
            List<String> providers = lm.getProviders(true);
            for (String p : providers) {
                Location l = lm.getLastKnownLocation(p);
                if (l != null) {
                    if (bestLastKnown == null || l.getTime() > bestLastKnown.getTime()) {
                        bestLastKnown = l;
                    }
                }
            }
        } catch (Exception ignored) {
        }

        if (bestLastKnown != null && (System.currentTimeMillis() - bestLastKnown.getTime()) < 5 * 60_000L) {
            callback.accept(bestLastKnown);
            return;
        }

        final Location fallbackLoc = bestLastKnown;
        final boolean[] delivered = {false};
        final Handler handler = new Handler(Looper.getMainLooper());

        final LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (!delivered[0]) {
                    delivered[0] = true;
                    try {
                        lm.removeUpdates(this);
                    } catch (Exception ignored) {}
                    callback.accept(location);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };

        try {
            String provider = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    ? LocationManager.GPS_PROVIDER
                    : (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                            ? LocationManager.NETWORK_PROVIDER : null);
            if (provider == null) {
                callback.accept(fallbackLoc);
                return;
            }
            handler.post(() -> {
                try {
                    lm.requestLocationUpdates(provider, 0L, 0f, listener);
                } catch (Exception ignored) {
                }
            });
            handler.postDelayed(() -> {
                if (!delivered[0]) {
                    delivered[0] = true;
                    try {
                        lm.removeUpdates(listener);
                    } catch (Exception ignored) {}
                    callback.accept(fallbackLoc);
                }
            }, 3500L);
        } catch (Exception e) {
            callback.accept(fallbackLoc);
        }
    }
}
