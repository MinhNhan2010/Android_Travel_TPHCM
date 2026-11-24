# Explore Saigon – Cẩm nang Du lịch TP.HCM (Nhóm 15)

![Platform](https://img.shields.io/badge/Platform-Android%20(API%2024%2B)-3DDC84?logo=android&logoColor=white)
![Language](https://img.shields.io/badge/Language-Java%2011-ED8B00?logo=openjdk&logoColor=white)
![Database](https://img.shields.io/badge/SQLite-18%20Tables%20(v4)-003B57?logo=sqlite&logoColor=white)
![AI](https://img.shields.io/badge/AI-Gemini%20%2B%20Offline%20RAG-4285F4?logo=googlegemini&logoColor=white)
![Gradle](https://img.shields.io/badge/AGP%209.5.1-Gradle%209.6.0-02303A?logo=gradle&logoColor=white)

Ứng dụng Android hỗ trợ khám phá và du lịch **Thành phố Hồ Chí Minh** (cùng các vùng kết nối Bình Dương & Vũng Tàu), được xây dựng bằng **Java thuần + Android XML Layouts**, lưu trữ dữ liệu cục bộ bằng **SQLite** và tích hợp **Trợ lý Du lịch AI** (Google Gemini kết hợp cơ chế dự phòng ngoại tuyến).

---

## Tính năng nổi bật

### 5 Tab điều hướng chính
1. **Khám phá Địa điểm (`AttractionListFragment`)**: Danh sách 26 địa điểm tham quan kèm chi tiết lịch sử, văn hóa, giá vé, giờ mở cửa, mẹo cho du khách, đánh giá/bình luận và bộ lọc theo khu vực (`RegionZone`) hoặc khoảng cách gần vị trí hiện tại.
2. **Ẩm thực Sài Gòn (`FoodListFragment`)**: Giới thiệu 9 món ăn đặc sản cùng câu chuyện ẩm thực, nguyên liệu, cách thưởng thức và gợi ý 20 nhà hàng/quán ăn tiêu biểu có tọa độ chỉ đường.
3. **Tiện ích Công cộng (`PublicUtilitiesFragment`)**: Tra cứu 17 tiện ích thiết yếu (bệnh viện, đồn công an, điểm đổi tiền, bưu điện, nhà ga, trung tâm hỗ trợ du khách...) kèm nút gọi điện và mở bản đồ.
4. **Sổ tay Giao tiếp (`PhrasesFragment`)**: 25 mẫu câu giao tiếp du lịch thông dụng, hỗ trợ phát âm **Text-to-Speech (TTS)**, chế độ phóng to toàn màn hình **"Đưa cho người bản xứ xem" (Show to Local)** và lưu câu nói cá nhân.
5. **Bản đồ Tương tác (`CityMapFragment`)**: Bản đồ Leaflet + MarkerCluster nhúng trong `WebView` (`MapHtmlBuilder`), hỗ trợ nhiều lớp nền (Google Maps, Giao thông, Vệ tinh, Nền tối với fallback OSM/Esri), 13 polygon ranh giới hành chính sau sáp nhập, chỉ đường OSRM và tự động khám phá địa điểm mới qua Wikipedia GeoSearch.

### Trợ lý AI & Các màn hình chuyên sâu
- **Trợ lý Du lịch AI (`AiAssistantFragment`)**: Kết hợp **Gemini REST API** (hỗ trợ failover đa model) với **Offline RAG (`AiTravelEngine`)** tra cứu trực tiếp dữ liệu SQLite, ghi nhớ ngữ cảnh hội thoại và gắn thẻ địa điểm/món ăn tương tác.
- **Lập Lịch trình Thông minh (`TripPlannerFragment`)**: Tạo, sắp xếp và tối ưu hóa lịch trình tham quan theo ngày (`TripOptimizer`) kèm ví vé QR (`TicketWalletManager`).
- **Metro Số 1 & Saigon Waterbus (`MetroWaterbusFragment`)**: Tra cứu lộ trình 14 ga Tuyến Metro số 1 (Bến Thành – Suối Tiên), 5 bến Saigon Waterbus (Bạch Đằng – Linh Đông), tính giá vé (tiền mặt / không tiền mặt) và thời gian di chuyển ước tính.
- **Đặt xe & Ước tính cước (`RideBookingFragment`)**: So sánh giá cước ước tính giữa 5 hãng phổ biến tại Việt Nam (**Xanh SM**, **Grab**, **Be**, **Mai Linh**, **Vinasun**), kèm thẻ câu giao tiếp cho tài xế và mở ứng dụng / CH Play trực tiếp.
- **Bộ Công cụ Du lịch (`TravelToolkitFragment`)**:
  - Thời tiết trực tuyến (Open-Meteo) & Quy đổi tỷ giá VND (ExchangeRate-API) có bộ nhớ đệm SQLite.
  - Hộ chiếu Du lịch số (`TravelPassportManager`) tích dấu check-in và xếp hạng nhà thám hiểm (`ExplorerRank`).
  - Quản lý chi tiêu & chia tiền nhóm (`TripExpenseManager`).
  - Danh sách hành lý theo khu vực (`PackingListManager`), Nhật ký hành trình (`TravelJournalManager`) và Sao lưu/Khôi phục dữ liệu JSON (`UserDataBackupManager`).
- **Trung tâm An toàn & SOS (`SafetyHubFragment`)**: Số khẩn cấp, danh bạ lãnh sự quán tại TP.HCM, mẹo phòng tránh lừa đảo và thẻ cảnh báo dị ứng/chế độ ăn song ngữ cho đầu bếp.
- **Trắc nghiệm Khám phá (`DiscoveryQuizFragment`) & Bộ sưu tập Yêu thích (`FavoritesFragment`)**.
- **Đa ngôn ngữ & Giao diện**: Hỗ trợ 6 ngôn ngữ (`Tiếng Việt`, `English`, `Français`, `日本語`, `한국어`, `中文`) và chuyển đổi chế độ **Sáng / Tối** (`ThemeManager`).

---

## Kiến trúc & Công nghệ

### Tech Stack
| Thành phần | Chi tiết |
| :--- | :--- |
| Ngôn ngữ | Java 11 (`android.builtInKotlin=false`) |
| Giao diện | Android XML Layouts, AndroidX (`AppCompat`, `Fragment`, `RecyclerView`, `CoordinatorLayout`, `SwipeRefreshLayout`, `WebKit`) |
| Hệ thống Build | Android Gradle Plugin `9.5.1`, Gradle `9.6.0` |
| SDK | `minSdk = 24`, `targetSdk = 36`, `compileSdk = 37` |
| Cơ sở dữ liệu | SQLite (`SQLiteOpenHelper` – `saigon_travel.db`, version `4`) |

### Tích hợp API bên ngoài & Cơ chế Offline Fallback
| Dịch vụ | Nguồn / Endpoint | Dự phòng Offline |
| :--- | :--- | :--- |
| Trợ lý AI (`GeminiService`) | `generativelanguage.googleapis.com` (`gemini-3-flash-preview`, `gemini-3.1-flash-lite-preview`, `gemini-flash-lite-latest`, `gemini-3.8-flash`) | `AiTravelEngine` (RAG cục bộ từ SQLite) |
| Bản đồ & Chỉ đường (`MapHtmlBuilder`, `TravelDataService`) | Google Map Tiles, `router.project-osrm.org`, `vi.wikipedia.org` GeoSearch | OSM / Esri tiles & danh sách địa điểm SQLite |
| Thời tiết (`TravelDataService`) | `api.open-meteo.com` | Dữ liệu cache / mặc định cục bộ |
| Tỷ giá ngoại tệ (`TravelDataService`) | `open.er-api.com` | Tỷ giá tham chiếu cached trong `app_settings` |

### Cấu trúc CSDL SQLite (`saigon_travel.db` – 18 bảng)
- **Nội dung du lịch**: `attractions`, `foods`, `restaurants`, `utilities`, `phrases`
- **Tương tác & cá nhân hóa**: `saved_phrases`, `favorites`, `reviews`, `trip_stops`, `trip_settings`
- **Tiện ích & hành trình**: `passport_stamps`, `expenses`, `tickets`, `travel_journals`, `packing_items`
- **Hệ thống & bảo mật**: `users`, `audit_logs`, `app_settings`

### Cấu trúc thư mục mã nguồn
```text
app/src/main/java/com/example/nhom_15_dulichtphcm/
├── MainActivity.java              # Host 5 tab chính, điều hướng Fragment & khởi tạo dịch vụ
├── data/
│   ├── ai/                        # GeminiService, AiTravelEngine (RAG), AiChatSession, TripOptimizer
│   ├── db/                        # SaigonTravelContract (18 bảng), SaigonTravelDatabaseHelper
│   ├── local/                     # AuthManager, Favorites, TripPlan, Passport, Expense, Ticket, Journal, Packing, Backup
│   ├── model/                     # Các Entity & Enum (Attraction, SpecialtyFood, UserRole, RegionZone...)
│   ├── remote/                    # TravelDataService (Open-Meteo, ExchangeRate, OSRM, Wikipedia)
│   └── repository/                # Repository & SeedData (Địa điểm, Ẩm thực, Tiện ích, Giao tiếp, Metro, Đặt xe)
├── ui/
│   ├── AppNavigator.java          # Giao diện điều hướng màn hình
│   ├── adapters/                  # RecyclerView Adapters
│   ├── components/                # MapHtmlBuilder, DialogHelper, ExperienceDialogs, QrBitmapGenerator, AppIcons2D...
│   └── screens/                   # 17 Fragments màn hình chức năng & AdminDashboardFragment
└── util/                          # Đa ngôn ngữ (6 ngôn ngữ), ThemeManager, LocationHelper, SpeechHelper, ImageLoader...
```

---

## Hệ thống Phân quyền (RBAC) & Tài khoản Demo

Ứng dụng triển khai phân quyền **4 cấp độ** qua `AuthManager` (băm mật khẩu **PBKDF2** 12.000 vòng lặp kèm salt ngẫu nhiên và ghi nhận `audit_logs`):

| Vai trò | Quyền hạn chính | Tài khoản demo (`username` / `password`) |
| :--- | :--- | :--- |
| **Guest** (`GUEST`) | Xem cẩm nang công khai | Không cần đăng nhập |
| **Member** (`MEMBER`) | Xem cẩm nang, lưu yêu thích, quản lý câu nói cá nhân, đăng đánh giá | `tourist` / `user123` |
| **Editor** (`EDITOR`) | Quyền Member + Quản lý nội dung CMS & kiểm duyệt đánh giá | `guide` / `guide123` |
| **Admin** (`ADMIN`) | Toàn quyền hệ thống, quản lý người dùng, xem Audit Log & cấu hình | `admin` / `admin123` |

---

## Hướng dẫn Cài đặt & Chạy ứng dụng

### Yêu cầu môi trường
- **Android Studio** (phiên bản hỗ trợ AGP 9.5.1 / Gradle 9.6.0)
- **JDK 11** trở lên
- **Android SDK** (`compileSdk 37`, `targetSdk 36`, `minSdk 24`)

### Các bước cài đặt
1. **Clone repository:**
   ```bash
   git clone https://github.com/MinhNhan2010/Android_Travel_TPHCM.git
   cd Android_Travel_TPHCM
   ```
2. **Cấu hình `local.properties` (tùy chọn cho Gemini AI):**
   Mở hoặc tạo file `local.properties` ở thư mục gốc (đã được `.gitignore` loại trừ) và thêm khóa API nếu muốn tích hợp sẵn khi build:
   ```properties
   sdk.dir=C:\\Users\\<TEN_USER>\\AppData\\Local\\Android\\Sdk
   GEMINI_API_KEY=your_gemini_api_key_here
   ```
   > **Lưu ý:** Nếu không cấu hình `GEMINI_API_KEY` trong `local.properties`, bạn vẫn có thể nhập API key trực tiếp trong màn hình Trợ lý AI / Admin Dashboard hoặc sử dụng chế độ **Offline RAG** mặc định.
3. **Kiểm tra đường dẫn Java trong `gradle.properties`:**
   File `gradle.properties` hiện khai báo `org.gradle.java.home=C:\\Program Files\\Android\\Android Studio\\jbr`. Nếu cài Android Studio ở thư mục khác hoặc chạy trên macOS/Linux, hãy điều chỉnh lại cho phù hợp với máy của bạn.
4. **Build APK Debug bằng Gradle Wrapper:**
   - Windows (PowerShell / CMD):
     ```powershell
     .\gradlew.bat assembleDebug
     ```
   - macOS / Linux:
     ```bash
     ./gradlew assembleDebug
     ```

---

## Kiểm thử (Testing)

Dự án bao gồm **6 bộ JVM Unit Test** trong `app/src/test/java/com/example/nhom_15_dulichtphcm/` và **1 bộ Instrumented Test** trong `app/src/androidTest/`:
- `AppValidationTest` – Kiểm tra tính hợp lệ dữ liệu ứng dụng
- `IconAsset2DTest` – Kiểm tra tài nguyên icon 2D
- `MapAutoSyncTest` – Kiểm tra đồng bộ bản đồ & dữ liệu khám phá mới
- `PassportAndExpenseTest` – Kiểm tra logic hộ chiếu du lịch & chia chi tiêu
- `RolePermissionTest` – Kiểm tra phân quyền RBAC 4 cấp độ
- `SqliteDatabaseSchemaTest` – Kiểm tra cấu trúc 18 bảng SQLite (`SaigonTravelContract`)

Chạy toàn bộ Unit Test bằng lệnh:
```bash
./gradlew testDebugUnitTest
```
*(Trên Windows dùng `.\gradlew.bat testDebugUnitTest`)*
