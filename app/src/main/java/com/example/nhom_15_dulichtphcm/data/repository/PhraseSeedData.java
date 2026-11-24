package com.example.nhom_15_dulichtphcm.data.repository;

import com.example.nhom_15_dulichtphcm.data.model.Phrase;
import com.example.nhom_15_dulichtphcm.data.model.PhraseCategory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PhraseSeedData {
    private PhraseSeedData() {}

    public static List<Phrase> createDefaultPhrases() {
        List<Phrase> list = new ArrayList<>();
        list.add(new Phrase(
            "p_greet_1",
            "Hello / Hi",
            "Xin chào!",
            "Xin chao!",
            "Sin chow!",
            "Polite and friendly greeting for anyone you meet in Vietnam.",
            PhraseCategory.GREETING,
            false,
            false
        ));
        list.add(new Phrase(
            "p_greet_2",
            "How are you?",
            "Bạn có khỏe không?",
            "Ban co khoe khong?",
            "Bahn caw kweh khohng?",
            "Friendly inquiry about someone's wellbeing.",
            PhraseCategory.GREETING,
            false,
            false
        ));
        list.add(new Phrase(
            "p_greet_3",
            "I'm fine, thanks",
            "Cảm ơn, tôi khỏe.",
            "Cam on, toi khoe.",
            "Kahm uhn, toy kweh.",
            "Standard positive reply when asked how you are.",
            PhraseCategory.GREETING,
            false,
            false
        ));
        list.add(new Phrase(
            "p_greet_4",
            "I'm not well, thanks",
            "Tôi không khỏe lắm.",
            "Toi khong khoe lam.",
            "Toy khohng kweh lahm.",
            "Use if you are feeling under the weather or exhausted.",
            PhraseCategory.GREETING,
            false,
            false
        ));
        list.add(new Phrase(
            "p_greet_5",
            "Thank you very much!",
            "Cảm ơn bạn rất nhiều!",
            "Cam on ban rat nhieu!",
            "Kahm uhn bahn ruht nyew!",
            "Express heartfelt gratitude to drivers, servers, and locals.",
            PhraseCategory.GREETING,
            false,
            false
        ));
        list.add(new Phrase(
            "p_greet_6",
            "Excuse me / Sorry",
            "Xin lỗi...",
            "Xin loi...",
            "Sin loy...",
            "Essential for getting polite attention or apologizing.",
            PhraseCategory.GREETING,
            false,
            false
        ));
        list.add(new Phrase(
            "p_greet_7",
            "You're welcome / No problem",
            "Không có chi / Không sao đâu!",
            "Khong co chi / Khong sao dau!",
            "Khohng caw chee / Khohng sow doh!",
            "Standard polite reply when someone thanks you.",
            PhraseCategory.GREETING,
            false,
            false
        ));
        list.add(new Phrase(
            "p_greet_8",
            "Goodbye, see you again!",
            "Tạm biệt, hẹn gặp lại!",
            "Tam biet, hen gap lai!",
            "Tahm byet, hen gahp lye!",
            "Friendly farewell when parting ways.",
            PhraseCategory.GREETING,
            false,
            false
        ));
        list.add(new Phrase(
            "p_shop_1",
            "How much does this cost?",
            "Cái này bao nhiêu tiền?",
            "Cai nay bao nhieu tien?",
            "Kye nye bow nyew tyen?",
            "The single most useful phrase when shopping at markets or boutiques.",
            PhraseCategory.SHOPPING,
            false,
            false
        ));
        list.add(new Phrase(
            "p_shop_2",
            "Too expensive! Can you discount a bit?",
            "Đắt quá! Bớt một chút được không?",
            "Dat qua! Bot mot chut duoc khong?",
            "Duht quah! Buht moht choot duw-uhk khohng?",
            "Bargaining with good humor is part of Saigon market culture. Always smile!",
            PhraseCategory.SHOPPING,
            false,
            false
        ));
        list.add(new Phrase(
            "p_shop_3",
            "I will take this one",
            "Tôi lấy cái này nhé.",
            "Toi lay cai nay nhe.",
            "Toy lay kye nye nyeh.",
            "Confirming your purchase when you agree on the price.",
            PhraseCategory.SHOPPING,
            false,
            false
        ));
        list.add(new Phrase(
            "p_shop_4",
            "Can I pay by credit card?",
            "Có thể thanh toán bằng thẻ không?",
            "Co the thanh toan bang the khong?",
            "Caw theh thahn twahn bahng theh khohng?",
            "Ask before ordering or purchasing at shops.",
            PhraseCategory.SHOPPING,
            false,
            false
        ));
        list.add(new Phrase(
            "p_dine_1",
            "May I see the menu, please?",
            "Cho tôi xem thực đơn với!",
            "Cho toi xem thuc don voi!",
            "Chaw toy sem took duhn vuh-ee!",
            "Say this upon sitting down at any restaurant or cafe.",
            PhraseCategory.DINING,
            false,
            false
        ));
        list.add(new Phrase(
            "p_dine_2",
            "No chili, please!",
            "Không bỏ ớt nhé!",
            "Khong bo ot nhe!",
            "Khohng baw uht nyeh!",
            "Vital for travelers sensitive to fiery fresh bird's eye chilies.",
            PhraseCategory.DINING,
            false,
            false
        ));
        list.add(new Phrase(
            "p_dine_3",
            "Give me one iced milk coffee with less sugar",
            "Cho tôi một ly cà phê sữa đá ít đường.",
            "Cho toi mot ly ca phe sua da it duong.",
            "Chaw toy moht lee cah feh soo-ah dah eet duwng.",
            "The classic Saigon order for Vietnam's world-famous iced coffee.",
            PhraseCategory.DINING,
            false,
            false
        ));
        list.add(new Phrase(
            "p_dine_4",
            "Server, check please!",
            "Em ơi, tính tiền giúp anh / chị!",
            "Em oi, tinh tien giup anh / chi!",
            "Em oy, tin tyen zoop ahn / chee!",
            "'Em ơi' is the universally accepted, friendly way to call waitstaff.",
            PhraseCategory.DINING,
            false,
            false
        ));
        list.add(new Phrase(
            "p_dine_5",
            "This dish is delicious!",
            "Món này rất ngon!",
            "Mon nay rat ngon!",
            "Mawn nye ruht ngawn!",
            "Complimenting street vendors or chefs makes them very happy.",
            PhraseCategory.DINING,
            false,
            false
        ));
        list.add(new Phrase(
            "p_dir_1",
            "Please take me to this address",
            "Cho tôi đến địa chỉ này.",
            "Cho toi den dia chi nay.",
            "Chaw toy den dee-ah chee nye.",
            "Show the written address on your phone to taxi or Grab drivers.",
            PhraseCategory.DIRECTION,
            false,
            false
        ));
        list.add(new Phrase(
            "p_dir_2",
            "How long does it take to Ben Thanh Market?",
            "Đi đến chợ Bến Thành mất bao lâu?",
            "Di den cho Ben Thanh mat bao lau?",
            "Dee den chaw Ben Thahn muht bow low?",
            "Helpful to estimate travel time in Saigon traffic.",
            PhraseCategory.DIRECTION,
            false,
            false
        ));
        list.add(new Phrase(
            "p_dir_3",
            "Turn left / Turn right / Go straight",
            "Rẽ trái / Rẽ phải / Đi thẳng.",
            "Re trai / Re phai / Di thang.",
            "Reh try / Reh fye / Dee thahng.",
            "Basic navigation directives for walking or guiding a driver.",
            PhraseCategory.DIRECTION,
            false,
            false
        ));
        list.add(new Phrase(
            "p_dir_4",
            "Please stop right here, thank you!",
            "Dừng lại ở đây nhé, cảm ơn!",
            "Dung lai o day nhe, cam on!",
            "Zuhng lye uh dye nyeh, kahm uhn!",
            "Tell your taxi or motorbike driver when you arrive.",
            PhraseCategory.DIRECTION,
            false,
            false
        ));
        list.add(new Phrase(
            "p_emg_1",
            "Please help me!",
            "Làm ơn giúp tôi với!",
            "Lam on giup toi voi!",
            "Lahm uhn zoop toy vuh-ee!",
            "Crucial emergency phrase. Vietnamese locals are very helpful to travelers.",
            PhraseCategory.EMERGENCY,
            false,
            false
        ));
        list.add(new Phrase(
            "p_emg_2",
            "I am lost",
            "Tôi bị lạc đường rồi.",
            "Toi bi lac duong roi.",
            "Toy bee lahk duwng roy.",
            "Use when asking police officers, security guards, or hotel staff for guidance.",
            PhraseCategory.EMERGENCY,
            false,
            false
        ));
        list.add(new Phrase(
            "p_emg_3",
            "Where is the nearest hospital or pharmacy?",
            "Bệnh viện hoặc hiệu thuốc gần nhất ở đâu?",
            "Benh vien hoac hieu thuoc gan nhat o dau?",
            "Benh vyen hwahk hyew thwohk guhn nyoht uh doh?",
            "Pharmacies (Hiệu thuốc) are found on almost every street in Saigon.",
            PhraseCategory.EMERGENCY,
            false,
            false
        ));
        list.add(new Phrase(
            "p_emg_4",
            "Do you speak English?",
            "Bạn có nói tiếng Anh không?",
            "Ban co noi tieng Anh khong?",
            "Bahn caw noy tyeng Ahn khohng?",
            "Many students and young hospitality staff speak conversational English.",
            PhraseCategory.EMERGENCY,
            false,
            false
        ));
        return Collections.unmodifiableList(list);
    }
}
