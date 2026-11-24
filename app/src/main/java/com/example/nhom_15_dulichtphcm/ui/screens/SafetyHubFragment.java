package com.example.nhom_15_dulichtphcm.ui.screens;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.nhom_15_dulichtphcm.R;
import com.example.nhom_15_dulichtphcm.data.local.UserDataBackupManager;
import com.example.nhom_15_dulichtphcm.ui.AppNavigator;
import com.example.nhom_15_dulichtphcm.ui.components.ChipViewFactory;
import com.example.nhom_15_dulichtphcm.ui.components.CompactHeaderHelper;
import com.example.nhom_15_dulichtphcm.util.Localized;
import com.example.nhom_15_dulichtphcm.util.MapLauncher;
import com.example.nhom_15_dulichtphcm.util.RideLauncher;
import com.example.nhom_15_dulichtphcm.util.SpeechHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Pure Java + XML SOS Emergency Hub, Bilingual Food Allergy / Dietary Chef Card (SQLite persisted),
 * Consulate Directory in HCMC, and Anti-Scam Safety Tips.
 */
public final class SafetyHubFragment extends Fragment {

    private static final class DietaryOption {
        final String code;
        final String chipLabelVi;
        final String chipLabelEn;
        final String chefVi;
        final String chefEn;

        DietaryOption(String code, String chipLabelVi, String chipLabelEn, String chefVi, String chefEn) {
            this.code = code;
            this.chipLabelVi = chipLabelVi;
            this.chipLabelEn = chipLabelEn;
            this.chefVi = chefVi;
            this.chefEn = chefEn;
        }
    }

    private static final DietaryOption[] DIETARY_OPTIONS = {
            new DietaryOption("peanut", "🥜 Đậu phộng", "🥜 Peanuts",
                    "dị ứng NẶNG với đậu phộng (lạc) và dầu phộng",
                    "severely allergic to peanuts & peanut oil"),
            new DietaryOption("shellfish", "🦐 Tôm cua / Hải sản", "🦐 Shellfish",
                    "dị ứng với tôm, cua và hải sản có vỏ",
                    "allergic to shrimp, crab & shellfish"),
            new DietaryOption("vegan", "🥬 Ăn chay trường", "🥬 Strictly Vegan",
                    "ăn chay hoàn toàn (xin KHÔNG dùng nước mắm, thịt, mỡ động vật)",
                    "strictly vegetarian/vegan (no fish sauce, meat broth, or lard)"),
            new DietaryOption("nomsg", "🧂 Không bột ngọt", "🧂 No MSG",
                    "không dùng bột ngọt (mì chính)",
                    "sensitive to MSG (no monosodium glutamate)"),
            new DietaryOption("gluten", "🌾 Kiêng Gluten", "🌾 Gluten-Free",
                    "dị ứng với bột mì và nước tương có chứa gluten",
                    "intolerant to wheat flour & gluten soy sauce"),
            new DietaryOption("dairy", "🥛 Kiêng Sữa bò", "🥛 Dairy-Free",
                    "dị ứng sữa bò, bơ và phô mai",
                    "lactose/dairy allergic (no condensed milk, butter, or cheese)"),
            new DietaryOption("nospicy", "🌶️ Không ăn cay", "🌶️ No Chili",
                    "không ăn được cay, xin KHÔNG bỏ ớt và tiêu",
                    "cannot eat spicy food (please no chili or black pepper)")
    };

    private static final String[][] CONSULATES = {
            {"🇺🇸 Tổng Lãnh sự quán Hoa Kỳ", "US Consulate General HCMC", "4 Lê Duẩn, Quận 1", "02835204200", "10.7831", "106.7006"},
            {"🇯🇵 Tổng Lãnh sự quán Nhật Bản", "Consulate General of Japan", "261 Điện Biên Phủ, Quận 3", "02839333510", "10.7804", "106.6883"},
            {"🇰🇷 Tổng Lãnh sự quán Hàn Quốc", "Consulate General of Korea", "107 Nguyễn Du, Quận 1", "02838225757", "10.7746", "106.6953"},
            {"🇫🇷 Tổng Lãnh sự quán Pháp", "Consulate General of France", "27 Nguyễn Thị Minh Khai, Quận 1", "02835206800", "10.7839", "106.7014"},
            {"🇦🇺 Tổng Lãnh sự quán Úc", "Australian Consulate-General", "20th Fl, Vincom Center, 47 Lý Tự Trọng, Q.1", "02835218100", "10.7781", "106.7018"},
            {"🇬🇧 Tổng Lãnh sự quán Vương quốc Anh", "British Consulate General", "25 Lê Duẩn, Quận 1", "02838298433", "10.7824", "106.7001"},
            {"🇸🇬 Tổng Lãnh sự quán Singapore", "Consulate-General of Singapore", "Saigon Centre Tower 2, 67 Lê Lợi, Q.1", "02838225174", "10.7733", "106.7005"}
    };

    private static final String[][] SAFETY_TIPS = {
            {"🚖 Chỉ đi Taxi chính hãng hoặc đặt qua ứng dụng",
                    "Always book Grab, Xanh SM EV, Be, Vinasun (028 38 27 27 27), or Mai Linh (1055) with clear fare meters."},
            {"🥥 Hỏi kỹ giá trước khi chụp ảnh gánh hàng rong / dừa tươi",
                    "Politely confirm the exact VND price before accepting street coconuts, shoe-shining, or souvenir photos."},
            {"🦀 Chọn quán hải sản niêm yết giá rõ ràng theo kg",
                    "At Vung Tau or Saigon seafood streets, verify per-kg prices on the menu board before ordering."},
            {"🚶 Sang đường đều chân, giơ tay xin đường rõ ràng",
                    "Walk at a steady, predictable pace when crossing streets — motorbike riders will smoothly weave around you."},
            {"📱 Giữ chặt điện thoại khi đứng sát lề đường chụp ảnh",
                    "Hold smartphones with both hands or a wrist strap when standing near busy curbsides."}
    };

    private View rootView;
    private String currentChefVietnameseSpeech = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_safety_hub, container, false);

        CompactHeaderHelper.bindSubscreenToolbar(
                rootView,
                Localized.tr("🚨 Khẩn cấp SOS & Thẻ Dị ứng", "🚨 SOS & Allergy Safety Hub"),
                getNavigator(),
                null,
                null
        );

        rootView.findViewById(R.id.btn_hub_113).setOnClickListener(v -> RideLauncher.callHotline(requireContext(), "113"));
        rootView.findViewById(R.id.btn_hub_114).setOnClickListener(v -> RideLauncher.callHotline(requireContext(), "114"));
        rootView.findViewById(R.id.btn_hub_115).setOnClickListener(v -> RideLauncher.callHotline(requireContext(), "115"));
        rootView.findViewById(R.id.btn_hub_1022).setOnClickListener(v -> RideLauncher.callHotline(requireContext(), "0281022"));

        rootView.findViewById(R.id.btn_speak_allergy_card).setOnClickListener(v -> {
            AppNavigator nav = getNavigator();
            SpeechHelper sh = nav != null ? nav.getSpeechHelper() : null;
            if (sh != null && !currentChefVietnameseSpeech.isEmpty()) {
                sh.speakVietnamese(currentChefVietnameseSpeech);
            }
        });

        rootView.findViewById(R.id.btn_share_allergy_card).setOnClickListener(v -> {
            TextView tvVi = rootView.findViewById(R.id.tv_allergy_vi_card);
            TextView tvEn = rootView.findViewById(R.id.tv_allergy_en_card);
            MapLauncher.shareText(
                    requireContext(),
                    "Thẻ Dị ứng / Dietary Card",
                    tvVi.getText().toString() + "\n\n" + tvEn.getText().toString()
            );
        });

        renderAllergyCard();
        renderConsulatesAndTips();
        return rootView;
    }

    private void renderAllergyCard() {
        if (!isAdded() || rootView == null) return;
        LinearLayout chipBox = rootView.findViewById(R.id.container_allergy_chips);
        TextView tvVi = rootView.findViewById(R.id.tv_allergy_vi_card);
        TextView tvEn = rootView.findViewById(R.id.tv_allergy_en_card);

        Set<String> selected = UserDataBackupManager.getSelectedAllergyCodes();
        chipBox.removeAllViews();

        List<String> viParts = new ArrayList<>();
        List<String> enParts = new ArrayList<>();

        for (DietaryOption opt : DIETARY_OPTIONS) {
            boolean isSel = selected.contains(opt.code);
            if (isSel) {
                viParts.add(opt.chefVi);
                enParts.add(opt.chefEn);
            }
            chipBox.addView(ChipViewFactory.createChip(
                    requireContext(),
                    Localized.tr(opt.chipLabelVi, opt.chipLabelEn),
                    isSel,
                    0,
                    () -> {
                        UserDataBackupManager.toggleAllergyCode(opt.code);
                        renderAllergyCard();
                    }
            ));
        }

        if (viParts.isEmpty()) {
            currentChefVietnameseSpeech = "Xin chào anh chị chủ quán! Xin vui lòng cho tôi món ăn ít dầu mỡ và hợp vệ sinh. Cảm ơn rất nhiều!";
            tvVi.setText("Xin chào nhà bếp! Chạm chọn các mục dị ứng / ăn chay phía trên (Đậu phộng, Hải sản, Ăn chay, Không cay...) để tạo thẻ cảnh báo tiếng Việt cỡ lớn.");
            tvEn.setText("Tap one or more dietary/allergy chips above to generate an instant high-visibility Vietnamese flashcard & voice message for restaurant staff.");
        } else {
            currentChefVietnameseSpeech = "Xin chào đầu bếp! Tôi bị " + String.join(", đồng thời ", viParts)
                    + ". Xin vui lòng kiểm tra kỹ nguyên liệu giúp tôi. Xin chân thành cảm ơn!";
            tvVi.setText("⚠️ " + currentChefVietnameseSpeech);
            tvEn.setText("🇬🇧 Chef Note: I am " + String.join("; and ", enParts) + ". Please kindly prepare my meal safely.");
        }
    }

    private void renderConsulatesAndTips() {
        LinearLayout consulateBox = rootView.findViewById(R.id.container_consulates_list);
        LinearLayout tipsBox = rootView.findViewById(R.id.container_safety_tips);
        consulateBox.removeAllViews();
        tipsBox.removeAllViews();

        float d = getResources().getDisplayMetrics().density;

        for (String[] c : CONSULATES) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackgroundResource(R.drawable.bg_stat_tile);
            int p = (int) (11 * d);
            row.setPadding(p, p, p, p);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = (int) (8 * d);
            row.setLayoutParams(lp);

            LinearLayout col = new LinearLayout(requireContext());
            col.setOrientation(LinearLayout.VERTICAL);
            col.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView tvName = new TextView(requireContext());
            tvName.setText(Localized.tr(c[0], c[1]));
            tvName.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            tvName.setTextSize(13.5f);
            tvName.setTypeface(null, Typeface.BOLD);
            col.addView(tvName);

            TextView tvAddr = new TextView(requireContext());
            tvAddr.setText("📍 " + c[2] + "  •  ☎️ " + c[3]);
            tvAddr.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            tvAddr.setTextSize(11.5f);
            col.addView(tvAddr);
            row.addView(col);

            TextView btnCall = new TextView(requireContext());
            btnCall.setText("📞");
            int sz = (int) (34 * d);
            LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(sz, sz);
            cLp.setMarginStart((int) (6 * d));
            btnCall.setLayoutParams(cLp);
            btnCall.setGravity(Gravity.CENTER);
            btnCall.setBackgroundResource(R.drawable.bg_peach_pill);
            btnCall.setOnClickListener(v -> RideLauncher.callHotline(requireContext(), c[3]));
            row.addView(btnCall);

            TextView btnMap = new TextView(requireContext());
            btnMap.setText("🗺️");
            LinearLayout.LayoutParams mLp = new LinearLayout.LayoutParams(sz, sz);
            mLp.setMarginStart((int) (6 * d));
            btnMap.setLayoutParams(mLp);
            btnMap.setGravity(Gravity.CENTER);
            btnMap.setBackgroundResource(R.drawable.bg_lavender_pill);
            btnMap.setOnClickListener(v -> {
                AppNavigator nav = getNavigator();
                double lat = Double.parseDouble(c[4]);
                double lng = Double.parseDouble(c[5]);
                if (nav != null) nav.openMapAt(lat, lng, Localized.tr(c[0], c[1]));
            });
            row.addView(btnMap);

            consulateBox.addView(row);
        }

        for (String[] tip : SAFETY_TIPS) {
            LinearLayout card = new LinearLayout(requireContext());
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.bg_stat_tile);
            int p = (int) (11 * d);
            card.setPadding(p, p, p, p);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = (int) (7 * d);
            card.setLayoutParams(lp);

            TextView tvT = new TextView(requireContext());
            tvT.setText(tip[0]);
            tvT.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            tvT.setTextSize(13f);
            tvT.setTypeface(null, Typeface.BOLD);
            card.addView(tvT);

            TextView tvS = new TextView(requireContext());
            tvS.setText(tip[1]);
            tvS.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            tvS.setTextSize(12f);
            tvS.setPadding(0, (int) (3 * d), 0, 0);
            card.addView(tvS);

            tipsBox.addView(card);
        }
    }

    private AppNavigator getNavigator() {
        return getActivity() instanceof AppNavigator ? (AppNavigator) getActivity() : null;
    }
}
