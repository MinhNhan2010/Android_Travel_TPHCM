package com.example.nhom_15_dulichtphcm.data.local;

import android.content.Context;

import com.example.nhom_15_dulichtphcm.data.db.SaigonTravelDatabaseHelper;
import com.example.nhom_15_dulichtphcm.data.model.Permission;
import com.example.nhom_15_dulichtphcm.data.model.PlaceReview;
import com.example.nhom_15_dulichtphcm.data.model.ResultBox;
import com.example.nhom_15_dulichtphcm.data.model.UserAccount;
import com.example.nhom_15_dulichtphcm.data.model.UserRole;
import com.example.nhom_15_dulichtphcm.util.Localized;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Community ratings & reviews manager backed by SQLite table reviews (saigon_travel.db).
 */
public final class ReviewManager {

    private static final List<PlaceReview> reviews = new ArrayList<>();

    static {
        seedDefaultReviews();
    }

    private ReviewManager() {}

    public static synchronized void initialize(Context context) {
        if (context == null) return;
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.getInstance(context);
        List<PlaceReview> stored = db.getAllReviews();
        reviews.clear();
        if (stored.isEmpty()) {
            seedDefaultReviews();
            db.replaceAllReviews(reviews);
        } else {
            reviews.addAll(stored);
        }
    }

    public static synchronized void resetForTesting() {
        reviews.clear();
        seedDefaultReviews();
    }

    private static void seedDefaultReviews() {
        long now = System.currentTimeMillis();
        reviews.add(new PlaceReview(
                "rev_1",
                "notre_dame",
                "u_guide",
                "Hướng dẫn viên Sài Gòn",
                UserRole.EDITOR,
                5,
                "Nên ghé lúc 7h30 sáng kết hợp uống cà phê bệt Công xã Paris và gửi bưu thiếp ở Bưu điện Trung tâm ngay đối diện!",
                now - 86_400_000L * 3
        ));
        reviews.add(new PlaceReview(
                "rev_2",
                "ben_thanh_market",
                "u_tourist",
                "Du khách Quốc tế",
                UserRole.MEMBER,
                5,
                "Vibrant local market! Great chè and bánh bèo inside the food court, and Metro Line 1 station is right outside.",
                now - 86_400_000L * 2
        ));
        reviews.add(new PlaceReview(
                "rev_3",
                "independence_palace",
                "u_guide",
                "Hướng dẫn viên Sài Gòn",
                UserRole.EDITOR,
                5,
                "Đừng bỏ qua tầng hầm chỉ huy kiên cố và phòng khánh tiết thiết kế theo triết lý phong thủy phương Đông của KTS Ngô Viết Thụ.",
                now - 86_400_000L
        ));
    }

    public static synchronized List<PlaceReview> getReviews() {
        return Collections.unmodifiableList(new ArrayList<>(reviews));
    }

    public static synchronized List<PlaceReview> getReviewsForTarget(String targetId) {
        List<PlaceReview> result = new ArrayList<>();
        if (targetId == null) return result;
        for (PlaceReview r : reviews) {
            if (targetId.equals(r.targetId)) {
                result.add(r);
            }
        }
        return result;
    }

    public static synchronized double averageRating(String targetId) {
        List<PlaceReview> list = getReviewsForTarget(targetId);
        if (list.isEmpty()) return 0.0;
        double sum = 0.0;
        for (PlaceReview r : list) sum += r.rating;
        return Math.round((sum / list.size()) * 10.0) / 10.0;
    }

    public static synchronized int countByUser(String userId) {
        if (userId == null) return 0;
        int count = 0;
        for (PlaceReview r : reviews) {
            if (userId.equals(r.authorId)) count++;
        }
        return count;
    }

    public static synchronized ResultBox<PlaceReview> addReview(String targetId, int rating, String comment) {
        ResultBox<Void> perm = AuthManager.requirePermission(Permission.POST_REVIEW);
        if (perm.isFailure()) return ResultBox.failure(perm.exceptionOrNull());

        UserAccount author = AuthManager.getCurrentUser();
        if (author == null) {
            return ResultBox.failure(new SecurityException("Sign in required"));
        }
        String cleanComment = comment != null ? comment.trim() : "";
        if (cleanComment.length() < 4) {
            return ResultBox.failureMessage(Localized.tr(
                    "Nội dung bình luận tối thiểu 4 ký tự",
                    "Comment must be at least 4 characters"));
        }
        int clampedRating = Math.max(1, Math.min(5, rating));
        PlaceReview created = new PlaceReview(
                "rev_" + UUID.randomUUID().toString().substring(0, 8),
                targetId,
                author.id,
                author.fullName,
                author.role,
                clampedRating,
                cleanComment,
                System.currentTimeMillis()
        );
        reviews.add(0, created);
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            db.insertReview(created);
        }
        AuthManager.recordAudit(
                "Đăng đánh giá " + clampedRating + "★ cho " + targetId,
                "Posted " + clampedRating + "★ review on " + targetId
        );
        return ResultBox.success(created);
    }

    public static synchronized ResultBox<Void> deleteReview(String reviewId) {
        PlaceReview target = null;
        for (PlaceReview r : reviews) {
            if (r.id.equals(reviewId)) {
                target = r;
                break;
            }
        }
        if (target == null) return ResultBox.failureMessage("Review not found");

        UserAccount current = AuthManager.getCurrentUser();
        boolean isOwnReview = current != null && current.id.equals(target.authorId);
        boolean canModerate = AuthManager.hasPermission(Permission.MODERATE_REVIEWS);

        if (!isOwnReview && !canModerate) {
            return ResultBox.failure(new SecurityException(Localized.tr(
                    "Bạn chỉ có thể xóa bình luận của chính mình (hoặc cần quyền Kiểm duyệt viên)",
                    "You can only delete your own review (or need Moderator permission)")));
        }

        reviews.remove(target);
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            db.deleteReview(target.id);
        }
        AuthManager.recordAudit(
                "Xóa bình luận của " + target.authorName,
                "Removed review by " + target.authorName
        );
        return ResultBox.success(null);
    }
}
