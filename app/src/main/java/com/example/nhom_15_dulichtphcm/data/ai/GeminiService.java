package com.example.nhom_15_dulichtphcm.data.ai;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.nhom_15_dulichtphcm.BuildConfig;
import com.example.nhom_15_dulichtphcm.data.db.SaigonTravelDatabaseHelper;
import com.example.nhom_15_dulichtphcm.data.local.AuthManager;
import com.example.nhom_15_dulichtphcm.data.model.AiChatMessage;
import com.example.nhom_15_dulichtphcm.data.model.Attraction;
import com.example.nhom_15_dulichtphcm.data.model.Permission;
import com.example.nhom_15_dulichtphcm.data.model.ResultBox;
import com.example.nhom_15_dulichtphcm.data.model.SpecialtyFood;
import com.example.nhom_15_dulichtphcm.data.repository.AttractionRepository;
import com.example.nhom_15_dulichtphcm.data.repository.FoodRepository;
import com.example.nhom_15_dulichtphcm.util.AppLanguage;
import com.example.nhom_15_dulichtphcm.util.SearchUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Hybrid Cloud Gemini + SQLite RAG Travel Concierge with multi-model failover,
 * multi-turn conversation memory, interactive card linking, and seamless offline fallback.
 */
public final class GeminiService {

    private static final String KEY_GEMINI_API_KEY = "gemini_api_key";
    private static final String KEY_GEMINI_PERSONAL_KEY = "gemini_personal_key";
    private static final String KEY_GEMINI_ENABLED = "gemini_enabled";

    private static final List<String> MODEL_CANDIDATES = Collections.unmodifiableList(Arrays.asList(
            "gemini-3-flash-preview",
            "gemini-3.1-flash-lite-preview",
            "gemini-flash-lite-latest",
            "gemini-3.8-flash"
    ));

    private static final ExecutorService POOL = Executors.newSingleThreadExecutor();
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
        }
        action.run();
    }

    private static volatile String userApiKey = "";
    private static volatile String personalApiKey = "";
    private static volatile boolean enabled = true;

    private GeminiService() {}

    public static synchronized void initialize(Context context) {
        if (context == null) return;
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.getInstance(context);
        userApiKey = db.getSetting(KEY_GEMINI_API_KEY, "");
        personalApiKey = db.getSetting(KEY_GEMINI_PERSONAL_KEY, "");
        enabled = !"false".equalsIgnoreCase(db.getSetting(KEY_GEMINI_ENABLED, "true"));
    }

    public static boolean hasBuildKey() {
        return BuildConfig.GEMINI_API_KEY != null && !BuildConfig.GEMINI_API_KEY.trim().isEmpty();
    }

    public static String getUserApiKey() {
        if (personalApiKey != null && !personalApiKey.trim().isEmpty()) {
            return personalApiKey;
        }
        return userApiKey != null ? userApiKey : "";
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean isActive() {
        if (!enabled) return false;
        return !getEffectiveKey().isEmpty();
    }

    private static String getEffectiveKey() {
        if (personalApiKey != null && !personalApiKey.trim().isEmpty()) {
            return personalApiKey.trim();
        }
        if (userApiKey != null && !userApiKey.trim().isEmpty()) {
            return userApiKey.trim();
        }
        return BuildConfig.GEMINI_API_KEY != null ? BuildConfig.GEMINI_API_KEY.trim() : "";
    }

    public static synchronized ResultBox<Void> saveApiKey(String apiKey) {
        ResultBox<Void> perm = AuthManager.requirePermission(Permission.CONFIGURE_SYSTEM);
        if (!perm.isSuccess()) {
            return perm;
        }
        userApiKey = apiKey != null ? apiKey.trim() : "";
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            db.putSetting(KEY_GEMINI_API_KEY, userApiKey);
        }
        AuthManager.recordAudit("Cập nhật khóa kết nối Trợ lý Đám mây", "Updated Cloud Concierge API key");
        return ResultBox.success(null);
    }

    public static synchronized void savePersonalKey(String apiKey) {
        personalApiKey = apiKey != null ? apiKey.trim() : "";
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            db.putSetting(KEY_GEMINI_PERSONAL_KEY, personalApiKey);
        }
    }

    public static synchronized ResultBox<Void> updateEnabled(boolean isEnabled) {
        ResultBox<Void> perm = AuthManager.requirePermission(Permission.CONFIGURE_SYSTEM);
        if (!perm.isSuccess()) {
            return perm;
        }
        enabled = isEnabled;
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            db.putSetting(KEY_GEMINI_ENABLED, String.valueOf(isEnabled));
        }
        return ResultBox.success(null);
    }

    public static void generateReplyAsync(
            String query,
            AppLanguage language,
            Consumer<AiChatMessage> callback
    ) {
        generateReplyAsync(query, Collections.emptyList(), language, callback);
    }

    public static void generateReplyAsync(
            String query,
            List<AiChatMessage> history,
            AppLanguage language,
            Consumer<AiChatMessage> callback
    ) {
        if (callback == null) return;
        final List<AiChatMessage> safeHistory = history != null
                ? new ArrayList<>(history)
                : Collections.emptyList();

        POOL.execute(() -> {
            AiChatMessage reply = null;
            if (isActive()) {
                reply = callGeminiWithFailover(query, safeHistory, language, getEffectiveKey());
            }
            if (reply == null) {
                reply = AiTravelEngine.generateOfflineReply(query, safeHistory, language);
            }
            final AiChatMessage finalReply = reply;
            postToMain(() -> callback.accept(finalReply));
        });
    }

    private static AiChatMessage callGeminiWithFailover(
            String query,
            List<AiChatMessage> history,
            AppLanguage lang,
            String apiKey
    ) {
        String payloadJson = buildRequestPayload(query, history, lang);
        if (payloadJson == null) return null;
        byte[] bodyBytes = payloadJson.getBytes(StandardCharsets.UTF_8);

        for (String modelName : MODEL_CANDIDATES) {
            HttpResult res = postJsonModel(modelName, apiKey, bodyBytes);
            if (res.code >= 200 && res.code < 300 && res.body != null) {
                AiChatMessage parsed = parseGeminiSuccess(query, res.body, lang);
                if (parsed != null) {
                    return parsed;
                }
            }
            // Stop retrying immediately if API key itself is invalid/unauthorized
            if (res.code == 400 || res.code == 401 || res.code == 403) {
                break;
            }
        }
        return null;
    }

    private static String buildRequestPayload(
            String query,
            List<AiChatMessage> history,
            AppLanguage lang
    ) {
        try {
            JSONObject root = new JSONObject();

            String systemText = AiTravelEngine.buildGroundedContextForPrompt(query, lang);
            JSONObject sysPart = new JSONObject().put("text", systemText);
            JSONObject sysInstruction = new JSONObject().put("parts", new JSONArray().put(sysPart));
            root.put("system_instruction", sysInstruction);

            JSONArray contents = new JSONArray();
            if (history != null && !history.isEmpty()) {
                // Collect up to the last 6 messages prior to the newly appended user message
                int endExclusive = history.size();
                if (endExclusive > 0 && history.get(endExclusive - 1).isUser
                        && query.trim().equals(history.get(endExclusive - 1).text.trim())) {
                    endExclusive--;
                }
                int startIdx = Math.max(0, endExclusive - 6);
                String lastRole = "";
                for (int i = startIdx; i < endExclusive; i++) {
                    AiChatMessage m = history.get(i);
                    if (m == null || m.text == null || m.text.trim().isEmpty()) continue;
                    String role = m.isUser ? "user" : "model";
                    // Gemini requires contents to begin with a "user" turn and alternate roles
                    if (contents.length() == 0 && !"user".equals(role)) continue;
                    if (role.equals(lastRole)) continue;
                    JSONObject msgPart = new JSONObject().put("text", m.text.trim());
                    JSONObject turn = new JSONObject()
                            .put("role", role)
                            .put("parts", new JSONArray().put(msgPart));
                    contents.put(turn);
                    lastRole = role;
                }
                // Ensure the history ends on a "model" turn before appending the current "user" query
                if ("user".equals(lastRole) && contents.length() > 0) {
                    contents.remove(contents.length() - 1);
                }
            }

            JSONObject userPart = new JSONObject().put("text", query.trim());
            JSONObject userTurn = new JSONObject()
                    .put("role", "user")
                    .put("parts", new JSONArray().put(userPart));
            contents.put(userTurn);
            root.put("contents", contents);

            JSONObject genConfig = new JSONObject()
                    .put("temperature", 0.45)
                    .put("maxOutputTokens", 1200);
            root.put("generationConfig", genConfig);

            return root.toString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static HttpResult postJsonModel(String modelName, String apiKey, byte[] bodyBytes) {
        HttpURLConnection conn = null;
        try {
            String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + modelName + ":generateContent?key=" + apiKey;
            URL url = new URL(endpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(6500);
            conn.setReadTimeout(11000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(bodyBytes);
            }

            int code = conn.getResponseCode();
            if (code >= 200 && code < 300) {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                }
                return new HttpResult(code, sb.toString());
            }
            return new HttpResult(code, null);
        } catch (Exception ignored) {
            return new HttpResult(-1, null);
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private static AiChatMessage parseGeminiSuccess(String query, String jsonBody, AppLanguage lang) {
        try {
            JSONObject res = new JSONObject(jsonBody);
            JSONArray candidates = res.optJSONArray("candidates");
            if (candidates == null || candidates.length() == 0) return null;
            JSONObject c0 = candidates.getJSONObject(0);
            JSONObject content = c0.optJSONObject("content");
            if (content == null) return null;
            JSONArray parts = content.optJSONArray("parts");
            if (parts == null || parts.length() == 0) return null;

            StringBuilder textBuilder = new StringBuilder();
            for (int i = 0; i < parts.length(); i++) {
                JSONObject part = parts.optJSONObject(i);
                if (part == null || part.optBoolean("thought", false)) continue;
                String piece = part.optString("text", "");
                if (!piece.isEmpty()) {
                    textBuilder.append(piece);
                }
            }
            String text = textBuilder.toString().trim();
            if (text.isEmpty()) return null;

            String combinedNorm = SearchUtils.normalize(query + " " + text);
            List<Attraction> matchedAttr = AiTravelEngine.findMatchingAttractions(
                    combinedNorm, AttractionRepository.getAttractions(), 3);
            List<SpecialtyFood> matchedFood = AiTravelEngine.findMatchingFoods(
                    combinedNorm, FoodRepository.getSpecialtyFoods(), 2);

            List<String> followUps = buildContextualFollowUps(combinedNorm, lang == AppLanguage.VIETNAMESE);

            return new AiChatMessage(
                    UUID.randomUUID().toString(),
                    false,
                    text,
                    matchedAttr,
                    matchedFood,
                    Collections.emptyList(),
                    followUps,
                    true
            );
        } catch (Exception ignored) {
            return null;
        }
    }

    private static List<String> buildContextualFollowUps(String normText, boolean isVi) {
        if (normText.contains("vung tau") || normText.contains("ho tram")) {
            return isVi
                    ? Arrays.asList("Bánh khọt Vũng Tàu quán nào ngon?", "Tàu cao tốc Bạch Đằng - Vũng Tàu", "Khách sạn gần Bãi Sau")
                    : Arrays.asList("Best Banh Khot in Vung Tau", "Bach Dang to Vung Tau fast ferry", "Where to stay at Back Beach");
        }
        if (normText.contains("an ") || normText.contains("quan ") || normText.contains("com tam") || normText.contains("banh mi")) {
            return isVi
                    ? Arrays.asList("Quán ăn đêm ở Sài Gòn", "Quán cà phê đẹp Quận 1", "Quán chay ngon ở Sài Gòn")
                    : Arrays.asList("Best late-night street food", "Iconic Saigon coffee spots", "Vegetarian dining in Saigon");
        }
        return isVi
                ? Arrays.asList("Ước tính chi phí chuyến đi", "Cách đi Metro số 1 & Waterbus", "Mua quà lưu niệm gì ở Sài Gòn?")
                : Arrays.asList("Estimated daily budget", "Metro Line 1 & Waterbus guide", "Best souvenirs to buy in Saigon");
    }

    private static final class HttpResult {
        final int code;
        final String body;

        HttpResult(int code, String body) {
            this.code = code;
            this.body = body;
        }
    }
}
