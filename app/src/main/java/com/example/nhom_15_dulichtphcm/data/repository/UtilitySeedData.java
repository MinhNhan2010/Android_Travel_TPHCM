package com.example.nhom_15_dulichtphcm.data.repository;

import com.example.nhom_15_dulichtphcm.data.model.PublicUtility;
import com.example.nhom_15_dulichtphcm.data.model.UtilityCategory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class UtilitySeedData {
    private UtilitySeedData() {}

    public static List<PublicUtility> createDefaultUtilities() {
        List<PublicUtility> list = new ArrayList<>();
        list.add(new PublicUtility(
            "wc_ben_thanh",
            "Ben Thanh Market Modern Public Restroom",
            "Nhà vệ sinh công cộng Chợ Bến Thành",
            UtilityCategory.RESTROOM,
            "District 1",
            "Phan Chu Trinh Gate, Ben Thanh Market, District 1",
            10.77252,
            106.69751,
            "06:00 - 22:00 (Daily)",
            "5,000 VND (~$0.20 USD) or Free inside market",
            null,
            "Clean, air-conditioned public toilet facility located on the west flank of Ben Thanh Market.",
            "Nhà vệ sinh sạch sẽ, có máy lạnh nằm ở cổng phía Tây chợ Bến Thành.",
            "Attendants provide toilet paper and hand soap. Small VND change is recommended."
        ));
        list.add(new PublicUtility(
            "wc_nguyen_hue",
            "Nguyen Hue Promenade Underground Restroom",
            "Nhà vệ sinh ngầm Phố đi bộ Nguyễn Huệ",
            UtilityCategory.RESTROOM,
            "District 1",
            "Near intersection of Nguyen Hue & Ton Duc Thang, District 1",
            10.77452,
            106.70312,
            "06:00 - 23:00 (Daily)",
            "Free of charge (Miễn phí)",
            null,
            "Modern subterranean public restroom located directly under the Nguyen Hue walking promenade.",
            "Nhà vệ sinh công cộng hiện đại dưới lòng đất ngay phố đi bộ Nguyễn Huệ.",
            "Equipped with wheelchair ramp and baby changing facilities. Highly hygienic."
        ));
        list.add(new PublicUtility(
            "wc_bach_dang",
            "Bach Dang Riverfront Park Smart Restroom",
            "Nhà vệ sinh Công viên Bến Bạch Đằng",
            UtilityCategory.RESTROOM,
            "District 1",
            "Bach Dang Wharf Park, Ton Duc Thang, District 1",
            10.77382,
            106.70615,
            "05:00 - 23:00 (Daily)",
            "Free of charge (Miễn phí)",
            null,
            "Brand new glass-and-steel eco-friendly restroom built inside the scenic riverfront park.",
            "Nhà vệ sinh thông minh hiện đại mới xây dựng trong công viên ven sông Bạch Đằng.",
            "Ideal stop while taking a stroll along the Saigon River or boarding the Waterbus."
        ));
        list.add(new PublicUtility(
            "wc_23_9_park",
            "23/9 Park 24/7 Public Restroom",
            "Nhà vệ sinh Công viên 23/9 (Gần phố Tây)",
            UtilityCategory.RESTROOM,
            "District 1",
            "Pham Ngu Lao & Le Lai, District 1 (Opposite Bui Vien area)",
            10.76885,
            106.69125,
            "Open 24/7",
            "5,000 VND (~$0.20 USD)",
            null,
            "Convenient 24-hour public restroom facility situated near the backpacker quarter.",
            "Nhà vệ sinh công cộng mở cửa 24/7 đối diện khu phố đi bộ Bùi Viện.",
            "Very convenient during late night walks around the backpacker street."
        ));
        list.add(new PublicUtility(
            "curr_ha_tam",
            "Ha Tam Jewelry (Top Currency Exchange in Saigon)",
            "Tiệm vàng Hà Tâm (Điểm đổi ngoại tệ uy tín)",
            UtilityCategory.CURRENCY_EXCHANGE,
            "District 1",
            "02 Nguyễn An Ninh, Phường Bến Thành, Quận 1 (Opposite West Gate)",
            10.77289,
            106.69701,
            "07:30 - 20:30 (Daily)",
            "Competitive official daily rates (Tỷ giá tốt)",
            "028 3823 7243",
            "Legendary exchange venue renowned among international expatriates for the most favorable exchange rates for USD, EUR, JPY, GBP, KRW, SGD, AUD.",
            "Địa chỉ đổi ngoại tệ uy tín nổi tiếng lâu đời đối diện chợ Bến Thành với tỷ giá tốt nhất Sài Gòn.",
            "Crisp, uncreased, unmarked USD/EUR 100 banknotes yield the highest exchange value."
        ));
        list.add(new PublicUtility(
            "curr_vietcombank",
            "Vietcombank Head Office & Global ATM Center",
            "Ngân hàng Ngoại thương Vietcombank & ATM Quốc tế",
            UtilityCategory.CURRENCY_EXCHANGE,
            "District 1",
            "05 Công Trường Mê Linh, Phường Bến Nghé, Quận 1",
            10.77582,
            106.70524,
            "Counter: 08:00 - 16:30 (Mon-Fri) | ATM: 24/7",
            "Standard bank exchange rate & ATM fee",
            "1900 545413",
            "Vietnam's premier foreign trade bank offering official bank counter exchange and multi-currency international ATMs accepting Visa, MasterCard, Cirrus, and JCB.",
            "Trụ sở ngân hàng ngoại thương lớn nhất Việt Nam, hỗ trợ giao dịch ngoại tệ và ATM quốc tế 24/7.",
            "Bring original physical passport if exchanging currency at the bank service counter."
        ));
        list.add(new PublicUtility(
            "transit_ben_thanh_metro",
            "Ben Thanh Central Underground Metro Station",
            "Ga ngầm Trung tâm Bến Thành (Metro Tuyến số 1)",
            UtilityCategory.TRANSIT_METRO,
            "District 1",
            "Underground Quách Thị Trang Square, Lê Lợi, District 1",
            10.77192,
            106.69824,
            "05:30 - 23:00 (Daily)",
            "12,000 - 20,000 VND (~$0.50 - $0.80 USD) per trip",
            "028 3829 7988",
            "Architectural underground centerpiece of HCMC Metro Line 1, spanning 4 basement levels with direct pedestrian links to Ben Thanh Market.",
            "Nhà ga ngầm hiện đại nhất của Tuyến Metro số 1 Bến Thành - Suối Tiên.",
            "Equipped with automatic ticket vending machines accepting cash and international contactless debit/credit cards."
        ));
        list.add(new PublicUtility(
            "transit_waterbus",
            "Saigon Waterbus Bach Dang Central Pier",
            "Bến Tàu thủy Buýt Sài Gòn (Bạch Đằng)",
            UtilityCategory.TRANSIT_METRO,
            "District 1",
            "10B Tôn Đức Thắng, Phường Bến Nghé, Quận 1",
            10.77421,
            106.70682,
            "07:00 - 20:00 (Daily)",
            "15,000 VND (~$0.60 USD) per one-way ticket",
            "1900 636830",
            "Affordable, scenic river catamaran transit traversing the Saigon River through District 1, Binh Thanh, and Thu Duc City.",
            "Tuyến xe buýt đường sông ngắm hoàng hôn và cảnh quan sông Sài Gòn chỉ với 15.000đ.",
            "Take the 16:30 or 17:00 departure for magnificent sunset views of the Landmark 81 skyline."
        ));
        list.add(new PublicUtility(
            "transit_ham_nghi_bus",
            "Saigon Central Bus Transit Hub (Ham Nghi)",
            "Trạm trung chuyển Xe buýt Bến Thành (Hàm Nghi)",
            UtilityCategory.TRANSIT_METRO,
            "District 1",
            "Hàm Nghi Boulevard, Phường Bến Nghé, Quận 1",
            10.77095,
            106.70252,
            "05:00 - 21:00 (Daily)",
            "5,000 - 7,000 VND per ticket",
            "1022",
            "Central bus exchange terminal where routes to Tan Son Nhat Airport (Bus 109 & 152), Chinatown (Bus 01), and suburbs originate.",
            "Trạm xe buýt trung tâm kết nối các tuyến xe đi sân bay Tân Sơn Nhất và Chợ Lớn.",
            "Bus 109 is a yellow airport express shuttle with generous luggage racks straight to Tan Son Nhat International Airport."
        ));
        list.add(new PublicUtility(
            "med_columbia",
            "Columbia Asia International Clinic Saigon",
            "Phòng khám Quốc tế Columbia Asia Sài Gòn",
            UtilityCategory.MEDICAL,
            "District 1",
            "08 Alexandre de Rhodes, Phường Bến Nghé, Quận 1",
            10.78012,
            106.69785,
            "24/7 Emergency & Outpatient",
            "Accepts International Health Insurance & Credit Cards",
            "028 3823 8888",
            "International medical clinic with multilingual staff fluent in English and French, offering comprehensive diagnostics and emergency services.",
            "Phòng khám đa khoa quốc tế 24/7 với đội ngũ bác sĩ giao tiếp tiếng Anh và Pháp thành thạo.",
            "Direct billing partnerships with major global travel insurers (Allianz, Bupa, Cigna, AXA)."
        ));
        list.add(new PublicUtility(
            "med_cho_ray",
            "Cho Ray National General Hospital (Top Trauma Center)",
            "Bệnh viện Chợ Rẫy (Cấp cứu hàng đầu miền Nam)",
            UtilityCategory.MEDICAL,
            "District 5",
            "201B Nguyễn Chí Thanh, Phường 12, Quận 5",
            10.75821,
            106.65982,
            "24/7 Emergency Department",
            "Government regulated medical fee",
            "028 3855 4137",
            "The highest tier referral hospital in Southern Vietnam, equipped with trauma centers and specialized surgical wards.",
            "Bệnh viện tuyến cuối lớn nhất miền Nam với trung tâm cấp cứu 24/7 chuyên nghiệp.",
            "In acute medical emergencies, dial 115 or take a direct taxi to Cho Ray Emergency Gate."
        ));
        list.add(new PublicUtility(
            "info_tourist_center",
            "HCMC Tourist Information & Support Center",
            "Trung tâm Thông tin & Hỗ trợ Du khách TP.HCM",
            UtilityCategory.TOURIST_INFO_SIM,
            "District 1",
            "Nguyen Hue Promenade (Near City Hall), District 1",
            10.77362,
            106.70391,
            "08:00 - 18:00 (Daily)",
            "Free maps & travel advisory",
            "028 3823 4058",
            "Official city tourism office providing complimentary multilingual travel brochures, walking maps, and consumer rights assistance for tourists.",
            "Văn phòng du lịch chính thức cung cấp bản đồ miễn phí và hỗ trợ du khách quốc tế.",
            "Staff speak English, French, and Japanese. Excellent place to collect free high-detail paper maps."
        ));
        list.add(new PublicUtility(
            "info_tourist_police",
            "District 1 Tourist Security & Assistance Station",
            "Đội Cảnh sát & Trật tự Du lịch Quận 1",
            UtilityCategory.TOURIST_INFO_SIM,
            "District 1",
            "82 Pasteur, Phường Bến Nghé, Quận 1",
            10.77615,
            106.70182,
            "24/7 Duty Station",
            "Free public service",
            "028 3829 7473",
            "Specialized tourist police precinct dedicated to safeguarding visitors, reporting lost passports/belongings, and mediating merchant disputes.",
            "Đội cảnh sát hỗ trợ du khách 24/7, tiếp nhận trình báo mất tài sản, hộ chiếu và giải quyết sự cố.",
            "Police officers issue official certified police reports (Giấy báo mất) necessary for consular replacement of lost passports."
        ));
        list.add(new PublicUtility(
            "info_viettel_sim",
            "Viettel Official Tourist eSIM & 5G Hub",
            "Trung tâm SIM 4G/5G Du lịch Viettel chính hãng",
            UtilityCategory.TOURIST_INFO_SIM,
            "District 1",
            "70 Hai Bà Trưng, Phường Bến Nghé, Quận 1",
            10.77881,
            106.69852,
            "08:00 - 20:00 (Daily)",
            "150,000 - 300,000 VND (~$6 - $12 USD) unlimited 4G/5G data",
            "1800 8098",
            "Official flagship store of Vietnam's largest telecom carrier, providing instant physical SIM and digital eSIM activations for foreign passports.",
            "Cửa hàng chính hãng đăng ký SIM và eSIM du lịch tốc độ cao không giới hạn dữ liệu.",
            "Requires original physical passport for carrier registration compliance. Takes under 5 minutes to activate."
        ));
        list.add(new PublicUtility(
            "emg_police",
            "National Police Emergency Hotline (Cảnh sát)",
            "Đường dây nóng Cảnh sát phản ứng nhanh 113",
            UtilityCategory.EMERGENCY_HOTLINE,
            "City-wide",
            "Toll-free emergency dial across Vietnam",
            10.7769,
            106.7009,
            "24/7 / 365 Days",
            "Free call from any phone",
            "113",
            "National police emergency dispatch number for immediate reporting of theft, crime, or physical distress.",
            "Đường dây khẩn cấp tiếp nhận tin báo tội phạm và bảo đảm an ninh trật tự 24/7.",
            "Can be dialed without country code or mobile balance on any smartphone."
        ));
        list.add(new PublicUtility(
            "emg_ambulance",
            "National Emergency Ambulance Hotline (Cấp cứu 115)",
            "Đường dây nóng Cấp cứu Y tế 115",
            UtilityCategory.EMERGENCY_HOTLINE,
            "City-wide",
            "Toll-free medical emergency dispatch across Vietnam",
            10.7769,
            106.7009,
            "24/7 / 365 Days",
            "Free emergency call",
            "115",
            "Paramedic dispatch service providing emergency ambulances and first-aid response.",
            "Tổng đài điều phối xe cứu thương và nhân viên y tế cấp cứu khẩn cấp.",
            "Speak clearly and state your exact landmark or street intersection in District 1."
        ));
        list.add(new PublicUtility(
            "emg_tourist_hotline",
            "HCMC Tourist Support Hotline 1022 (Extension 8)",
            "Tổng đài Hỗ trợ Du khách TP.HCM 1022 (Nhánh 8)",
            UtilityCategory.EMERGENCY_HOTLINE,
            "City-wide",
            "Department of Tourism, Ho Chi Minh City",
            10.7769,
            106.7009,
            "24/7 Service",
            "Standard local call rate",
            "02838234058",
            "Dedicated 24/7 multilingual telephone support hotline for international travelers seeking guidance, taxi overcharging resolution, or tourism aid.",
            "Đường dây nóng 24/7 giải quyết khiếu nại, hỗ trợ du khách bị chặt chém hoặc cần trợ giúp.",
            "English operators are on duty around the clock."
        ));
        return Collections.unmodifiableList(list);
    }
}
