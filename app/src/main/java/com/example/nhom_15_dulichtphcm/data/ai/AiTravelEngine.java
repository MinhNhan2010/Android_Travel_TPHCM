package com.example.nhom_15_dulichtphcm.data.ai;

import com.example.nhom_15_dulichtphcm.data.model.AdministrativeDistrict;
import com.example.nhom_15_dulichtphcm.data.model.AiChatMessage;
import com.example.nhom_15_dulichtphcm.data.model.AiItineraryDay;
import com.example.nhom_15_dulichtphcm.data.model.Attraction;
import com.example.nhom_15_dulichtphcm.data.model.Phrase;
import com.example.nhom_15_dulichtphcm.data.model.PublicUtility;
import com.example.nhom_15_dulichtphcm.data.model.Restaurant;
import com.example.nhom_15_dulichtphcm.data.model.RideEstimate;
import com.example.nhom_15_dulichtphcm.data.model.RideProvider;
import com.example.nhom_15_dulichtphcm.data.model.SpecialtyFood;
import com.example.nhom_15_dulichtphcm.data.model.UtilityCategory;
import com.example.nhom_15_dulichtphcm.data.remote.TravelDataService;
import com.example.nhom_15_dulichtphcm.data.repository.AdministrativeDistrictRepository;
import com.example.nhom_15_dulichtphcm.data.repository.AttractionRepository;
import com.example.nhom_15_dulichtphcm.data.repository.FoodRepository;
import com.example.nhom_15_dulichtphcm.data.repository.MetroTransitData;
import com.example.nhom_15_dulichtphcm.data.repository.PhraseRepository;
import com.example.nhom_15_dulichtphcm.data.repository.RideRepository;
import com.example.nhom_15_dulichtphcm.data.repository.UtilityRepository;
import com.example.nhom_15_dulichtphcm.util.AppLanguage;
import com.example.nhom_15_dulichtphcm.util.SearchUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Comprehensive Smart Travel Concierge Engine backed by all local SQLite repositories
 * (Attractions, Specialty Foods & Restaurants, Public Utilities, Metro Line 1 & Waterbus,
 * Ride-Hailing Fares, Administrative Districts, Exchange Rates, and Survival Phrases),
 * plus RAG grounding context builder for Cloud Gemini.
 */
public final class AiTravelEngine {

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "la", "gi", "o", "dau", "tai", "cua", "va", "hay", "hoac", "cho", "toi", "minh",
            "ban", "co", "khong", "nhu", "the", "nao", "bao", "nhieu", "may", "gio", "di",
            "den", "tu", "ve", "voi", "nhung", "cac", "mot", "nhat", "tot", "dep", "ngon",
            "sai", "gon", "tp", "hcm", "ho", "chi", "thanh", "pho", "quan", "duong", "khu",
            "the", "is", "are", "in", "at", "to", "from", "for", "of", "and", "or", "how",
            "what", "where", "when", "much", "many", "best", "good", "can", "you", "me",
            "please", "tell", "about", "saigon", "city", "vietnam", "go", "visit", "get"
    ));

    private static final class NamedGeoPoint {
        final String nameVi;
        final String nameEn;
        final double lat;
        final double lng;
        final Attraction linkedAttraction;

        NamedGeoPoint(String nameVi, String nameEn, double lat, double lng, Attraction linkedAttraction) {
            this.nameVi = nameVi;
            this.nameEn = nameEn;
            this.lat = lat;
            this.lng = lng;
            this.linkedAttraction = linkedAttraction;
        }
    }

    private AiTravelEngine() {}

    public static AiChatMessage buildWelcomeMessage(AppLanguage lang) {
        boolean isVi = lang == AppLanguage.VIETNAMESE;
        String text = isVi
                ? "Xin chào! Tôi là **Trợ lý Du lịch AI — TP.HCM Mở rộng** ✨\n\n"
                        + "Bạn có thể hỏi tôi bất kỳ điều gì:\n"
                        + "• 🏛️ **Giá vé, giờ mở cửa, địa chỉ & lịch sử** từng điểm tham quan\n"
                        + "• 🍜 **Quán ăn ngon, ăn đêm, cà phê, ăn chay** kèm giá & địa chỉ\n"
                        + "• 🚕 **Khoảng cách & cước Xanh SM / Grab / Metro / Waterbus** giữa 2 nơi\n"
                        + "• 💰 **Dự toán chi phí**, thời tiết/trời mưa đi đâu, đổi tiền, khách sạn, quà lưu niệm & hỗ trợ khẩn cấp!"
                : "Welcome! I am your **Greater Saigon AI Travel Concierge** ✨\n\n"
                        + "Ask me anything about your trip:\n"
                        + "• 🏛️ **Ticket prices, opening hours, addresses & history** of any landmark\n"
                        + "• 🍜 **Local eateries, late-night food, cafes, vegetarian spots** & prices\n"
                        + "• 🚕 **Distances & Xanh SM / Grab / Metro / Waterbus fares** between places\n"
                        + "• 💰 **Trip budgets**, rainy-day plans, FX rates, hotels, souvenirs & emergency help!";

        List<String> chips = isVi
                ? Arrays.asList(
                        "Lịch trình 3 ngày TP.HCM - Bình Dương - Vũng Tàu",
                        "Giá vé & giờ mở cửa Dinh Độc Lập?",
                        "Từ sân bay Tân Sơn Nhất về Chợ Bến Thành đi sao?",
                        "Quán ăn đêm & ốc ngon ở Sài Gòn?",
                        "Ước tính chi phí du lịch Sài Gòn 3 ngày"
                )
                : Arrays.asList(
                        "3-Day HCMC + Binh Duong + Vung Tau Itinerary",
                        "Independence Palace ticket price & hours?",
                        "How to get from Tan Son Nhat Airport to Ben Thanh?",
                        "Best late-night street food & snails in Saigon",
                        "Estimated budget for 3 days in Saigon"
                );

        return new AiChatMessage(
                UUID.randomUUID().toString(),
                false,
                text,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                chips,
                false
        );
    }

    public static AiChatMessage generateOfflineReply(String rawQuery, AppLanguage lang) {
        return generateOfflineReply(rawQuery, Collections.emptyList(), lang);
    }

    public static AiChatMessage generateOfflineReply(
            String rawQuery,
            List<AiChatMessage> history,
            AppLanguage lang
    ) {
        String safeRaw = rawQuery != null ? rawQuery.trim() : "";
        String q = SearchUtils.normalize(safeRaw);
        boolean isVi = lang == AppLanguage.VIETNAMESE;

        List<Attraction> allAttractions = AttractionRepository.getAttractions();
        List<SpecialtyFood> allFoods = FoodRepository.getSpecialtyFoods();
        List<PublicUtility> allUtilities = UtilityRepository.getUtilities();

        // Greeting / Capabilities check
        if (q.length() <= 18 && containsAny(q, "xin chao", "hello", "hi", "chao ban", "help", "ban lam duoc gi", "giup gi")) {
            return buildWelcomeMessage(lang);
        }

        // Match specific entities from query (or pronoun follow-up from conversation history)
        List<Attraction> matchedAttractions = findMatchingAttractions(q, allAttractions, 4);
        List<SpecialtyFood> matchedFoods = findMatchingFoods(q, allFoods, 4);
        List<PublicUtility> matchedUtilities = findMatchingUtilities(q, allUtilities, 3);

        if (matchedAttractions.isEmpty() && matchedFoods.isEmpty() && isPronounFollowUp(q)) {
            Attraction prevAttr = findLastRecommendedAttraction(history);
            SpecialtyFood prevFood = findLastRecommendedFood(history);
            if (prevAttr != null && !containsAny(q, "mon ", "quan an", "an gi", "dish")) {
                matchedAttractions = Collections.singletonList(prevAttr);
            } else if (prevFood != null) {
                matchedFoods = Collections.singletonList(prevFood);
            }
        }

        // 1. Point-to-point Route, Distance & Ride Fare Calculation (e.g., "Từ X đến Y bao xa / đi xe gì")
        List<NamedGeoPoint> routeEndpoints = extractTwoRoutePoints(q, allAttractions, allUtilities);
        if (routeEndpoints.size() >= 2 && (containsAny(q,
                "tu ", "from ", "den ", "toi ", "ve ", "bao xa", "khoang cach", "distance",
                "di sao", "di nhu the nao", "di bang gi", "cuoc", "fare", "grab", "taxi", "xanh sm", "metro"))) {
            return buildPointToPointRouteReply(routeEndpoints.get(0), routeEndpoints.get(1), isVi);
        }

        // 2. Trip Budget & Cost Estimation (checked before "3 ngày" itinerary trigger unless "lịch trình" is explicit)
        if (!containsAny(q, "lich trinh", "itinerary")
                && containsAny(q, "chi phi", "ngan sach", "budget", "ton bao nhieu", "het bao nhieu tien",
                "du lich tiet kiem", "diem mien phi", "free spot")) {
            return buildBudgetAndFreeSpotsReply(q, isVi, allAttractions, allFoods);
        }

        // 3. Multi-day / Regional Itinerary Request
        if (containsAny(q, "lich trinh", "itinerary", "ke hoach", "tour ", "1 ngay", "2 ngay", "3 ngay", "4 ngay",
                "2n1d", "3n2d", "1-day", "2-day", "3-day", "one day", "two day", "three day")
                || (containsAny(q, "plan") && !containsAny(q, "airplane", "planet"))) {
            return buildItineraryResponse(q, isVi, allAttractions, allFoods);
        }

        // 4. Hotels / Where to Stay & Souvenirs / Gifts (even if a district/market name is mentioned)
        if (containsAny(q, "khach san", "nha nghi", "homestay", "resort", "luu tru", "o khu nao", "ngu o dau",
                "hotel", "accommodation", "hostel", "where to stay")) {
            return buildHotelAccommodationReply(isVi, allAttractions);
        }
        if (containsAny(q, "mua qua", "qua luu niem", "mang ve", "souvenir", "gift", "marou")) {
            return buildShoppingAndSouvenirReply(isVi, allAttractions);
        }

        // 5. Specific Specialty Food or Restaurant Question (when a concrete dish/restaurant is matched!)
        if (!matchedFoods.isEmpty() && (matchedAttractions.isEmpty() || containsAny(q,
                "an ", "mon ", "quan ", "food", "eat", "dish", "how to eat", "cach an"))) {
            return buildSpecificFoodReply(matchedFoods, q, isVi);
        }

        // 6. Specific Landmark / Attraction Deep Q&A (ticket price, opening hours, address, history, tips)
        if (!matchedAttractions.isEmpty() && !containsAny(q, "thoi tiet", "troi mua", "doi tien")) {
            return buildSpecificAttractionReply(matchedAttractions, q, isVi, allFoods);
        }

        // 5. Emergency, Hospitals, Embassies, SIM/4G, ATMs, Public Restrooms & Tourist Safety
        if (!matchedUtilities.isEmpty() || containsAny(q,
                "khan cap", "cap cuu", "cong an", "canh sat", "benh vien", "y te", "thuoc",
                "lanh su", "dai su quan", "mat ho chieu", "passport", "hospital", "emergency",
                "police", "embassy", "consulate", "sim", "esim", "4g", "wifi", "atm", "rut tien",
                "nha ve sinh", "wc", "restroom", "toilet",
                "lua dao", "moc tui", "scam", "an toan", "safety")) {
            return buildEmergencyAndUtilityReply(q, matchedUtilities, allUtilities, isVi);
        }

        // 6. Currency Exchange, VND Notes, Tipping & Card Payments
        if (containsAny(q, "doi tien", "ty gia", "exchange", "currency", "usd", "eur", "jpy", "krw",
                "ngoai te", "the tin dung", "credit card", "apple pay", "tien boa", "tipping")) {
            return buildCurrencyAndPaymentReply(isVi, allAttractions);
        }

        // 7. Trip Budget & Cost Estimation ("Ước tính chi phí chuyến đi", "ngân sách", "miễn phí")
        if (containsAny(q, "chi phi", "ngan sach", "budget", "cost", "ton bao nhieu", "het bao nhieu tien",
                "du lich tiet kiem", "gia re", "cheap", "free", "mien phi")) {
            return buildBudgetAndFreeSpotsReply(q, isVi, allAttractions, allFoods);
        }

        // 8. Weather, Best Time to Visit & Rainy-Day Indoor Activities
        if (containsAny(q, "thoi tiet", "troi mua", "mua mua", "mua kho", "nang nong", "nhiet do",
                "mua nao dep", "thang may", "weather", "rain", "rainy", "indoor", "climate", "best time")) {
            return buildWeatherAndRainPlanReply(q, isVi, allAttractions);
        }

        // 9. Specialized Food Queries: Late-Night, Vegetarian/Halal, Cafes, Seafood
        if (containsAny(q, "an chay", "chay", "vegetarian", "vegan", "halal")) {
            return buildVegetarianAndHalalReply(isVi, allFoods);
        }
        if (containsAny(q, "an dem", "choi dem", "buoi toi", "ban dem", "nightlife", "late night",
                "bui vien", "rooftop", "bar", "chill", "bia")) {
            return buildNightlifeAndLateFoodReply(isVi, allAttractions, allFoods);
        }
        if (containsAny(q, "ca phe", "cafe", "coffee", "tra sua", "chung cu 42", "quan nuoc", "check in")) {
            return buildCoffeeCultureReply(isVi, allAttractions, allFoods);
        }
        if (containsAny(q, "an gi", "mon ngon", "am thuc", "dac san", "quan ngon", "an sang", "an trua", "an toi",
                "hai san", "oc ", "quan oc", "food", "eat", "street food", "restaurant", "seafood", "breakfast", "dinner")) {
            return buildGeneralFoodShowcaseReply(q, isVi, allFoods);
        }

        // 10. Metro Line 1, Waterbus, Airport Transfer & Ride-Hailing Guide
        if (containsAny(q, "metro", "tau dien", "waterbus", "buyt song", "xe buyt", "taxi", "grab",
                "xanh sm", "be ", "san bay", "tan son nhat", "airport", "di chuyen", "phuong tien", "transport")) {
            return buildTransportAndAirportReply(q, isVi, allAttractions);
        }

        // 11. Hotels / Where to Stay
        if (containsAny(q, "khach san", "nha nghi", "homestay", "resort", "luu tru", "o khu nao", "ngu o dau",
                "hotel", "stay", "accommodation", "hostel", "where to stay")) {
            return buildHotelAccommodationReply(isVi, allAttractions);
        }

        // 12. Shopping, Markets & Souvenirs ("mua quà gì", "đặc sản mang về", "trung tâm thương mại")
        if (containsAny(q, "mua qua", "qua luu niem", "mang ve", "mua sam", "cho ", "mall", "trung tam thuong mai",
                "souvenir", "shopping", "gift", "ao dai", "marou")) {
            return buildShoppingAndSouvenirReply(isVi, allAttractions);
        }

        // 13. Family with Kids, Couples, Festivals & Events
        if (containsAny(q, "tre em", "gia dinh", "con nho", "cap doi", "hen ho", "lang man", "le hoi", "su kien",
                "tet", "family", "kids", "children", "couple", "romantic", "festival", "show", "a o show")) {
            return buildFamilyCoupleFestivalReply(q, isVi, allAttractions);
        }

        // 14. Vietnamese Survival Phrases & Bargaining
        if (containsAny(q, "tieng viet", "cau giao tiep", "noi nhu the nao", "phat am", "mac ca", "tra gia",
                "phrase", "speak vietnamese", "how to say", "bargain")) {
            return buildVietnamesePhrasesReply(q, isVi);
        }

        // 15. Administrative Districts & Post-Merger Greater HCMC Info
        if (containsAny(q, "sap nhap", "hanh chinh", "dia gioi", "thu duc", "cho lon", "dien tich", "dan so",
                "district", "merger", "administrative", "ward")) {
            return buildAdministrativeDistrictReply(q, isVi, allAttractions);
        }

        // 16. Regional Corridors: Vung Tau & Binh Duong
        if (containsAny(q, "vung tau", "con dao", "ho tram", "bien", "beach", "coastal", "tau cao toc", "tau canh ngam")) {
            return buildVungTauReply(isVi, allAttractions, allFoods);
        }
        if (containsAny(q, "binh duong", "thu dau mot", "thuan an", "di an", "lai thieu", "gom", "ceramic", "son mai")) {
            return buildBinhDuongReply(isVi, allAttractions, allFoods);
        }

        // Smart Helpful Fallback: provide direct guidance + top curated highlights instead of vague filler
        List<Attraction> topPicks = allAttractions.subList(0, Math.min(4, allAttractions.size()));
        String fallback = isVi
                ? "Về câu hỏi **\"" + safeRaw + "\"**, dưới đây là gợi ý thực tế dành cho hành trình tại **TP.HCM mở rộng**:\n\n"
                        + "• **Điểm tham quan nổi bật nhất:** Dinh Độc Lập, Nhà thờ Đức Bà, Bưu điện Trung tâm, Bảo tàng Chứng tích Chiến tranh, Chợ Bến Thành, Landmark 81 & Công viên Bờ sông Thủ Thiêm.\n"
                        + "• **Trải nghiệm nên thử:** Đi tàu **Metro số 1** (6.000đ–20.000đ), đón hoàng hôn **Saigon Waterbus** (15.000đ) và thưởng thức Cơm tấm, Bánh mì, Phở, Ốc Sài Gòn.\n"
                        + "• 💡 *Mẹo hỏi nhanh:* Bạn có thể hỏi cụ thể tên địa điểm (VD: *\"Giá vé Suối Tiên\"*), tên món (*\"Quán ốc ngon\"*), lộ trình (*\"Từ sân bay về Quận 1\"*), *\"Khách sạn Quận 1\"* hoặc *\"Chi phí 3 ngày\"*!"
                : "Regarding **\"" + safeRaw + "\"**, here are practical recommendations for **Greater Ho Chi Minh City**:\n\n"
                        + "• **Top Landmarks:** Independence Palace, Notre-Dame Cathedral, Central Post Office, War Remnants Museum, Ben Thanh Market, Landmark 81 & Thu Thiem Riverfront.\n"
                        + "• **Signature Experiences:** Ride **Metro Line 1** (6k–20k VND), catch sunset on **Saigon Waterbus** (15k VND), and taste Cơm Tấm, Bánh Mì, Phở & Street Snails.\n"
                        + "• 💡 *Pro tip:* Ask me about any specific landmark (*\"Cu Chi Tunnels ticket\"*), route (*\"Airport to Ben Thanh fare\"*), *\"Where to stay\"*, or *\"3-day budget\"*!";

        return new AiChatMessage(
                UUID.randomUUID().toString(),
                false,
                fallback,
                topPicks,
                allFoods.subList(0, Math.min(2, allFoods.size())),
                Collections.emptyList(),
                isVi
                        ? Arrays.asList("Lịch trình 3 ngày TP.HCM - Bình Dương - Vũng Tàu", "Ước tính chi phí chuyến đi", "Khách sạn nên ở khu nào?", "Trời mưa ở Sài Gòn đi đâu?")
                        : Arrays.asList("3-Day Greater Saigon Itinerary", "Estimated trip budget", "Best areas to stay in Saigon", "Rainy-day indoor activities"),
                false
        );
    }

    // =========================================================================
    // GROUNDED RAG CONTEXT BUILDER FOR GEMINI CLOUD
    // =========================================================================

    public static String buildGroundedContextForPrompt(String rawQuery, AppLanguage lang) {
        String q = SearchUtils.normalize(rawQuery != null ? rawQuery : "");
        List<Attraction> allAttractions = AttractionRepository.getAttractions();
        List<SpecialtyFood> allFoods = FoodRepository.getSpecialtyFoods();
        List<PublicUtility> allUtilities = UtilityRepository.getUtilities();

        StringBuilder sb = new StringBuilder();
        sb.append("You are the official AI Travel Concierge inside the 'Khám phá Sài Gòn / Explore Greater Saigon' Android app ");
        sb.append("(covering Ho Chi Minh City, Thu Duc City, Binh Duong, and Ba Ria - Vung Tau).\n");
        sb.append("RULES:\n");
        sb.append("1. Answer EVERY question the user asks directly, accurately, warmly, and thoroughly in ").append(lang.displayName).append(".\n");
        sb.append("2. Never refuse reasonable travel, lifestyle, hotel, shopping, culture, history, food, transport, budget, or general questions. ");
        sb.append("If the user asks something outside the app's preloaded database (e.g., specific hotels, cafés, bars, shopping malls, visa rules, ");
        sb.append("souvenirs, packing tips, or general knowledge), use your broad knowledge to give helpful, concrete recommendations!\n");
        sb.append("3. Format clearly with concise **bold** headings and bullet points (•). Include practical VND prices, addresses, and hours whenever relevant.\n");
        sb.append("4. Ground facts in the verified local SQLite app data below when applicable:\n");
        sb.append("- Metro Line 1 (Bến Thành – Suối Tiên, 14 stations): 7,000–20,000 VND cash (6,000–19,000 VND contactless Visa/Mastercard), 05:00–22:00.\n");
        sb.append("- Saigon Waterbus (Bạch Đằng – Linh Đông, 5 piers): 15,000 VND/way. Greenlines DP Fast Ferry Bạch Đằng – Vũng Tàu: 320,000–350,000 VND (~2 hours).\n");
        sb.append("- Ride-hailing: Xanh SM (1900 2088), Grab, Be, Mai Linh Taxi (1055), Vinasun ((028) 38 27 27 27). Emergency: Police 113, Fire 114, Ambulance 115.\n");

        List<Attraction> topAttr = findMatchingAttractions(q, allAttractions, 5);
        if (topAttr.isEmpty()) {
            topAttr = allAttractions.subList(0, Math.min(6, allAttractions.size()));
        }
        sb.append("\n[RELEVANT APP LANDMARKS]\n");
        for (Attraction a : topAttr) {
            sb.append("- ").append(a.nameVi).append(" (").append(a.nameEn).append(") | ")
              .append(a.address).append(" | Hours: ").append(a.openingHours)
              .append(" | Ticket: ").append(a.ticketPrice)
              .append(" | Tip: ").append(a.foreignerTips).append("\n");
        }

        List<SpecialtyFood> topFoods = findMatchingFoods(q, allFoods, 4);
        if (topFoods.isEmpty()) {
            topFoods = allFoods.subList(0, Math.min(4, allFoods.size()));
        }
        sb.append("\n[RELEVANT APP SPECIALTY FOODS & EATERIES]\n");
        for (SpecialtyFood f : topFoods) {
            sb.append("- ").append(f.nameVi).append(" (").append(f.nameEn).append("): ");
            for (int i = 0; i < Math.min(2, f.restaurants.size()); i++) {
                Restaurant r = f.restaurants.get(i);
                if (i > 0) sb.append("; ");
                sb.append(r.name).append(" (").append(r.address).append(", ").append(r.priceRange).append(", ").append(r.openingHours).append(")");
            }
            sb.append("\n");
        }

        List<PublicUtility> topUtils = findMatchingUtilities(q, allUtilities, 3);
        if (!topUtils.isEmpty()) {
            sb.append("\n[RELEVANT PUBLIC UTILITIES]\n");
            for (PublicUtility u : topUtils) {
                sb.append("- ").append(u.nameVi).append(" | ").append(u.address)
                  .append(" | Hours: ").append(u.operatingHours)
                  .append(u.phone != null ? " | Tel: " + u.phone : "").append("\n");
            }
        }

        List<NamedGeoPoint> pts = extractTwoRoutePoints(q, allAttractions, allUtilities);
        if (pts.size() >= 2) {
            NamedGeoPoint p1 = pts.get(0);
            NamedGeoPoint p2 = pts.get(1);
            double km = RideRepository.calculateDistanceKm(p1.lat, p1.lng, p2.lat, p2.lng);
            RideEstimate grabEst = RideRepository.estimateFare(RideRepository.providers.get(1), km);
            sb.append(String.format(Locale.US,
                    "\n[COMPUTED ROUTE] %s -> %s: ~%.1f km (~%d mins). GrabBike ~%,d VND, 4-seat Car ~%,d VND, 7-seat SUV ~%,d VND.\n",
                    p1.nameVi, p2.nameVi, km, grabEst.durationMinutes, grabEst.bikeFareVnd, grabEst.car4FareVnd, grabEst.car7FareVnd));
        }

        return sb.toString();
    }

    // =========================================================================
    // SPECIALIZED INTENT BUILDERS
    // =========================================================================

    private static AiChatMessage buildSpecificAttractionReply(
            List<Attraction> matched,
            String q,
            boolean isVi,
            List<SpecialtyFood> allFoods
    ) {
        Attraction primary = matched.get(0);
        boolean askTicket = containsAny(q, "gia ve", "ve vao", "bao nhieu tien", "mien phi", "ticket", "price", "fee", "cost", "admission");
        boolean askHours = containsAny(q, "may gio", "gio mo cua", "dong cua", "open", "hour", "close", "when");
        boolean askAddress = containsAny(q, "o dau", "dia chi", "quan may", "duong nao", "where", "address", "location");

        StringBuilder sb = new StringBuilder();
        sb.append("🏛️ **").append(isVi ? primary.nameVi : primary.nameEn).append("**");
        if (isVi && !primary.nameEn.isEmpty()) {
            sb.append(" _(").append(primary.nameEn).append(")_");
        }
        sb.append("\n\n");

        if (askTicket) {
            sb.append("🎟️ **").append(isVi ? "Giá vé tham quan:" : "Ticket Price:").append("** ").append(primary.ticketPrice).append("\n");
            sb.append("🕒 **").append(isVi ? "Giờ mở cửa:" : "Opening Hours:").append("** ").append(primary.openingHours).append("\n");
            sb.append("📍 **").append(isVi ? "Địa chỉ:" : "Address:").append("** ").append(primary.address).append("\n\n");
        } else if (askHours) {
            sb.append("🕒 **").append(isVi ? "Giờ mở cửa:" : "Opening Hours:").append("** ").append(primary.openingHours).append("\n");
            sb.append("🎟️ **").append(isVi ? "Giá vé:" : "Ticket Price:").append("** ").append(primary.ticketPrice).append("\n");
            sb.append("📍 **").append(isVi ? "Địa chỉ:" : "Address:").append("** ").append(primary.address).append("\n\n");
        } else if (askAddress) {
            sb.append("📍 **").append(isVi ? "Địa chỉ chính xác:" : "Exact Address:").append("** ").append(primary.address)
              .append(" (").append(primary.province).append(")\n");
            sb.append("🕒 **").append(isVi ? "Giờ mở cửa:" : "Opening Hours:").append("** ").append(primary.openingHours).append("\n");
            sb.append("🎟️ **").append(isVi ? "Giá vé:" : "Ticket Price:").append("** ").append(primary.ticketPrice).append("\n\n");
        } else {
            sb.append("📍 **").append(isVi ? "Địa chỉ:" : "Address:").append("** ").append(primary.address).append("\n");
            sb.append("🕒 **").append(isVi ? "Giờ mở cửa:" : "Hours:").append("** ").append(primary.openingHours).append("\n");
            sb.append("🎟️ **").append(isVi ? "Giá vé:" : "Admission:").append("** ").append(primary.ticketPrice).append("\n\n");
        }

        sb.append("✨ **").append(isVi ? "Điểm nhấn & Lịch sử:" : "Highlights & Story:").append("** ")
          .append(primary.shortSummary).append(" ").append(primary.history).append("\n\n");
        sb.append("💡 **").append(isVi ? "Mẹo thực tế:" : "Local Tip:").append("** ").append(primary.foreignerTips);

        if (matched.size() > 1) {
            sb.append("\n\n📌 **").append(isVi ? "Điểm liên quan:" : "Also matched:").append("** ");
            for (int i = 1; i < matched.size(); i++) {
                Attraction other = matched.get(i);
                if (i > 1) sb.append(" • ");
                sb.append(isVi ? other.nameVi : other.nameEn).append(" (").append(other.ticketPrice).append(")");
            }
        }

        List<String> followUps = isVi
                ? Arrays.asList(
                        "Từ Chợ Bến Thành đến " + primary.nameVi + " bao xa?",
                        "Ăn gì ngon ở Sài Gòn?",
                        "Lịch trình 1 ngày Quận 1")
                : Arrays.asList(
                        "Distance from Ben Thanh Market to " + primary.nameEn,
                        "Best nearby local food",
                        "1-Day Saigon Itinerary");

        return new AiChatMessage(
                UUID.randomUUID().toString(),
                false,
                sb.toString(),
                matched,
                Collections.emptyList(),
                Collections.emptyList(),
                followUps,
                false
        );
    }

    private static AiChatMessage buildSpecificFoodReply(List<SpecialtyFood> matched, String q, boolean isVi) {
        SpecialtyFood food = matched.get(0);
        StringBuilder sb = new StringBuilder();
        sb.append("🍜 **").append(isVi ? food.nameVi : food.nameEn).append("**\n\n");
        sb.append(food.shortDescription).append("\n\n");
        sb.append("🥢 **").append(isVi ? "Cách thưởng thức chuẩn người bản địa:" : "How to Eat Like a Local:").append("** ")
          .append(food.howToEatLikeALocal).append("\n\n");

        if (!food.restaurants.isEmpty()) {
            sb.append("🏪 **").append(isVi ? "Quán ngon trứ danh kèm giá & địa chỉ:" : "Top Verified Eateries & Prices:").append("**\n");
            for (Restaurant r : food.restaurants) {
                sb.append("• **").append(r.name).append("** (⭐ ").append(r.rating).append(")\n")
                  .append("  📍 ").append(r.address).append("\n")
                  .append("  🕒 ").append(r.openingHours).append("  •  💰 ").append(r.priceRange).append("\n")
                  .append("  🏆 _").append(r.highlight).append("_\n");
            }
        }

        return new AiChatMessage(
                UUID.randomUUID().toString(),
                false,
                sb.toString().trim(),
                Collections.emptyList(),
                matched,
                Collections.emptyList(),
                isVi
                        ? Arrays.asList("Quán ăn đêm ở Sài Gòn", "Cà phê vợt & quán cà phê đẹp", "Quán ăn chay ngon ở Sài Gòn")
                        : Arrays.asList("Best late-night food in Saigon", "Iconic Saigon coffee spots", "Best vegetarian restaurants"),
                false
        );
    }

    private static AiChatMessage buildPointToPointRouteReply(NamedGeoPoint from, NamedGeoPoint to, boolean isVi) {
        double km = RideRepository.calculateDistanceKm(from.lat, from.lng, to.lat, to.lng);
        RideProvider xanhSm = RideRepository.providers.get(0);
        RideProvider grab = RideRepository.providers.get(1);
        RideEstimate estGrab = RideRepository.estimateFare(grab, km);
        RideEstimate estXanh = RideRepository.estimateFare(xanhSm, km);

        StringBuilder sb = new StringBuilder();
        if (isVi) {
            sb.append("🧭 **Lộ trình & Giá cước: ").append(from.nameVi).append(" → ").append(to.nameVi).append("**\n\n");
            sb.append(String.format(Locale.US, "• **Khoảng cách đường bộ ước tính:** ~%.1f km _(khoảng %d – %d phút di chuyển)_\n",
                    km, estGrab.durationMinutes, estGrab.durationMinutes + 8));
            sb.append(String.format(Locale.US, "• **Xe máy công nghệ (GrabBike / Xanh SM Bike):** %,dđ – %,dđ\n",
                    estGrab.bikeFareVnd, estXanh.bikeFareVnd));
            sb.append(String.format(Locale.US, "• **Ô tô 4 chỗ (GrabCar / Xanh SM Taxi):** %,dđ – %,dđ\n",
                    estGrab.car4FareVnd, estXanh.car4FareVnd));
            sb.append(String.format(Locale.US, "• **Ô tô 7 chỗ gia đình:** ~%,dđ\n", estGrab.car7FareVnd));

            if (km > 55.0 && (SearchUtils.fuzzyContains(to.nameVi, "Vung Tau") || SearchUtils.fuzzyContains(from.nameVi, "Vung Tau"))) {
                sb.append("• ⛴️ **Gợi ý tối ưu đi Vũng Tàu:** Đi **Tàu cao tốc Greenlines DP** từ Bến Bạch Đằng (320.000đ – 350.000đ/vé, ~2 giờ không kẹt xe) hoặc Limousine cao tốc Long Thành (~180.000đ).\n");
            } else if (SearchUtils.fuzzyContains(from.nameVi + " " + to.nameVi, "San bay")) {
                sb.append("• ✈️ **Mẹo đón xe tại Sân bay Tân Sơn Nhất:** Đi ra làn xếp hàng chính thức (nhà xe TCP Ga Quốc nội hoặc Cột B/C/D Ga Quốc tế), hoặc bắt **Xe buýt 109 / 152** về thẳng Chợ Bến Thành (5.000đ – 20.000đ)!\n");
            } else if (km <= 18.0) {
                sb.append("• 🚇 **Phương tiện công cộng:** Nếu đi dọc trục Quận 1 – Bình Thạnh (Landmark 81) – Thảo Điền – Suối Tiên, đi **Metro số 1** chỉ **6.000đ – 19.000đ** (chạm thẻ Visa/Mastercard)!\n");
            }
        } else {
            sb.append("🧭 **Route & Fare Estimate: ").append(from.nameEn).append(" → ").append(to.nameEn).append("**\n\n");
            sb.append(String.format(Locale.US, "• **Estimated Urban Road Distance:** ~%.1f km _(~%d–%d mins)_\n",
                    km, estGrab.durationMinutes, estGrab.durationMinutes + 8));
            sb.append(String.format(Locale.US, "• **Motorbike (GrabBike / Xanh SM Bike):** %,d – %,d VND\n",
                    estGrab.bikeFareVnd, estXanh.bikeFareVnd));
            sb.append(String.format(Locale.US, "• **4-Seat Car (GrabCar / Xanh SM EV):** %,d – %,d VND\n",
                    estGrab.car4FareVnd, estXanh.car4FareVnd));
            sb.append(String.format(Locale.US, "• **7-Seat SUV (with luggage):** ~%,d VND\n", estGrab.car7FareVnd));
            sb.append("• 💡 **Safety tip:** Always verify the license plate on your app before boarding; dial **1055** for official Mai Linh metered taxi.");
        }

        List<Attraction> cards = new ArrayList<>();
        if (to.linkedAttraction != null) cards.add(to.linkedAttraction);
        if (from.linkedAttraction != null && !cards.contains(from.linkedAttraction)) cards.add(from.linkedAttraction);

        return new AiChatMessage(
                UUID.randomUUID().toString(),
                false,
                sb.toString().trim(),
                cards,
                Collections.emptyList(),
                Collections.emptyList(),
                isVi
                        ? Arrays.asList("Cách đi Metro số 1 & Waterbus", "Mẹo an toàn & tránh chặt chém", "Ước tính chi phí chuyến đi")
                        : Arrays.asList("How to ride Metro Line 1 & Waterbus", "Airport pickup & safety tips", "Estimated daily budget"),
                false
        );
    }

    private static AiChatMessage buildBudgetAndFreeSpotsReply(
            String q,
            boolean isVi,
            List<Attraction> allAttractions,
            List<SpecialtyFood> allFoods
    ) {
        int days = q.contains("1") ? 1 : (q.contains("2") ? 2 : 3);
        List<Attraction> freeSpots = new ArrayList<>();
        for (Attraction a : allAttractions) {
            if (SearchUtils.fuzzyContains(a.ticketPrice, "free") || SearchUtils.fuzzyContains(a.ticketPrice, "mien phi")) {
                freeSpots.add(a);
                if (freeSpots.size() >= 4) break;
            }
        }

        String text = isVi
                ? "💰 **Dự toán Chi phí Du lịch TP.HCM (" + days + " ngày / 1 người):**\n\n"
                        + "**1. Mức Tiết kiệm / Tự túc (~600.000đ – 850.000đ/ngày):**\n"
                        + "• Lưu trú Homestay / Hostel Quận 1: 250.000đ – 400.000đ/đêm\n"
                        + "• Ăn uống đặc sản đường phố (Bánh mì, Cơm tấm, Phở, Cà phê): 200.000đ – 300.000đ/ngày\n"
                        + "• Di chuyển (Metro số 1 + Waterbus + GrabBike): 80.000đ – 120.000đ/ngày\n\n"
                        + "**2. Mức Thoải mái / Phổ thông (~1.400.000đ – 2.200.000đ/ngày):**\n"
                        + "• Khách sạn 3–4 sao trung tâm Quận 1 / Quận 3: 800.000đ – 1.300.000đ/đêm\n"
                        + "• Ẩm thực nhà hàng bản địa & quán ốc tối: 400.000đ – 600.000đ/ngày\n"
                        + "• Vé tham quan (Dinh Độc Lập 40k, Bảo tàng 40k, Địa đạo Củ Chi 125k) + Xanh SM Taxi: ~250.000đ/ngày\n\n"
                        + "**3. Top điểm tham quan MIỄN PHÍ vé vào cổng:**\n"
                        + "• Nhà thờ Đức Bà, Bưu điện Trung tâm, Phố đi bộ Nguyễn Huệ, Công viên Bờ sông Thủ Thiêm, Chùa Ngọc Hoàng, Chùa Bửu Long, Chùa Bà Bình Dương, Tượng Chúa Kitô Vũng Tàu!"
                : "💰 **Estimated Saigon Trip Budget (" + days + " Days / Per Person):**\n\n"
                        + "**1. Budget / Backpacker (~600k – 850k VND | $24–$34/day):**\n"
                        + "• Hostel / Boutique Homestay in District 1: 250k–400k VND/night\n"
                        + "• Street food feasts (Bánh Mì, Cơm Tấm, Phở, Iced Coffee): 200k–300k VND/day\n"
                        + "• Metro Line 1 + Waterbus + GrabBike: 80k–120k VND/day\n\n"
                        + "**2. Mid-Range Comfort (~1.4M – 2.2M VND | $55–$88/day):**\n"
                        + "• 3–4★ Central Hotel (District 1 / 3): 800k–1.3M VND/night\n"
                        + "• Local restaurants, rooftop cafe & seafood snails: 400k–600k VND/day\n"
                        + "• Sightseeing tickets (40k–125k VND) + Xanh SM EV Taxi: ~250k VND/day\n\n"
                        + "**3. Iconic FREE Landmarks:**\n"
                        + "• Notre-Dame Cathedral, Central Post Office, Nguyen Hue Promenade, Thu Thiem Riverfront Park, Jade Emperor Pagoda, Buu Long Pagoda & Christ of Vung Tau!";

        return new AiChatMessage(
                UUID.randomUUID().toString(),
                false,
                text,
                freeSpots,
                allFoods.subList(0, Math.min(2, allFoods.size())),
                Collections.emptyList(),
                isVi
                        ? Arrays.asList("Khách sạn nên ở khu nào?", "Đổi tiền USD & thẻ thanh toán", "Lịch trình 3 ngày tiết kiệm")
                        : Arrays.asList("Best areas to stay in Saigon", "Currency exchange & ATM tips", "3-Day Greater Saigon Itinerary"),
                false
        );
    }

    private static AiChatMessage buildWeatherAndRainPlanReply(String q, boolean isVi, List<Attraction> allAttractions) {
        List<Attraction> indoorPicks = new ArrayList<>();
        for (Attraction a : allAttractions) {
            if (Arrays.asList("independence_palace", "war_remnants_museum", "central_post_office", "landmark_81").contains(a.id)) {
                indoorPicks.add(a);
            }
        }
        String text = isVi
                ? "🌦️ **Thời tiết Sài Gòn & Kế hoạch đi chơi ngày mưa / nắng:**\n\n"
                        + "• **Mùa khô (Tháng 12 – Tháng 4):** Trời nắng vàng trong xanh (25°C – 34°C), lý tưởng nhất để đi dạo Quận 1, đi tàu Waterbus hoàng hôn và ra biển Vũng Tàu.\n"
                        + "• **Mùa mưa (Tháng 5 – Tháng 11):** Đặc trưng Sài Gòn là **mưa rào nhiệt đới bất chợt lúc 15:00 – 17:30** rồi tạnh nhanh, buổi sáng vẫn nắng đẹp.\n\n"
                        + "☔ **Top điểm đi chơi TRONG NHÀ cực chất khi trời mưa:**\n"
                        + "• **Dinh Độc Lập & Hầm chỉ huy ngầm** (40.000đ) — Tham quan toàn bộ bên trong dinh thự mát mẻ.\n"
                        + "• **Bảo tàng Chứng tích Chiến tranh** (40.000đ) & **Bưu điện Trung tâm Sài Gòn**.\n"
                        + "• **Tổ hợp Vincom Center Landmark 81 / Saigon Centre Takashimaya** (đi thẳng bằng tàu ngầm Metro số 1 không lo ướt!).\n"
                        + "• Xem **À Ố Show (Xiếc tre Việt Nam)** tại Nhà hát Thành phố hoặc ngồi nhâm nhi cà phê vợt Cheo Leo nghe mưa."
                : "🌦️ **Saigon Weather Guide & Rainy-Day Indoor Plans:**\n\n"
                        + "• **Dry Season (Dec – Apr):** Sunny skies (25°C–34°C), best for walking tours, sunset Waterbus, and Vũng Tàu beaches.\n"
                        + "• **Green/Rainy Season (May – Nov):** Warm mornings followed by dramatic **30–60 min afternoon showers (3:00–5:30 PM)** that cool the city down.\n\n"
                        + "☔ **Best Indoor Activities When It Rains:**\n"
                        + "• **Independence Palace & Underground War Bunker** (40,000 VND).\n"
                        + "• **War Remnants Museum** (40,000 VND) & **Central Post Office**.\n"
                        + "• **Landmark 81 Mall & SkyView** or **Takashimaya** (ride underground Metro Line 1 completely dry!).\n"
                        + "• Watch the **À Ố Bamboo Circus Show** at Saigon Opera House or sip hot phin coffee in Chung Cư 42 Nguyễn Huệ.";

        return new AiChatMessage(
                UUID.randomUUID().toString(),
                false,
                text,
                indoorPicks,
                Collections.emptyList(),
                Collections.emptyList(),
                isVi
                        ? Arrays.asList("Giá vé Dinh Độc Lập", "Quán cà phê đẹp ở Quận 1", "Cách đi Metro số 1")
                        : Arrays.asList("Independence Palace ticket info", "Best cafes in District 1", "How to ride Metro Line 1"),
                false
        );
    }

    private static AiChatMessage buildCurrencyAndPaymentReply(boolean isVi, List<Attraction> allAttractions) {
        double usd = TravelDataService.fallbackRates.getOrDefault("USD", 25450.0);
        double eur = TravelDataService.fallbackRates.getOrDefault("EUR", 27600.0);
        double krw = TravelDataService.fallbackRates.getOrDefault("KRW", 18.4);
        double jpy = TravelDataService.fallbackRates.getOrDefault("JPY", 171.5);

        String text = isVi
                ? String.format(Locale.US,
                        "💱 **Tỷ giá Tham khảo & Kinh nghiệm Đổi tiền / Thanh toán:**\n\n"
                                + "• 🇺🇸 **1 USD** ≈ **%,.0f VNĐ**  |  🇪🇺 **1 EUR** ≈ **%,.0f VNĐ**\n"
                                + "• 🇯🇵 **100 JPY** ≈ **%,.0f VNĐ**  |  🇰🇷 **1.000 KRW** ≈ **%,.0f VNĐ**\n\n"
                                + "🏦 **Địa chỉ đổi ngoại tệ uy tín, tỷ giá tốt nhất Quận 1:**\n"
                                + "• **Tiệm vàng Hà Tâm & Mai Vân:** Số 2 Nguyễn An Ninh, P. Bến Thành, Quận 1 _(ngay cửa Tây Chợ Bến Thành — tỷ giá sát chuẩn ngân hàng, làm việc nhanh)_.\n"
                                + "• **Ngân hàng Vietcombank / ACB / Eximbank** quanh đường Đồng Khởi – Lê Lợi (mang theo hộ chiếu gốc).\n\n"
                                + "💳 **Mẹo thanh toán thông minh:**\n"
                                + "• Thẻ **Visa / Mastercard / Apple Pay** dùng chạm trực tiếp tại cổng **Metro số 1**, trung tâm thương mại, Xanh SM và Grab.\n"
                                + "• Luôn chuẩn bị ~300.000đ – 500.000đ tiền mặt lẻ cho quán ăn vỉa hè. **Lưu ý nhìn kỹ số 0** giữa tờ **20.000đ** và **500.000đ** (cùng màu xanh)!",
                        usd, eur, jpy * 100, krw * 1000)
                : String.format(Locale.US,
                        "💱 **Reference FX Rates & Money / Payment Tips in Saigon:**\n\n"
                                + "• 🇺🇸 **1 USD** ≈ **%,.0f VND**  |  🇪🇺 **1 EUR** ≈ **%,.0f VND**\n"
                                + "• 🇯🇵 **100 JPY** ≈ **%,.0f VND**  |  🇰🇷 **1,000 KRW** ≈ **%,.0f VND**\n\n"
                                + "🏦 **Trusted Currency Exchange Spots (District 1):**\n"
                                + "• **Ha Tam & Mai Van Gold/FX Counters:** 2 Nguyen An Ninh St, right beside the West Gate of **Ben Thanh Market** (best spread for crisp $50/$100 bills).\n"
                                + "• **Vietcombank / ACB / HSBC ATMs** along Dong Khoi & Nguyen Hue (lowest foreign card fees).\n\n"
                                + "💳 **Payment & Cash Safety:**\n"
                                + "• Contactless **Visa / Mastercard / Apple Pay** works on **Metro Line 1**, Grab, Xanh SM, malls, and cafés.\n"
                                + "• Keep 300k–500k VND in small notes for street stalls, and double-check **20,000 VND vs 500,000 VND** (both are blue polymer notes!).",
                        usd, eur, jpy * 100, krw * 1000);

        return new AiChatMessage(
                UUID.randomUUID().toString(),
                false,
                text,
                filterByIds(allAttractions, "ben_thanh_market"),
                Collections.emptyList(),
                Collections.emptyList(),
                isVi
                        ? Arrays.asList("Ước tính chi phí chuyến đi", "Mẹo an toàn & số khẩn cấp", "Mua quà gì ở Chợ Bến Thành?")
                        : Arrays.asList("Estimated daily trip budget", "Emergency & hospital contacts", "What souvenirs to buy?"),
                false
        );
    }

    private static AiChatMessage buildEmergencyAndUtilityReply(
            String q,
            List<PublicUtility> matchedUtilities,
            List<PublicUtility> allUtilities,
            boolean isVi
    ) {
        List<PublicUtility> picks = matchedUtilities;
        if (picks.isEmpty()) {
            picks = new ArrayList<>();
            boolean wantMedical = containsAny(q, "benh vien", "cap cuu", "y te", "thuoc", "hospital", "medical", "doctor");
            boolean wantEmergency = containsAny(q, "lanh su", "dai su", "ho chieu", "passport", "embassy", "consulate", "cong an", "police");
            boolean wantSimAtm = containsAny(q, "sim", "esim", "4g", "wifi", "atm", "rut tien");
            boolean wantRestroom = containsAny(q, "nha ve sinh", "wc", "restroom", "toilet");
            for (PublicUtility u : allUtilities) {
                if (wantMedical && u.category == UtilityCategory.MEDICAL) picks.add(u);
                else if (wantEmergency && u.category == UtilityCategory.EMERGENCY_HOTLINE) picks.add(u);
                else if (wantSimAtm && (u.category == UtilityCategory.TOURIST_INFO_SIM || u.category == UtilityCategory.CURRENCY_EXCHANGE)) picks.add(u);
                else if (wantRestroom && u.category == UtilityCategory.RESTROOM) picks.add(u);
                if (picks.size() >= 4) break;
            }
            if (picks.isEmpty()) {
                picks.addAll(allUtilities.subList(0, Math.min(4, allUtilities.size())));
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(isVi
                ? "🆘 **Danh bạ Khẩn cấp & Tiện ích Hỗ trợ Du khách 24/7:**\n\n"
                        + "• 🚨 **Công an / Cảnh sát phản ứng nhanh:** `113`\n"
                        + "• 🚑 **Cấp cứu Y tế:** `115`  |  🚒 **Cứu hỏa / Cứu hộ:** `114`\n"
                        + "• ☎️ **Tổng đài Hỗ trợ Du khách TP.HCM:** `1022` (nhánh Du lịch) hoặc `(028) 3824 2903`\n\n"
                        + "🏥 **Địa điểm tiện ích xác thực trong CSDL ứng dụng:**\n"
                : "🆘 **24/7 Emergency Hotlines & Verified Tourist Utilities:**\n\n"
                        + "• 🚨 **Police:** `113`  |  🚑 **Ambulance:** `115`  |  🚒 **Fire/Rescue:** `114`\n"
                        + "• ☎️ **HCMC Tourist Support Hotline:** `1022` or `(028) 3824 2903`\n\n"
                        + "🏥 **Verified Facilities in App Database:**\n");

        for (PublicUtility u : picks) {
            sb.append("• **").append(isVi ? u.nameVi : u.nameEn).append("**\n")
              .append("  📍 ").append(u.address).append("  •  🕒 ").append(u.operatingHours);
            if (u.phone != null) sb.append("  •  📞 `").append(u.phone).append("`");
            sb.append("\n  💡 _").append(u.tips).append("_\n");
        }

        return new AiChatMessage(
                UUID.randomUUID().toString(),
                false,
                sb.toString().trim(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                isVi
                        ? Arrays.asList("Đổi tiền USD uy tín ở đâu?", "Mẹo đi taxi sân bay không bị chặt chém", "Câu giao tiếp tiếng Việt khẩn cấp")
                        : Arrays.asList("Trusted currency exchange spots", "Airport taxi safety rules", "Emergency Vietnamese phrases"),
                false
        );
    }

    private static AiChatMessage buildVegetarianAndHalalReply(boolean isVi, List<SpecialtyFood> allFoods) {
        String text = isVi
                ? "🥗 **Gợi ý Quán Chay (Vegetarian) & Halal Uy tín tại Sài Gòn:**\n\n"
                        + "**1. Nhà hàng Chay thanh tịnh, nổi tiếng:**\n"
                        + "• **Hum Garden & Lounge** _(32 Võ Văn Tần, Quận 3 & Thảo Điền)_ — Nhà hàng chay Michelin Selected trong biệt thự sân vườn xanh mát (150.000đ – 350.000đ/món).\n"
                        + "• **Pi Vegetarian Bistro** _(Lầu 1, 187/15 Điện Biên Phủ, Quận 1)_ — Ẩm thực chay Nam Bộ tinh tế, giá hợp lý (65.000đ – 140.000đ).\n"
                        + "• **Bánh mì chay / Phở chay / Cơm tấm chay Giác Ngộ** _(Quận 1, Quận 3, Quận 10)_ — Bình dân ngon miệng (30.000đ – 55.000đ), cực đông vào ngày Rằm & Mùng 1.\n\n"
                        + "**2. Khu ẩm thực Halal (dành cho khách Hồi giáo):**\n"
                        + "• **Phố Halal Nguyễn An Ninh** _(sát cửa Tây Chợ Bến Thành, Quận 1)_: Halal@Saigon, Kampung Pandan, The Daun Restaurant.\n\n"
                        + "🗣️ **Câu dặn quán ăn:** _\"Cho mình món chay trường (không thịt, không nước mắm nhé)!\"_"
                : "🥗 **Top Vegetarian, Vegan & Halal Dining in Saigon:**\n\n"
                        + "**1. Acclaimed Vegetarian & Vegan Spots:**\n"
                        + "• **Hum Vegetarian, Garden & Restaurant** _(32 Vo Van Tan, Dist. 3 & Thao Dien)_ — Michelin-selected serene garden villa serving refined plant-based Vietnamese cuisine.\n"
                        + "• **Pi Vegetarian Bistro** _(187/15 Dien Bien Phu, Dist. 1)_ — Cozy rooftop loft with vegan Bánh Xèo, pomelo salad & mushroom hotpot (65k–140k VND).\n"
                        + "• **Shamballa Vegetarian Lounge** _(17–19 Trinh Van Can, Dist. 1)_ — Tranquil Tibetan-inspired wooden teahouse near Ben Thanh.\n\n"
                        + "**2. Certified Halal Dining Street:**\n"
                        + "• **Nguyen An Ninh Street** _(West Gate of Ben Thanh Market, Dist. 1)_ — Home to Halal@Saigon, The Daun, and Malaysian/Vietnamese Halal eateries.\n\n"
                        + "🗣️ **Show this to staff:** _\"Tôi ăn chay, không thịt và không nước mắm\"_ (I eat vegan — no meat and no fish sauce).";

        return new AiChatMessage(
                UUID.randomUUID().toString(),
                false,
                text,
                Collections.emptyList(),
                allFoods.subList(0, Math.min(2, allFoods.size())),
                Collections.emptyList(),
                isVi
                        ? Arrays.asList("Quán cà phê đẹp ở Sài Gòn", "Chùa Bửu Long & Chùa Ngọc Hoàng", "Câu giao tiếp gọi món")
                        : Arrays.asList("Best specialty cafes in Saigon", "Top peaceful temples to visit", "Vietnamese dining phrases"),
                false
        );
    }

    private static AiChatMessage buildNightlifeAndLateFoodReply(
            boolean isVi,
            List<Attraction> allAttractions,
            List<SpecialtyFood> allFoods
    ) {
        List<SpecialtyFood> nightFoods = filterFoodsByIds(allFoods, "oc_saigon", "pho_saigon", "hu_tieu", "ca_phe_vot");
        List<Attraction> nightSpots = filterByIds(allAttractions, "nguyen_hue_walking_street", "thu_thiem_waterfront", "landmark_81");

        String text = isVi
                ? "🌃 **Ăn Đêm & Chơi Tối ở Sài Gòn Đi Đâu Vui Nhất?**\n\n"
                        + "**1. Thiên đường Ẩm thực Đêm (mở tới khuya / 24/7):**\n"
                        + "• **Phố Ốc Vĩnh Khánh (Quận 4):** Quán **Ốc Oanh** (534 Vĩnh Khánh, mở tới 24:00) — ốc hương xào bơ tỏi, càng ghẹ rang muối rực lửa.\n"
                        + "• **Phở Lệ Chợ Lớn** _(415 Nguyễn Trãi, Quận 5 — mở tới 01:00 sáng)_ — Michelin Bib Gourmand.\n"
                        + "• **Hủ Tiếu Nam Vang Thành Đạt** _(34 Cô Bắc, Quận 1 — mở 24/24)_ & **Cà phê vợt Phan Đình Phùng** _(330 Phan Đình Phùng, Phú Nhuận — thức trắng 24/7)_.\n\n"
                        + "**2. Hoạt động Dạo phố & Ngắm cảnh Đêm:**\n"
                        + "• **Phố đi bộ Nguyễn Huệ & Chung cư Cafe 42 Nguyễn Huệ** _(nhộn nhịp nhất tối Thứ 7, CN)_.\n"
                        + "• **Công viên Bờ sông Sài Gòn & Cầu Ba Son (Thủ Thiêm):** Ngắm trọn skyline Quận 1 & Bitexco lung linh bên sông.\n"
                        + "• **Phố Tây Bùi Viện (Quận 1)** sôi động âm nhạc, hoặc chill nhẹ tại **SkyView Landmark 81 / Rooftop Quận 1**."
                : "🌃 **Saigon After Dark: Best Nightlife & Late-Night Street Food**\n\n"
                        + "**1. Legendary Late-Night Eats:**\n"
                        + "• **Vinh Khanh Food Street (District 4):** **Ốc Oanh** (534 Vinh Khanh, till midnight) for sizzling garlic-butter sea snails & spicy crab claws.\n"
                        + "• **Phở Lệ** _(415 Nguyen Trai, Dist. 5 — open till 01:00 AM, Michelin Bib Gourmand)_.\n"
                        + "• **Hủ Tiếu Thành Đạt** _(34 Co Bac, Dist. 1 — open 24/7)_ & **Phan Dinh Phung 24/7 Racquet Coffee** _(330 Phan Dinh Phung)_.\n\n"
                        + "**2. Evening Strolls & Night Views:**\n"
                        + "• **Nguyen Hue Walking Promenade & 42 Nguyen Hue Cafe Apartment**.\n"
                        + "• **Thu Thiem Riverfront Park & Ba Son Bridge:** Cool river breeze overlooking the illuminated downtown skyline.\n"
                        + "• **À Ố Show** at the 1900 Opera House, vibrant **Bui Vien Walking Street**, or sky-high lounges at **Landmark 81**.";

        return new AiChatMessage(
                UUID.randomUUID().toString(),
                false,
                text,
                nightSpots,
                nightFoods,
                Collections.emptyList(),
                isVi
                        ? Arrays.asList("Chi tiết Ốc Sài Gòn", "Mẹo gọi xe an toàn ban đêm", "Lịch trình 1 ngày Quận 1")
                        : Arrays.asList("Tell me about Oc Saigon snails", "Night taxi & cash safety tips", "1-Day District 1 itinerary"),
                false
        );
    }

    private static AiChatMessage buildCoffeeCultureReply(
            boolean isVi,
            List<Attraction> allAttractions,
            List<SpecialtyFood> allFoods
    ) {
        List<SpecialtyFood> coffeeFood = filterFoodsByIds(allFoods, "ca_phe_vot", "banh_mi");
        String text = isVi
                ? "☕ **Bản đồ Văn hóa Cà Phê Sài Gòn Đặc sắc Nhất:**\n\n"
                        + "• **Cà phê Vợt Cheo Leo (Từ năm 1938):** _109/36 Nguyễn Thiện Thuật, Quận 3_ (18.000đ – 30.000đ, mở 05:15 – 22:00) — Di sản cà phê pha bằng vợt vải trên bếp củi hơn 85 năm.\n"
                        + "• **Chung cư 42 Nguyễn Huệ (Quận 1):** 9 tầng chung cư cổ quy tụ hàng chục quán indie cafe, ban công nhìn thẳng xuống Phố đi bộ Nguyễn Huệ.\n"
                        + "• **Cà phê Bệt Công Xã Paris:** Thưởng thức ly **Cà phê sữa đá / Bạc xỉu** buổi sáng sớm (07:00 – 09:00) dưới tán cây xanh cạnh Nhà thờ Đức Bà & Bưu điện.\n"
                        + "• **Cà phê ven sông Thảo Điền / Thủ Thiêm:** Ngắm hoàng hôn buông sau cầu Ba Son và tháp Landmark 81."
                : "☕ **Ultimate Saigon Coffee Culture Guide:**\n\n"
                        + "• **Cheo Leo Racquet Coffee (Since 1938):** _109/36 Nguyen Thien Thuat, Dist. 3_ (18k–30k VND) — Saigon's oldest heritage cafe brewing coffee in cloth filters over clay pots.\n"
                        + "• **42 Nguyen Hue Cafe Apartment (Dist. 1):** Iconic 9-story vintage apartment block packed with balcony cafes overlooking the pedestrian boulevard.\n"
                        + "• **Morning 'Cà Phê Bệt' (Curbside Iced Milk Coffee):** Grab a **Bạc Xỉu** (creamy coconut/condensed milk coffee) around Notre-Dame Park at 7:30 AM.\n"
                        + "• **Thao Dien & Thu Thiem Riverside Cafes:** Take Metro Line 1 to Thao Dien Station for breezy garden roasteries.";

        return new AiChatMessage(
                UUID.randomUUID().toString(),
                false,
                text,
                filterByIds(allAttractions, "nguyen_hue_walking_street", "notre_dame"),
                coffeeFood,
                Collections.emptyList(),
                isVi
                        ? Arrays.asList("Bánh mì Sài Gòn ăn quán nào ngon?", "Quán ăn đêm ở Sài Gòn", "Cách đi Metro đến Thảo Điền")
                        : Arrays.asList("Best Saigon Banh Mi spots", "Late-night street food", "Metro Line 1 to Thao Dien"),
                false
        );
    }

    private static AiChatMessage buildGeneralFoodShowcaseReply(String q, boolean isVi, List<SpecialtyFood> allFoods) {
        StringBuilder sb = new StringBuilder();
        sb.append(isVi
                ? "🍜 **Bản đồ 9 Đặc sản Trứ danh TP.HCM – Bình Dương – Vũng Tàu:**\n\n"
                : "🍜 **Complete Guide to 9 Iconic Greater Saigon Specialties:**\n\n");
        for (SpecialtyFood f : allFoods) {
            Restaurant topR = f.restaurants.isEmpty() ? null : f.restaurants.get(0);
            sb.append("• **").append(isVi ? f.nameVi : f.nameEn).append("**");
            if (topR != null) {
                sb.append(" — _Gợi ý:_ **").append(topR.name).append("** (").append(topR.district).append(" • ").append(topR.priceRange).append(")");
            }
            sb.append("\n");
        }
        sb.append(isVi
                ? "\n💡 Chạm vào thẻ món ăn bên dưới hoặc hỏi tên món cụ thể (VD: *\"Cơm tấm ăn ở đâu\"*, *\"Ốc Sài Gòn\"*, *\"Bánh khọt Vũng Tàu\"*) để xem toàn bộ địa chỉ quán!"
                : "\n💡 Tap any dish card below or ask about a specific dish (e.g., *\"Where to eat Com Tam\"*, *\"Banh Xeo\"*, *\"Vung Tau Banh Khot\"*) for full addresses!");

        return new AiChatMessage(
                UUID.randomUUID().toString(),
                false,
                sb.toString(),
                Collections.emptyList(),
                allFoods.subList(0, Math.min(4, allFoods.size())),
                Collections.emptyList(),
                isVi
                        ? Arrays.asList("Cơm tấm Sài Gòn quán nào ngon?", "Quán ăn đêm ở Sài Gòn", "Quán chay ngon ở Sài Gòn", "Bánh khọt Vũng Tàu")
                        : Arrays.asList("Best Com Tam eateries", "Late-night street food", "Vegetarian restaurants", "Vung Tau Banh Khot"),
                false
        );
    }

    private static AiChatMessage buildTransportAndAirportReply(String q, boolean isVi, List<Attraction> allAttractions) {
        String text = isVi
                ? "🚇 **Cẩm nang Di chuyển: Sân bay, Metro Số 1, Waterbus & Gọi xe:**\n\n"
                        + "**1. Từ Sân bay Tân Sơn Nhất (SGN) về Trung tâm Quận 1 (~7.5 km):**\n"
                        + "• **Xanh SM / Grab / Be:** ~35.000đ (xe máy) hoặc **110.000đ – 145.000đ** (ô tô 4 chỗ, ~25 phút).\n"
                        + "• **Xe buýt sân bay chất lượng cao số 109 / 152:** Đậu ngay trước sảnh đến, về thẳng **Chợ Bến Thành** chỉ **5.000đ – 20.000đ**!\n\n"
                        + "**2. Tàu điện Metro Tuyến số 1 (Bến Thành – Suối Tiên, 14 nhà ga):**\n"
                        + "• Hoạt động **05:00 – 22:00**, giá vé **6.000đ – 19.000đ** khi chạm thẻ không tiếp xúc (Visa/Mastercard/NAPAS) hoặc 7.000đ – 20.000đ tiền mặt.\n"
                        + "• Kết nối trực tiếp: Chợ Bến Thành → Nhà hát TP → Ba Son → Landmark 81 (Tân Cảng) → Thảo Điền → Suối Tiên & Bến xe Miền Đông Mới.\n\n"
                        + "**3. Saigon Waterbus & Tàu cao tốc Vũng Tàu (Bến Bạch Đằng Q.1):**\n"
                        + "• **Waterbus:** 15.000đ/lượt ngắm hoàng hôn sông Sài Gòn. **Tàu cao tốc Vũng Tàu:** 320.000đ – 350.000đ (~2 giờ)."
                : "🚇 **Complete Transport Guide: Airport, Metro Line 1, Waterbus & Taxis:**\n\n"
                        + "**1. Tan Son Nhat Airport (SGN) → District 1 (~7.5 km):**\n"
                        + "• **Xanh SM EV / Grab / Be:** ~35k VND (bike) or **110k–145k VND** (~$5 USD for 4-seat car, ~25 mins).\n"
                        + "• **Airport Bus #109 or #152:** Direct to **Ben Thanh Market** for **5,000–20,000 VND**.\n\n"
                        + "**2. Metro Line 1 (Ben Thanh – Suoi Tien, 14 Stations):**\n"
                        + "• Runs **05:00–22:00**; tap contactless **Visa/Mastercard** right at the gate (**6,000–19,000 VND**).\n"
                        + "• Stops right at Ben Thanh, Opera House, Ba Son, Landmark 81 (Tan Cang), Thao Dien & Suoi Tien.\n\n"
                        + "**3. River Boats from Bach Dang Pier (District 1):**\n"
                        + "• **Saigon Waterbus:** 15,000 VND/way (magic at 5:00 PM sunset). **Vung Tau Fast Ferry:** 320k–350k VND (~2 hrs).";

        return new AiChatMessage(
                UUID.randomUUID().toString(),
                false,
                text,
                filterByIds(allAttractions, "ben_thanh_market", "landmark_81", "ben_tau_cao_toc_vung_tau"),
                Collections.emptyList(),
                Collections.emptyList(),
                isVi
                        ? Arrays.asList("Từ sân bay Tân Sơn Nhất về Chợ Bến Thành đi sao?", "Từ Chợ Bến Thành đi Địa đạo Củ Chi bao xa?", "Đổi tiền USD ở đâu?")
                        : Arrays.asList("Airport to Ben Thanh fare", "Ben Thanh to Cu Chi Tunnels distance", "Currency exchange tips"),
                false
        );
    }

    private static AiChatMessage buildHotelAccommodationReply(boolean isVi, List<Attraction> allAttractions) {
        String text = isVi
                ? "🏨 **Kinh nghiệm Chọn Khu Lưu trú & Khách sạn theo Nhu cầu:**\n\n"
                        + "**1. Khu Đồng Khởi – Nguyễn Huệ – Nhà hát TP (Quận 1):**\n"
                        + "• *Phù hợp:* Nghỉ dưỡng sang trọng, đi bộ ra Nhà thờ Đức Bà, Bến Bạch Đằng & ga ngầm Metro.\n"
                        + "• *Gợi ý:* Caravelle Saigon, Hotel Majestic, Park Hyatt, The Myst Đồng Khởi _(từ 2.800.000đ+/đêm)_ hoặc boutique hotel đường Lý Tự Trọng _(900.000đ – 1.600.000đ)_.\n\n"
                        + "**2. Khu Chợ Bến Thành – Lê Thánh Tôn – Trương Định (Quận 1):**\n"
                        + "• *Phù hợp:* Lần đầu đến Sài Gòn, cực tiện ăn sáng/ăn đêm, mua sắm và trung chuyển Metro số 1.\n"
                        + "• *Tầm giá:* 700.000đ – 1.500.000đ/đêm (Fusion Suites, Silverland, Liberty Central, Alagon).\n\n"
                        + "**3. Khu Thảo Điền (TP. Thủ Đức) & Vinhomes Landmark 81 (Bình Thạnh):**\n"
                        + "• *Phù hợp:* Gia đình, cặp đôi thích không gian xanh ven sông, căn hộ dịch vụ cao cấp, kết nối Quận 1 chỉ 7 phút đi Metro _(850.000đ – 2.200.000đ/đêm)_.\n\n"
                        + "**4. Nếu đi Vũng Tàu:** Chọn trục **Thùy Vân (Bãi Sau)** để bước qua đường là tắm biển, hoặc **Trần Phú – Hạ Long (Bãi Trước)** để ngắm hoàng hôn yên bình."
                : "🏨 **Where to Stay in Greater Saigon — Neighborhood Breakdown:**\n\n"
                        + "**1. Dong Khoi & Nguyen Hue Boulevard (District 1 — Heritage & Luxury):**\n"
                        + "• Walkable to Opera House, Notre-Dame, Bach Dang Pier & Metro. Iconic stays: *The Myst Dong Khoi, Hotel Majestic, Caravelle, Park Hyatt* ($110–$300+) or boutique hotels on Ly Tu Trong ($40–$75).\n\n"
                        + "**2. Ben Thanh & Tao Dan Park Quarter (District 1 — Best All-Rounder):**\n"
                        + "• Steps from Ben Thanh Metro Hub & street food stalls. Great 3–4★ value (*Silverland, Fusion Original, Liberty Central*: $35–$85/night).\n\n"
                        + "**3. Thao Dien (Thu Duc City) & Landmark 81 (Binh Thanh):**\n"
                        + "• Leafy riverside cafés, serviced apartments & skyline views, just 3–4 Metro stops from downtown ($35–$100/night).\n\n"
                        + "**4. In Vũng Tàu:** Stay along **Thuy Van St (Back Beach)** for sandy beach access, or **Tran Phu St** for sunset sea-view villas.";

        return new AiChatMessage(
                UUID.randomUUID().toString(),
                false,
                text,
                filterByIds(allAttractions, "nguyen_hue_walking_street", "ben_thanh_market", "landmark_81", "bai_sau_thuy_van"),
                Collections.emptyList(),
                Collections.emptyList(),
                isVi
                        ? Arrays.asList("Ước tính chi phí chuyến đi", "Mua quà lưu niệm gì ở Sài Gòn?", "Lịch trình 3 ngày liên vùng")
                        : Arrays.asList("Estimated trip budget", "Best souvenirs to bring home", "3-Day Greater Saigon Itinerary"),
                false
        );
    }

    private static AiChatMessage buildShoppingAndSouvenirReply(boolean isVi, List<Attraction> allAttractions) {
        String text = isVi
                ? "🛍️ **Mua sắm & Quà Lưu niệm Đặc trưng Mang về từ Sài Gòn:**\n\n"
                        + "**1. Top Đặc sản & Quà tinh tế dễ mang lên máy bay:**\n"
                        + "• ☕ **Cà phê rang xay nguyên hạt / Phin nhôm Việt Nam** (Trung Nguyên Legend, La Viet, Shin Coffee).\n"
                        + "• 🍫 **Sô-cô-la thủ công Maison Marou** _(167–169 Calmette, Quận 1)_ — Sô-cô-la làm từ ca cao nguyên bản từng tỉnh miền Nam Việt Nam.\n"
                        + "• 🏺 **Gốm sứ cổ Lái Thiêu & Sơn mài Tương Bình Hiệp (Bình Dương):** Chén đĩa vẽ tay con gà trống vintage giá tận xưởng chỉ từ 25.000đ – 80.000đ/món.\n"
                        + "• 🥜 **Hạt điều rang muối Bình Phước, Bánh pía sầu riêng, Xoài sấy dẻo & Trà sen**.\n\n"
                        + "**2. Địa điểm Mua sắm theo Phân khúc:**\n"
                        + "• **Truyền thống:** **Chợ Bến Thành** (nhớ trả giá vui vẻ giảm ~30–40% cho hàng lưu niệm/vải lụa; khu niêm yết giá cố định thì không cần mặc cả) & **Chợ Bình Tây (Chợ Lớn)**.\n"
                        + "• **Hiện đại máy lạnh:** **Saigon Centre (Takashimaya)**, **Vincom Đồng Khởi**, **Vincom Landmark 81**."
                : "🛍️ **Best Saigon Shopping Spots & Authentic Souvenirs:**\n\n"
                        + "**1. Meaningful Gifts That Pack Easily:**\n"
                        + "• 🍫 **Maison Marou Single-Origin Chocolate** _(167 Calmette, Dist. 1)_ — World-famous bean-to-bar chocolate crafted from Southern Vietnamese cacao.\n"
                        + "• ☕ **Vietnamese Robusta/Arabica Beans + Traditional Metal Phin Filter**.\n"
                        + "• 🏺 **Hand-painted Lai Thieu Ceramics & Lacquerware** (vintage rooster bowls & vases).\n"
                        + "• 🧣 **Tailor-made Silk Áo Dài, Roasted Cashews & Dried Mango**.\n\n"
                        + "**2. Where to Shop:**\n"
                        + "• **Markets:** **Ben Thanh Market** (bargain ~30–40% politely with a smile at general stalls, or shop at fixed-price counters).\n"
                        + "• **Modern Malls:** **Saigon Centre / Takashimaya**, **Vincom Dong Khoi**, and **Landmark 81**.";

        return new AiChatMessage(
                UUID.randomUUID().toString(),
                false,
                text,
                filterByIds(allAttractions, "ben_thanh_market", "lang_gom_lai_thieu", "landmark_81"),
                Collections.emptyList(),
                Collections.emptyList(),
                isVi
                        ? Arrays.asList("Câu tiếng Việt để mặc cả khi mua hàng", "Giá vé & giờ mở cửa Chợ Bến Thành", "Ăn gì gần Chợ Bến Thành?")
                        : Arrays.asList("Vietnamese bargaining phrases", "Ben Thanh Market hours & tips", "Best food near Ben Thanh"),
                false
        );
    }

    private static AiChatMessage buildFamilyCoupleFestivalReply(String q, boolean isVi, List<Attraction> allAttractions) {
        boolean isCouple = containsAny(q, "cap doi", "hen ho", "lang man", "couple", "romantic", "date");
        if (isCouple) {
            String text = isVi
                    ? "💑 **Top Trải nghiệm Lãng mạn cho Cặp đôi tại Sài Gòn & Vũng Tàu:**\n\n"
                            + "• **17:00 — Đi Saigon Waterbus từ Bến Bạch Đằng** (15.000đ) ngắm mặt trời lặn nhuộm vàng sông Sài Gòn và tòa tháp Landmark 81.\n"
                            + "• **18:30 — Dạo bộ Cầu Ba Son & Công viên Bờ sông Thủ Thiêm**, sau đó dùng bữa tối tại Thảo Điền hoặc rooftop Quận 1.\n"
                            + "• **20:00 — Thưởng thức À Ố Show** tại Nhà hát Thành phố kiến trúc Pháp hơn 120 năm tuổi.\n"
                            + "• **Cuối tuần đổi gió Vũng Tàu:** Ngắm hoàng hôn tại **Ngọn Hải đăng Vũng Tàu**, **Mũi Nghinh Phong** và cầu ngắm biển **Hamptons Pier Hồ Tràm**!"
                    : "💑 **Most Romantic Date & Couple Experiences in Saigon & Vũng Tàu:**\n\n"
                            + "• **5:00 PM — Sunset Saigon Waterbus** from Bach Dang Pier (15,000 VND) gliding past the golden skyline.\n"
                            + "• **6:30 PM — Stroll across Ba Son Bridge & Thu Thiem Riverfront Park**, followed by riverside dining in Thao Dien.\n"
                            + "• **8:00 PM — À Ố Bamboo Circus Show** inside the 1900 French Belle Époque Opera House.\n"
                            + "• **Coastal Getaway:** Sunset views from **Vung Tau Lighthouse**, **Nghinh Phong Cape**, and **Hamptons Pier Ho Tram**!";
            return new AiChatMessage(
                    UUID.randomUUID().toString(),
                    false,
                    text,
                    filterByIds(allAttractions, "thu_thiem_waterfront", "saigon_opera_house", "hai_dang_vung_tau", "mui_nghinh_phong"),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    isVi
                            ? Arrays.asList("Đi Vũng Tàu 2 ngày 1 đêm", "Quán cà phê đẹp ở Sài Gòn")
                            : Arrays.asList("2-Day Vung Tau beach trip", "Best romantic cafes in Saigon"),
                    false
            );
        }

        String text = isVi
                ? "👨‍👩‍👧‍👦 **Gợi ý Điểm vui chơi cho Gia đình, Trẻ em & Lễ hội Đặc sắc:**\n\n"
                        + "• **KDL Văn hóa Suối Tiên (TP. Thủ Đức):** Đi thẳng bằng **Metro số 1** đến ga Suối Tiên — có biển nhân tạo Tiên Đồng cực mát cho bé (150.000đ).\n"
                        + "• **KDL Sinh thái Thủy Châu (Dĩ An, Bình Dương):** Tắm suối thác nhân tạo mát lạnh dưới tán rừng xanh cuối tuần (100.000đ người lớn, trẻ dưới 1m2 miễn phí).\n"
                        + "• **KDL Đại Nam (Bình Dương):** Vườn thú mở Open Zoo (hổ trắng, hươu cao cổ, tê giác) & biển nhân tạo khổng lồ.\n"
                        + "• **Cáp treo Hồ Mây Park (Vũng Tàu):** Cáp treo vượt biển + công viên nước & nhạc nước trên đỉnh Núi Lớn (400.000đ trọn gói).\n"
                        + "• 🎉 **Lễ hội lớn trong năm:** Đường hoa Nguyễn Huệ dịp Tết Nguyên Đán, Lễ hội Chùa Bà Bình Dương (Rằm tháng Giêng), Lễ hội Sông nước TP.HCM (tháng 6)."
                : "👨‍👩‍👧‍👦 **Best Family-Friendly Parks & Annual Festivals:**\n\n"
                        + "• **Suoi Tien Cultural Theme Park:** Ride **Metro Line 1** straight to the gate; features mythic dragon sculptures & Tien Dong saltwater beach (150k VND).\n"
                        + "• **Thuy Chau Eco-Stream Park (Binh Duong):** Shaded forest picnic lawns and fresh cascading waterfalls (20 km from downtown).\n"
                        + "• **Dai Nam Open Zoo & Wonderland:** White tigers, giraffes, rhinos, and wave pools.\n"
                        + "• **Ho May Cable Car Park (Vung Tau):** Scenic sea-view cable car + mountaintop amusement park.\n"
                        + "• 🎉 **Major Festivals:** Nguyen Hue Tet Flower Street (Jan/Feb), Chua Ba Binh Duong Lantern Procession (1st Lunar Full Moon), and HCMC River Festival (June).";

        return new AiChatMessage(
                UUID.randomUUID().toString(),
                false,
                text,
                filterByIds(allAttractions, "suoi_tien_park", "kdl_thuy_chau", "dai_nam_binh_duong", "ho_may_park"),
                Collections.emptyList(),
                Collections.emptyList(),
                isVi
                        ? Arrays.asList("Giá vé KDL Suối Tiên", "Cách đi Metro số 1 đến Suối Tiên", "Lịch trình 3 ngày cho gia đình")
                        : Arrays.asList("Suoi Tien ticket details", "Metro Line 1 to Suoi Tien", "3-Day family itinerary"),
                false
        );
    }

    private static AiChatMessage buildVietnamesePhrasesReply(String q, boolean isVi) {
        List<Phrase> allPhrases = PhraseRepository.getDefaultPhrases();
        List<Phrase> matched = new ArrayList<>();
        for (Phrase p : allPhrases) {
            if (scoreTextOverlap(q, p.vietnamese + " " + p.english + " " + p.category.name()) > 0) {
                matched.add(p);
                if (matched.size() >= 5) break;
            }
        }
        if (matched.isEmpty()) {
            matched = allPhrases.subList(0, Math.min(5, allPhrases.size()));
        }

        StringBuilder sb = new StringBuilder();
        sb.append(isVi
                ? "🗣️ **Sổ tay Câu Giao tiếp & Mặc cả Tiếng Việt Thông dụng:**\n\n"
                : "🗣️ **Essential Survival Vietnamese Phrases & Pronunciation:**\n\n");
        for (Phrase p : matched) {
            sb.append("• **\"").append(p.vietnamese).append("\"** — _").append(p.english).append("_\n")
              .append("  🔊 Phát âm / Pronounce: `").append(p.pronunciation).append("`\n")
              .append("  💡 ").append(p.explanation).append("\n");
        }
        sb.append(isVi
                ? "\n👉 Bạn có thể mở tab **Giao tiếp** ở thanh điều hướng dưới để bấm nghe phát âm giọng đọc trực tiếp (TTS)!"
                : "\n👉 Open the **Phrases** tab on the bottom bar anytime to play native Text-to-Speech audio!");

        return new AiChatMessage(
                UUID.randomUUID().toString(),
                false,
                sb.toString(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                isVi
                        ? Arrays.asList("Mua quà gì ở Chợ Bến Thành?", "Quán ăn chay ngon ở Sài Gòn")
                        : Arrays.asList("Shopping & bargaining at Ben Thanh", "Best street food etiquette"),
                false
        );
    }

    private static AiChatMessage buildAdministrativeDistrictReply(String q, boolean isVi, List<Attraction> allAttractions) {
        List<AdministrativeDistrict> districts = AdministrativeDistrictRepository.getDistricts();
        List<AdministrativeDistrict> matched = new ArrayList<>();
        for (AdministrativeDistrict d : districts) {
            if (scoreTextOverlap(q, d.nameVi + " " + d.nameEn + " " + d.mergerStatus) >= 2) {
                matched.add(d);
            }
        }
        List<AdministrativeDistrict> show = matched.isEmpty()
                ? districts.subList(0, Math.min(5, districts.size()))
                : matched.subList(0, Math.min(4, matched.size()));

        StringBuilder sb = new StringBuilder();
        sb.append(isVi
                ? "🗺️ **Thông tin Hành chính & Vùng Đô thị TP.HCM Mở rộng (" + districts.size() + " phân khu trọng điểm):**\n\n"
                : "🗺️ **Greater Ho Chi Minh City Post-Merger Administrative Zones (" + districts.size() + " key corridors):**\n\n");
        for (AdministrativeDistrict d : show) {
            sb.append("• **").append(isVi ? d.nameVi : d.nameEn).append("** (")
              .append(String.format(Locale.US, "%.1f km² • %s", d.areaKm2, d.population)).append(")\n")
              .append("  📌 _").append(d.mergerStatus).append("_ — ").append(d.description).append("\n");
        }

        return new AiChatMessage(
                UUID.randomUUID().toString(),
                false,
                sb.toString().trim(),
                allAttractions.subList(0, Math.min(3, allAttractions.size())),
                Collections.emptyList(),
                Collections.emptyList(),
                isVi
                        ? Arrays.asList("Khám phá Bình Dương 1 ngày", "Đi Vũng Tàu 2 ngày 1 đêm", "Lịch trình 3 ngày liên vùng")
                        : Arrays.asList("1-Day Binh Duong heritage", "2-Day Vung Tau coastal trip", "3-Day Greater Saigon Itinerary"),
                false
        );
    }

    private static AiChatMessage buildVungTauReply(boolean isVi, List<Attraction> allAttractions, List<SpecialtyFood> allFoods) {
        List<Attraction> vtSpots = filterByProvince(allAttractions, "Vũng Tàu", 5);
        List<SpecialtyFood> vtFoods = filterFoodsByIds(allFoods, "banh_khot_vung_tau", "oc_saigon");
        String reply = isVi
                ? "🏖️ **Cẩm nang Khám phá Bà Rịa - Vũng Tàu & Hồ Tràm:**\n\n"
                        + "• **Cách đi nhanh & đẹp nhất:** Tàu cao tốc **Greenlines DP** từ Bến Bạch Đằng Q.1 (320.000đ – 350.000đ, 2 giờ lướt sông & biển) hoặc Limousine cao tốc Long Thành (~2 giờ).\n"
                        + "• **Điểm check-in biểu tượng:** **Tượng Chúa Kitô Vua** (miễn phí, 800 bậc thang), **Ngọn Hải đăng cổ 1862**, **Bạch Dinh (Villa Blanche)** (15.000đ), **Mũi Nghinh Phong**, **Bãi Sau Thùy Vân**, **Cáp treo Hồ Mây** và cầu biển **Hamptons Pier Hồ Tràm**.\n"
                        + "• **Ẩm thực phải thử:** **Bánh khọt Gốc Vú Sữa** (14 Nguyễn Trường Tộ), lẩu cá đuối Trương Công Định & hải sản tươi sống."
                : "🏖️ **Exploring Ba Ria - Vung Tau & Ho Tram Coastline:**\n\n"
                        + "• **Fastest scenic route:** **Greenlines DP High-Speed Catamaran** from Bach Dang Pier (320k–350k VND, ~2 hours) or expressway limousine.\n"
                        + "• **Must-see landmarks:** **Christ the King Statue** (free), **1862 Vung Tau Lighthouse**, **Villa Blanche (Bach Dinh)** (15k VND), **Nghinh Phong Cape**, **Back Beach**, and **Hamptons Pier Ho Tram**.\n"
                        + "• **Signature dish:** Sizzling shrimp **Bánh Khọt** at Gốc Vú Sữa (14 Nguyen Truong To).";

        return new AiChatMessage(
                UUID.randomUUID().toString(),
                false,
                reply,
                vtSpots,
                vtFoods,
                Collections.emptyList(),
                isVi
                        ? Arrays.asList("Đi Vũng Tàu 2 ngày 1 đêm", "Giá vé Bạch Dinh Vũng Tàu", "Bánh khọt Vũng Tàu quán nào ngon?")
                        : Arrays.asList("2-Day Vung Tau beach itinerary", "Villa Blanche Bach Dinh details", "Where to eat Banh Khot?"),
                false
        );
    }

    private static AiChatMessage buildBinhDuongReply(boolean isVi, List<Attraction> allAttractions, List<SpecialtyFood> allFoods) {
        List<Attraction> bdSpots = filterByProvince(allAttractions, "Bình Dương", 5);
        List<SpecialtyFood> bdFoods = filterFoodsByIds(allFoods, "banh_beo_bi_binh_duong");
        String reply = isVi
                ? "🌳 **Cẩm nang Khám phá Bình Dương — Di sản Trăm năm & Sinh thái Xanh:**\n\n"
                        + "• **Khoảng cách:** Chỉ cách trung tâm Quận 1 từ **15 – 35 km** (30–55 phút theo trục Quốc lộ 13 hoặc Xa lộ Hà Nội).\n"
                        + "• **Điểm đến đặc sắc:** **Chùa Bà Thiên Hậu Thủ Dầu Một**, **Làng gốm cổ Lái Thiêu & Lò lu Đại Hưng**, **Vườn trái cây Lái Thiêu**, **Nhà thờ Phú Cường**, **KDL Đại Nam**, **Suối thác Thủy Châu** và **Tổ hợp Hikari Thành phố Mới**.\n"
                        + "• **Đặc sản trứ danh:** **Bánh bèo bì Mỹ Liên Chợ Búng** (di sản hơn 110 năm từ 1910) & **Gỏi gà măng cụt Lái Thiêu** (mùa rộ tháng 5 – tháng 8)."
                : "🌳 **Discovering Binh Duong — Craft Villages, Orchards & Eco-Parks:**\n\n"
                        + "• **Easy day trip:** Only **15–35 km** north of central Saigon (30–55 mins).\n"
                        + "• **Top highlights:** **Thien Hau Pagoda (Chua Ba)**, **Lai Thieu Pottery Kilns**, **Lai Thieu Mangosteen Orchards**, **Phu Cuong Gothic Cathedral**, **Thuy Chau Waterfall Park**, and **Dai Nam Wonderland**.\n"
                        + "• **Heritage cuisine:** **My Lien Shredded Pork Rice Cakes (Bánh Bèo Bì, since 1910)** & **Mangosteen Chicken Salad**.";

        return new AiChatMessage(
                UUID.randomUUID().toString(),
                false,
                reply,
                bdSpots,
                bdFoods,
                Collections.emptyList(),
                isVi
                        ? Arrays.asList("Bánh bèo bì Bình Dương ăn ở đâu?", "Giá vé KDL Thủy Châu", "Lịch trình 3 ngày TP.HCM - Bình Dương - Vũng Tàu")
                        : Arrays.asList("Tell me about Binh Duong Banh Beo Bi", "Thuy Chau Eco-Park tickets", "3-Day Greater Saigon Itinerary"),
                false
        );
    }

    private static AiChatMessage buildItineraryResponse(
            String q,
            boolean isVi,
            List<Attraction> allAttractions,
            List<SpecialtyFood> allFoods
    ) {
        boolean onlyVt = containsAny(q, "vung tau") && !containsAny(q, "binh duong");
        boolean onlyBd = containsAny(q, "binh duong") && !containsAny(q, "vung tau");
        int daysCount = q.contains("1") && !q.contains("2n1d") ? 1 : (q.contains("2") ? 2 : (q.contains("4") ? 4 : 3));

        List<AiItineraryDay> days = new ArrayList<>();
        if (onlyVt) {
            days.add(new AiItineraryDay(
                    isVi ? "Ngày 1: Tàu cao tốc Sài Gòn – Vũng Tàu & Đỉnh Núi Nhỏ" : "Day 1: Fast Ferry to Vung Tau & Mount Nho",
                    isVi ? "08:00 Lên tàu cao tốc tại Bến Bạch Đằng → 10:00 Nhận phòng & chinh phục Tượng Chúa Kitô Vua."
                         : "08:00 High-speed ferry from Bach Dang Pier → Climb Christ the King Statue.",
                    isVi ? "12:30 Ăn trưa Bánh khọt Gốc Vú Sữa → Tham quan Bạch Dinh (Villa Blanche) dưới rừng hoa sứ."
                         : "12:30 Crispy Banh Khot lunch → Visit historic French Villa Blanche (Bach Dinh).",
                    isVi ? "16:45 Ngắm hoàng hôn tại Hải đăng Vũng Tàu & Mũi Nghinh Phong → Tối ăn lẩu cá đuối & dạo biển Bãi Sau."
                         : "16:45 Sunset at Vung Tau Lighthouse & Nghinh Phong Cape → Seafood dinner along Back Beach."
            ));
            if (daysCount >= 2) {
                days.add(new AiItineraryDay(
                        isVi ? "Ngày 2: Bình minh Bãi Sau – Cáp treo Hồ Mây / Hồ Tràm" : "Day 2: Back Beach Sunrise & Ho May / Ho Tram",
                        isVi ? "06:00 Tắm biển đón bình minh Bãi Sau → Trải nghiệm Cáp treo Hồ Mây Park trên đỉnh Núi Lớn."
                             : "06:00 Sunrise swim at Back Beach → Ride Ho May Sea-View Cable Car.",
                        isVi ? "12:30 Thưởng thức hải sản tươi sống & mua bánh bông lan trứng muối làm quà → 15:30 Về lại TP.HCM."
                             : "12:30 Fresh coastal seafood lunch & salted-egg sponge cake souvenirs → Return to HCMC."
                ,       isVi ? "18:00 Về tới Bến Bạch Đằng, dạo mát Phố đi bộ Nguyễn Huệ."
                             : "18:00 Arrive back at Bach Dang Wharf for an evening stroll on Nguyen Hue."
                ));
            }
        } else if (onlyBd) {
            days.add(new AiItineraryDay(
                    isVi ? "Lịch trình 1 Ngày: Làng nghề & Miệt vườn Bình Dương" : "1-Day Binh Duong Craft & Orchard Escape",
                    isVi ? "07:30 Khởi hành đi Thuận An: Ăn sáng Bánh bèo bì Mỹ Liên Chợ Búng → Tham quan Làng gốm cổ Lái Thiêu."
                         : "07:30 Depart HCMC → Breakfast at My Lien Banh Beo Bi (since 1910) → Hands-on pottery at Lai Thieu.",
                    isVi ? "11:30 Dạo Vườn trái cây Lái Thiêu thưởng thức Gỏi gà măng cụt → Lễ Chùa Bà Thiên Hậu Thủ Dầu Một."
                         : "11:30 Mangosteen chicken salad lunch in Lai Thieu orchards → Visit Thien Hau Pagoda.",
                    isVi ? "16:30 Check-in Nhà thờ Gothic Phú Cường lên đèn lung linh & Tổ hợp Hikari trước khi về Sài Gòn."
                         : "16:30 Photograph Phu Cuong Gothic Cathedral & Hikari New City Park before returning."
            ));
        } else {
            days.add(new AiItineraryDay(
                    isVi ? "Ngày 1: Trái tim Di sản Sài Gòn (Quận 1 & Quận 3)" : "Day 1: Historic Saigon Core (District 1 & 3)",
                    isVi ? "08:00 Cơm tấm Ba Ghiền / Calmette & Cà phê sữa đá → Dinh Độc Lập, Nhà thờ Đức Bà & Bưu điện Trung tâm."
                         : "08:00 Com Tam & iced coffee → Independence Palace, Notre-Dame Cathedral & Central Post Office.",
                    isVi ? "13:30 Bảo tàng Chứng tích Chiến tranh → Mua sắm & uống chè tại Chợ Bến Thành."
                         : "13:30 War Remnants Museum → Ben Thanh Market & Maison Marou chocolate.",
                    isVi ? "17:15 Đi Saigon Waterbus (15k) ngắm hoàng hôn Landmark 81 → Tối dạo Phố đi bộ Nguyễn Huệ & Cầu Ba Son."
                         : "17:15 Sunset Saigon Waterbus (15k VND) → Nguyen Hue Promenade & Ba Son Riverfront."
            ));
            if (daysCount >= 2) {
                days.add(new AiItineraryDay(
                        isVi ? "Ngày 2: Địa đạo Củ Chi & Di sản Làng nghề Bình Dương" : "Day 2: Cu Chi Tunnels & Binh Duong Heritage",
                        isVi ? "07:30 Khám phá Địa đạo Củ Chi huyền thoại (125k) thưởng thức khoai mì chấm muối mè."
                             : "07:30 Explore underground Cu Chi Tunnels (125k VND) & taste steamed tapioca.",
                        isVi ? "13:30 Ghé Làng gốm Lái Thiêu, Vườn trái cây & lễ Chùa Bà Thiên Hậu Bình Dương."
                             : "13:30 Visit Lai Thieu Pottery kilns, tropical orchards & Chua Ba Binh Duong.",
                        isVi ? "18:30 Ăn tối Bánh xèo 46A Đinh Công Tráng hoặc Phố Ốc Vĩnh Khánh Quận 4."
                             : "18:30 Crispy Banh Xeo 46A dinner or sizzling street snails on Vinh Khanh Food Street."
                ));
            }
            if (daysCount >= 3) {
                days.add(new AiItineraryDay(
                        isVi ? "Ngày 3: Biển xanh Bà Rịa - Vũng Tàu" : "Day 3: Vung Tau Coastal Escape",
                        isVi ? "07:30 Tàu cao tốc từ Bến Bạch Đằng ra Vũng Tàu → Chinh phục Tượng Chúa Kitô Vua."
                             : "07:30 High-speed ferry from Bach Dang Pier → Climb Christ the King Statue.",
                        isVi ? "12:30 Ăn trưa Bánh khọt Gốc Vú Sữa → Tham quan Bạch Dinh & Ngọn Hải đăng Vũng Tàu."
                             : "12:30 Crispy Banh Khot lunch → Villa Blanche (Bach Dinh) & Vung Tau Lighthouse.",
                        isVi ? "16:30 Dạo biển Bãi Sau & Mũi Nghinh Phong ngắm hoàng hôn trước khi quay về trung tâm TP.HCM."
                             : "16:30 Sunset stroll along Back Beach & Nghinh Phong Cape before returning to HCMC."
                ));
            }
            if (daysCount >= 4) {
                days.add(new AiItineraryDay(
                        isVi ? "Ngày 4: Trải nghiệm Metro Số 1 – Thủ Đức Hiện đại & Mua quà" : "Day 4: Metro Line 1, Thu Duc & Souvenirs",
                        isVi ? "08:00 Trải nghiệm Metro số 1 từ Bến Thành → Tham quan Chùa Bửu Long tuyệt đẹp tại TP. Thủ Đức."
                             : "08:00 Ride Metro Line 1 from Ben Thanh → Visit golden Gotama Stupa at Buu Long Pagoda.",
                        isVi ? "13:00 Ghé Thảo Điền ăn trưa ven sông & lên đài quan sát SkyView Landmark 81."
                             : "13:00 Riverside lunch in Thao Dien & panoramic view from Landmark 81 SkyView.",
                        isVi ? "17:00 Mua cà phê, sô-cô-la Marou, gốm sứ làm quà trước khi ra sân bay Tân Sơn Nhất."
                             : "17:00 Pick up coffee, Marou chocolate & souvenirs before heading to Tan Son Nhat Airport."
                ));
            }
        }

        String summary = isVi
                ? "Tôi đã thiết kế **lịch trình tối ưu cung đường di chuyển** theo đúng nhu cầu của bạn:"
                : "Here is a **route-optimized itinerary** tailored to your request:";

        List<Attraction> cardPicks = onlyVt
                ? filterByProvince(allAttractions, "Vũng Tàu", 4)
                : (onlyBd ? filterByProvince(allAttractions, "Bình Dương", 4)
                : allAttractions.subList(0, Math.min(4, allAttractions.size())));

        return new AiChatMessage(
                UUID.randomUUID().toString(),
                false,
                summary,
                cardPicks,
                allFoods.subList(0, Math.min(2, allFoods.size())),
                days,
                isVi
                        ? Arrays.asList("Ước tính chi phí chuyến đi", "Khách sạn nên ở khu nào?", "Từ sân bay về Quận 1 đi sao?")
                        : Arrays.asList("Estimated trip budget", "Where to stay in Saigon?", "Airport to District 1 transport"),
                false
        );
    }

    // =========================================================================
    // ENTITY SCORING & ROUTE EXTRACTION HELPERS
    // =========================================================================

    public static List<Attraction> findMatchingAttractions(String normQuery, List<Attraction> all, int limit) {
        if (normQuery == null || normQuery.isEmpty()) return Collections.emptyList();
        List<Scored<Attraction>> scored = new ArrayList<>();
        for (Attraction a : all) {
            int s = scoreEntityName(normQuery, a.nameVi, a.nameEn, a.id.replace('_', ' '));
            if (s >= 2) {
                scored.add(new Scored<>(a, s));
            }
        }
        scored.sort((x, y) -> Integer.compare(y.score, x.score));
        List<Attraction> out = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, scored.size()); i++) {
            out.add(scored.get(i).item);
        }
        return out;
    }

    public static List<SpecialtyFood> findMatchingFoods(String normQuery, List<SpecialtyFood> all, int limit) {
        if (normQuery == null || normQuery.isEmpty()) return Collections.emptyList();
        List<Scored<SpecialtyFood>> scored = new ArrayList<>();
        for (SpecialtyFood f : all) {
            StringBuilder aliases = new StringBuilder(f.id.replace('_', ' '));
            for (Restaurant r : f.restaurants) {
                aliases.append(" ").append(r.name);
            }
            if ("banh_beo_bi_binh_duong".equals(f.id)) {
                aliases.append(" goi ga mang cut my lien cho bung");
            } else if ("oc_saigon".equals(f.id)) {
                aliases.append(" oc dao oc oanh vinh khanh nghêu so oc huong");
            } else if ("com_tam".equals(f.id)) {
                aliases.append(" suon bi cha ba ghien phuc loc tho");
            }
            int s = scoreEntityName(normQuery, f.nameVi, f.nameEn, aliases.toString());
            if (s >= 2) {
                scored.add(new Scored<>(f, s));
            }
        }
        scored.sort((x, y) -> Integer.compare(y.score, x.score));
        List<SpecialtyFood> out = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, scored.size()); i++) {
            out.add(scored.get(i).item);
        }
        return out;
    }

    private static List<PublicUtility> findMatchingUtilities(String normQuery, List<PublicUtility> all, int limit) {
        if (normQuery == null || normQuery.isEmpty()) return Collections.emptyList();
        List<Scored<PublicUtility>> scored = new ArrayList<>();
        for (PublicUtility u : all) {
            int s = scoreEntityName(normQuery, u.nameVi, u.nameEn, u.id.replace('_', ' '));
            if (s >= 2) {
                scored.add(new Scored<>(u, s));
            }
        }
        scored.sort((x, y) -> Integer.compare(y.score, x.score));
        List<PublicUtility> out = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, scored.size()); i++) {
            out.add(scored.get(i).item);
        }
        return out;
    }

    private static int scoreEntityName(String normQuery, String nameVi, String nameEn, String extraAliases) {
        String nVi = SearchUtils.normalize(nameVi);
        String nEn = SearchUtils.normalize(nameEn);
        String nAlias = SearchUtils.normalize(extraAliases);

        int score = 0;
        if (!nVi.isEmpty() && normQuery.contains(nVi)) score += 12;
        if (!nEn.isEmpty() && normQuery.contains(nEn)) score += 12;

        // Strip parenthetical suffixes e.g. "Chùa Bửu Long (TP. Thủ Đức)" -> "chua buu long"
        String coreVi = stripParen(nVi);
        String coreEn = stripParen(nEn);
        if (coreVi.length() >= 5 && normQuery.contains(coreVi)) score += 10;
        if (coreEn.length() >= 5 && normQuery.contains(coreEn)) score += 10;

        Set<String> qTokens = meaningfulTokens(normQuery);
        Set<String> entityTokens = meaningfulTokens(coreVi + " " + coreEn + " " + nAlias);
        int matchedTokens = 0;
        for (String qt : qTokens) {
            if (qt.length() >= 3 && entityTokens.contains(qt)) {
                matchedTokens++;
            }
        }
        // Check contiguous 2-grams from entity inside query (e.g., "doc lap", "ben thanh", "duc ba", "com tam", "huynh hoa")
        String[] words = (coreVi + " " + nAlias).trim().split("\\s+");
        for (int i = 0; i + 1 < words.length; i++) {
            String bigram = words[i] + " " + words[i + 1];
            if (bigram.length() >= 5 && !STOP_WORDS.contains(words[i]) && normQuery.contains(bigram)) {
                score += 6;
            }
        }
        if (matchedTokens >= 2) {
            score += matchedTokens * 2;
        }
        return score;
    }

    private static List<NamedGeoPoint> extractTwoRoutePoints(
            String normQuery,
            List<Attraction> attractions,
            List<PublicUtility> utilities
    ) {
        List<PosMatch> matches = new ArrayList<>();

        for (RideRepository.LocationPoint lp : RideRepository.popularPickupPoints) {
            int pos = findMentionIndex(normQuery, lp.nameVi, lp.nameEn);
            if (pos >= 0) {
                matches.add(new PosMatch(pos, new NamedGeoPoint(lp.nameVi, lp.nameEn, lp.lat, lp.lng, null)));
            }
        }
        // Common shorthand aliases for airport & District 1 center
        if (normQuery.contains("san bay") || normQuery.contains("airport") || normQuery.contains("tan son nhat")) {
            int pos = Math.max(normQuery.indexOf("san bay"), Math.max(normQuery.indexOf("airport"), normQuery.indexOf("tan son nhat")));
            matches.add(new PosMatch(pos, new NamedGeoPoint("Sân bay Tân Sơn Nhất", "Tan Son Nhat Airport (SGN)", 10.8185, 106.6588, null)));
        }
        if (normQuery.contains("quan 1") || normQuery.contains("trung tam")) {
            int pos = Math.max(normQuery.indexOf("quan 1"), normQuery.indexOf("trung tam"));
            Attraction bt = AttractionRepository.getAttractionById("ben_thanh_market");
            matches.add(new PosMatch(pos, new NamedGeoPoint("Trung tâm Quận 1 (Chợ Bến Thành)", "Central District 1 (Ben Thanh)", 10.7725, 106.6980, bt)));
        }

        for (Attraction a : attractions) {
            int pos = findMentionIndex(normQuery, stripParen(a.nameVi), stripParen(a.nameEn));
            if (pos >= 0) {
                matches.add(new PosMatch(pos, new NamedGeoPoint(a.nameVi, a.nameEn, a.latitude, a.longitude, a)));
            }
        }
        for (PublicUtility u : utilities) {
            int pos = findMentionIndex(normQuery, stripParen(u.nameVi), stripParen(u.nameEn));
            if (pos >= 0) {
                matches.add(new PosMatch(pos, new NamedGeoPoint(u.nameVi, u.nameEn, u.latitude, u.longitude, null)));
            }
        }

        matches.sort((a, b) -> Integer.compare(a.pos, b.pos));
        List<NamedGeoPoint> distinct = new ArrayList<>();
        for (PosMatch pm : matches) {
            boolean nearExisting = false;
            for (NamedGeoPoint existing : distinct) {
                double d = Math.hypot(existing.lat - pm.point.lat, existing.lng - pm.point.lng);
                if (d < 0.003) { // < ~300m same spot
                    nearExisting = true;
                    break;
                }
            }
            if (!nearExisting) {
                distinct.add(pm.point);
                if (distinct.size() >= 2) break;
            }
        }
        return distinct;
    }

    private static int findMentionIndex(String normQuery, String nameVi, String nameEn) {
        String v = stripParen(SearchUtils.normalize(nameVi));
        if (v.length() >= 5) {
            int idx = normQuery.indexOf(v);
            if (idx >= 0) return idx;
            // Also check without generic prefix like "cho ", "dinh ", "chua ", "nha tho ", "khu du lich "
            String stripped = v.replaceFirst("^(khu du lich van hoa |khu du lich sinh thai |khu du lich |toa nha the |toa nha |ngon )", "").trim();
            if (stripped.length() >= 6) {
                int idx2 = normQuery.indexOf(stripped);
                if (idx2 >= 0) return idx2;
            }
        }
        String e = stripParen(SearchUtils.normalize(nameEn));
        if (e.length() >= 5) {
            int idx = normQuery.indexOf(e);
            if (idx >= 0) return idx;
        }
        return -1;
    }

    private static boolean isPronounFollowUp(String q) {
        return containsAny(q, "o do", "cho do", "noi nay", "cho nay", "gia ve", "may gio",
                "mo cua", "dia chi", "there", "its ticket", "opening hour", "how much");
    }

    private static Attraction findLastRecommendedAttraction(List<AiChatMessage> history) {
        if (history == null) return null;
        for (int i = history.size() - 1; i >= 0; i--) {
            AiChatMessage m = history.get(i);
            if (!m.isUser && !m.recommendedAttractions.isEmpty()) {
                return m.recommendedAttractions.get(0);
            }
        }
        return null;
    }

    private static SpecialtyFood findLastRecommendedFood(List<AiChatMessage> history) {
        if (history == null) return null;
        for (int i = history.size() - 1; i >= 0; i--) {
            AiChatMessage m = history.get(i);
            if (!m.isUser && !m.recommendedFoods.isEmpty()) {
                return m.recommendedFoods.get(0);
            }
        }
        return null;
    }

    private static String stripParen(String s) {
        if (s == null) return "";
        int idx = s.indexOf('(');
        String base = idx > 0 ? s.substring(0, idx) : s;
        int slash = base.indexOf('/');
        base = slash > 0 ? base.substring(0, slash) : base;
        return base.trim();
    }

    private static Set<String> meaningfulTokens(String normText) {
        Set<String> set = new LinkedHashSet<>();
        if (normText == null || normText.isEmpty()) return set;
        for (String tok : normText.split("[^a-z0-9]+")) {
            if (tok.length() >= 2 && !STOP_WORDS.contains(tok)) {
                set.add(tok);
            }
        }
        return set;
    }

    private static int scoreTextOverlap(String normQuery, String candidateText) {
        Set<String> qTokens = meaningfulTokens(normQuery);
        Set<String> cTokens = meaningfulTokens(SearchUtils.normalize(candidateText));
        int hits = 0;
        for (String qt : qTokens) {
            if (cTokens.contains(qt)) hits++;
        }
        return hits;
    }

    private static List<Attraction> filterByProvince(List<Attraction> list, String keyword, int max) {
        List<Attraction> out = new ArrayList<>();
        for (Attraction a : list) {
            if (SearchUtils.fuzzyContains(a.province, keyword)) {
                out.add(a);
                if (out.size() >= max) break;
            }
        }
        return out;
    }

    private static List<Attraction> filterByIds(List<Attraction> list, String... ids) {
        Set<String> wanted = new HashSet<>(Arrays.asList(ids));
        List<Attraction> out = new ArrayList<>();
        for (Attraction a : list) {
            if (wanted.contains(a.id)) out.add(a);
        }
        return out;
    }

    private static List<SpecialtyFood> filterFoodsByIds(List<SpecialtyFood> list, String... ids) {
        Set<String> wanted = new HashSet<>(Arrays.asList(ids));
        List<SpecialtyFood> out = new ArrayList<>();
        for (SpecialtyFood f : list) {
            if (wanted.contains(f.id)) out.add(f);
        }
        return out;
    }

    private static boolean containsAny(String normalizedText, String... keywords) {
        for (String kw : keywords) {
            if (normalizedText.contains(kw)) return true;
        }
        return false;
    }

    private static final class Scored<T> {
        final T item;
        final int score;

        Scored(T item, int score) {
            this.item = item;
            this.score = score;
        }
    }

    private static final class PosMatch {
        final int pos;
        final NamedGeoPoint point;

        PosMatch(int pos, NamedGeoPoint point) {
            this.pos = pos;
            this.point = point;
        }
    }
}
