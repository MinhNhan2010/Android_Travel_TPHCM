package com.example.nhom_15_dulichtphcm.data.model;

import com.example.nhom_15_dulichtphcm.util.Localized;

public enum Permission {
    VIEW_PUBLIC_GUIDE(
            "Tra cứu cẩm nang, bản đồ & AI",
            "Browse travel guide, map & AI",
            "Xem địa điểm, món ăn, tiện ích công cộng, bản đồ và hỏi đáp trợ lý du lịch.",
            "View attractions, specialty dishes, public utilities, map, and AI travel guide."
    ),
    SAVE_PERSONAL_COLLECTION(
            "Lưu yêu thích, lịch trình & Hộ chiếu",
            "Save favorites, trip plan & Passport",
            "Đánh dấu địa điểm/món ăn yêu thích, lập lịch trình cá nhân và đóng dấu Check-in.",
            "Bookmark favorite places/foods, build personal itineraries, and collect check-in stamps."
    ),
    MANAGE_OWN_PHRASES(
            "Tạo sổ câu giao tiếp cá nhân",
            "Create custom Vietnamese phrases",
            "Thêm và quản lý các câu giao tiếp tiếng Việt riêng của bạn.",
            "Add and manage your own custom Vietnamese survival phrases."
    ),
    POST_REVIEW(
            "Đăng đánh giá & bình luận",
            "Post ratings & reviews",
            "Viết nhận xét và chấm sao thực tế cho các địa điểm tham quan.",
            "Write community reviews and star ratings for tourist attractions."
    ),
    MANAGE_CONTENT(
            "Biên tập nội dung du lịch (CMS)",
            "Manage travel content (CMS)",
            "Thêm mới hoặc cập nhật thông tin Địa điểm, Món ăn và Tiện ích công cộng.",
            "Create or edit Attractions, Specialty Dishes, and Public Utilities."
    ),
    MODERATE_REVIEWS(
            "Kiểm duyệt bình luận cộng đồng",
            "Moderate community reviews",
            "Gỡ bỏ các bình luận spam hoặc không phù hợp của người dùng khác.",
            "Remove spam or inappropriate reviews posted by other users."
    ),
    DELETE_CONTENT(
            "Xóa dữ liệu gốc & khôi phục mặc định",
            "Delete master data & factory reset",
            "Xóa vĩnh viễn địa điểm/món ăn/tiện ích hoặc đặt lại dữ liệu hệ thống.",
            "Permanently delete master entries or restore factory default content."
    ),
    MANAGE_USERS(
            "Quản lý tài khoản & cấp quyền RBAC",
            "Manage user accounts & RBAC roles",
            "Tạo người dùng, cấp đổi vai trò (Member/Editor/Admin), khóa hoặc xóa tài khoản.",
            "Create accounts, assign roles (Member/Editor/Admin), lock or delete users."
    ),
    CONFIGURE_SYSTEM(
            "Cấu hình hệ thống & Gemini API Key",
            "System settings & Gemini API Key",
            "Thay đổi khóa Gemini AI toàn cục và xem nhật ký kiểm toán hệ thống.",
            "Configure global Gemini AI credentials and inspect system audit logs."
    );

    public final String titleVi;
    public final String titleEn;
    public final String descVi;
    public final String descEn;

    Permission(String titleVi, String titleEn, String descVi, String descEn) {
        this.titleVi = titleVi;
        this.titleEn = titleEn;
        this.descVi = descVi;
        this.descEn = descEn;
    }

    public String localizedTitle() {
        return Localized.tr(titleVi, titleEn);
    }

    public String localizedDesc() {
        return Localized.tr(descVi, descEn);
    }
}
