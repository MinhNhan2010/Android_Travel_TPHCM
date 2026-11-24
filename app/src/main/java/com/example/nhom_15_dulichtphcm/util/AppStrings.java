package com.example.nhom_15_dulichtphcm.util;

public final class AppStrings {

    private AppStrings() {}

    public static String get(String key) {
        return get(key, LanguageManager.getCurrentLanguage());
    }

    public static String get(String key, AppLanguage lang) {
        if (key == null) return "";
        if (lang == null) lang = AppLanguage.ENGLISH;

        switch (key) {
            case "app_title":
                switch (lang) {
                    case VIETNAMESE: return "Khám phá Sài Gòn 🇻🇳";
                    case FRENCH: return "Explorer Saigon 🇻🇳";
                    case JAPANESE: return "サイゴン探索 🇻🇳";
                    case KOREAN: return "사이공 탐험 🇻🇳";
                    case CHINESE: return "探索西贡 🇻🇳";
                    default: return "Explore Saigon 🇻🇳";
                }
            case "app_subtitle":
                switch (lang) {
                    case VIETNAMESE: return "Cẩm nang du lịch TP. Hồ Chí Minh";
                    case FRENCH: return "Guide de voyage de Ho Chi Minh Ville";
                    case JAPANESE: return "ホーチミン市旅行ガイド";
                    case KOREAN: return "호치민시 여행 가이드";
                    case CHINESE: return "胡志明市旅游指南";
                    default: return "Ho Chi Minh City Travel Guide";
                }
            case "tab_places":
                switch (lang) {
                    case VIETNAMESE: return "Địa điểm";
                    case FRENCH: return "Lieux";
                    case JAPANESE: return "観光地";
                    case KOREAN: return "명소";
                    case CHINESE: return "景点";
                    default: return "Places";
                }
            case "tab_food":
                switch (lang) {
                    case VIETNAMESE: return "Ẩm thực";
                    case FRENCH: return "Cuisine";
                    case JAPANESE: return "グルメ";
                    case KOREAN: return "음식";
                    case CHINESE: return "美食";
                    default: return "Food";
                }
            case "tab_utilities":
                switch (lang) {
                    case VIETNAMESE: return "Tiện ích";
                    case FRENCH: return "Services";
                    case JAPANESE: return "公共施設";
                    case KOREAN: return "편의시설";
                    case CHINESE: return "公共设施";
                    default: return "Utilities";
                }
            case "tab_phrases":
                switch (lang) {
                    case VIETNAMESE: return "Giao tiếp";
                    case FRENCH: return "Phrases";
                    case JAPANESE: return "会話";
                    case KOREAN: return "회화";
                    case CHINESE: return "会话";
                    default: return "Phrases";
                }
            case "tab_map":
                switch (lang) {
                    case VIETNAMESE: return "Bản đồ";
                    case FRENCH: return "Carte";
                    case JAPANESE: return "地図";
                    case KOREAN: return "지도";
                    case CHINESE: return "地图";
                    default: return "Map";
                }
            case "ai_assistant_fab":
                switch (lang) {
                    case VIETNAMESE: return "Cẩm nang hỏi đáp";
                    case FRENCH: return "Guide de voyage";
                    case JAPANESE: return "観光ガイド";
                    case KOREAN: return "여행 가이드";
                    case CHINESE: return "旅游向导";
                    default: return "Travel Guide";
                }
            case "ai_banner_text":
                switch (lang) {
                    case VIETNAMESE: return "Gợi ý lịch trình, món ngon & tuyến đi lại TP.HCM";
                    case FRENCH: return "Idées d'itinéraires, restaurants et trajets à Saigon";
                    case JAPANESE: return "モデルコース・グルメ・移動手段をまとめて検索";
                    case KOREAN: return "일정 추천, 맛집 및 이동 경로 한눈에 보기";
                    case CHINESE: return "获取西贡行程建议、地道美食与出行攻略";
                    default: return "Smart itinerary, food & transit guide for Saigon";
                }
            case "utilities_title":
                switch (lang) {
                    case VIETNAMESE: return "Tiện ích công cộng Sài Gòn 🏙️";
                    case FRENCH: return "Services Publics de Saigon 🏙️";
                    case JAPANESE: return "サイゴン公共サービス 🏙️";
                    case KOREAN: return "사이공 공공 편의시설 🏙️";
                    case CHINESE: return "西贡公共服务设施 🏙️";
                    default: return "Saigon Public Utilities 🏙️";
                }
            case "utilities_subtitle":
                switch (lang) {
                    case VIETNAMESE: return "Nhà vệ sinh, đổi tiền, trạm Metro, bệnh viện & cứu trợ du khách";
                    case FRENCH: return "Toilettes, change, métro, hôpitaux et assistance touristique";
                    case JAPANESE: return "公衆トイレ、両替、地下鉄、救急病院、観光サポート";
                    case KOREAN: return "공중 화장실, 환전소, 지하철, 병원 및 관광 안내";
                    case CHINESE: return "公厕、货币兑换、地铁站、医院和旅游求助";
                    default: return "Public restrooms, money exchange, metro transit, clinics & emergency";
                }
            case "action_directions":
                switch (lang) {
                    case VIETNAMESE: return "Chỉ đường";
                    case FRENCH: return "Itinéraire";
                    case JAPANESE: return "道順";
                    case KOREAN: return "길찾기";
                    case CHINESE: return "导航";
                    default: return "Directions";
                }
            case "action_google_maps":
                return "Google Maps";
            case "action_view_on_map":
                switch (lang) {
                    case VIETNAMESE: return "Xem trên bản đồ";
                    case FRENCH: return "Voir sur la carte";
                    case JAPANESE: return "地図で見る";
                    case KOREAN: return "지도에서 보기";
                    case CHINESE: return "在地图上查看";
                    default: return "View on Map";
                }
            case "action_call_now":
                switch (lang) {
                    case VIETNAMESE: return "Gọi ngay";
                    case FRENCH: return "Appeler";
                    case JAPANESE: return "電話をかける";
                    case KOREAN: return "전화 걸기";
                    case CHINESE: return "拨打电话";
                    default: return "Call Hotline";
                }
            case "action_copy_coords":
                switch (lang) {
                    case VIETNAMESE: return "Chép tọa độ";
                    case FRENCH: return "Copier coords";
                    case JAPANESE: return "座標をコピー";
                    case KOREAN: return "좌표 복사";
                    case CHINESE: return "复制坐标";
                    default: return "Copy Coordinates";
                }
            case "section_history":
                switch (lang) {
                    case VIETNAMESE: return "Lịch sử & Kiến trúc";
                    case FRENCH: return "Histoire & Architecture";
                    case JAPANESE: return "歴史と建築";
                    case KOREAN: return "역사 및 건축";
                    case CHINESE: return "历史与建筑";
                    default: return "Historical Background";
                }
            case "section_culture":
                switch (lang) {
                    case VIETNAMESE: return "Ý nghĩa văn hóa";
                    case FRENCH: return "Importance culturelle";
                    case JAPANESE: return "文化的意義";
                    case KOREAN: return "문화적 의미";
                    case CHINESE: return "文化意义";
                    default: return "Cultural Significance";
                }
            case "section_eateries":
                switch (lang) {
                    case VIETNAMESE: return "Địa chỉ các quán ăn tương ứng";
                    case FRENCH: return "Restaurants recommandés";
                    case JAPANESE: return "おすすめの名店";
                    case KOREAN: return "추천 맛집 및 위치";
                    case CHINESE: return "推荐餐馆及地址";
                    default: return "Famous Eateries & Coordinates";
                }
            case "section_local_eating":
                switch (lang) {
                    case VIETNAMESE: return "Cách ăn chuẩn người Sài Gòn";
                    case FRENCH: return "Comment manger comme un local";
                    case JAPANESE: return "ローカル流の食べ方";
                    case KOREAN: return "현지인처럼 먹는 법";
                    case CHINESE: return "地道吃法指南";
                    default: return "How to Eat Like a Local";
                }
            case "food_screen_title":
                switch (lang) {
                    case VIETNAMESE: return "Ẩm thực Sài Gòn 🍜";
                    case FRENCH: return "Cuisine de Saigon 🍜";
                    case JAPANESE: return "サイゴン グルメ 🍜";
                    case KOREAN: return "사이공 대표 미식 🍜";
                    case CHINESE: return "西贡特色美食 🍜";
                    default: return "Saigon Culinary Trail 🍜";
                }
            case "food_screen_subtitle":
                switch (lang) {
                    case VIETNAMESE: return "Món ngon trứ danh & quán ăn lâu đời với tọa độ bản đồ chính xác";
                    case FRENCH: return "Plats emblématiques & restaurants réputés avec coordonnées GPS";
                    case JAPANESE: return "名物料理と有名店の正確なGPS地図座標付きガイド";
                    case KOREAN: return "정확한 GPS 좌표가 포함된 대표 요리와 유명 맛집";
                    case CHINESE: return "地道招牌美食与老字号名店（附精确地图导航）";
                    default: return "Iconic dishes & famous eateries with exact map coordinates";
                }
            case "food_search_placeholder":
                switch (lang) {
                    case VIETNAMESE: return "Tìm cơm tấm, phở, bánh mì, quán ăn, quận...";
                    case FRENCH: return "Trouver com tam, pho, banh mi, restaurants...";
                    case JAPANESE: return "コムタム、フォー、バインミー、店舗、地区を検索...";
                    case KOREAN: return "껌땀, 쌀국수, 반미, 식당, 지역 검색...";
                    case CHINESE: return "搜索碎米饭、河粉、法棍、餐馆、地区...";
                    default: return "Find broken rice, pho, banh mi, restaurants, districts...";
                }
            case "food_category_all":
                switch (lang) {
                    case VIETNAMESE: return "Tất cả món";
                    case FRENCH: return "Tous les plats";
                    case JAPANESE: return "すべての料理";
                    case KOREAN: return "전체 요리";
                    case CHINESE: return "全部美食";
                    default: return "All Dishes";
                }
            case "food_category_rice_noodles":
                switch (lang) {
                    case VIETNAMESE: return "Cơm & Mì Phở";
                    case FRENCH: return "Riz & Nouilles";
                    case JAPANESE: return "ご飯・麺類";
                    case KOREAN: return "밥 & 면 요리";
                    case CHINESE: return "米饭与面食";
                    default: return "Rice & Noodles";
                }
            case "food_category_street_snacks":
                switch (lang) {
                    case VIETNAMESE: return "Ăn vặt & Đường phố";
                    case FRENCH: return "Street Food & Encas";
                    case JAPANESE: return "屋台軽食";
                    case KOREAN: return "길거리 간식";
                    case CHINESE: return "街头小吃";
                    default: return "Street Food & Snacks";
                }
            case "food_category_beverages_desserts":
                switch (lang) {
                    case VIETNAMESE: return "Cà phê & Đồ uống";
                    case FRENCH: return "Café & Boissons";
                    case JAPANESE: return "カフェ・デザート";
                    case KOREAN: return "커피 & 디저트";
                    case CHINESE: return "咖啡与饮品";
                    default: return "Coffee & Desserts";
                }
            case "food_category_seafood":
                switch (lang) {
                    case VIETNAMESE: return "Hải sản & Quán Ốc";
                    case FRENCH: return "Fruits de mer & Escargots";
                    case JAPANESE: return "海鮮・巻貝料理";
                    case KOREAN: return "해산물 & 소라 요리";
                    case CHINESE: return "海鲜与螺肉";
                    default: return "Seafood & Snails";
                }
            case "food_eateries_on_map":
                switch (lang) {
                    case VIETNAMESE: return "quán trứ danh trên bản đồ";
                    case FRENCH: return "restaurants sur la carte";
                    case JAPANESE: return "軒の名店（地図対応）";
                    case KOREAN: return "개 명소 지도 확인";
                    case CHINESE: return "家老字号名店";
                    default: return "Famous Eateries on Map";
                }
            case "food_view_eateries":
                switch (lang) {
                    case VIETNAMESE: return "Xem địa chỉ & quán →";
                    case FRENCH: return "Voir restaurants →";
                    case JAPANESE: return "店舗を見る →";
                    case KOREAN: return "맛집 보기 →";
                    case CHINESE: return "查看餐馆 →";
                    default: return "View eateries →";
                }
            case "food_no_results":
                switch (lang) {
                    case VIETNAMESE: return "Không tìm thấy món ăn nào phù hợp với";
                    case FRENCH: return "Aucune spécialité trouvée pour";
                    case JAPANESE: return "該当する料理が見つかりませんでした";
                    case KOREAN: return "검색 결과가 없습니다";
                    case CHINESE: return "未找到相关特色美食";
                    default: return "No culinary specialties found for";
                }
            case "food_ride_banner_title":
                switch (lang) {
                    case VIETNAMESE: return "Khám phá Phố Ẩm thực & Chợ đêm Sài Gòn 🚖";
                    case FRENCH: return "Rues Gourmandes & Marchés Nocturnes 🚖";
                    case JAPANESE: return "サイゴン グルメ通り＆夜市巡り 🚖";
                    case KOREAN: return "사이공 미식 거리 & 야시장 투어 🚖";
                    case CHINESE: return "探索西贡美食街与夜市 🚖";
                    default: return "Saigon Food Streets & Night Markets 🚖";
                }
            case "food_ride_banner_desc":
                switch (lang) {
                    case VIETNAMESE: return "Vĩnh Khánh Q4, Phan Xích Long, Chợ Lớn • Đặt xe Grab/Xanh SM/Be đến quán";
                    case FRENCH: return "Vinh Khanh D4, Phan Xich Long, Chinatown • Réserver un chauffeur";
                    case JAPANESE: return "ビンカイン4区、ファンシックロン、中華街 • タクシー・配車で直行";
                    case KOREAN: return "4군 빈카인, 판씩롱, 차이나타운 • 그랩/전기택시 바로 호출";
                    case CHINESE: return "4区永庆街、藩赤龙、唐人街 • 一键叫车直达";
                    default: return "Vinh Khanh D4, Phan Xich Long, Chinatown • Book ride to restaurants";
                }
            case "food_district_all":
                switch (lang) {
                    case VIETNAMESE: return "Tất cả quận";
                    case FRENCH: return "Tous districts";
                    case JAPANESE: return "全地区";
                    case KOREAN: return "모든 지역";
                    case CHINESE: return "所有地区";
                    default: return "All Districts";
                }
            default:
                return key;
        }
    }
}
