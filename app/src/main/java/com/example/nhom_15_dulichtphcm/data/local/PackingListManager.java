package com.example.nhom_15_dulichtphcm.data.local;

import android.content.Context;

import com.example.nhom_15_dulichtphcm.data.db.SaigonTravelDatabaseHelper;
import com.example.nhom_15_dulichtphcm.util.Localized;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Smart Regional Packing Checklist manager backed by SQLite table packing_items.
 */
public final class PackingListManager {

    public enum PresetRegion {
        SAIGON_CITY("saigon", "🏙️ Phố thị Sài Gòn", "🏙️ Saigon City"),
        VUNG_TAU_BEACH("vungtau", "🌊 Biển Vũng Tàu", "🌊 Vung Tau Beach"),
        BINH_DUONG_TEMPLE("binhduong", "🌳 Chùa & Làng nghề BD", "🌳 Binh Duong Heritage");

        public final String code;
        public final String labelVi;
        public final String labelEn;

        PresetRegion(String code, String labelVi, String labelEn) {
            this.code = code;
            this.labelVi = labelVi;
            this.labelEn = labelEn;
        }
    }

    private static final List<SaigonTravelDatabaseHelper.PackingRow> items = new ArrayList<>();

    private PackingListManager() {}

    public static synchronized void initialize(Context context) {
        if (context != null) {
            SaigonTravelDatabaseHelper.getInstance(context);
        }
        reloadActiveScope();
    }

    public static synchronized void reloadActiveScope() {
        items.clear();
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        String scope = AuthManager.getActiveUserScope();
        if (db != null) {
            List<SaigonTravelDatabaseHelper.PackingRow> saved = db.getPackingItems(scope);
            if (saved.isEmpty()) {
                seedDefaultPreset(scope, PresetRegion.SAIGON_CITY, db);
                items.addAll(db.getPackingItems(scope));
            } else {
                items.addAll(saved);
            }
        } else {
            items.addAll(buildPresetRows(PresetRegion.SAIGON_CITY));
        }
    }

    public static synchronized List<SaigonTravelDatabaseHelper.PackingRow> getItemsByPreset(PresetRegion region) {
        List<SaigonTravelDatabaseHelper.PackingRow> filtered = new ArrayList<>();
        for (SaigonTravelDatabaseHelper.PackingRow r : items) {
            if (region == null || region.code.equals(r.presetGroup) || "custom".equals(r.presetGroup)) {
                filtered.add(r);
            }
        }
        if (filtered.isEmpty() && region != null) {
            ensurePresetLoaded(region);
            for (SaigonTravelDatabaseHelper.PackingRow r : items) {
                if (region.code.equals(r.presetGroup) || "custom".equals(r.presetGroup)) {
                    filtered.add(r);
                }
            }
        }
        return Collections.unmodifiableList(filtered);
    }

    public static synchronized List<SaigonTravelDatabaseHelper.PackingRow> getAllItems() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

    public static synchronized void ensurePresetLoaded(PresetRegion region) {
        if (region == null) return;
        for (SaigonTravelDatabaseHelper.PackingRow r : items) {
            if (region.code.equals(r.presetGroup)) return;
        }
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        String scope = AuthManager.getActiveUserScope();
        List<SaigonTravelDatabaseHelper.PackingRow> rows = buildPresetRows(region);
        for (SaigonTravelDatabaseHelper.PackingRow r : rows) {
            items.add(r);
            if (db != null) db.upsertPackingItem(scope, r);
        }
    }

    public static synchronized void toggleItem(String id) {
        if (id == null) return;
        String scope = AuthManager.getActiveUserScope();
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        for (int i = 0; i < items.size(); i++) {
            SaigonTravelDatabaseHelper.PackingRow cur = items.get(i);
            if (id.equals(cur.id)) {
                SaigonTravelDatabaseHelper.PackingRow updated = new SaigonTravelDatabaseHelper.PackingRow(
                        cur.id, cur.presetGroup, cur.titleVi, cur.titleEn, !cur.isChecked, cur.sortOrder);
                items.set(i, updated);
                if (db != null) db.upsertPackingItem(scope, updated);
                return;
            }
        }
    }

    public static synchronized boolean addCustomItem(PresetRegion region, String rawTitle) {
        String clean = rawTitle != null ? rawTitle.trim() : "";
        if (clean.length() < 2) return false;
        String scope = AuthManager.getActiveUserScope();
        String group = region != null ? region.code : PresetRegion.SAIGON_CITY.code;
        SaigonTravelDatabaseHelper.PackingRow row = new SaigonTravelDatabaseHelper.PackingRow(
                "pk_" + UUID.randomUUID().toString().substring(0, 8),
                group,
                "✨ " + clean,
                "✨ " + clean,
                false,
                items.size() + 1
        );
        items.add(row);
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) db.upsertPackingItem(scope, row);
        return true;
    }

    public static synchronized void deleteItem(String id) {
        if (id == null) return;
        items.removeIf(r -> id.equals(r.id));
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) db.deletePackingItem(id);
    }

    public static synchronized int getCompletionPercent(PresetRegion region) {
        List<SaigonTravelDatabaseHelper.PackingRow> list = getItemsByPreset(region);
        if (list.isEmpty()) return 0;
        int checked = 0;
        for (SaigonTravelDatabaseHelper.PackingRow r : list) {
            if (r.isChecked) checked++;
        }
        return Math.round((checked * 100f) / list.size());
    }

    private static void seedDefaultPreset(String scope, PresetRegion region, SaigonTravelDatabaseHelper db) {
        for (SaigonTravelDatabaseHelper.PackingRow r : buildPresetRows(region)) {
            db.upsertPackingItem(scope, r);
        }
    }

    private static List<SaigonTravelDatabaseHelper.PackingRow> buildPresetRows(PresetRegion region) {
        List<SaigonTravelDatabaseHelper.PackingRow> list = new ArrayList<>();
        String[][] pairs;
        if (region == PresetRegion.VUNG_TAU_BEACH) {
            pairs = new String[][]{
                    {"🧴 Kem chống nắng SPF 50+ & kính râm", "🧴 SPF 50+ Sunscreen & Sunglasses"},
                    {"👙 Đồ bơi, khăn tắm biển & dép xỏ ngón", "👙 Swimwear, Beach Towel & Flip-Flops"},
                    {"💊 Thuốc chống say tàu cao tốc / xe khách", "💊 Motion Sickness Tablets (Fast Ferry)"},
                    {"🧢 Mũ rộng vành chụp ảnh Mũi Nghinh Phong", "🧢 Wide-Brim Hat for Coastal Walk"},
                    {"📱 Túi chống nước bảo vệ điện thoại", "📱 Waterproof Phone Pouch"}
            };
        } else if (region == PresetRegion.BINH_DUONG_TEMPLE) {
            pairs = new String[][]{
                    {"👕 Trang phục kín đáo, lịch sự khi viếng Chùa Bà", "👕 Modest Attire for Temple Visits"},
                    {"👟 Giày thể thao êm chân đi KDL Đại Nam", "👟 Comfortable Walking Sneakers"},
                    {"🛍️ Túi vải chống sốc mua gốm sứ Lái Thiêu", "🛍️ Padded Tote Bag for Lai Thieu Pottery"},
                    {"💧 Bình nước giữ nhiệt cá nhân", "💧 Reusable Insulated Water Bottle"},
                    {"🦟 Xịt chống muỗi sinh thái vườn trái cây", "🦟 Herbal Mosquito Repellent"}
            };
        } else {
            pairs = new String[][]{
                    {"🪪 CCCD / Hộ chiếu & Thẻ thanh toán chạm Metro", "🪪 Passport / ID & Contactless Metro Card"},
                    {"🔋 Sạc dự phòng 10.000mAh & cáp sạc nhanh", "🔋 Power Bank & Fast Charging Cable"},
                    {"☂️ Ô gấp gọn / Áo mưa mỏng phòng mưa bất chợt", "☂️ Compact Umbrella / Light Raincoat"},
                    {"💵 Tiền mặt mệnh giá nhỏ (10k–50k) ăn vặt vỉa hè", "💵 Small VND Notes for Street Food Stalls"},
                    {"👟 Giày đi bộ thoáng khí dạo Quận 1 – Nguyễn Huệ", "👟 Breathable Walking Shoes for District 1"}
            };
        }
        String scope = AuthManager.getActiveUserScope();
        for (int i = 0; i < pairs.length; i++) {
            list.add(new SaigonTravelDatabaseHelper.PackingRow(
                    "pk_" + scope + "_" + region.code + "_" + i,
                    region.code,
                    pairs[i][0],
                    pairs[i][1],
                    i == 0,
                    i
            ));
        }
        return list;
    }

    public static String formatChecklistSummary(PresetRegion region) {
        List<SaigonTravelDatabaseHelper.PackingRow> list = getItemsByPreset(region);
        StringBuilder sb = new StringBuilder();
        sb.append(Localized.tr(region.labelVi, region.labelEn))
                .append(" (").append(getCompletionPercent(region)).append("%)\n");
        for (SaigonTravelDatabaseHelper.PackingRow r : list) {
            sb.append(r.isChecked ? "☑ " : "☐ ")
                    .append(Localized.tr(r.titleVi, r.titleEn))
                    .append("\n");
        }
        return sb.toString();
    }
}
