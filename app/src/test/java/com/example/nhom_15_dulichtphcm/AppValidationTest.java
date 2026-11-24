package com.example.nhom_15_dulichtphcm;

import com.example.nhom_15_dulichtphcm.data.ai.AiTravelEngine;
import com.example.nhom_15_dulichtphcm.data.ai.TripOptimizer;
import com.example.nhom_15_dulichtphcm.data.model.AdministrativeDistrict;
import com.example.nhom_15_dulichtphcm.data.model.AiChatMessage;
import com.example.nhom_15_dulichtphcm.data.model.Attraction;
import com.example.nhom_15_dulichtphcm.data.model.Phrase;
import com.example.nhom_15_dulichtphcm.data.model.PublicUtility;
import com.example.nhom_15_dulichtphcm.data.model.RideEstimate;
import com.example.nhom_15_dulichtphcm.data.model.RideProvider;
import com.example.nhom_15_dulichtphcm.data.model.SpecialtyFood;
import com.example.nhom_15_dulichtphcm.data.repository.AdministrativeDistrictRepository;
import com.example.nhom_15_dulichtphcm.data.repository.AttractionRepository;
import com.example.nhom_15_dulichtphcm.data.repository.FoodRepository;
import com.example.nhom_15_dulichtphcm.data.repository.PhraseRepository;
import com.example.nhom_15_dulichtphcm.data.repository.RideRepository;
import com.example.nhom_15_dulichtphcm.data.repository.UtilityRepository;
import com.example.nhom_15_dulichtphcm.util.AppLanguage;
import com.example.nhom_15_dulichtphcm.util.SearchUtils;
import com.example.nhom_15_dulichtphcm.util.SeedImageResolver;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AppValidationTest {

    @Before
    public void setUp() {
        AttractionRepository.resetForTesting();
        FoodRepository.resetForTesting();
        UtilityRepository.resetForTesting();
    }

    @Test
    public void seedCatalogs_containCompleteGreaterHcmcDataset() {
        List<Attraction> attractions = AttractionRepository.getAttractions();
        List<SpecialtyFood> foods = FoodRepository.getSpecialtyFoods();
        List<PublicUtility> utilities = UtilityRepository.getUtilities();
        List<Phrase> phrases = PhraseRepository.getDefaultPhrases();
        List<AdministrativeDistrict> districts = AdministrativeDistrictRepository.getDistricts();

        assertEquals(26, attractions.size());
        assertEquals(9, foods.size());
        assertEquals(17, utilities.size());
        assertEquals(25, phrases.size());
        assertEquals(13, districts.size());

        int totalRestaurants = 0;
        for (SpecialtyFood f : foods) {
            totalRestaurants += f.restaurants.size();
        }
        assertEquals(20, totalRestaurants);
    }

    @Test
    public void searchUtils_accentInsensitiveVietnameseMatching() {
        assertTrue(SearchUtils.fuzzyContains("Chợ Bến Thành", "cho ben thanh"));
        assertTrue(SearchUtils.fuzzyContains("Dinh Độc Lập", "dinh doc lap"));
        assertTrue(SearchUtils.fuzzyContains("Cơm tấm Sài Gòn", "com tam"));
        assertFalse(SearchUtils.fuzzyContains("Chợ Bến Thành", "ha noi"));
    }

    @Test
    public void rideFareAndTripOptimizer_produceValidEstimates() {
        RideProvider grab = RideRepository.providers.get(1);
        RideEstimate estimate = RideRepository.estimateFare(grab, 6.5);
        assertTrue(estimate.bikeFareVnd > 0);
        assertTrue(estimate.car4FareVnd > estimate.bikeFareVnd);
        assertTrue(estimate.car7FareVnd > estimate.car4FareVnd);

        List<Attraction> all = AttractionRepository.getAttractions();
        TripOptimizer.OptimizedPlan plan = TripOptimizer.buildPlan(
                Arrays.asList(all.get(0), all.get(1), all.get(2)),
                510
        );
        assertEquals(3, plan.stops.size());
        assertTrue(plan.totalDistanceKm > 0.0);
        assertTrue(plan.totalDurationMinutes > 0);
    }

    @Test
    public void offlineConciergeEngine_generatesItineraryAndFoodReplies() {
        AiChatMessage itineraryMsg = AiTravelEngine.generateOfflineReply(
                "Lịch trình 3 ngày TP.HCM", AppLanguage.VIETNAMESE);
        assertNotNull(itineraryMsg);
        assertFalse(itineraryMsg.text.isEmpty());
        assertFalse(itineraryMsg.itineraryDays.isEmpty());

        AiChatMessage foodMsg = AiTravelEngine.generateOfflineReply(
                "Must-try Saigon street food", AppLanguage.ENGLISH);
        assertNotNull(foodMsg);
        assertFalse(foodMsg.recommendedFoods.isEmpty());
    }

    @Test
    public void smartConcierge_answersSpecificTicketsRoutesNightFoodBudgetWeatherHotelsAndFollowUps() {
        // 1. Specific landmark ticket & hours
        AiChatMessage palaceMsg = AiTravelEngine.generateOfflineReply(
                "Giá vé Dinh Độc Lập bao nhiêu và mấy giờ mở cửa?", AppLanguage.VIETNAMESE);
        assertTrue(palaceMsg.text.contains("40,000 VND"));
        assertEquals("independence_palace", palaceMsg.recommendedAttractions.get(0).id);

        // 2. Pronoun follow-up using conversation history ("ở đó địa chỉ thế nào?")
        AiChatMessage followUpMsg = AiTravelEngine.generateOfflineReply(
                "Địa chỉ ở đó thế nào?", Arrays.asList(palaceMsg), AppLanguage.VIETNAMESE);
        assertTrue(followUpMsg.text.contains("Nam Kỳ Khởi Nghĩa"));

        // 3. Specific dish / eatery lookup ("Bánh mì Huỳnh Hoa" / "Ốc Sài Gòn")
        AiChatMessage banhMiMsg = AiTravelEngine.generateOfflineReply(
                "Bánh mì Huỳnh Hoa giá bao nhiêu và ở đâu?", AppLanguage.VIETNAMESE);
        assertTrue(banhMiMsg.text.contains("68,000 VND"));
        assertEquals("banh_mi", banhMiMsg.recommendedFoods.get(0).id);

        // 4. Point-to-point route distance & ride fare ("Từ sân bay Tân Sơn Nhất về Chợ Bến Thành")
        AiChatMessage routeMsg = AiTravelEngine.generateOfflineReply(
                "Từ sân bay Tân Sơn Nhất về Chợ Bến Thành bao xa và cước Grab bao nhiêu?", AppLanguage.VIETNAMESE);
        assertTrue(routeMsg.text.contains("km"));
        assertTrue(routeMsg.text.contains("GrabBike"));

        // 5. Trip budget, night food, vegetarian, rainy day, hotels, souvenirs, emergency
        assertTrue(AiTravelEngine.generateOfflineReply("Ước tính chi phí chuyến đi 3 ngày", AppLanguage.VIETNAMESE)
                .text.contains("Dự toán Chi phí"));
        assertTrue(AiTravelEngine.generateOfflineReply("Quán ăn đêm ở Sài Gòn", AppLanguage.VIETNAMESE)
                .text.contains("Vĩnh Khánh"));
        assertTrue(AiTravelEngine.generateOfflineReply("Gợi ý quán ăn chay ngon", AppLanguage.VIETNAMESE)
                .text.contains("Hum"));
        assertTrue(AiTravelEngine.generateOfflineReply("Trời mưa ở Sài Gòn đi đâu chơi?", AppLanguage.VIETNAMESE)
                .text.contains("TRONG NHÀ"));
        assertTrue(AiTravelEngine.generateOfflineReply("Nên ở khách sạn khu nào tại Sài Gòn?", AppLanguage.VIETNAMESE)
                .text.contains("Đồng Khởi"));
        assertTrue(AiTravelEngine.generateOfflineReply("Mua quà lưu niệm gì mang về?", AppLanguage.VIETNAMESE)
                .text.contains("Maison Marou"));
        assertTrue(AiTravelEngine.generateOfflineReply("Số điện thoại cấp cứu và bệnh viện quốc tế", AppLanguage.VIETNAMESE)
                .text.contains("115"));

        // 6. RAG prompt grounding for Cloud Gemini
        String ragPrompt = AiTravelEngine.buildGroundedContextForPrompt(
                "Từ Chợ Bến Thành đi Địa đạo Củ Chi bao xa?", AppLanguage.VIETNAMESE);
        assertTrue(ragPrompt.contains("[COMPUTED ROUTE]"));
        assertTrue(ragPrompt.contains("Địa đạo Củ Chi"));
    }

    @Test
    public void seedImageResolver_healsStaleSqliteDrawableIdsAndRejectsUiShapes() throws Exception {
        // 1. Verify non-photo UI drawables/icons are strictly rejected
        assertFalse(SeedImageResolver.isValidPhotoDrawable(null, 0, false));
        assertFalse(SeedImageResolver.isValidPhotoDrawable(null, R.drawable.bg_fab_guide, false));
        assertFalse(SeedImageResolver.isValidPhotoDrawable(null, R.drawable.bg_gold_pill, false));
        assertFalse(SeedImageResolver.isValidPhotoDrawable(null, R.drawable.ic_app_logo_badge, true));

        // 2. Verify all 26 attractions & 9 foods have valid distinct photo drawables and self-heal from shifted IDs
        for (Attraction a : AttractionRepository.getAttractions()) {
            assertTrue("Expected valid photo drawable for " + a.id,
                    SeedImageResolver.isValidPhotoDrawable(null, a.imageResId, false));
            Attraction corrupted = new Attraction(
                    a.id, a.nameEn, a.nameVi, a.category, a.imageUrl,
                    R.drawable.bg_fab_guide, // simulated stale shifted ID from SQLite
                    a.district, a.address, a.latitude, a.longitude,
                    a.ticketPrice, a.openingHours, a.shortSummary,
                    a.history, a.culture, a.foreignerTips, a.province
            );
            Attraction healed = SeedImageResolver.healAttraction(null, corrupted);
            assertEquals("Failed to heal stale drawable for " + a.id, a.imageResId, healed.imageResId);
        }

        for (SpecialtyFood f : FoodRepository.getSpecialtyFoods()) {
            assertTrue("Expected valid food drawable for " + f.id,
                    SeedImageResolver.isValidPhotoDrawable(null, f.imageResId, true));
            SpecialtyFood corrupted = new SpecialtyFood(
                    f.id, f.nameEn, f.nameVi, f.category, f.imageUrl,
                    R.drawable.ic_app_logo_badge, // simulated stale shifted ID from SQLite
                    f.shortDescription, f.storyAndHistory, f.howToEatLikeALocal,
                    f.ingredients, f.restaurants
            );
            SpecialtyFood healed = SeedImageResolver.healFood(null, corrupted);
            assertEquals("Failed to heal stale food drawable for " + f.id, f.imageResId, healed.imageResId);
        }

        // 3. Verify all 35 physical JPEG files exist in res/drawable with valid JPEG SOI marker (FF D8 FF)
        File cwd = new File(System.getProperty("user.dir", "."));
        File appDir = new File(cwd, "src/main/res/drawable").isDirectory() ? cwd : new File(cwd, "app");
        File drawableDir = new File(appDir, "src/main/res/drawable");
        File[] jpgs = drawableDir.listFiles((dir, name) ->
                (name.startsWith("attraction_") || name.startsWith("food_")) && name.endsWith(".jpg"));
        assertNotNull(jpgs);
        assertEquals("Expected 35 photo JPEGs (26 attractions + 9 foods)", 35, jpgs.length);
        for (File jpg : jpgs) {
            assertTrue("Photo unexpectedly small: " + jpg.getName(), jpg.length() > 50_000L);
            byte[] header = new byte[3];
            try (FileInputStream fis = new FileInputStream(jpg)) {
                assertEquals(3, fis.read(header));
            }
            assertEquals("Invalid JPEG SOI byte 0 in " + jpg.getName(), (byte) 0xFF, header[0]);
            assertEquals("Invalid JPEG SOI byte 1 in " + jpg.getName(), (byte) 0xD8, header[1]);
            assertEquals("Invalid JPEG SOI byte 2 in " + jpg.getName(), (byte) 0xFF, header[2]);
        }
    }

    @Test
    public void rideLauncherAndManifest_enforceGooglePlayAndAndroid11PackageVisibility() throws Exception {
        assertEquals("com.android.vending", com.example.nhom_15_dulichtphcm.util.RideLauncher.GOOGLE_PLAY_PACKAGE);

        File cwd = new File(System.getProperty("user.dir", "."));
        File appDir = new File(cwd, "src/main/AndroidManifest.xml").isFile() ? cwd : new File(cwd, "app");
        File manifestFile = new File(appDir, "src/main/AndroidManifest.xml");
        assertTrue(manifestFile.isFile());

        byte[] raw = new byte[(int) manifestFile.length()];
        try (FileInputStream fis = new FileInputStream(manifestFile)) {
            assertEquals(raw.length, fis.read(raw));
        }
        String manifestXml = new String(raw, java.nio.charset.StandardCharsets.UTF_8);
        assertTrue("Manifest must declare <queries> for Android 11+ visibility", manifestXml.contains("<queries>"));
        assertTrue("Manifest must query Google Play Store com.android.vending", manifestXml.contains("com.android.vending"));

        for (RideProvider p : RideRepository.providers) {
            assertNotNull(p.packageName);
            assertFalse(p.packageName.trim().isEmpty());
            assertTrue("Manifest <queries> missing ride package: " + p.packageName,
                    manifestXml.contains(p.packageName));
            assertEquals(
                    "https://play.google.com/store/apps/details?id=" + p.packageName,
                    com.example.nhom_15_dulichtphcm.util.RideLauncher.buildPlayStoreWebUrl(p.packageName)
            );
        }
    }
}
