package com.example.nhom_15_dulichtphcm.data.model;

public final class PlaceReview {
    public final String id;
    public final String targetId;
    public final String authorId;
    public final String authorName;
    public final UserRole authorRole;
    public final int rating;
    public final String comment;
    public final long createdAt;

    public PlaceReview(
            String id,
            String targetId,
            String authorId,
            String authorName,
            UserRole authorRole,
            int rating,
            String comment,
            long createdAt
    ) {
        this.id = id != null ? id : "";
        this.targetId = targetId != null ? targetId : "";
        this.authorId = authorId != null ? authorId : "";
        this.authorName = authorName != null ? authorName : "";
        this.authorRole = authorRole != null ? authorRole : UserRole.MEMBER;
        this.rating = rating;
        this.comment = comment != null ? comment : "";
        this.createdAt = createdAt;
    }
}
