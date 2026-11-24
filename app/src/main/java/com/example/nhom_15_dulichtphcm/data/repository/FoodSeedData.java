package com.example.nhom_15_dulichtphcm.data.repository;

import com.example.nhom_15_dulichtphcm.R;
import com.example.nhom_15_dulichtphcm.data.model.FoodCategory;
import com.example.nhom_15_dulichtphcm.data.model.Restaurant;
import com.example.nhom_15_dulichtphcm.data.model.SpecialtyFood;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class FoodSeedData {
    private FoodSeedData() {}

    public static List<SpecialtyFood> createDefaultFoods() {
        List<SpecialtyFood> list = new ArrayList<>();
        list.add(new SpecialtyFood(
            "com_tam",
            "Saigon Broken Rice (Cơm Tấm)",
            "Cơm Tấm Sài Gòn",
            FoodCategory.RICE_AND_NOODLES,
            "https://images.unsplash.com/photo-1576618148400-f54bed99fcfd?auto=format&fit=crop&w=1200&q=80",
            R.drawable.food_com_tam,
            "Saigon's quintessential soul food: fragrant broken rice grains crowned with lemongrass chargrilled pork chop and savoury scallion oil.",
            "Originating during hard economic times in the Mekong Delta, farmers gathered 'tấm' – the fractured, broken rice kernels discarded during the milling process – to cook for their households. In the mid-20th century, Saigon street vendors elevated this humble grain into an iconic culinary sensation by pairing it with French-influenced pork chops, savory steamed egg-pork meatloaf (chả trứng), shredded pork skin (bì), and crispy fried pork lardons.",
            "Pour several spoonfuls of sweet-and-sour spiced fish sauce (nước mắm chua ngọt) directly over the warm rice. Mix a pinch of fresh red chili if you like heat, and alternate bites of charred pork with crunchy pickled daikon and carrot to cleanse the palate.",
            Arrays.asList("Broken rice (Gạo tấm)", "Charcoal grilled pork chop (Sườn nướng)", "Steamed egg meatloaf (Chả trứng)", "Shredded pork skin (Bì)", "Scallion oil (Mỡ hành)", "Sweet garlic-chili fish sauce"),
            Arrays.asList(
                new Restaurant("com_tam_ba_ghien", "Cơm Tấm Ba Ghiền", "84 Đặng Văn Ngữ, Phường 10, Quận Phú Nhuận, TP. Hồ Chí Minh", "Phú Nhuận District", 10.79374, 106.67382, "07:00 - 21:00", "70,000 - 120,000 VND (~$3 - $5 USD)", "Michelin Bib Gourmand awardee famous for gargantuan, juicy charcoal-grilled pork chops marinated in lemongrass and condensed milk.", 4.8),
                new Restaurant("com_tam_phuc_loc_tho", "Cơm Tấm Phúc Lộc Thọ", "223 Nguyễn Thị Thập, Phường Tân Phú, Quận 7, TP. Hồ Chí Minh", "District 7", 10.74128, 106.71214, "06:00 - 22:00", "45,000 - 85,000 VND (~$2 - $3.5 USD)", "Extremely clean, modern air-conditioned ambiance with consistent Saigon traditional flavor and crispy fried scallion rolls.", 4.6),
                new Restaurant("com_tam_calmette", "Cơm Tấm Calmette (Quận 1)", "83 Calmette, Phường Nguyễn Thái Bình, Quận 1, TP. Hồ Chí Minh", "District 1", 10.76865, 106.69742, "06:30 - 20:00", "50,000 - 90,000 VND (~$2 - $4 USD)", "Conveniently nestled in District 1 near Ben Thanh Market; exceptional tender rib chops and rich steamed egg meatloaf.", 4.7)
            )
        ));
        list.add(new SpecialtyFood(
            "banh_mi",
            "Saigon Crispy Baguette (Bánh Mì Sài Gòn)",
            "Bánh Mì Sài Gòn",
            FoodCategory.STREET_SNACKS,
            "https://images.unsplash.com/photo-1626804475297-41608ea09aeb?auto=format&fit=crop&w=1200&q=80",
            R.drawable.food_banh_mi,
            "World-acclaimed crispy airy baguette filled with rich liver pâté, Vietnamese cold cuts, pickled vegetables, and fragrant cilantro.",
            "The French introduced the wheat baguette to Indochina in the 19th century, then called 'pain de mie'. Saigon bakers modified the dough by adding rice flour to achieve a feather-light, cloud-soft interior and an extra-crisp crust that crackles at first bite. By replacing European butter and cheese with liver pâté, cha lua (pork sausage), cucumber, and coriander, Saigon created the ultimate street food marvel.",
            "Always eat it hot and fresh off the toaster! Ask for 'ít cay' (less spicy) or 'không ớt' (no chili) if you are sensitive to fresh bird's eye chilies. Pair with an iced coffee for the classic Saigon breakfast ritual.",
            Arrays.asList("Crispy airy baguette", "French-style pork liver pâté", "Mayonnaise/egg yolk butter", "Chả lụa (Vietnamese ham)", "Pickled daikon & carrots", "Fresh coriander & cucumber", "Maggi seasoning sauce"),
            Arrays.asList(
                new Restaurant("banh_mi_huynh_hoa", "Bánh Mì Huỳnh Hoa", "26 Lê Thị Riêng, Phường Phạm Ngũ Lão, Quận 1, TP. Hồ Chí Minh", "District 1", 10.77123, 106.69248, "11:00 - 21:00", "68,000 VND (~$2.80 USD)", "Saigon's heavyweight champion! Packed with over 10 layers of artisan charcuterie, ham, buttery pate, and pork floss.", 4.9),
                new Restaurant("banh_mi_bay_ho", "Bánh Mì Bảy Hổ (Since 1930)", "19 Huỳnh Khương Ninh, Phường Đa Kao, Quận 1, TP. Hồ Chí Minh", "District 1", 10.79042, 106.69611, "06:00 - 11:30 & 13:30 - 19:00", "25,000 - 35,000 VND (~$1 - $1.5 USD)", "Over 90 years of culinary heritage; famous for freshly simmered juicy roast pork marinated in traditional family herbal broth.", 4.7),
                new Restaurant("banh_mi_nhu_lan", "Bánh Mì Như Lan", "64 Hàm Nghi, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh", "District 1", 10.77189, 106.70428, "05:00 - 23:00", "35,000 - 60,000 VND (~$1.5 - $2.5 USD)", "An enduring institution right across from Bitexco Tower; fresh baked breads around the clock with delicious roast pork and sausage.", 4.5)
            )
        ));
        list.add(new SpecialtyFood(
            "pho_saigon",
            "Saigon-Style Beef Pho (Phở Sài Gòn)",
            "Phở Bò Sài Gòn",
            FoodCategory.RICE_AND_NOODLES,
            "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?auto=format&fit=crop&w=1200&q=80",
            R.drawable.food_pho_saigon,
            "Rich, aromatic beef bone broth simmered with cinnamon and star anise, accompanied by fresh herbs, bean sprouts, and sweet hoisin sauce.",
            "While Pho was born in Northern Vietnam (Nam Dinh and Hanoi), Southerners welcomed it in the 1950s and imbued it with Southern abundance: a slightly sweeter, richer broth simmered for 18 hours with charred ginger, accompanied by a lavish basket of fresh herbs (sawtooth coriander, Thai basil) and hoisin-sriracha dipping sauces.",
            "Taste the natural broth first with a spoon before adding condiments. Then tear Thai basil leaves, squeeze fresh calamansi or lime, dip beef slices in a small dish mixed with hoisin (tương đen) and chili sauce (tương ớt).",
            Arrays.asList("Flat rice noodles (Bánh phở)", "Slow-simmered beef marrow bone broth", "Tender sliced brisket, flank & rare beef", "Sawtooth herb & Thai basil", "Crunchy bean sprouts", "Hoisin & Sriracha sauce"),
            Arrays.asList(
                new Restaurant("pho_le", "Phở Lệ (Quận 5 - Chinatown)", "415 Nguyễn Trãi, Phường 7, Quận 5, TP. Hồ Chí Minh", "District 5 (Chợ Lớn)", 10.75471, 106.67139, "06:00 - 01:00 (Late Night)", "75,000 - 110,000 VND (~$3 - $4.5 USD)", "Michelin Bib Gourmand awardee; intensely flavorful broth with springy handmade beef balls (bò viên) and silky rare beef flank.", 4.9),
                new Restaurant("pho_hoa_pasteur", "Phở Hòa Pasteur", "260C Pasteur, Phường Võ Thị Sáu, Quận 3, TP. Hồ Chí Minh", "District 3", 10.78761, 106.68972, "06:00 - 22:30", "85,000 - 120,000 VND (~$3.5 - $5 USD)", "Historic pho palace operating since 1968, revered by generations of international food critics and travelers.", 4.7)
            )
        ));
        list.add(new SpecialtyFood(
            "hu_tieu",
            "Nam Vang Noodle Soup (Hủ Tiếu Nam Vang)",
            "Hủ Tiếu Nam Vang",
            FoodCategory.RICE_AND_NOODLES,
            "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?auto=format&fit=crop&w=1200&q=80",
            R.drawable.food_hu_tieu,
            "Chewy translucent tapioca rice noodles served in pork bone and dried squid broth, topped with minced pork, fresh prawns, and quail eggs.",
            "A cross-cultural masterpiece reflecting Saigon's cosmopolitan heritage. Originating from Cambodian-Chinese merchants (Phnom Penh is called 'Nam Vang' in Vietnamese), it was modified by Vietnamese cooks who added fragrant fried garlic, Chinese celery, chives, and ground pork.",
            "Can be ordered 'nước' (with soup) or 'khô' (dry-tossed in a dark umami sweet soy sauce with soup served in a separate bowl). The dry version ('hủ tiếu khô') is the locals' absolute favorite!",
            Arrays.asList("Clear chewy noodles", "Rich pork and dried squid broth", "Minced pork & pork liver", "Fresh river prawns", "Quail eggs", "Golden crispy garlic oil", "Chinese celery & chives"),
            Arrays.asList(
                new Restaurant("hu_tieu_nhan_quan", "Hủ Tiếu Nam Vang Nhân Quán", "122E Bùi Thị Xuân, Phường Phạm Ngũ Lão, Quận 1, TP. Hồ Chí Minh", "District 1", 10.77015, 106.68832, "06:30 - 23:00", "80,000 - 110,000 VND (~$3.5 - $4.5 USD)", "Pristine sweet broth brewed with dried squid and rock sugar; famous for their signature dry noodles loaded with fresh tiger shrimp.", 4.8),
                new Restaurant("hu_tieu_thanh_dat", "Hủ Tiếu Nam Vang Thành Đạt", "34 Cô Bắc, Phường Cầu Ông Lãnh, Quận 1, TP. Hồ Chí Minh", "District 1", 10.76342, 106.69485, "Open 24/7", "60,000 - 85,000 VND (~$2.5 - $3.5 USD)", "Open 24 hours daily; vibrant local scene with piping hot bowls served within minutes.", 4.6)
            )
        ));
        list.add(new SpecialtyFood(
            "banh_xeo",
            "Sizzling Crispy Pancake (Bánh Xèo Nam Bộ)",
            "Bánh Xèo Sài Gòn",
            FoodCategory.STREET_SNACKS,
            "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&w=1200&q=80",
            R.drawable.food_banh_xeo,
            "Giant golden turmeric-infused crispy rice crepe folded over pork belly, shrimp, mung beans, and crunchy bean sprouts.",
            "The name 'xèo' is onomatopoeic, mimicking the loud sizzling sizzle sound when the yellow batter hits the smoking-hot cast iron skillet with pork fat. The Southern Vietnamese version is grand and broad, measuring nearly 40cm across, with thin, wafer-crisp edges made using coconut milk.",
            "Tear a piece of the crispy crepe with your hands, wrap it inside a large mustard green leaf (cải bẹ xanh) along with aromatic herbs, roll it up like a burrito, and dunk deeply into tangy garlic fish sauce.",
            Arrays.asList("Turmeric coconut rice flour batter", "Sliced pork belly", "Fresh prawns", "Steamed mung beans", "Bean sprouts", "Fresh mustard greens & perilla leaves"),
            Arrays.asList(
                new Restaurant("banh_xeo_46a", "Bánh Xèo 46A Đinh Công Tráng", "46A Đinh Công Tráng, Phường Tân Định, Quận 1, TP. Hồ Chí Minh", "District 1", 10.78912, 106.69188, "10:00 - 21:00", "100,000 - 180,000 VND (~$4 - $7.5 USD)", "Legendary open-air courtyard where master cooks fry dozens of crepes over wood-fired stoves. Featured by Anthony Bourdain.", 4.7),
                new Restaurant("banh_xeo_an_la_ghien", "Bánh Xèo Ăn Là Ghiền", "74 Sương Nguyệt Ánh, Phường Bến Thành, Quận 1, TP. Hồ Chí Minh", "District 1", 10.77192, 106.69041, "09:00 - 22:00", "70,000 - 130,000 VND (~$3 - $5 USD)", "Diverse Southern fillings including enoki mushrooms, coconut heart (củ hủ dừa), and crispy seafood.", 4.5)
            )
        ));
        list.add(new SpecialtyFood(
            "ca_phe_vot",
            "Saigon Racquet Coffee & Iced Milk Coffee",
            "Cà Phê Vợt & Cà Phê Sữa Đá Sài Gòn",
            FoodCategory.BEVERAGES_DESSERTS,
            "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?auto=format&fit=crop&w=1200&q=80",
            R.drawable.food_ca_phe_vot,
            "Potent Robusta coffee brewed through cloth racquets over charcoal embers, poured over sweetened condensed milk and crushed ice.",
            "Before French drip metal filters ('phin') became ubiquitous, Saigon cafes in the 1930s brewed ground coffee inside long fabric cloth filters ('vợt') immersed in boiling water inside clay pots kept hot on charcoal stoves. This traditional slow-extraction yields an earthy, intensely aromatic and velvety coffee experience.",
            "Stir the thick layer of condensed milk from the bottom until it turns caramel-golden with the ice. Sip slowly while sitting on low plastic stools watching Saigon scooters zip past.",
            Arrays.asList("Highland Robusta coffee beans", "Cloth filter racquet", "Charcoal simmering water", "Sweetened condensed milk", "Crushed ice"),
            Arrays.asList(
                new Restaurant("ca_phe_cheo_leo", "Cà Phê Vợt Cheo Leo (Since 1938)", "109/36 Nguyễn Thiện Thuật, Phường 2, Quận 3, TP. Hồ Chí Minh", "District 3", 10.76722, 106.68115, "05:15 - 22:00", "18,000 - 30,000 VND (~$0.80 - $1.2 USD)", "Saigon's oldest operational coffee house (over 85 years), retaining vintage music, wood-fired pots, and nostalgic neighborhood warmth.", 4.9),
                new Restaurant("ca_phe_vot_phan_dinh_phung", "Cà Phê Vợt Phan Đình Phùng", "330 Phan Đình Phùng, Phường 1, Quận Phú Nhuận, TP. Hồ Chí Minh", "Phú Nhuận District", 10.79782, 106.68532, "Open 24/7 (Never sleeps!)", "15,000 - 25,000 VND (~$0.60 - $1.0 USD)", "Legendary 24-hour street stall where night owls, travelers, and artists gather under the canopy of Saigon night sky.", 4.8)
            )
        ));
        list.add(new SpecialtyFood(
            "oc_saigon",
            "Saigon Street Seafood & Sea Snails (Ốc Sài Gòn)",
            "Ốc & Hải Sản Đường Phố Sài Gòn",
            FoodCategory.SEAFOOD,
            "https://images.unsplash.com/photo-1534422298391-e4f8c172dddb?auto=format&fit=crop&w=1200&q=80",
            R.drawable.food_oc_saigon,
            "Vibrant evening street feast featuring sweet snails, scallops, and crab claws stir-fried with salted egg sauce or tamarind glaze.",
            "Eating 'ốc' is the defining social ritual of Saigon nightlife ('nhậu'). Friends and coworkers pull up tiny red plastic stools along sidewalk alleyways, chatting over plates of sizzling clams steamed with lemongrass, snails tossed in toasted garlic or chili butter.",
            "Use a small metal pin to extract the snail meat, dip it in chili-lime salt (muối tiêu chanh), and mop up the savory butter-garlic sauce with a warm loaf of bread.",
            Arrays.asList("Various freshwater & sea snails", "Salted egg yolk sauce (sốt trứng muối)", "Garlic chili butter", "Tamarind glaze (sốt me)", "Vietnamese coriander (rau răm)"),
            Arrays.asList(
                new Restaurant("oc_dao", "Quán Ốc Đào (Nguyễn Trãi)", "212B/C79 Nguyễn Trãi, Phường Nguyễn Cư Trinh, Quận 1, TP. Hồ Chí Minh", "District 1", 10.76388, 106.68729, "10:30 - 22:00", "70,000 - 150,000 VND per plate (~$3 - $6 USD)", "Michelin Selected culinary treasure; sensational grilled razor clams with morning glory and rich salted egg snails.", 4.8),
                new Restaurant("oc_oanh", "Quán Ốc Oanh (Phố Ẩm Thực Vĩnh Khánh)", "534 Vĩnh Khánh, Phường 8, Quận 4, TP. Hồ Chí Minh", "District 4 (Food Street)", 10.75841, 106.70295, "15:00 - 24:00", "80,000 - 180,000 VND (~$3.5 - $7 USD)", "Heart of Saigon's wildest food street in District 4; electric open-air atmosphere with mouth-watering spicy grilled crab claws.", 4.7)
            )
        ));
        list.add(new SpecialtyFood(
            "banh_khot_vung_tau",
            "Vung Tau Crispy Mini Pancakes (Bánh Khọt Vũng Tàu)",
            "Bánh Khọt Vũng Tàu",
            FoodCategory.STREET_SNACKS,
            "https://images.unsplash.com/photo-1548013146-72479768bada?auto=format&fit=crop&w=1200&q=80",
            R.drawable.food_banh_khot,
            "Iconic sizzling golden mini rice cakes topped with fresh ocean shrimp, savory shrimp dust, wrapped in crisp mustard greens and dipped in sweet fish sauce.",
            "Originating from the coastal fishermen of Vung Tau and heavily inspired by Central Vietnamese bánh căn, bánh khọt is fried in small earthen cast-iron dimpled pans with bubbling oil and coconut milk. The bottom turns extraordinarily crispy while the top remains soft and succulent with whole fresh shrimp straight from the fishing boats.",
            "Wrap a hot crispy cake inside a large mustard green (cải bẹ xanh) leaf, add leafy lettuce and fresh herbs (mint, perilla), crown with crunchy shredded green papaya, and douse generously in sweet-sour garlic chili fish sauce.",
            Arrays.asList("Rice flour & turmeric", "Coconut cream", "Fresh sea shrimp (Tôm biển tươi)", "Dried shrimp powder (Bột tôm cháy)", "Scallion oil", "Mustard greens & herbs", "Sweet garlic fish sauce with pickled papaya"),
            Arrays.asList(
                new Restaurant("banh_khot_goc_vu_sua", "Bánh Khọt Gốc Vú Sữa Vũng Tàu", "14 Nguyễn Trường Tộ, Phường 2, TP. Vũng Tàu, Bà Rịa - Vũng Tàu", "TP. Vũng Tàu", 10.34215, 107.07842, "06:00 - 21:00", "60,000 - 80,000 VND (~$2.5 - $3.5 USD)", "The undisputed legend of Vung Tau with over 30 years of history; watch theatrical charcoal fires frying hundreds of crispy cakes simultaneously.", 4.8),
                new Restaurant("banh_khot_co_ba_saigon", "Bánh Khọt Cô Ba Vũng Tàu (Saigon Branch)", "102 Cao Thắng, Phường 4, Quận 3, TP. Hồ Chí Minh", "District 3", 10.77112, 106.68124, "07:00 - 22:00", "75,000 - 120,000 VND (~$3 - $5 USD)", "Vung Tau's premier heritage brand brought to the heart of Saigon; elegant air-conditioned heritage decor with jumbo tiger prawn cakes.", 4.7)
            )
        ));
        list.add(new SpecialtyFood(
            "banh_beo_bi_binh_duong",
            "Binh Duong Shredded Pork Rice Cakes (Bánh Bèo Bì Chợ Búng)",
            "Bánh Bèo Bì Bình Dương & Gỏi Măng Cụt",
            FoodCategory.STREET_SNACKS,
            "https://images.unsplash.com/photo-1509062522246-3755977927d7?auto=format&fit=crop&w=1200&q=80",
            R.drawable.food_banh_beo_bi,
            "Century-old National Intangible Cultural Heritage of Binh Duong: tender steamed rice discs blanketed with savory seasoned shredded pork skin and fragrant roasted rice powder.",
            "Created over 110 years ago at Cho Bung (Bung Market, Thuan An, Binh Duong) by the My Lien family around 1910. The dish was officially honored by the Vietnam Record Organization in the Top 50 Special Dishes of Vietnam and recognized as National Intangible Cultural Heritage. The pairing of tender steamed rice cake, savory pork belly bì marinated in garlic and five-spice, and seasonal mangosteen chicken salad represents the culinary soul of Binh Duong.",
            "Pour savory warm dipping fish sauce laced with pickled carrots and radishes all over the plate. Scoop a full bite combining the bouncy rice cake, crunchy shredded bì, and crispy fried mung beans.",
            Arrays.asList("Steamed rice flour cake (Bánh bèo)", "Seasoned pork belly & skin (Bì thịt heo)", "Roasted rice powder (Thính gạo thơm)", "Mashed mung beans (Đậu xanh tán nhuyễn)", "Scallion oil & fried shallots", "Special light garlic-chili dipping broth"),
            Arrays.asList(
                new Restaurant("banh_beo_my_lien", "Bánh Bèo Bì Mỹ Liên (Since 1910 - Chợ Búng)", "01 ĐT745, Phường An Thạnh, TP. Thuận An, Bình Dương", "TP. Thuận An, Bình Dương", 10.93245, 106.67891, "06:30 - 21:00", "40,000 - 65,000 VND (~$1.6 - $2.7 USD)", "Over 110 years of unbroken culinary tradition; Vietnam National Heritage specialty with unmatched savory garlic shredded pork bì.", 4.9),
                new Restaurant("goi_mang_cut_lai_thieu", "Quán Vườn Xưa Lái Thiêu (Gỏi Gà Măng Cụt)", "Khu phố Hưng Thọ, Phường Hưng Định, TP. Thuận An, Bình Dương", "TP. Thuận An, Bình Dương", 10.90824, 106.68953, "08:00 - 21:00", "250,000 - 350,000 VND / đĩa lớn (Shareable 3-4 pax)", "The viral sensational chicken salad made with freshly peeled crisp sweet green mangosteen directly from Lai Thieu riverside orchards.", 4.8)
            )
        ));
        return Collections.unmodifiableList(list);
    }
}
