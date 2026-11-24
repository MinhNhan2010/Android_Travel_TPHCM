package com.example.nhom_15_dulichtphcm.ui.screens;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.nhom_15_dulichtphcm.R;
import com.example.nhom_15_dulichtphcm.data.local.TripPlanManager;
import com.example.nhom_15_dulichtphcm.data.local.UserDataBackupManager;
import com.example.nhom_15_dulichtphcm.data.model.Attraction;
import com.example.nhom_15_dulichtphcm.data.model.ResultBox;
import com.example.nhom_15_dulichtphcm.data.model.SpecialtyFood;
import com.example.nhom_15_dulichtphcm.data.repository.AttractionRepository;
import com.example.nhom_15_dulichtphcm.data.repository.FoodRepository;
import com.example.nhom_15_dulichtphcm.ui.AppNavigator;
import com.example.nhom_15_dulichtphcm.ui.components.ChipViewFactory;
import com.example.nhom_15_dulichtphcm.ui.components.CompactHeaderHelper;
import com.example.nhom_15_dulichtphcm.util.ImageLoader;
import com.example.nhom_15_dulichtphcm.util.Localized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Pure Java + XML "Hôm nay Ăn gì – Đi đâu?" Smart Combo Spinner
 * + 10-Question Saigon Heritage Quiz unlocking the SQLite "🎓 Học Giả Sài Gòn" badge.
 */
public final class DiscoveryQuizFragment extends Fragment {

    private static final class QuizQuestion {
        final String questionVi;
        final String questionEn;
        final String[] optionsVi;
        final String[] optionsEn;
        final int correctIdx;
        final String explainVi;
        final String explainEn;

        QuizQuestion(String questionVi, String questionEn, String[] optionsVi, String[] optionsEn,
                     int correctIdx, String explainVi, String explainEn) {
            this.questionVi = questionVi;
            this.questionEn = questionEn;
            this.optionsVi = optionsVi;
            this.optionsEn = optionsEn;
            this.correctIdx = correctIdx;
            this.explainVi = explainVi;
            this.explainEn = explainEn;
        }
    }

    private static final List<QuizQuestion> QUIZ_BANK = Arrays.asList(
            new QuizQuestion(
                    "1. Chợ Bến Thành hiện nay tại Quận 1 chính thức khánh thành vào năm nào?",
                    "1. In which year was the current Ben Thanh Market in District 1 inaugurated?",
                    new String[]{"Năm 1880", "Năm 1914", "Năm 1932", "Năm 1954"},
                    new String[]{"1880", "1914", "1932", "1954"},
                    1,
                    "Chợ Bến Thành mới khởi công năm 1912 và khánh thành tháng 3/1914 với tháp đồng hồ 4 mặt biểu tượng của Sài Gòn.",
                    "The new Ben Thanh Market opened in March 1914 featuring its iconic four-faced clock tower."
            ),
            new QuizQuestion(
                    "2. Kiến trúc sư Việt Nam tài hoa nào đã thiết kế Dinh Độc Lập (Hội trường Thống Nhất)?",
                    "2. Which renowned Vietnamese architect designed the Independence Palace?",
                    new String[]{"Ngô Viết Thụ", "Huỳnh Tấn Phát", "Trần Văn Tốt", "Võ Trọng Nghĩa"},
                    new String[]{"Ngo Viet Thu", "Huynh Tan Phat", "Tran Van Tot", "Vo Trong Nghia"},
                    0,
                    "KTS Ngô Viết Thụ (Khôi nguyên Giải thưởng La Mã) thiết kế Dinh Độc Lập kết hợp kiến trúc hiện đại và triết lý phương Đông (chữ Cát, Khẩu, Trung).",
                    "Architect Ngo Viet Thu (Prix de Rome laureate) blended modernist lines with auspicious Eastern calligraphy."
            ),
            new QuizQuestion(
                    "3. Tuyến Metro Số 1 (Bến Thành – Suối Tiên) của TP.HCM có tổng cộng bao nhiêu nhà ga?",
                    "3. How many stations are on HCMC Metro Line 1 (Ben Thanh – Suoi Tien)?",
                    new String[]{"10 ga", "12 ga", "14 ga (3 ga ngầm, 11 ga trên cao)", "18 ga"},
                    new String[]{"10 stations", "12 stations", "14 stations (3 underground, 11 elevated)", "18 stations"},
                    2,
                    "Tuyến Metro số 1 dài 19,7 km gồm 3 ga ngầm (Bến Thành, Nhà hát TP, Ba Son) và 11 ga trên cao đến Bến xe Suối Tiên.",
                    "Metro Line 1 spans 19.7 km with 3 underground stations in District 1 and 11 elevated stations."
            ),
            new QuizQuestion(
                    "4. Linh hồn làm nên hương vị đặc trưng của đĩa Cơm tấm Sài Gòn chuẩn vị gồm bộ tứ nào?",
                    "4. What is the classic four-topping combo ('Sườn - Bì - Chả - Trứng') of Saigon Com Tam?",
                    new String[]{"Sườn nướng, bì thính, chả trứng, trứng ốp la & mỡ hành", "Thịt kho tàu, dưa cải, chả lụa, trứng luộc", "Gà xối mỡ, pate, đồ chua, nước tương", "Thịt quay, xá xíu, hoành thánh, hẹ"},
                    new String[]{"Grilled pork chop, shredded pork skin, steamed egg meatloaf & sunny-side egg", "Braised pork belly & pickled mustard", "Crispy chicken & soy sauce", "Roast pork & wontons"},
                    0,
                    "Cơm tấm Sài Gòn nấu từ hạt gạo tấm dẻo thơm, ăn kèm sườn nướng mật ong, bì, chả trứng hấp, mỡ hành và nước mắm kẹo chua ngọt.",
                    "Broken rice topped with honey-grilled pork chop, shredded pork skin, steamed egg cake, scallion oil, and sweet-savory fish sauce."
            ),
            new QuizQuestion(
                    "5. Hệ thống Địa đạo Củ Chi dưới lòng đất có tổng chiều dài ước tính khoảng bao nhiêu km?",
                    "5. Approximately how long was the underground Cu Chi Tunnels network?",
                    new String[]{"Khoảng 25 km", "Khoảng 70 km", "Khoảng 250 km nhiều tầng", "Khoảng 500 km"},
                    new String[]{"~25 km", "~70 km", "~250 km multi-level network", "~500 km"},
                    2,
                    "Địa đạo Củ Chi vươn dài khoảng 250 km gồm 3 tầng sâu với bếp Hoàng Cầm giấu khói, phòng họp và trạm quân y dưới lòng đất.",
                    "Cu Chi Tunnels stretched roughly 250 km across 3 subterranean levels complete with smokeless Hoang Cam kitchens."
            ),
            new QuizQuestion(
                    "6. Bưu điện Trung tâm Sài Gòn nổi bật với mái vòm sắt thanh lịch mang đậm dấu ấn trường phái kiến trúc nào?",
                    "6. Saigon Central Post Office showcases ironwork & vaulted arches from which era?",
                    new String[]{"Kiến trúc Pháp cuối thế kỷ XIX (1886–1891)", "Kiến trúc Gothic thời Trung cổ", "Kiến trúc Baroque Tây Ban Nha", "Kiến trúc Brutalism thập niên 1970"},
                    new String[]{"Late 19th-century French Colonial (1886–1891)", "Medieval Gothic", "Spanish Baroque", "1970s Brutalism"},
                    0,
                    "Xây dựng giai đoạn 1886–1891 theo đồ án của KTS Villedieu, bên trong có hai bản đồ lịch sử Sài Gòn và đường dây thép Nam Kỳ.",
                    "Constructed between 1886 and 1891 with soaring wrought-iron arches and historic hand-painted telegraph maps."
            ),
            new QuizQuestion(
                    "7. Làng nghề truyền thống hàng trăm năm tuổi nào của vùng Bình Dương nổi danh khắp Nam Bộ?",
                    "7. Which century-old craft heritage is Binh Duong famous for across Southern Vietnam?",
                    new String[]{"Dệt lụa Vạn Phúc", "Gốm sứ Lái Thiêu – Tân Phước Khánh & Sơn mài Tương Bình Hiệp", "Đúc đồng Ngũ Xã", "Làm nón bài thơ"},
                    new String[]{"Van Phuc Silk", "Lai Thieu Ceramics & Tuong Binh Hiep Lacquerware", "Ngu Xa Bronze Casting", "Conical Hats"},
                    1,
                    "Gốm sứ Lái Thiêu và Sơn mài Tương Bình Hiệp (Bình Dương) là Di sản Văn hóa Phi vật thể Quốc gia nổi tiếng.",
                    "Lai Thieu pottery and Tuong Binh Hiep lacquerware are recognized National Intangible Cultural Heritages."
            ),
            new QuizQuestion(
                    "8. Tượng Chúa Kitô Vua trên đỉnh Núi Nhỏ (Vũng Tàu) có bao nhiêu bậc thang xoắn ốc bên trong lòng tượng?",
                    "8. How many interior spiral steps lead up inside the Christ of Vung Tau statue's shoulders?",
                    new String[]{"50 bậc", "133 bậc thang xoắn ốc", "300 bậc", "500 bậc"},
                    new String[]{"50 steps", "133 interior spiral steps", "300 steps", "500 steps"},
                    1,
                    "Sau khi chinh phục gần 800 bậc đá lên đỉnh núi Nhỏ, du khách bước tiếp 133 bậc xoắn ốc trong lòng tượng cao 32m để ngắm toàn cảnh biển Vũng Tàu.",
                    "Inside the 32m-tall statue, 133 spiral steps lead right onto the shoulder lookout overlooking Vung Tau's beaches."
            ),
            new QuizQuestion(
                    "9. Cách pha 'Cà phê vợt' cổ truyền hơn 70 năm ở Sài Gòn – Chợ Lớn sử dụng dụng cụ gì?",
                    "9. How is traditional 70-year-old Saigon 'Ca Phe Vot' brewed?",
                    new String[]{"Vợt vải cotton dài nhúng trong siêu đất nung giữ nhiệt", "Máy pha áp suất Espresso Ý", "Bình thủy tinh Syphon chân không", "Ủ lạnh Cold Brew 24 giờ"},
                    new String[]{"Long cotton cloth filter steeped in clay pots", "Italian Espresso machine", "Vacuum Syphon", "24h Cold Brew"},
                    0,
                    "Cà phê vợt (như ở hẻm Phan Đình Phùng hay Chợ Thiếc) dùng vợt vải trụng trong siêu đất đun bếp than cho hương thơm khói mộc mạc.",
                    "Roasted grounds are steeped in a cloth sock filter inside earthenware pots over charcoal for a velvety aroma."
            ),
            new QuizQuestion(
                    "10. Nghệ thuật âm nhạc dân gian Nam Bộ nào được UNESCO vinh danh là Di sản Văn hóa Phi vật thể Đại diện Nhân loại?",
                    "10. Which Southern Vietnamese musical art is inscribed by UNESCO as Intangible Cultural Heritage?",
                    new String[]{"Nhã nhạc cung đình Huế", "Đờn ca tài tử Nam Bộ", "Dân ca Quan họ", "Không gian văn hóa Cồng chiêng"},
                    new String[]{"Hue Royal Court Music", "Don Ca Tai Tu (Southern Amateur Chamber Music)", "Quan Ho Folk Songs", "Central Highlands Gong"},
                    1,
                    "Đờn ca tài tử Nam Bộ được UNESCO vinh danh năm 2013 — cội nguồn của sân khấu Cải lương gắn liền với đất phương Nam.",
                    "Inscribed by UNESCO in 2013, Don Ca Tai Tu is the soulful chamber music tradition of Saigon and the Southern delta."
            )
    );

    private static final String[][] MOODS = {
            {"🌅 Sáng năng lượng", "🌅 Morning Vibe"},
            {"☀️ Trưa khám phá", "☀️ Midday Tour"},
            {"🌆 Chiều hoàng hôn", "🌆 Sunset Chill"},
            {"🌙 Sài Gòn Đêm", "🌙 Nightlife"}
    };

    private static final String[][] REGIONS = {
            {"all", "🌏 Tất cả vùng", "🌏 All Regions"},
            {"TP. Hồ Chí Minh", "🏙️ TP.HCM", "🏙️ HCMC"},
            {"Bình Dương", "🌳 Bình Dương", "🌳 Binh Duong"},
            {"Bà Rịa - Vũng Tàu", "🌊 Vũng Tàu", "🌊 Vung Tau"}
    };

    private boolean isQuizTab = false;
    private int selectedMoodIdx = 0;
    private int selectedRegionIdx = 0;
    private Attraction pickedPlace;
    private SpecialtyFood pickedFood;
    private final Random random = new Random();

    // Quiz state
    private int quizIndex = 0;
    private int quizScore = 0;
    private int answeredIdx = -1;

    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_discovery_quiz, container, false);

        CompactHeaderHelper.bindSubscreenToolbar(
                rootView,
                Localized.tr("🎯 Ăn gì • Đi đâu & Đố vui Văn hóa", "🎯 Combo Spinner & Heritage Quiz"),
                getNavigator(),
                null,
                null
        );

        rootView.findViewById(R.id.tab_disc_spinner).setOnClickListener(v -> {
            isQuizTab = false;
            renderTabs();
        });
        rootView.findViewById(R.id.tab_disc_quiz).setOnClickListener(v -> {
            isQuizTab = true;
            renderTabs();
        });

        rootView.findViewById(R.id.btn_spin_combo).setOnClickListener(v -> {
            spinCombo();
            Toast.makeText(requireContext(),
                    Localized.tr("🎲 Đã chọn Combo Ăn – Chơi mới cho bạn!", "🎲 Fresh Eat & Go combo picked!"),
                    Toast.LENGTH_SHORT).show();
        });

        rootView.findViewById(R.id.btn_spin_open_place).setOnClickListener(v -> {
            AppNavigator nav = getNavigator();
            if (nav != null && pickedPlace != null) nav.openAttractionDetail(pickedPlace.id);
        });

        rootView.findViewById(R.id.btn_spin_add_trip).setOnClickListener(v -> {
            if (pickedPlace == null) return;
            ResultBox<Boolean> res = TripPlanManager.toggle(pickedPlace.id);
            if (!res.isSuccess()) {
                AppNavigator nav = getNavigator();
                if (nav != null) nav.showPermissionRequiredDialog(res.getErrorMessage());
                return;
            }
            renderSpinnerPanel();
        });

        rootView.findViewById(R.id.btn_spin_open_food).setOnClickListener(v -> {
            AppNavigator nav = getNavigator();
            if (nav != null && pickedFood != null) nav.openFoodDetail(pickedFood.id);
        });

        rootView.findViewById(R.id.btn_quiz_restart).setOnClickListener(v -> {
            quizIndex = 0;
            quizScore = 0;
            answeredIdx = -1;
            renderQuizPanel();
        });

        rootView.findViewById(R.id.btn_quiz_next).setOnClickListener(v -> {
            if (answeredIdx == -1) {
                Toast.makeText(requireContext(),
                        Localized.tr("Hãy chọn 1 đáp án trước nhé!", "Please choose an answer first!"),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (quizIndex < QUIZ_BANK.size() - 1) {
                quizIndex++;
                answeredIdx = -1;
                renderQuizPanel();
            } else {
                UserDataBackupManager.recordQuizScore(quizScore);
                String msg = quizScore >= 7
                        ? Localized.tr("🎉 Xuất sắc " + quizScore + "/10! Bạn đã mở khóa danh hiệu 🎓 Học Giả Sài Gòn!",
                                "🎉 Awesome " + quizScore + "/10! Unlocked 🎓 Saigon Cultural Scholar badge!")
                        : Localized.tr("Hoàn thành " + quizScore + "/10 câu! Đạt từ 7/10 để mở huy hiệu 🎓 Học Giả Sài Gòn.",
                                "Scored " + quizScore + "/10! Reach 7/10 to unlock 🎓 Cultural Scholar.");
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
                quizIndex = 0;
                quizScore = 0;
                answeredIdx = -1;
                renderQuizPanel();
            }
        });

        spinCombo();
        renderTabs();
        return rootView;
    }

    private void renderTabs() {
        if (!isAdded() || rootView == null) return;
        TextView tabSpin = rootView.findViewById(R.id.tab_disc_spinner);
        TextView tabQuiz = rootView.findViewById(R.id.tab_disc_quiz);
        View panelSpin = rootView.findViewById(R.id.panel_disc_spinner);
        View panelQuiz = rootView.findViewById(R.id.panel_disc_quiz);

        tabSpin.setBackgroundResource(!isQuizTab ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        tabSpin.setTextColor(ContextCompat.getColor(requireContext(), !isQuizTab ? R.color.white : R.color.text_primary));
        tabQuiz.setBackgroundResource(isQuizTab ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        tabQuiz.setTextColor(ContextCompat.getColor(requireContext(), isQuizTab ? R.color.white : R.color.text_primary));

        panelSpin.setVisibility(!isQuizTab ? View.VISIBLE : View.GONE);
        panelQuiz.setVisibility(isQuizTab ? View.VISIBLE : View.GONE);

        if (!isQuizTab) renderSpinnerPanel();
        else renderQuizPanel();
    }

    private void spinCombo() {
        List<Attraction> allPlaces = AttractionRepository.getAttractions();
        List<Attraction> candidates = new ArrayList<>();
        String regFilter = REGIONS[selectedRegionIdx][0];
        for (Attraction a : allPlaces) {
            if ("all".equals(regFilter) || (a.province != null && a.province.contains(regFilter))) {
                candidates.add(a);
            }
        }
        if (candidates.isEmpty()) candidates = allPlaces;
        if (!candidates.isEmpty()) {
            pickedPlace = candidates.get(random.nextInt(candidates.size()));
        }

        List<SpecialtyFood> foods = FoodRepository.getSpecialtyFoods();
        if (!foods.isEmpty()) {
            pickedFood = foods.get(random.nextInt(foods.size()));
        }
        if (isAdded() && rootView != null) {
            renderSpinnerPanel();
        }
    }

    private void renderSpinnerPanel() {
        LinearLayout moodBox = rootView.findViewById(R.id.container_spin_moods);
        LinearLayout regBox = rootView.findViewById(R.id.container_spin_regions);
        moodBox.removeAllViews();
        regBox.removeAllViews();

        for (int i = 0; i < MOODS.length; i++) {
            final int idx = i;
            moodBox.addView(ChipViewFactory.createChip(
                    requireContext(),
                    Localized.tr(MOODS[i][0], MOODS[i][1]),
                    idx == selectedMoodIdx,
                    0,
                    () -> {
                        selectedMoodIdx = idx;
                        spinCombo();
                    }
            ));
        }

        for (int i = 0; i < REGIONS.length; i++) {
            final int idx = i;
            regBox.addView(ChipViewFactory.createSkyChip(
                    requireContext(),
                    Localized.tr(REGIONS[i][1], REGIONS[i][2]),
                    idx == selectedRegionIdx,
                    () -> {
                        selectedRegionIdx = idx;
                        spinCombo();
                    }
            ));
        }

        if (pickedPlace != null) {
            ImageView ivP = rootView.findViewById(R.id.iv_spin_place);
            TextView tvPName = rootView.findViewById(R.id.tv_spin_place_name);
            TextView tvPMeta = rootView.findViewById(R.id.tv_spin_place_meta);
            TextView tvPSum = rootView.findViewById(R.id.tv_spin_place_summary);
            TextView btnAddTrip = rootView.findViewById(R.id.btn_spin_add_trip);

            ImageLoader.loadAttraction(ivP, pickedPlace);
            tvPName.setText(Localized.tr(pickedPlace.nameVi, pickedPlace.nameEn));
            tvPMeta.setText("📍 " + pickedPlace.district + " • " + pickedPlace.province + " • 🎟️ " + pickedPlace.ticketPrice);
            tvPSum.setText(pickedPlace.shortSummary);
            boolean inTrip = TripPlanManager.contains(pickedPlace.id);
            btnAddTrip.setText(inTrip
                    ? Localized.tr("✓ Đã có trong Lịch trình", "✓ In My Trip")
                    : Localized.tr("📅 + Lịch trình", "📅 + Add to Trip"));
        }

        if (pickedFood != null) {
            ImageView ivF = rootView.findViewById(R.id.iv_spin_food);
            TextView tvFName = rootView.findViewById(R.id.tv_spin_food_name);
            TextView tvFDesc = rootView.findViewById(R.id.tv_spin_food_desc);

            ImageLoader.loadFood(ivF, pickedFood);
            String priceInfo = !pickedFood.restaurants.isEmpty()
                    ? pickedFood.restaurants.get(0).priceRange
                    : Localized.tr(pickedFood.category.labelVi, pickedFood.category.label);
            tvFName.setText(Localized.tr(pickedFood.nameVi, pickedFood.nameEn) + " • " + priceInfo);
            tvFDesc.setText(pickedFood.shortDescription);
        }
    }

    private void renderQuizPanel() {
        TextView tvBadge = rootView.findViewById(R.id.tv_quiz_badge_status);
        TextView tvProg = rootView.findViewById(R.id.tv_quiz_progress);
        TextView tvQ = rootView.findViewById(R.id.tv_quiz_question);
        LinearLayout optBox = rootView.findViewById(R.id.container_quiz_options);
        View explainBox = rootView.findViewById(R.id.box_quiz_explanation);
        TextView tvExplain = rootView.findViewById(R.id.tv_quiz_explanation);
        TextView btnNext = rootView.findViewById(R.id.btn_quiz_next);

        int best = UserDataBackupManager.getBestQuizScore();
        boolean scholar = UserDataBackupManager.hasCulturalScholarBadge();

        tvBadge.setText(scholar
                ? Localized.tr("🎓 Đã đạt danh hiệu: Học Giả Sài Gòn (Kỷ lục " + best + "/10)",
                        "🎓 Badge Unlocked: Saigon Cultural Scholar (Best " + best + "/10)")
                : Localized.tr("🏅 Thử thách Học Giả Sài Gòn (Kỷ lục hiện tại: " + best + "/10)",
                        "🏅 Saigon Scholar Challenge (Best: " + best + "/10 — Need 7/10)"));

        tvProg.setText(Localized.tr(
                "Câu " + (quizIndex + 1) + "/" + QUIZ_BANK.size() + "  •  Điểm lượt này: " + quizScore,
                "Question " + (quizIndex + 1) + "/" + QUIZ_BANK.size() + "  •  Current Score: " + quizScore));

        QuizQuestion q = QUIZ_BANK.get(quizIndex);
        tvQ.setText(Localized.tr(q.questionVi, q.questionEn));

        optBox.removeAllViews();
        float d = getResources().getDisplayMetrics().density;
        String[] prefixes = {"A. ", "B. ", "C. ", "D. "};

        for (int i = 0; i < q.optionsVi.length; i++) {
            final int optIdx = i;
            TextView optView = new TextView(requireContext());
            String label = prefixes[i] + Localized.tr(q.optionsVi[i], q.optionsEn[i]);

            if (answeredIdx != -1) {
                if (i == q.correctIdx) {
                    label = "✅ " + label;
                    optView.setBackgroundResource(R.drawable.bg_teal_mint_pill);
                    optView.setTextColor(ContextCompat.getColor(requireContext(), R.color.saigon_teal_dark));
                } else if (i == answeredIdx) {
                    label = "❌ " + label;
                    optView.setBackgroundResource(R.drawable.bg_peach_pill);
                    optView.setTextColor(ContextCompat.getColor(requireContext(), R.color.danger_red));
                } else {
                    optView.setBackgroundResource(R.drawable.bg_stat_tile);
                    optView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
                }
            } else {
                optView.setBackgroundResource(R.drawable.bg_stat_tile);
                optView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            }

            optView.setText(label);
            optView.setTextSize(13.5f);
            optView.setTypeface(null, Typeface.BOLD);
            int padH = (int) (14 * d);
            int padV = (int) (12 * d);
            optView.setPadding(padH, padV, padH, padV);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = (int) (8 * d);
            optView.setLayoutParams(lp);

            optView.setOnClickListener(v -> {
                if (answeredIdx != -1) return;
                answeredIdx = optIdx;
                if (optIdx == q.correctIdx) {
                    quizScore++;
                    UserDataBackupManager.recordQuizScore(quizScore);
                }
                renderQuizPanel();
            });
            optBox.addView(optView);
        }

        if (answeredIdx != -1) {
            explainBox.setVisibility(View.VISIBLE);
            tvExplain.setText("💡 " + Localized.tr(q.explainVi, q.explainEn));
        } else {
            explainBox.setVisibility(View.GONE);
        }

        btnNext.setText(quizIndex == QUIZ_BANK.size() - 1
                ? Localized.tr("🏆 Tổng kết & Lưu kỷ lục", "🏆 Finish & Save Record")
                : Localized.tr("Câu tiếp theo →", "Next Question →"));
    }

    private AppNavigator getNavigator() {
        return getActivity() instanceof AppNavigator ? (AppNavigator) getActivity() : null;
    }
}
