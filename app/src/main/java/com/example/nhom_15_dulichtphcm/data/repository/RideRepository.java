package com.example.nhom_15_dulichtphcm.data.repository;

import com.example.nhom_15_dulichtphcm.data.model.DriverPhraseCard;
import com.example.nhom_15_dulichtphcm.data.model.RideEstimate;
import com.example.nhom_15_dulichtphcm.data.model.RideProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Ride-hailing & Official Taxi comparison repository for Saigon travelers.
 */
public final class RideRepository {

    public static final class LocationPoint {
        public final String nameEn;
        public final String nameVi;
        public final double lat;
        public final double lng;

        public LocationPoint(String nameEn, String nameVi, double lat, double lng) {
            this.nameEn = nameEn;
            this.nameVi = nameVi;
            this.lat = lat;
            this.lng = lng;
        }
    }

    public static final List<RideProvider> providers = Collections.unmodifiableList(Arrays.asList(
            new RideProvider(
                    "xanh_sm",
                    "Xanh SM (VinFast Electric)",
                    "100% Electric Taxi & Bike • Zero Emission",
                    "Eco Choice",
                    "🩵",
                    "com.gsm.customer",
                    "https://www.xanhsm.com/",
                    "19002088",
                    "Vietnam's all-electric VinFast fleet with uniformed drivers, clean cabins, and transparent fixed pricing.",
                    "Hãng xe thuần điện VinFast 100%, tài xế lịch sự đồng phục chuẩn, xe mới sạch không mùi xăng và giá minh bạch.",
                    Arrays.asList("Xanh SM Bike", "Xanh SM Taxi", "Xanh SM Luxury"),
                    Arrays.asList("Cash", "Visa/Mastercard", "MoMo / ZaloPay"),
                    Arrays.asList("No surge surprises on metered mode", "Brand-new VinFast EVs with strong A/C"),
                    13_800, 4_800, 28_000, 14_500
            ),
            new RideProvider(
                    "grab",
                    "Grab",
                    "#1 Super-App in Southeast Asia",
                    "Most Popular",
                    "💚",
                    "com.grabtaxi.passenger",
                    "https://www.grab.com/vn/en/",
                    null,
                    "Largest driver network across HCMC, Binh Duong, and Vung Tau with full English UI and international card support.",
                    "Mạng lưới tài xế lớn nhất tại TP.HCM, Bình Dương và Vũng Tàu, hỗ trợ thẻ quốc tế và giao diện đa ngôn ngữ.",
                    Arrays.asList("GrabBike", "GrabCar 4/7", "GrabCar Plus"),
                    Arrays.asList("Visa/Mastercard", "Cash", "Apple Pay / MoMo"),
                    Arrays.asList("Fastest pickup (< 3 mins in District 1)", "Built-in auto-translated driver chat"),
                    12_500, 4_300, 27_000, 13_500
            ),
            new RideProvider(
                    "be",
                    "Be",
                    "Vietnamese Multi-Modal Mobility App",
                    "Budget Friendly",
                    "💛",
                    "xyz.be.customer",
                    "https://be.com.vn/en/",
                    "1900232345",
                    "Competitive local ride-hailing app wearing iconic yellow helmets, often cheaper during peak hours.",
                    "Ứng dụng gọi xe Việt Nam nhận diện áo vàng - mũ vàng nổi bật, thường có mã giảm giá tốt giờ cao điểm.",
                    Arrays.asList("beBike", "beCar 4/7", "beAirport"),
                    Arrays.asList("Cash", "Visa/Mastercard", "SmartPay"),
                    Arrays.asList("Great airport pickup queue", "Frequent promo vouchers"),
                    12_000, 4_100, 26_000, 13_000
            ),
            new RideProvider(
                    "mai_linh",
                    "Mai Linh Taxi",
                    "Iconic Green Official Metered Taxi",
                    "Trusted Meter",
                    "🟢",
                    "ml.online.passenger",
                    "https://mailinh.vn/",
                    "1055",
                    "Nationwide official green taxi callable from any phone by dialing 1055 — ideal if mobile data runs out.",
                    "Hãng taxi truyền thống màu xanh lá uy tín toàn quốc, gọi tổng đài 1055 cực nhanh ngay cả khi không có 4G.",
                    Arrays.asList("4-Seat Sedan", "7-Seat SUV"),
                    Arrays.asList("Cash", "QR Banking", "POS Card"),
                    Arrays.asList("Nationwide short hotline 1055", "Official airport & ferry ranks"),
                    0, 0, 30_000, 15_200
            ),
            new RideProvider(
                    "vinasun",
                    "Vinasun Taxi",
                    "Southern Vietnam's Classic White-Red-Green Fleet",
                    "Airport Staple",
                    "🤍",
                    "com.vinasuntaxi.clientapp",
                    "https://vinasuntaxi.com/",
                    "02838272727",
                    "Long-established Southern fleet stationed at every major hotel, hospital, mall, and Tan Son Nhat terminal.",
                    "Hãng taxi lâu đời phủ sóng khắp khách sạn, bệnh viện, trung tâm thương mại và sân bay Tân Sơn Nhất.",
                    Arrays.asList("4-Seat Sedan", "7-Seat Innova/Fortuner"),
                    Arrays.asList("Cash", "Visa/Mastercard POS", "Vinasun App"),
                    Arrays.asList("Dedicated uniformed dispatchers at malls & hospitals", "Abundant 7-seater SUVs for luggage"),
                    0, 0, 30_000, 15_500
            )
    ));

    public static final List<DriverPhraseCard> driverPhrases = Collections.unmodifiableList(Arrays.asList(
            new DriverPhraseCard(
                    "dp1",
                    "Please take me to this address.",
                    "Cho tôi đến đúng địa chỉ này nhé.",
                    "Chaw toy den doong dee-ah chee nay nyeh",
                    "Pickup",
                    "Show the destination screen together with this card."
            ),
            new DriverPhraseCard(
                    "dp2",
                    "I am waiting at the main gate / lobby.",
                    "Tôi đang đứng đợi ở cổng chính / sảnh chính.",
                    "Toy dang doong doy uh kong chinh / sanh chinh",
                    "Pickup",
                    "Copy and paste into Grab/Xanh SM chat if the driver calls."
            ),
            new DriverPhraseCard(
                    "dp3",
                    "Please turn on the taximeter.",
                    "Anh làm ơn bật đồng hồ tính tiền giúp tôi.",
                    "Anh lam uhn bat dong ho tinh tien zoop toy",
                    "Safety",
                    "Always use for street-hailed taxis (Mai Linh 1055 / Vinasun)."
            ),
            new DriverPhraseCard(
                    "dp4",
                    "Please drive slowly and safely, I'm not in a rush.",
                    "Anh chạy chậm và an toàn giúp tôi nhé, tôi không vội.",
                    "Anh chay cham va an toan zoop toy nyeh",
                    "Comfort",
                    "Very helpful when riding motorbike taxis in Saigon traffic."
            ),
            new DriverPhraseCard(
                    "dp5",
                    "Please drop me off right here.",
                    "Cho tôi xuống ngay chỗ này nhé, cảm ơn anh!",
                    "Chaw toy suong ngay cho nay nyeh, kam uhn anh",
                    "Drop-off",
                    "Tap the driver's shoulder gently or show this card."
            ),
            new DriverPhraseCard(
                    "dp6",
                    "Do you have a spare helmet with a visor?",
                    "Anh có nón bảo hiểm sạch / có kính chắn không?",
                    "Anh kaw non bao hiem sach khong",
                    "Bike",
                    "Wearing a strapped helmet is mandatory by law in Vietnam."
            ),
            new DriverPhraseCard(
                    "dp7",
                    "Can I pay by QR code / exact cash?",
                    "Tôi thanh toán bằng chuyển khoản QR / tiền mặt nhé?",
                    "Toy thanh toan bang chuyen khoan QR / tien mat nyeh",
                    "Payment",
                    "Double-check zeroes on 20,000 vs 500,000 VND polymer notes."
            )
    ));

    public static final List<LocationPoint> popularPickupPoints = Collections.unmodifiableList(Arrays.asList(
            new LocationPoint("Ben Thanh Market (Center)", "Chợ Bến Thành (Quận 1)", 10.7725, 106.6980),
            new LocationPoint("Tan Son Nhat Airport (SGN)", "Sân bay Tân Sơn Nhất", 10.8185, 106.6588),
            new LocationPoint("Nguyen Hue Walking Street", "Phố đi bộ Nguyễn Huệ", 10.7744, 106.7035),
            new LocationPoint("Bui Vien Backpacker Quarter", "Phố Tây Bùi Viện", 10.7674, 106.6939),
            new LocationPoint("Landmark 81 Skyscraper", "Tòa nhà Landmark 81", 10.7950, 106.7218),
            new LocationPoint("Thu Dau Mot (Binh Duong)", "Thủ Dầu Một (Bình Dương)", 10.9804, 106.6519),
            new LocationPoint("Back Beach (Vung Tau)", "Bãi Sau (TP. Vũng Tàu)", 10.3414, 107.0942)
    ));

    public static final List<String> safetyRulesVi = Collections.unmodifiableList(Arrays.asList(
            "Luôn đối chiếu biển số xe trên ứng dụng trước khi lên xe hoặc đội mũ bảo hiểm.",
            "Tại sân bay Tân Sơn Nhất, chỉ bắt xe tại làn xếp hàng chính thức (Cột B/C/D Ga Quốc tế hoặc nhà xe TCP Ga Quốc nội) — từ chối cò mồi chèo kéo.",
            "Cẩn thận phân biệt tờ tiền polymer 20.000đ và 500.000đ (cùng tông màu xanh dương) khi trả tiền mặt buổi tối.",
            "Ngồi xe ôm công nghệ: cất điện thoại vào túi đeo chéo phía trước thay vì cầm sát mép đường."
    ));

    public static final List<String> safetyRulesEn = Collections.unmodifiableList(Arrays.asList(
            "Always match the license plate number with your app screen before boarding.",
            "At Tan Son Nhat Airport (SGN), ignore touts inside the arrival hall and walk to official Grab/Xanh SM/Vinasun/Mai Linh queues.",
            "Check zeroes carefully: 20,000 VND and 500,000 VND polymer notes are both blue-tinted.",
            "On motorbike taxis, keep your phone inside a front bag rather than holding it near passing traffic."
    ));

    private RideRepository() {}

    /**
     * Calculates urban road distance (Haversine * 1.28 road-winding factor).
     */
    public static double calculateDistanceKm(double lat1, double lng1, double lat2, double lng2) {
        double r = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2.0) * Math.sin(dLat / 2.0)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2.0) * Math.sin(dLng / 2.0);
        double straightKm = r * 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        double urbanKm = Math.max(0.8, straightKm * 1.28);
        return Math.round(urbanKm * 10.0) / 10.0;
    }

    public static RideEstimate estimateFare(RideProvider provider, double distanceKm) {
        double km = Math.max(1.0, distanceKm);
        double billableAfter2Km = Math.max(0.0, km - 2.0);

        int bikeFare = 0;
        if (provider.bikeBaseFare > 0) {
            int rawBike = (int) Math.round(provider.bikeBaseFare + billableAfter2Km * provider.bikePerKm);
            bikeFare = roundToThousand(rawBike);
        }

        int rawCar4 = (int) Math.round(provider.carBaseFare + billableAfter2Km * provider.carPerKm);
        int car4Fare = roundToThousand(rawCar4);
        int car7Fare = roundToThousand((int) Math.round(car4Fare * 1.22));
        int durationMins = Math.max(5, (int) Math.round(km * 3.2 + 3.0));

        return new RideEstimate(km, durationMins, bikeFare, car4Fare, car7Fare);
    }

    private static int roundToThousand(int amount) {
        return ((amount + 500) / 1000) * 1000;
    }
}
