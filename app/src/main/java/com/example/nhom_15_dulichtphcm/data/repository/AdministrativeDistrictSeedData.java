package com.example.nhom_15_dulichtphcm.data.repository;

import com.example.nhom_15_dulichtphcm.data.model.AdministrativeDistrict;
import com.example.nhom_15_dulichtphcm.data.model.AdministrativeType;
import com.example.nhom_15_dulichtphcm.data.model.LatLngPoint;
import com.example.nhom_15_dulichtphcm.data.model.RegionZone;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class AdministrativeDistrictSeedData {
    private AdministrativeDistrictSeedData() {}

    public static List<AdministrativeDistrict> createDefaultDistricts() {
        List<AdministrativeDistrict> list = new ArrayList<>();
        list.add(new AdministrativeDistrict(
            "thu_duc_city",
            "Thành phố Thủ Đức",
            "Thu Duc City",
            AdministrativeType.CITY,
            RegionZone.EAST_THU_DUC,
            211.56,
            "1.215.000+ người",
            34,
            "SÁP NHẬP NĂM 2021: Thành lập ngày 01/01/2021 theo Nghị quyết số 1111/NQ-UBTVQH14 trên cơ sở sáp nhập toàn bộ 3 quận: Quận 2, Quận 9 và Quận Thủ Đức cũ. Đây là thành phố thuộc thành phố trực thuộc trung ương đầu tiên của Việt Nam.",
            "Thành phố sáng tạo, tài chính quốc tế và công nghệ cao phía Đông TP.HCM. Sở hữu bán đảo Thủ Thiêm hiện đại, Đại học Quốc gia, Khu Công nghệ cao và những công trình văn hóa di sản nổi tiếng.",
            Arrays.asList("Khu đô thị mới Thủ Thiêm & Cầu Ba Son", "Chùa Bửu Long (Top chùa đẹp nhất thế giới)", "Khu du lịch Văn hóa Suối Tiên", "Bảo tàng Áo Dài Việt Nam", "Phố ẩm thực Tây Thảo Điền", "Công viên bờ sông Sài Gòn"),
            10.8400,
            106.7700,
            Arrays.asList(new LatLngPoint(10.768, 106.712), new LatLngPoint(10.745, 106.745), new LatLngPoint(10.780, 106.810), new LatLngPoint(10.820, 106.840), new LatLngPoint(10.885, 106.815), new LatLngPoint(10.880, 106.760), new LatLngPoint(10.865, 106.730), new LatLngPoint(10.830, 106.715), new LatLngPoint(10.795, 106.725), new LatLngPoint(10.768, 106.712))
        ));
        list.add(new AdministrativeDistrict(
            "quan_1",
            "Quận 1",
            "District 1",
            AdministrativeType.URBAN_DISTRICT,
            RegionZone.CENTRAL_CORE,
            7.72,
            "225.000+ người",
            10,
            "Trái tim trung tâm TP.HCM, giữ nguyên địa giới 10 phường trọng điểm (Bến Nghé, Bến Thành, Đa Kao, Tân Định, Phạm Ngũ Lão...).",
            "Trung tâm hành chính, kinh tế, thương mại và ngoại giao sầm uất bậc nhất Sài Gòn, nơi hội tụ các công trình kiến trúc thời kỳ đầu thế kỷ 20.",
            Arrays.asList("Chợ Bến Thành", "Nhà thờ Đức Bà", "Bưu điện Trung tâm", "Dinh Độc Lập", "Phố đi bộ Nguyễn Huệ", "Bánh mì Huỳnh Hoa"),
            10.7769,
            106.7009,
            Arrays.asList(new LatLngPoint(10.765, 106.690), new LatLngPoint(10.762, 106.700), new LatLngPoint(10.772, 106.708), new LatLngPoint(10.788, 106.705), new LatLngPoint(10.795, 106.693), new LatLngPoint(10.778, 106.685), new LatLngPoint(10.765, 106.690))
        ));
        list.add(new AdministrativeDistrict(
            "quan_3",
            "Quận 3",
            "District 3",
            AdministrativeType.URBAN_DISTRICT,
            RegionZone.CENTRAL_CORE,
            4.92,
            "190.000+ người",
            12,
            "SÁP NHẬP PHƯỜNG: Đã tiến hành sắp xếp sáp nhập Phường 6, Phường 7 và Phường 8 thành phường mới theo Nghị quyết 1111/NQ-UBTVQH14.",
            "Vùng đệm biệt thự cổ kính, trung tâm văn hóa và ẩm thực lâu đời với Bảo tàng Chứng tích Chiến tranh và các quán cà phê vợt lịch sử.",
            Arrays.asList("Bảo tàng Chứng tích Chiến tranh", "Hồ Con Rùa", "Chùa Vĩnh Nghiêm", "Phở Hòa Pasteur", "Cà phê Vợt Cheo Leo"),
            10.7845,
            106.6845,
            Arrays.asList(new LatLngPoint(10.775, 106.675), new LatLngPoint(10.778, 106.690), new LatLngPoint(10.792, 106.695), new LatLngPoint(10.790, 106.680), new LatLngPoint(10.775, 106.675))
        ));
        list.add(new AdministrativeDistrict(
            "quan_4",
            "Quận 4",
            "District 4",
            AdministrativeType.URBAN_DISTRICT,
            RegionZone.CENTRAL_CORE,
            4.18,
            "175.000+ người",
            13,
            "SÁP NHẬP PHƯỜNG: Sáp nhập Phường 5 vào Phường 2, Phường 12 vào Phường 13 theo đề án sắp xếp đơn vị hành chính của TP.HCM.",
            "Cù lao sông nước độc đáo nằm giữa Quận 1 và Quận 7, thiên đường ẩm thực đường phố và phố ốc Vĩnh Khánh nức tiếng.",
            Arrays.asList("Phố ẩm thực Ốc Vĩnh Khánh", "Bến Nhà Rồng (Bảo tàng Hồ Chí Minh)", "Cầu Mống lịch sử"),
            10.7600,
            106.7020,
            Arrays.asList(new LatLngPoint(10.755, 106.695), new LatLngPoint(10.755, 106.712), new LatLngPoint(10.768, 106.708), new LatLngPoint(10.765, 106.695), new LatLngPoint(10.755, 106.695))
        ));
        list.add(new AdministrativeDistrict(
            "quan_5",
            "Quận 5 (Chợ Lớn)",
            "District 5 (Chinatown)",
            AdministrativeType.URBAN_DISTRICT,
            RegionZone.WEST_CHO_LON,
            4.27,
            "170.000+ người",
            14,
            "SÁP NHẬP PHƯỜNG: Sáp nhập Phường 15 vào Phường 12 theo Nghị quyết 1111 của Quốc hội.",
            "Trái tim của khu Chợ Lớn - nơi lưu giữ văn hóa Hoa kiều hàng trăm năm với hội quán, chùa miếu cổ và ẩm thực truyền thống phong phú.",
            Arrays.asList("Chợ Lớn", "Hội quán Bà Thiên Hậu", "Hội quán Nghĩa An", "Phở Lệ", "Chợ vải Soái Kình Lâm"),
            10.7540,
            106.6660,
            Arrays.asList(new LatLngPoint(10.745, 106.655), new LatLngPoint(10.748, 106.675), new LatLngPoint(10.762, 106.678), new LatLngPoint(10.760, 106.658), new LatLngPoint(10.745, 106.655))
        ));
        list.add(new AdministrativeDistrict(
            "binh_thanh",
            "Quận Bình Thạnh",
            "Binh Thanh District",
            AdministrativeType.URBAN_DISTRICT,
            RegionZone.CENTRAL_CORE,
            20.78,
            "500.000+ người",
            20,
            "Cửa ngõ Đông Bắc kết nối Quận 1 và TP. Thủ Đức, sở hữu tòa tháp cao nhất Việt Nam Landmark 81.",
            "Khu đô thị ven sông Sài Gòn sầm uất, nổi bật với bán đảo Thanh Đa xanh mát và tòa tháp biểu tượng Landmark 81.",
            Arrays.asList("Tòa tháp Landmark 81 & Vincom", "Công viên Vinhomes Central Park", "Bán đảo Thanh Đa", "Lăng Tả quân Lê Văn Duyệt (Bà Chiểu)"),
            10.8030,
            106.7050,
            Arrays.asList(new LatLngPoint(10.788, 106.710), new LatLngPoint(10.815, 106.725), new LatLngPoint(10.835, 106.715), new LatLngPoint(10.815, 106.690), new LatLngPoint(10.795, 106.695), new LatLngPoint(10.788, 106.710))
        ));
        list.add(new AdministrativeDistrict(
            "phu_nhuan",
            "Quận Phú Nhuận",
            "Phu Nhuan District",
            AdministrativeType.URBAN_DISTRICT,
            RegionZone.CENTRAL_CORE,
            4.86,
            "180.000+ người",
            13,
            "SÁP NHẬP PHƯỜNG: Sáp nhập Phường 12 vào Phường 11, Phường 14 vào Phường 13 theo đề án của TP.HCM.",
            "Quận nội thành trung tâm nổi tiếng với ẩm thực đường phố, quán xá cà phê tấp nập và cơm tấm Ba Ghiền danh tiếng.",
            Arrays.asList("Phố ẩm thực Phan Xích Long", "Cơm Tấm Ba Ghiền (Michelin Bib Gourmand)", "Cà phê Vợt Phan Đình Phùng (24/7)"),
            10.7990,
            106.6800,
            Arrays.asList(new LatLngPoint(10.790, 106.670), new LatLngPoint(10.795, 106.690), new LatLngPoint(10.810, 106.685), new LatLngPoint(10.805, 106.668), new LatLngPoint(10.790, 106.670))
        ));
        list.add(new AdministrativeDistrict(
            "quan_7",
            "Quận 7",
            "District 7",
            AdministrativeType.URBAN_DISTRICT,
            RegionZone.SOUTH_ZONE,
            35.69,
            "360.000+ người",
            10,
            "Đô thị kiểu mẫu phía Nam TP.HCM, kết nối trung tâm với Nhà Bè và vùng sinh thái Cần Giờ.",
            "Khu đô thị quốc tế hiện đại với không gian cây xanh rộng lớn, trung tâm triển lãm SECC và Cầu Ánh Sao lãng mạn.",
            Arrays.asList("Hồ Bán Nguyệt & Cầu Ánh Sao", "Đô thị Phú Mỹ Hưng", "Bảo tàng tranh 3D Artinus", "Trung tâm Hội chợ SECC"),
            10.7350,
            106.7200,
            Arrays.asList(new LatLngPoint(10.710, 106.705), new LatLngPoint(10.715, 106.750), new LatLngPoint(10.748, 106.735), new LatLngPoint(10.740, 106.700), new LatLngPoint(10.710, 106.705))
        ));
        list.add(new AdministrativeDistrict(
            "can_gio",
            "Huyện Cần Giờ",
            "Can Gio District",
            AdministrativeType.RURAL_DISTRICT,
            RegionZone.SOUTH_ZONE,
            704.45,
            "75.000+ người",
            7,
            "Huyện đảo ven biển duy nhất của TP.HCM, Khu dự trữ sinh quyển thế giới được UNESCO công nhận.",
            "Lá phổi xanh của Sài Gòn với rừng ngập mặn bao la, đảo khỉ, chiến khu Rừng Sác và bãi biển Cần Giờ hoang sơ.",
            Arrays.asList("Khu dự trữ sinh quyển Rừng Sác", "Đảo Khỉ Cần Giờ", "Bãi biển 30/4", "Chợ hải sản Hàng Dương"),
            10.5100,
            106.8800,
            Arrays.asList(new LatLngPoint(10.420, 106.820), new LatLngPoint(10.430, 106.960), new LatLngPoint(10.640, 106.880), new LatLngPoint(10.580, 106.760), new LatLngPoint(10.420, 106.820))
        ));
        list.add(new AdministrativeDistrict(
            "cu_chi",
            "Huyện Củ Chi",
            "Cu Chi District",
            AdministrativeType.RURAL_DISTRICT,
            RegionZone.NORTH_ZONE,
            434.77,
            "460.000+ người",
            21,
            "Vùng đất thép anh hùng Tây Bắc TP.HCM, di tích lịch sử quốc gia đặc biệt thu hút hàng triệu du khách quốc tế.",
            "Nổi tiếng toàn cầu với hệ thống địa đạo ngầm huyền thoại dài hơn 250km được đào hoàn toàn bằng tay thời kháng chiến.",
            Arrays.asList("Địa đạo Bến Dược Củ Chi", "Địa đạo Bến Đình Củ Chi", "Làng sinh thái nông nghiệp Củ Chi", "Đặc sản Bò tơ Củ Chi"),
            11.0800,
            106.5200,
            Arrays.asList(new LatLngPoint(10.950, 106.450), new LatLngPoint(11.150, 106.420), new LatLngPoint(11.180, 106.580), new LatLngPoint(10.980, 106.650), new LatLngPoint(10.950, 106.450))
        ));
        list.add(new AdministrativeDistrict(
            "quan_10",
            "Quận 10",
            "District 10",
            AdministrativeType.URBAN_DISTRICT,
            RegionZone.CENTRAL_CORE,
            5.72,
            "235.000+ người",
            14,
            "SÁP NHẬP PHƯỜNG: Sáp nhập Phường 3 vào Phường 2 theo đề án sắp xếp đơn vị hành chính.",
            "Khu đô thị thương mại trung tâm nhộn nhịp với chợ hoa Hồ Thị Kỷ rực rỡ suốt ngày đêm và con phố ẩm thực Sư Vạn Hạnh.",
            Arrays.asList("Chợ hoa đêm Hồ Thị Kỷ", "Phố ẩm thực Sư Vạn Hạnh", "Việt Nam Quốc Tự"),
            10.7720,
            106.6680,
            Arrays.asList(new LatLngPoint(10.760, 106.660), new LatLngPoint(10.765, 106.678), new LatLngPoint(10.780, 106.675), new LatLngPoint(10.778, 106.658), new LatLngPoint(10.760, 106.660))
        ));
        list.add(new AdministrativeDistrict(
            "binh_duong_region",
            "Tỉnh Bình Dương (Cửa ngõ Phía Bắc)",
            "Binh Duong Province (Northern Gateway)",
            AdministrativeType.CITY,
            RegionZone.BINH_DUONG_GATEWAY,
            2694.4,
            "2.800.000+ người",
            9,
            "LIÊN KẾT VÙNG ĐÔ THỊ TP.HCM: Tiếp giáp trực tiếp phía Bắc TP. Thủ Đức và Quận 12. Kết nối chiến lược qua Tuyến Metro số 1 Bến Thành - Suối Tiên nối dài Dĩ An/Bình Dương, Quốc lộ 13 và Vành đai 3.",
            "Thủ phủ công nghiệp công nghệ cao và du lịch tâm linh, văn hóa làng nghề truyền thống gốm sứ Lái Thiêu trăm năm tuổi ven dòng sông Sài Gòn.",
            Arrays.asList("KDL Lạc Cảnh Đại Nam Văn Hiến (Kim Điện dát vàng 24K)", "Chùa Bà Thiên Hậu Thánh Mẫu (Lễ hội Rằm tháng Giêng)", "Làng gốm Lái Thiêu & Lò lu Đại Hưng cổ nhất Nam Bộ", "KDL Sinh thái Thủy Châu (Suối thác nhân tạo)", "Nhà thờ Chánh tòa Phú Cường (Kiến trúc Gothic châu Âu)", "Vườn trái cây Lái Thiêu & Gỏi gà măng cụt trứ danh"),
            10.9806,
            106.6542,
            Arrays.asList(new LatLngPoint(10.880, 106.650), new LatLngPoint(10.890, 106.800), new LatLngPoint(11.080, 106.750), new LatLngPoint(11.070, 106.580), new LatLngPoint(10.880, 106.650))
        ));
        list.add(new AdministrativeDistrict(
            "vung_tau_region",
            "TP. Vũng Tàu (Đô thị Biển Đông Nam Bộ)",
            "Vung Tau City (Coastal Beach Gateway)",
            AdministrativeType.CITY,
            RegionZone.VUNG_TAU_COASTAL,
            141.1,
            "530.000+ người",
            17,
            "LIÊN KẾT DU LỊCH BIỂN ĐÔNG NAM BỘ: Đô thị biển hạt nhân kết nối với TP.HCM bằng Tàu cao tốc Bến Bạch Đằng (Quận 1) - Cầu Đá (Vũng Tàu) chỉ 2 giờ, Tuyến Cao tốc TP.HCM - Long Thành - Dầu Giây và Cao tốc Biên Hòa - Vũng Tàu.",
            "Bán đảo du lịch biển sôi động với Tượng Chúa Kitô Vua lớn nhất châu Á, Hải đăng cổ 1862, Mũi Nghinh Phong, Bãi Sau Thùy Vân và cáp treo Hồ Mây Park.",
            Arrays.asList("Tượng Chúa Kitô Vua lớn nhất châu Á (Đỉnh Núi Nhỏ)", "Ngọn Hải đăng cổ xưa nhất Việt Nam (Xây dựng 1862)", "Mũi Nghinh Phong & Cổng Trời đón gió Biển Đông", "Bạch Dinh Villa Blanche & Kho báu gốm sứ Khang Hy", "Bãi Sau Thùy Vân phẳng mịn & Vịnh Bãi Trước", "Cáp treo Hồ Mây Park Núi Lớn", "Tàu cao tốc đường thủy Bến Bạch Đằng - Vũng Tàu"),
            10.3460,
            107.0844,
            Arrays.asList(new LatLngPoint(10.315, 107.080), new LatLngPoint(10.330, 107.110), new LatLngPoint(10.380, 107.090), new LatLngPoint(10.370, 107.050), new LatLngPoint(10.320, 107.065), new LatLngPoint(10.315, 107.080))
        ));
        return Collections.unmodifiableList(list);
    }
}
