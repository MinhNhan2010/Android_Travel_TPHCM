package com.example.nhom_15_dulichtphcm.data.repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Curated Transit Dataset for HCMC Metro Line 1 (Bến Thành – Suối Tiên, 14 stations)
 * and Saigon Waterbus (Bạch Đằng – Linh Đông, 5 piers).
 */
public final class MetroTransitData {

    public static final class TransitStation {
        public final int index;
        public final String nameVi;
        public final String nameEn;
        public final String zoneLabel;
        public final double latitude;
        public final double longitude;
        public final String nearbyHighlightsVi;
        public final String nearbyHighlightsEn;
        public final String linkedAttractionId;

        public TransitStation(
                int index,
                String nameVi,
                String nameEn,
                String zoneLabel,
                double latitude,
                double longitude,
                String nearbyHighlightsVi,
                String nearbyHighlightsEn,
                String linkedAttractionId
        ) {
            this.index = index;
            this.nameVi = nameVi;
            this.nameEn = nameEn;
            this.zoneLabel = zoneLabel;
            this.latitude = latitude;
            this.longitude = longitude;
            this.nearbyHighlightsVi = nearbyHighlightsVi;
            this.nearbyHighlightsEn = nearbyHighlightsEn;
            this.linkedAttractionId = linkedAttractionId;
        }
    }

    public static final List<TransitStation> METRO_LINE_1 = Collections.unmodifiableList(Arrays.asList(
            new TransitStation(0, "Ga Bến Thành (Ngầm)", "Ben Thanh Underground", "Quận 1", 10.7720, 106.6981,
                    "Chợ Bến Thành, Bảo tàng Mỹ thuật, Phố Bùi Viện", "Ben Thanh Market, Fine Arts Museum, Bui Vien", "ben_thanh_market"),
            new TransitStation(1, "Ga Nhà hát Thành phố (Ngầm)", "Opera House Underground", "Quận 1", 10.7768, 106.7032,
                    "Phố đi bộ Nguyễn Huệ, Nhà thờ Đức Bà, Bưu điện Trung tâm", "Nguyen Hue Walking St, Notre-Dame, Post Office", "nguyen_hue_walking_street"),
            new TransitStation(2, "Ga Ba Son (Ngầm)", "Ba Son Underground", "Quận 1", 10.7817, 106.7082,
                    "Cầu Ba Son, Bến Bạch Đằng, Bờ sông Thủ Thiêm", "Ba Son Bridge, Bach Dang Wharf, Riverfront", "thu_thiem_waterfront"),
            new TransitStation(3, "Ga Công viên Văn Thánh", "Van Thanh Park", "Bình Thạnh", 10.7958, 106.7158,
                    "Khu du lịch Văn Thánh, Ẩm thực đường D2", "Van Thanh Tourist Park, Pearl Plaza", "landmark_81"),
            new TransitStation(4, "Ga Tân Cảng (Landmark 81)", "Tan Cang (Landmark 81)", "Bình Thạnh", 10.7985, 106.7225,
                    "Tòa tháp Landmark 81, Công viên Vinhomes Central Park", "Landmark 81 SkyView & Riverside Park", "landmark_81"),
            new TransitStation(5, "Ga Thảo Điền", "Thao Dien", "TP. Thủ Đức", 10.8035, 106.7336,
                    "Khu phố Tây Xuân Thủy, Quán cà phê & nhà hàng ven sông", "Xuan Thuy Dining Quarter & Boutiques", "landmark_81"),
            new TransitStation(6, "Ga An Phú", "An Phu", "TP. Thủ Đức", 10.8021, 106.7429,
                    "Vincom Mega Mall Thảo Điền, Estella Place", "Mega Mall Thao Dien & Estella Place", "landmark_81"),
            new TransitStation(7, "Ga Rạch Chiếc", "Rach Chiec", "TP. Thủ Đức", 10.8080, 106.7554,
                    "Khu liên hợp Thể thao Quốc gia Rạch Chiếc", "Rach Chiec Sports Complex", "thu_thiem_waterfront"),
            new TransitStation(8, "Ga Phước Long", "Phuoc Long", "TP. Thủ Đức", 10.8213, 106.7582,
                    "Làng cà phê Thủ Đức, Xa lộ Võ Nguyên Giáp", "Vo Nguyen Giap Boulevard", "thu_thiem_waterfront"),
            new TransitStation(9, "Ga Bình Thái", "Binh Thai", "TP. Thủ Đức", 10.8327, 106.7639,
                    "Ngã tư Bình Thái, kết nối Vành đai 2", "Binh Thai Interchange", "thu_thiem_waterfront"),
            new TransitStation(10, "Ga Thủ Đức", "Thu Duc", "TP. Thủ Đức", 10.8464, 106.7717,
                    "Ngã tư Thủ Đức, Chợ Bắc Ninh, Phố trà sữa Võ Văn Ngân", "Vo Van Ngan Youth Street", "suoi_tien_park"),
            new TransitStation(11, "Ga Khu Công nghệ cao", "Hi-Tech Park", "TP. Thủ Đức", 10.8588, 106.7889,
                    "Khu Công nghệ cao TP.HCM (SHTP)", "Saigon Hi-Tech Park", "suoi_tien_park"),
            new TransitStation(12, "Ga Đại học Quốc gia", "National University", "TP. Thủ Đức", 10.8662, 106.8012,
                    "Khu Đô thị ĐHQG TP.HCM, Hồ Đá, KDL Văn hóa Suối Tiên", "Suoi Tien Theme Park & VNU Campus", "suoi_tien_park"),
            new TransitStation(13, "Ga Bến xe Suối Tiên", "Suoi Tien Terminal", "TP. Thủ Đức / Dĩ An", 10.8712, 106.8130,
                    "Bến xe Miền Đông Mới, kết nối đi Bình Dương & Vũng Tàu", "New Mien Dong Bus Station → Binh Duong & Vung Tau", "suoi_tien_park")
    ));

    public static final List<TransitStation> SAIGON_WATERBUS = Collections.unmodifiableList(Arrays.asList(
            new TransitStation(0, "Bến Bạch Đằng (Trung tâm Q.1)", "Bach Dang Central Pier", "Quận 1", 10.7745, 106.7068,
                    "Phố đi bộ Nguyễn Huệ, Cầu Ba Son, Tàu cao tốc đi Vũng Tàu", "Nguyen Hue St, Fast Ferry to Vung Tau", "nguyen_hue_walking_street"),
            new TransitStation(1, "Bến Bình An (Thủ Thiêm)", "Binh An Pier", "TP. Thủ Đức", 10.7852, 106.7294,
                    "Ngắm toàn cảnh Landmark 81 từ mặt sông lúc hoàng hôn", "Best sunset view of Landmark 81 skyline", "landmark_81"),
            new TransitStation(2, "Bến Thanh Đa (Bán đảo xanh)", "Thanh Da Peninsula Pier", "Bình Thạnh", 10.8124, 106.7235,
                    "Bán đảo Thanh Đa bình yên, cháo vịt Thanh Đa nổi tiếng", "Peaceful river peninsula & local duck porridge", "landmark_81"),
            new TransitStation(3, "Bến Hiệp Bình Chánh", "Hiep Binh Chanh Pier", "TP. Thủ Đức", 10.8290, 106.7220,
                    "Gigamall Phạm Văn Đồng, nhà hàng ven sông Sài Gòn", "Riverside dining & Gigamall", "thu_thiem_waterfront"),
            new TransitStation(4, "Bến Linh Đông", "Linh Dong Terminal Pier", "TP. Thủ Đức", 10.8455, 106.7422,
                    "Làng sinh thái ven sông Thủ Đức", "Riverside eco-gardens", "thu_thiem_waterfront")
    ));

    private MetroTransitData() {}

    /**
     * Official HCMC Metro Line 1 single-ride fare structure (7,000đ – 20,000đ cash; 6,000đ – 19,000đ cashless).
     */
    public static int calculateMetroCashFareVnd(int fromIdx, int toIdx) {
        int stops = Math.abs(toIdx - fromIdx);
        if (stops == 0) return 0;
        if (stops <= 3) return 7_000;
        if (stops <= 6) return 10_000;
        if (stops <= 9) return 14_000;
        if (stops <= 11) return 18_000;
        return 20_000;
    }

    public static int calculateMetroCashlessFareVnd(int fromIdx, int toIdx) {
        int cash = calculateMetroCashFareVnd(fromIdx, toIdx);
        return cash > 0 ? cash - 1_000 : 0;
    }

    public static int calculateTravelMinutes(boolean isWaterbus, int fromIdx, int toIdx) {
        int stops = Math.abs(toIdx - fromIdx);
        if (stops == 0) return 0;
        return isWaterbus ? stops * 12 : Math.max(3, Math.round(stops * 2.3f));
    }
}
