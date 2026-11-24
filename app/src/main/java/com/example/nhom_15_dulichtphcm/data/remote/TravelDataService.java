package com.example.nhom_15_dulichtphcm.data.remote;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.nhom_15_dulichtphcm.R;
import com.example.nhom_15_dulichtphcm.data.db.SaigonTravelDatabaseHelper;
import com.example.nhom_15_dulichtphcm.data.model.Attraction;
import com.example.nhom_15_dulichtphcm.data.model.AttractionCategory;
import com.example.nhom_15_dulichtphcm.data.model.LatLngPoint;
import com.example.nhom_15_dulichtphcm.util.Localized;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Live external data integration (Weather, Exchange Rates, OSRM Routing, Google Map Tiles,
 * and Auto-Discovery of New Places) with SQLite offline caching in saigon_travel.db.
 */
public final class TravelDataService {

    private static final String KEY_CACHED_RATES_JSON = "cached_fx_rates_json";
    private static final String KEY_CACHED_RATES_DATE = "cached_fx_rates_date";

    private static final ExecutorService IO_POOL = Executors.newFixedThreadPool(3);
    private static volatile Handler mainHandler;

    private static void postToMain(Runnable action) {
        try {
            if (mainHandler == null) {
                Looper looper = Looper.getMainLooper();
                if (looper != null) {
                    mainHandler = new Handler(looper);
                }
            }
            if (mainHandler != null) {
                mainHandler.post(action);
                return;
            }
        } catch (RuntimeException ignored) {
            // Plain JVM unit test environment without Android Looper
        }
        action.run();
    }

    public static final class CityCoord {
        public final String id;
        public final String nameVi;
        public final String nameEn;
        public final double lat;
        public final double lng;

        public CityCoord(String id, String nameVi, String nameEn, double lat, double lng) {
            this.id = id;
            this.nameVi = nameVi;
            this.nameEn = nameEn;
            this.lat = lat;
            this.lng = lng;
        }
    }

    public static final class DailyForecast {
        public final String date;
        public final double maxTempC;
        public final double minTempC;
        public final int weatherCode;
        public final int rainChancePercent;

        public DailyForecast(String date, double maxTempC, double minTempC, int weatherCode, int rainChancePercent) {
            this.date = date;
            this.maxTempC = maxTempC;
            this.minTempC = minTempC;
            this.weatherCode = weatherCode;
            this.rainChancePercent = rainChancePercent;
        }
    }

    public static final class WeatherInfo {
        public final double temperatureC;
        public final double feelsLikeC;
        public final int humidityPercent;
        public final double windSpeedKmh;
        public final int weatherCode;
        public final double uvIndex;
        public final List<DailyForecast> forecast3Days;
        public final boolean isLive;

        public WeatherInfo(
                double temperatureC,
                double feelsLikeC,
                int humidityPercent,
                double windSpeedKmh,
                int weatherCode,
                double uvIndex,
                List<DailyForecast> forecast3Days,
                boolean isLive
        ) {
            this.temperatureC = temperatureC;
            this.feelsLikeC = feelsLikeC;
            this.humidityPercent = humidityPercent;
            this.windSpeedKmh = windSpeedKmh;
            this.weatherCode = weatherCode;
            this.uvIndex = uvIndex;
            this.forecast3Days = forecast3Days != null
                    ? Collections.unmodifiableList(new ArrayList<>(forecast3Days))
                    : Collections.emptyList();
            this.isLive = isLive;
        }
    }

    public static final class ExchangeRateResult {
        public final Map<String, Double> vndPerUnit;
        public final String updatedDate;
        public final boolean isLive;

        public ExchangeRateResult(Map<String, Double> vndPerUnit, String updatedDate, boolean isLive) {
            this.vndPerUnit = Collections.unmodifiableMap(new LinkedHashMap<>(vndPerUnit));
            this.updatedDate = updatedDate != null ? updatedDate : "";
            this.isLive = isLive;
        }
    }

    public static final class OsrmRouteResult {
        public final double distanceKm;
        public final int durationMinutes;
        public final List<LatLngPoint> geometry;
        public final boolean isRoadSnapped;

        public OsrmRouteResult(double distanceKm, int durationMinutes, List<LatLngPoint> geometry, boolean isRoadSnapped) {
            this.distanceKm = distanceKm;
            this.durationMinutes = durationMinutes;
            this.geometry = geometry != null
                    ? Collections.unmodifiableList(new ArrayList<>(geometry))
                    : Collections.emptyList();
            this.isRoadSnapped = isRoadSnapped;
        }
    }

    public static final class SyncedPlacesResult {
        public final List<Attraction> places;
        public final String sourceLabel;
        public final boolean isLiveNetwork;

        public SyncedPlacesResult(List<Attraction> places, String sourceLabel, boolean isLiveNetwork) {
            this.places = places != null
                    ? Collections.unmodifiableList(new ArrayList<>(places))
                    : Collections.emptyList();
            this.sourceLabel = sourceLabel != null ? sourceLabel : "";
            this.isLiveNetwork = isLiveNetwork;
        }
    }

    public static final List<CityCoord> cities = Collections.unmodifiableList(Arrays.asList(
            new CityCoord("hcm", "TP. Hồ Chí Minh", "Ho Chi Minh City", 10.7769, 106.7009),
            new CityCoord("bd", "Bình Dương", "Binh Duong", 10.9804, 106.6519),
            new CityCoord("vt", "Vũng Tàu", "Vung Tau", 10.3460, 107.0843)
    ));

    public static final Map<String, Double> fallbackRates;
    public static final Map<String, String> currencyFlags;

    static {
        Map<String, Double> rates = new LinkedHashMap<>();
        rates.put("USD", 25450.0);
        rates.put("EUR", 27600.0);
        rates.put("JPY", 171.5);
        rates.put("KRW", 18.4);
        rates.put("CNY", 3520.0);
        rates.put("SGD", 19050.0);
        rates.put("AUD", 16580.0);
        rates.put("GBP", 32300.0);
        rates.put("THB", 748.0);
        fallbackRates = Collections.unmodifiableMap(rates);

        Map<String, String> flags = new LinkedHashMap<>();
        flags.put("USD", "🇺🇸");
        flags.put("EUR", "🇪🇺");
        flags.put("JPY", "🇯🇵");
        flags.put("KRW", "🇰🇷");
        flags.put("CNY", "🇨🇳");
        flags.put("SGD", "🇸🇬");
        flags.put("AUD", "🇦🇺");
        flags.put("GBP", "🇬🇧");
        flags.put("THB", "🇹🇭");
        currencyFlags = Collections.unmodifiableMap(flags);
    }

    private TravelDataService() {}

    // =========================================================================
    // 1. GOOGLE MAPS TILE TEMPLATES & DETERMINISTIC PARSERS
    // =========================================================================

    public static String buildGoogleTileTemplate(String styleKey) {
        String lyrs;
        if ("satellite".equalsIgnoreCase(styleKey)) {
            lyrs = "s";
        } else if ("hybrid".equalsIgnoreCase(styleKey)) {
            lyrs = "y";
        } else if ("terrain".equalsIgnoreCase(styleKey)) {
            lyrs = "p";
        } else {
            lyrs = "m"; // standard Google Roadmap
        }
        return "https://mt{s}.google.com/vt/lyrs=" + lyrs + "&hl=vi&x={x}&y={y}&z={z}";
    }

    public static List<Attraction> parseWikipediaGeoJson(String json, String province, String defaultDistrict) {
        List<Attraction> out = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) return out;
        try {
            JSONObject root = new JSONObject(json);
            JSONObject query = root.optJSONObject("query");
            if (query == null) return out;
            JSONArray geosearch = query.optJSONArray("geosearch");
            if (geosearch != null) {
                for (int i = 0; i < geosearch.length(); i++) {
                    JSONObject item = geosearch.getJSONObject(i);
                    long pageId = item.optLong("pageid", i + 1000L);
                    String title = item.optString("title", "").trim();
                    double lat = item.optDouble("lat", Double.NaN);
                    double lon = item.optDouble("lon", Double.NaN);
                    String thumbUrl = item.optString("thumb", "").trim();
                    if (title.isEmpty() || Double.isNaN(lat) || Double.isNaN(lon)) continue;

                    AttractionCategory cat = inferCategoryFromTitle(title);
                    out.add(new Attraction(
                            "sync_wiki_" + pageId,
                            title,
                            title,
                            cat,
                            thumbUrl,
                            defaultDrawableForCategory(cat),
                            defaultDistrict != null ? defaultDistrict : "Trung tâm",
                            defaultDistrict + ", " + province,
                            lat,
                            lon,
                            "Cập nhật trực tuyến • Vui lòng kiểm tra tại quầy",
                            "07:30 - 21:30",
                            "Địa điểm tự động đồng bộ từ cơ sở dữ liệu địa lý trực tuyến khu vực " + province + ".",
                            "Điểm tham quan được định vị thực tế trên bản đồ Google Maps & hệ thống dữ liệu mở.",
                            "Không gian văn hóa, kiến trúc và đời sống đô thị đặc trưng vùng Đông Nam Bộ.",
                            "Kiểm tra giờ mở cửa thực tế và đặt xe qua tab Gọi xe trước khi di chuyển.",
                            province != null ? province : "TP.HCM"
                    ));
                }
                return out;
            }

            JSONObject pages = query.optJSONObject("pages");
            if (pages != null) {
                Iterator<String> keys = pages.keys();
                while (keys.hasNext()) {
                    JSONObject page = pages.optJSONObject(keys.next());
                    if (page == null) continue;
                    long pageId = page.optLong("pageid", 1000L);
                    String title = page.optString("title", "").trim();
                    JSONArray coords = page.optJSONArray("coordinates");
                    if (title.isEmpty() || coords == null || coords.length() == 0) continue;
                    JSONObject c0 = coords.optJSONObject(0);
                    if (c0 == null) continue;
                    double lat = c0.optDouble("lat", Double.NaN);
                    double lon = c0.optDouble("lon", Double.NaN);
                    if (Double.isNaN(lat) || Double.isNaN(lon)) continue;
                    JSONObject thumbObj = page.optJSONObject("thumbnail");
                    String thumbUrl = thumbObj != null ? thumbObj.optString("source", "").trim() : "";

                    AttractionCategory cat = inferCategoryFromTitle(title);
                    out.add(new Attraction(
                            "sync_wiki_" + pageId,
                            title,
                            title,
                            cat,
                            thumbUrl,
                            defaultDrawableForCategory(cat),
                            defaultDistrict != null ? defaultDistrict : "Trung tâm",
                            defaultDistrict + ", " + province,
                            lat,
                            lon,
                            "Cập nhật trực tuyến • Vui lòng kiểm tra tại quầy",
                            "07:30 - 21:30",
                            "Địa điểm tự động đồng bộ từ cơ sở dữ liệu địa lý trực tuyến khu vực " + province + ".",
                            "Điểm tham quan được định vị thực tế trên bản đồ Google Maps & hệ thống dữ liệu mở.",
                            "Không gian văn hóa, kiến trúc và đời sống đô thị đặc trưng vùng Đông Nam Bộ.",
                            "Kiểm tra giờ mở cửa thực tế và đặt xe qua tab Gọi xe trước khi di chuyển.",
                            province != null ? province : "TP.HCM"
                    ));
                }
            }
        } catch (Exception ignored) {
        }
        return out;
    }

    public static List<Attraction> parsePhotonGeoJson(String json, String province, String defaultDistrict) {
        List<Attraction> out = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) return out;
        try {
            JSONObject root = new JSONObject(json);
            JSONArray features = root.optJSONArray("features");
            if (features == null) return out;

            for (int i = 0; i < features.length(); i++) {
                JSONObject feat = features.getJSONObject(i);
                JSONObject geom = feat.optJSONObject("geometry");
                JSONObject props = feat.optJSONObject("properties");
                if (geom == null || props == null) continue;

                JSONArray coords = geom.optJSONArray("coordinates");
                if (coords == null || coords.length() < 2) continue;
                double lng = coords.optDouble(0, Double.NaN);
                double lat = coords.optDouble(1, Double.NaN);
                String name = props.optString("name", "").trim();
                if (name.isEmpty() || Double.isNaN(lat) || Double.isNaN(lng)) continue;

                long osmId = props.optLong("osm_id", Math.abs(name.hashCode()));
                String street = props.optString("street", "");
                String district = props.optString("district", defaultDistrict != null ? defaultDistrict : "Trung tâm");
                String address = street.isEmpty() ? (district + ", " + province) : (street + ", " + district + ", " + province);
                AttractionCategory cat = inferCategoryFromTitle(name + " " + props.optString("osm_value", ""));

                out.add(new Attraction(
                        "sync_osm_" + osmId,
                        name,
                        name,
                        cat,
                        "",
                        defaultDrawableForCategory(cat),
                        district,
                        address,
                        lat,
                        lng,
                        "Tự do tham quan / Theo biểu phí điểm đến",
                        "08:00 - 22:00",
                        "Điểm đến mới cập nhật tự động trên bản đồ số khu vực " + province + ".",
                        "Dữ liệu tọa độ GPS chuẩn xác phục vụ dẫn đường trực tiếp.",
                        "Điểm nhấn trải nghiệm thực tế dành cho du khách khám phá vùng siêu đô thị.",
                        "Sử dụng nút Dẫn đường trên Bản đồ số để xem lộ trình tối ưu.",
                        province != null ? province : "TP.HCM"
                ));
            }
        } catch (Exception ignored) {
        }
        return out;
    }

    public static OsrmRouteResult parseOsrmRouteJson(String json) {
        if (json == null || json.trim().isEmpty()) return null;
        try {
            JSONObject root = new JSONObject(json);
            JSONArray routes = root.optJSONArray("routes");
            if (routes == null || routes.length() == 0) return null;
            JSONObject r0 = routes.getJSONObject(0);
            double distMeters = r0.optDouble("distance", 0.0);
            double durSeconds = r0.optDouble("duration", 0.0);
            JSONObject geom = r0.optJSONObject("geometry");
            List<LatLngPoint> pts = new ArrayList<>();
            if (geom != null) {
                JSONArray coords = geom.optJSONArray("coordinates");
                if (coords != null) {
                    for (int i = 0; i < coords.length(); i++) {
                        JSONArray pair = coords.getJSONArray(i);
                        double lng = pair.getDouble(0);
                        double lat = pair.getDouble(1);
                        pts.add(new LatLngPoint(lat, lng));
                    }
                }
            }
            if (pts.size() < 2) return null;
            double km = Math.round((distMeters / 1000.0) * 10.0) / 10.0;
            int mins = Math.max(2, (int) Math.round(durSeconds / 60.0));
            return new OsrmRouteResult(km, mins, pts, true);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static List<Attraction> curatedLatestPlaces(String regionKey) {
        List<Attraction> all = new ArrayList<>();
        all.add(new Attraction(
                "sync_curated_riverfront_park",
                "Saigon Riverfront Park & Sunflower Field",
                "Công viên Bờ sông Sài Gòn (Thủ Thiêm)",
                AttractionCategory.MODERN,
                "",
                R.drawable.attraction_thu_thiem_bason,
                "Phường Thủ Thiêm",
                "Khu đô thị mới Thủ Thiêm, TP. Thủ Đức, TP.HCM",
                10.7748,
                106.7112,
                "Miễn phí (Free)",
                "05:00 - 23:00",
                "Điểm ngắm hoàng hôn, cầu Ba Son và đường chân trời Quận 1 mới nhất bên bờ Đông sông Sài Gòn.",
                "Khánh thành giai đoạn mới năm 2024–2025 với cánh đồng hoa hướng dương, bến tàu thủy và phố đi bộ ven sông.",
                "Không gian dạo mát, đạp xe và thưởng thức cà phê ngắm skyline sôi động bậc nhất giới trẻ Sài Gòn.",
                "Đẹp nhất từ 16:45 đến 19:30; có thể đi Waterbus từ Bến Bạch Đằng sang thẳng công viên.",
                "TP.HCM"
        ));
        all.add(new Attraction(
                "sync_curated_metro_ben_thanh",
                "Ben Thanh Underground Metro Hub (Line 1)",
                "Ga ngầm Metro Bến Thành (Tuyến số 1)",
                AttractionCategory.MODERN,
                "",
                R.drawable.attraction_ben_thanh_market,
                "Phường Bến Thành",
                "Quảng trường Quách Thị Trang, Quận 1, TP.HCM",
                10.7716,
                106.6974,
                "7.000đ - 20.000đ / lượt",
                "05:00 - 22:00",
                "Nhà ga ngầm trung tâm biểu tượng mới của Tuyến Metro số 1 (Bến Thành – Suối Tiên) với giếng trời hoa sen khổng lồ.",
                "Chính thức vận hành thương mại kết nối trung tâm Quận 1 với Thảo Điền, Thủ Đức và Bến xe Miền Đông Mới.",
                "Trải nghiệm giao thông đô thị hiện đại, thanh toán chạm thẻ Visa/Mastercard không tiền mặt.",
                "Chạm trực tiếp thẻ ngân hàng quốc tế (Contactless) tại cổng soát vé để đi thẳng tới Thảo Điền hoặc Suối Tiên.",
                "TP.HCM"
        ));
        all.add(new Attraction(
                "sync_curated_hikari_bd",
                "Hikari Complex & Becamex New City Park",
                "Tổ hợp Ẩm thực Hikari & Công viên Thành phố Mới Bình Dương",
                AttractionCategory.MODERN,
                "",
                R.drawable.attraction_landmark_81,
                "Thành phố Mới Bình Dương",
                "Đường Lý Thái Tổ, Phường Hòa Phú, Thủ Dầu Một, Bình Dương",
                11.0535,
                106.6822,
                "Miễn phí vào cổng",
                "07:00 - 22:30",
                "Tổ hợp kiến trúc xanh chuẩn Nhật Bản kết hợp hồ sinh thái 75ha tại trung tâm hành chính Thành phố Mới Bình Dương.",
                "Phát triển theo mô hình đô thị thông minh Tokyu (Nhật Bản) - Becamex với hệ thống phân loại rác và cảnh quan bền vững.",
                "Điểm hẹn cuối tuần lý tưởng với vườn cây trái, hồ nhạc nước và ẩm thực đa quốc gia.",
                "Có tuyến xe buýt điện Kaze Shuttle kết nối thuận tiện quanh khu trung tâm Thành phố Mới.",
                "Bình Dương"
        ));
        all.add(new Attraction(
                "sync_curated_ho_tram_bridge",
                "Hamptons Pier Sea Observation Bridge",
                "Cầu ngắm biển Hamptons Pier Hồ Tràm",
                AttractionCategory.MODERN,
                "",
                R.drawable.attraction_tuong_chua_kito,
                "Hồ Tràm - Xuyên Mộc",
                "Đường ven biển Hồ Tràm, Huyện Xuyên Mộc, Bà Rịa - Vũng Tàu",
                10.4678,
                107.4315,
                "Miễn phí tham quan cầu",
                "06:00 - 22:00",
                "Cầu đi bộ vươn thẳng ra biển dài 270m đầu tiên tại khu vực Bà Rịa - Vũng Tàu, ngắm trọn bình minh và hoàng hôn.",
                "Điểm nhấn du lịch biển cao cấp trên cung đường ven biển Vũng Tàu – Long Hải – Hồ Tràm.",
                "Kết hợp phố thương mại ven biển, hải sản tươi sống và không gian chụp ảnh toàn cảnh đại dương 360 độ.",
                "Nên ghé lúc 05:45 đón bình minh hoặc 17:15 ngắm hoàng hôn trên biển Hồ Tràm.",
                "Bà Rịa - Vũng Tàu"
        ));

        if (regionKey == null || regionKey.isEmpty() || "ALL".equalsIgnoreCase(regionKey)) {
            return all;
        }
        List<Attraction> filtered = new ArrayList<>();
        for (Attraction a : all) {
            if ("HCM".equalsIgnoreCase(regionKey) && a.province.contains("TP.HCM")) {
                filtered.add(a);
            } else if ("BD".equalsIgnoreCase(regionKey) && a.province.contains("Bình Dương")) {
                filtered.add(a);
            } else if ("VT".equalsIgnoreCase(regionKey) && a.province.contains("Vũng Tàu")) {
                filtered.add(a);
            }
        }
        return filtered.isEmpty() ? all : filtered;
    }

    // =========================================================================
    // 2. ASYNC LIVE NETWORK CALLS WITH SQLITE / CURATED FALLBACKS
    // =========================================================================

    public static void fetchWeatherAsync(CityCoord city, Consumer<WeatherInfo> callback) {
        if (callback == null) return;
        final CityCoord target = city != null ? city : cities.get(0);
        IO_POOL.execute(() -> {
            WeatherInfo result = fetchWeatherBlocking(target);
            postToMain(() -> callback.accept(result));
        });
    }

    private static WeatherInfo fetchWeatherBlocking(CityCoord city) {
        try {
            String url = String.format(
                    Locale.US,
                    "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f"
                            + "&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m,uv_index"
                            + "&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max"
                            + "&timezone=Asia%%2FHo_Chi_Minh&forecast_days=3",
                    city.lat, city.lng
            );
            String json = httpGet(url, 4500);
            if (json != null) {
                JSONObject root = new JSONObject(json);
                JSONObject cur = root.getJSONObject("current");
                JSONObject daily = root.optJSONObject("daily");
                List<DailyForecast> days = new ArrayList<>();
                if (daily != null) {
                    JSONArray dates = daily.optJSONArray("time");
                    JSONArray maxs = daily.optJSONArray("temperature_2m_max");
                    JSONArray mins = daily.optJSONArray("temperature_2m_min");
                    JSONArray codes = daily.optJSONArray("weather_code");
                    JSONArray rains = daily.optJSONArray("precipitation_probability_max");
                    if (dates != null) {
                        for (int i = 0; i < Math.min(3, dates.length()); i++) {
                            days.add(new DailyForecast(
                                    dates.optString(i, ""),
                                    maxs != null ? maxs.optDouble(i, 33.0) : 33.0,
                                    mins != null ? mins.optDouble(i, 26.0) : 26.0,
                                    codes != null ? codes.optInt(i, 1) : 1,
                                    rains != null ? rains.optInt(i, 30) : 30
                            ));
                        }
                    }
                }
                return new WeatherInfo(
                        cur.optDouble("temperature_2m", 32.0),
                        cur.optDouble("apparent_temperature", 35.5),
                        cur.optInt("relative_humidity_2m", 72),
                        cur.optDouble("wind_speed_10m", 11.0),
                        cur.optInt("weather_code", 1),
                        cur.optDouble("uv_index", 7.0),
                        days,
                        true
                );
            }
        } catch (Exception ignored) {
        }

        double baseTemp = "vt".equals(city.id) ? 30.5 : ("bd".equals(city.id) ? 32.5 : 32.0);
        List<DailyForecast> fallbackDays = Arrays.asList(
                new DailyForecast("Hôm nay / Today", baseTemp + 1.5, baseTemp - 6.0, 1, 25),
                new DailyForecast("Ngày mai / Tomorrow", baseTemp + 1.0, baseTemp - 6.2, 2, 40),
                new DailyForecast("Ngày kia / Day +2", baseTemp + 0.8, baseTemp - 6.5, 80, 55)
        );
        return new WeatherInfo(baseTemp, baseTemp + 3.2, 74, 12.4, 1, 7.5, fallbackDays, false);
    }

    public static void fetchRatesAsync(Context context, Consumer<ExchangeRateResult> callback) {
        if (callback == null) return;
        final SaigonTravelDatabaseHelper db = context != null
                ? SaigonTravelDatabaseHelper.getInstance(context)
                : SaigonTravelDatabaseHelper.peekInstance();
        IO_POOL.execute(() -> {
            ExchangeRateResult res = fetchRatesBlocking(db);
            postToMain(() -> callback.accept(res));
        });
    }

    private static ExchangeRateResult fetchRatesBlocking(SaigonTravelDatabaseHelper db) {
        try {
            String json = httpGet("https://open.er-api.com/v6/latest/VND", 4500);
            if (json != null) {
                JSONObject root = new JSONObject(json);
                JSONObject ratesObj = root.optJSONObject("rates");
                if (ratesObj != null) {
                    Map<String, Double> computed = new LinkedHashMap<>();
                    for (String code : fallbackRates.keySet()) {
                        double perVnd = ratesObj.optDouble(code, 0.0);
                        if (perVnd > 0.0) {
                            computed.put(code, 1.0 / perVnd);
                        } else {
                            computed.put(code, fallbackRates.get(code));
                        }
                    }
                    String dateStr = root.optString("time_last_update_utc", "Live");
                    if (dateStr.length() > 16) dateStr = dateStr.substring(0, 16);
                    if (db != null) {
                        JSONObject cacheJson = new JSONObject();
                        for (Map.Entry<String, Double> e : computed.entrySet()) {
                            cacheJson.put(e.getKey(), e.getValue());
                        }
                        db.putSetting(KEY_CACHED_RATES_JSON, cacheJson.toString());
                        db.putSetting(KEY_CACHED_RATES_DATE, dateStr);
                    }
                    return new ExchangeRateResult(computed, dateStr, true);
                }
            }
        } catch (Exception ignored) {
        }

        if (db != null) {
            String cachedRaw = db.getSetting(KEY_CACHED_RATES_JSON, null);
            if (cachedRaw != null) {
                try {
                    JSONObject obj = new JSONObject(cachedRaw);
                    Map<String, Double> cachedMap = new LinkedHashMap<>(fallbackRates);
                    Iterator<String> keys = obj.keys();
                    while (keys.hasNext()) {
                        String k = keys.next();
                        cachedMap.put(k, obj.optDouble(k, fallbackRates.getOrDefault(k, 1.0)));
                    }
                    String cachedDate = db.getSetting(KEY_CACHED_RATES_DATE, "SQLite Cache");
                    return new ExchangeRateResult(cachedMap, cachedDate, false);
                } catch (Exception ignored) {
                }
            }
        }
        return new ExchangeRateResult(fallbackRates, "Reference Rates", false);
    }

    public static void fetchOsrmRouteAsync(
            double fromLat, double fromLng,
            double toLat, double toLng,
            Consumer<OsrmRouteResult> callback
    ) {
        if (callback == null) return;
        IO_POOL.execute(() -> {
            OsrmRouteResult res = fetchOsrmBlocking(fromLat, fromLng, toLat, toLng);
            postToMain(() -> callback.accept(res));
        });
    }

    private static OsrmRouteResult fetchOsrmBlocking(double fromLat, double fromLng, double toLat, double toLng) {
        try {
            String url = String.format(
                    Locale.US,
                    "https://router.project-osrm.org/route/v1/driving/%.5f,%.5f;%.5f,%.5f?overview=full&geometries=geojson",
                    fromLng, fromLat, toLng, toLat
            );
            String json = httpGet(url, 4000);
            OsrmRouteResult parsed = parseOsrmRouteJson(json);
            if (parsed != null) {
                return parsed;
            }
        } catch (Exception ignored) {
        }

        double dLat = Math.toRadians(toLat - fromLat);
        double dLng = Math.toRadians(toLng - fromLng);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(fromLat)) * Math.cos(Math.toRadians(toLat))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double km = Math.max(0.5, Math.round(6371.0 * 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a)) * 1.28 * 10.0) / 10.0);
        int mins = Math.max(4, (int) Math.round(km * 3.1 + 2.0));
        List<LatLngPoint> straight = Arrays.asList(
                new LatLngPoint(fromLat, fromLng),
                new LatLngPoint((fromLat + toLat) / 2.0 + 0.0012, (fromLng + toLng) / 2.0 - 0.0010),
                new LatLngPoint(toLat, toLng)
        );
        return new OsrmRouteResult(km, mins, straight, false);
    }

    public static void fetchLatestPlacesForRegionAsync(String regionKey, Consumer<SyncedPlacesResult> callback) {
        if (callback == null) return;
        IO_POOL.execute(() -> {
            SyncedPlacesResult res = fetchPlacesBlocking(regionKey);
            postToMain(() -> callback.accept(res));
        });
    }

    private static SyncedPlacesResult fetchPlacesBlocking(String regionKey) {
        List<Attraction> combined = new ArrayList<>(curatedLatestPlaces(regionKey));
        boolean liveOk = false;

        double lat = 10.7769;
        double lng = 106.7009;
        String province = "TP.HCM";
        String district = "Trung tâm TP.HCM";
        if ("BD".equalsIgnoreCase(regionKey)) {
            lat = 10.9804;
            lng = 106.6519;
            province = "Bình Dương";
            district = "Thủ Dầu Một";
        } else if ("VT".equalsIgnoreCase(regionKey)) {
            lat = 10.3460;
            lng = 107.0843;
            province = "Bà Rịa - Vũng Tàu";
            district = "TP. Vũng Tàu";
        }

        try {
            String wikiUrl = String.format(
                    Locale.US,
                    "https://vi.wikipedia.org/w/api.php?action=query&generator=geosearch&ggscoord=%.4f|%.4f&ggsradius=6000&ggslimit=6&prop=coordinates|pageimages&piprop=thumbnail&pithumbsize=800&format=json",
                    lat, lng
            );
            String wikiJson = httpGet(wikiUrl, 4000);
            List<Attraction> wikiPlaces = parseWikipediaGeoJson(wikiJson, province, district);
            if (!wikiPlaces.isEmpty()) {
                combined.addAll(wikiPlaces);
                liveOk = true;
            }
        } catch (Exception ignored) {
        }

        String label = liveOk
                ? Localized.tr("Google Tiles + Wikipedia GeoSearch trực tuyến", "Google Tiles + Live Wikipedia GeoSearch")
                : Localized.tr("Gói địa điểm mới 2025–2026 (SQLite Offline)", "2025–2026 Landmark Pack (SQLite Offline)");
        return new SyncedPlacesResult(combined, label, liveOk);
    }

    // =========================================================================
    // 3. WEATHER PRESENTATION HELPERS
    // =========================================================================

    public static String weatherEmoji(int code) {
        if (code == 0) return "☀️";
        if (code <= 3) return "⛅";
        if (code <= 48) return "🌫️";
        if (code <= 67) return "🌧️";
        if (code <= 82) return "🌦️";
        return "⛈️";
    }

    public static String weatherLabel(int code) {
        if (code == 0) return Localized.tr("Trời quang nắng đẹp", "Clear Sunny Sky");
        if (code <= 3) return Localized.tr("Nắng nhẹ, có mây", "Partly Cloudy");
        if (code <= 48) return Localized.tr("Sương mù nhẹ", "Misty / Hazy");
        if (code <= 67) return Localized.tr("Mưa rào nhẹ", "Light Rain");
        if (code <= 82) return Localized.tr("Mưa rào nhiệt đới", "Tropical Showers");
        return Localized.tr("Mưa dông chiều", "Thunderstorm");
    }

    public static String travelAdvice(WeatherInfo info) {
        if (info == null) return "";
        if (info.weatherCode >= 61) {
            return Localized.tr(
                    "Gợi ý: Có mưa rào nhiệt đới — ưu tiên Bảo tàng Chứng tích Chiến tranh, Dinh Độc Lập, Vincom hoặc đi Metro số 1.",
                    "Tip: Tropical showers expected — great time for War Remnants Museum, Independence Palace, indoor cafés, or Metro Line 1."
            );
        }
        if (info.uvIndex >= 7.0 || info.temperatureC >= 33.0) {
            return Localized.tr(
                    "Gợi ý: Chỉ số UV cao — mang kem chống nắng, uống nước dừa tươi và dạo Bến Bạch Đằng / Phố đi bộ Nguyễn Huệ sau 16:30.",
                    "Tip: High UV index — wear sunscreen, stay hydrated with fresh coconut water, and enjoy riverfront walks after 4:30 PM."
            );
        }
        return Localized.tr(
                "Thời tiết lý tưởng để dạo phố Sài Gòn, đi Waterbus trên sông và thưởng thức ẩm thực đường phố!",
                "Ideal weather for walking tours, Saigon Waterbus, and street food exploration!"
        );
    }

    private static AttractionCategory inferCategoryFromTitle(String raw) {
        String s = raw != null ? raw.toLowerCase(Locale.ROOT) : "";
        if (s.contains("chùa") || s.contains("pagoda") || s.contains("temple") || s.contains("nhà thờ") || s.contains("church")) {
            return AttractionCategory.SPIRITUAL;
        }
        if (s.contains("chợ") || s.contains("market") || s.contains("plaza") || s.contains("mall")) {
            return AttractionCategory.MARKET;
        }
        if (s.contains("dinh") || s.contains("palace") || s.contains("lịch sử") || s.contains("địa đạo") || s.contains("nhà tù")) {
            return AttractionCategory.HISTORICAL;
        }
        if (s.contains("park") || s.contains("công viên") || s.contains("metro") || s.contains("tower") || s.contains("bridge") || s.contains("cầu")) {
            return AttractionCategory.MODERN;
        }
        return AttractionCategory.CULTURAL;
    }

    private static int defaultDrawableForCategory(AttractionCategory category) {
        switch (category) {
            case HISTORICAL:
                return R.drawable.attraction_independence_palace;
            case SPIRITUAL:
                return R.drawable.attraction_jade_emperor_pagoda;
            case MARKET:
                return R.drawable.attraction_ben_thanh_market;
            case MODERN:
                return R.drawable.attraction_thu_thiem_bason;
            case CULTURAL:
            default:
                return R.drawable.attraction_saigon_opera_house;
        }
    }

    private static String httpGet(String urlStr, int timeoutMs) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            conn.setRequestProperty("User-Agent", "SaigonTravelGuide/2.0 (Android; Java SQLite)");
            int code = conn.getResponseCode();
            if (code >= 200 && code < 300) {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                }
                return sb.toString();
            }
        } catch (Exception ignored) {
        } finally {
            if (conn != null) conn.disconnect();
        }
        return null;
    }
}
