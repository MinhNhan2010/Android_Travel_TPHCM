package com.example.nhom_15_dulichtphcm.data.model;

import com.example.nhom_15_dulichtphcm.util.Localized;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum UserRole {
    GUEST(
            "Khách vãng lai",
            "Guest Visitor",
            "Khách",
            "Guest",
            "👤",
            0xFF64748B,
            EnumSet.of(Permission.VIEW_PUBLIC_GUIDE)
    ),
    MEMBER(
            "Du khách Thành viên",
            "Tourist Member",
            "Thành viên",
            "Member",
            "🧳",
            0xFF0284C7,
            EnumSet.of(
                    Permission.VIEW_PUBLIC_GUIDE,
                    Permission.SAVE_PERSONAL_COLLECTION,
                    Permission.MANAGE_OWN_PHRASES,
                    Permission.POST_REVIEW
            )
    ),
    EDITOR(
            "Hướng dẫn viên / Biên tập viên",
            "Tour Guide / Content Editor",
            "Biên tập",
            "Editor",
            "✍️",
            0xFFD97706,
            EnumSet.of(
                    Permission.VIEW_PUBLIC_GUIDE,
                    Permission.SAVE_PERSONAL_COLLECTION,
                    Permission.MANAGE_OWN_PHRASES,
                    Permission.POST_REVIEW,
                    Permission.MANAGE_CONTENT,
                    Permission.MODERATE_REVIEWS
            )
    ),
    ADMIN(
            "Quản trị viên Hệ thống",
            "System Administrator",
            "Admin",
            "Admin",
            "🛡️",
            0xFFD84315,
            EnumSet.allOf(Permission.class)
    );

    public final String titleVi;
    public final String titleEn;
    public final String shortVi;
    public final String shortEn;
    public final String badgeEmoji;
    public final int badgeColorHex;
    public final Set<Permission> permissions;

    UserRole(
            String titleVi,
            String titleEn,
            String shortVi,
            String shortEn,
            String badgeEmoji,
            int badgeColorHex,
            Set<Permission> permissions
    ) {
        this.titleVi = titleVi;
        this.titleEn = titleEn;
        this.shortVi = shortVi;
        this.shortEn = shortEn;
        this.badgeEmoji = badgeEmoji;
        this.badgeColorHex = badgeColorHex;
        this.permissions = Collections.unmodifiableSet(permissions);
    }

    public boolean hasPermission(Permission permission) {
        return permission != null && permissions.contains(permission);
    }

    public boolean canAccessConsole() {
        return this == EDITOR || this == ADMIN;
    }

    public String localizedTitle() {
        return Localized.tr(titleVi, titleEn);
    }

    public String localizedShort() {
        return Localized.tr(shortVi, shortEn);
    }

    public static UserRole fromName(String raw) {
        if (raw == null) return GUEST;
        for (UserRole role : values()) {
            if (role.name().equalsIgnoreCase(raw.trim())) {
                return role;
            }
        }
        return GUEST;
    }
}
