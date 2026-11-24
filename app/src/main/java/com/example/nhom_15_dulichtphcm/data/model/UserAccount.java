package com.example.nhom_15_dulichtphcm.data.model;

import java.util.Objects;

public final class UserAccount {
    public final String id;
    public final String username;
    public final String fullName;
    public final String email;
    public final String passwordHash;
    public final String passwordSalt;
    public final UserRole role;
    public final String avatarEmoji;
    public final String avatarPhotoPath;
    public final boolean isLocked;
    public final long createdAt;
    public final AuthProvider authProvider;
    public final String providerAccountId;

    public UserAccount(
            String id,
            String username,
            String fullName,
            String email,
            String passwordHash,
            String passwordSalt,
            UserRole role,
            String avatarEmoji,
            boolean isLocked,
            long createdAt,
            AuthProvider authProvider,
            String providerAccountId
    ) {
        this(id, username, fullName, email, passwordHash, passwordSalt,
                role, avatarEmoji, "", isLocked, createdAt, authProvider, providerAccountId);
    }

    public UserAccount(
            String id,
            String username,
            String fullName,
            String email,
            String passwordHash,
            String passwordSalt,
            UserRole role,
            String avatarEmoji,
            String avatarPhotoPath,
            boolean isLocked,
            long createdAt,
            AuthProvider authProvider,
            String providerAccountId
    ) {
        this.id = id != null ? id : "";
        this.username = username != null ? username : "";
        this.fullName = fullName != null ? fullName : "";
        this.email = email != null ? email : "";
        this.passwordHash = passwordHash != null ? passwordHash : "";
        this.passwordSalt = passwordSalt != null ? passwordSalt : "";
        this.role = role != null ? role : UserRole.MEMBER;
        this.avatarEmoji = (avatarEmoji != null && !avatarEmoji.isEmpty()) ? avatarEmoji : "🧳";
        this.avatarPhotoPath = avatarPhotoPath != null ? avatarPhotoPath.trim() : "";
        this.isLocked = isLocked;
        this.createdAt = createdAt;
        this.authProvider = authProvider != null ? authProvider : AuthProvider.LOCAL;
        this.providerAccountId = providerAccountId != null ? providerAccountId : "";
    }

    public boolean hasAvatarPhoto() {
        return !avatarPhotoPath.isEmpty();
    }

    public UserAccount withRole(UserRole newRole) {
        return new UserAccount(id, username, fullName, email, passwordHash, passwordSalt,
                newRole, avatarEmoji, avatarPhotoPath, isLocked, createdAt, authProvider, providerAccountId);
    }

    public UserAccount withLocked(boolean locked) {
        return new UserAccount(id, username, fullName, email, passwordHash, passwordSalt,
                role, avatarEmoji, avatarPhotoPath, locked, createdAt, authProvider, providerAccountId);
    }

    public UserAccount withProfile(String newFullName, String newEmoji) {
        return new UserAccount(id, username, newFullName, email, passwordHash, passwordSalt,
                role, newEmoji, avatarPhotoPath, isLocked, createdAt, authProvider, providerAccountId);
    }

    public UserAccount withProfileDetails(String newFullName, String newEmail, String newEmoji) {
        return new UserAccount(id, username, newFullName, newEmail, passwordHash, passwordSalt,
                role, newEmoji, avatarPhotoPath, isLocked, createdAt, authProvider, providerAccountId);
    }

    public UserAccount withProfileDetails(String newFullName, String newEmail, String newEmoji, String newPhotoPath) {
        return new UserAccount(id, username, newFullName, newEmail, passwordHash, passwordSalt,
                role, newEmoji, newPhotoPath, isLocked, createdAt, authProvider, providerAccountId);
    }

    public UserAccount withAvatarPhoto(String newPhotoPath) {
        return new UserAccount(id, username, fullName, email, passwordHash, passwordSalt,
                role, avatarEmoji, newPhotoPath, isLocked, createdAt, authProvider, providerAccountId);
    }

    public UserAccount withPassword(String newHash, String newSalt) {
        return new UserAccount(id, username, fullName, email, newHash, newSalt,
                role, avatarEmoji, avatarPhotoPath, isLocked, createdAt, authProvider, providerAccountId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserAccount)) return false;
        UserAccount that = (UserAccount) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
