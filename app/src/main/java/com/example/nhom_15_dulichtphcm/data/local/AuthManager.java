package com.example.nhom_15_dulichtphcm.data.local;

import android.content.Context;

import com.example.nhom_15_dulichtphcm.data.db.SaigonTravelDatabaseHelper;
import com.example.nhom_15_dulichtphcm.data.model.AuditLogEntry;
import com.example.nhom_15_dulichtphcm.data.model.AuthProvider;
import com.example.nhom_15_dulichtphcm.data.model.Permission;
import com.example.nhom_15_dulichtphcm.data.model.ResultBox;
import com.example.nhom_15_dulichtphcm.data.model.UserAccount;
import com.example.nhom_15_dulichtphcm.data.model.UserRole;
import com.example.nhom_15_dulichtphcm.util.Localized;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * RBAC Authentication & Account Manager persisted in SQLite (saigon_travel.db: users, audit_logs, app_settings).
 */
public final class AuthManager {

    private static final String KEY_CURRENT_USER_ID = "current_user_id";
    private static final String KEY_REMEMBER_ME_ENABLED = "auth_remember_me_enabled";
    private static final String KEY_SAVED_LOGINS_RAW = "auth_saved_logins_v1";
    private static final byte[] OBFUSCATION_MASK = "SaigonTravelGuide_SQLite_2026".getBytes(StandardCharsets.UTF_8);
    private static final int PBKDF2_ITERATIONS = 12_000;
    private static final int KEY_LENGTH_BITS = 256;

    public static final class SavedLoginItem {
        public final UserAccount account;
        public final String username;
        public final String savedPassword;
        public final long savedAt;

        public SavedLoginItem(UserAccount account, String username, String savedPassword, long savedAt) {
            this.account = account;
            this.username = username != null ? username : "";
            this.savedPassword = savedPassword != null ? savedPassword : "";
            this.savedAt = savedAt;
        }
    }

    private static final class RawSavedCredential {
        final String userId;
        final String username;
        final String password;
        final long savedAt;

        RawSavedCredential(String userId, String username, String password, long savedAt) {
            this.userId = userId;
            this.username = username;
            this.password = password;
            this.savedAt = savedAt;
        }
    }

    private static final List<UserAccount> users = new ArrayList<>();
    private static final List<AuditLogEntry> auditLogs = new ArrayList<>();
    private static final List<RawSavedCredential> savedCredentials = new ArrayList<>();
    private static volatile boolean rememberMeEnabled = true;
    private static volatile UserAccount currentUser = null;
    private static final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    static {
        seedDefaultAccountsInMemory();
    }

    private AuthManager() {}

    public static synchronized void initialize(Context context) {
        if (context == null) return;
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.getInstance(context);
        List<UserAccount> storedUsers = db.getAllUsers();
        users.clear();
        if (storedUsers.isEmpty()) {
            seedDefaultAccountsInMemory();
            db.replaceAllUsers(users);
            for (AuditLogEntry log : auditLogs) {
                db.insertAuditLog(log);
            }
        } else {
            users.addAll(storedUsers);
            auditLogs.clear();
            auditLogs.addAll(db.getAuditLogs(100));
        }

        rememberMeEnabled = !"false".equalsIgnoreCase(db.getSetting(KEY_REMEMBER_ME_ENABLED, "true"));
        deserializeSavedCredentials(db.getSetting(KEY_SAVED_LOGINS_RAW, ""));

        String savedUserId = db.getSetting(KEY_CURRENT_USER_ID, null);
        currentUser = null;
        if (savedUserId != null && !savedUserId.isEmpty()) {
            for (UserAccount acc : users) {
                if (acc.id.equals(savedUserId) && !acc.isLocked) {
                    currentUser = acc;
                    break;
                }
            }
        }
        notifyDependentScopes();
        notifyListeners();
    }

    public static synchronized void resetForTesting() {
        users.clear();
        auditLogs.clear();
        savedCredentials.clear();
        rememberMeEnabled = true;
        currentUser = null;
        seedDefaultAccountsInMemory();
        notifyDependentScopes();
    }

    private static void seedDefaultAccountsInMemory() {
        long now = System.currentTimeMillis();
        users.add(buildLocalAccount("u_admin", "admin", "Quản trị Hệ thống", "admin@dulichtphcm.vn", "admin123", UserRole.ADMIN, "🛡️", now - 30_000L));
        users.add(buildLocalAccount("u_guide", "guide", "Hướng dẫn viên Sài Gòn", "guide@dulichtphcm.vn", "guide123", UserRole.EDITOR, "✍️", now - 20_000L));
        users.add(buildLocalAccount("u_tourist", "tourist", "Du khách Quốc tế", "tourist@dulichtphcm.vn", "user123", UserRole.MEMBER, "🧳", now - 10_000L));
        auditLogs.add(new AuditLogEntry(
                "log_init",
                "system",
                UserRole.ADMIN,
                "Khởi tạo hệ thống phân quyền RBAC 4 cấp độ",
                "Initialized 4-tier RBAC security system",
                now
        ));
    }

    private static UserAccount buildLocalAccount(
            String id,
            String username,
            String fullName,
            String email,
            String rawPassword,
            UserRole role,
            String emoji,
            long createdAt
    ) {
        String salt = generateSaltHex();
        String hash = hashPasswordHex(rawPassword, salt);
        return new UserAccount(id, username, fullName, email, hash, salt, role, emoji, false, createdAt, AuthProvider.LOCAL, "");
    }

    public static UserAccount getCurrentUser() {
        return currentUser;
    }

    public static UserRole getCurrentRole() {
        UserAccount u = currentUser;
        return u != null ? u.role : UserRole.GUEST;
    }

    public static String getActiveUserScope() {
        UserAccount u = currentUser;
        return u != null ? u.id : "guest";
    }

    public static synchronized List<UserAccount> getUsers() {
        return Collections.unmodifiableList(new ArrayList<>(users));
    }

    public static synchronized List<AuditLogEntry> getAuditLogs() {
        return Collections.unmodifiableList(new ArrayList<>(auditLogs));
    }

    public static boolean hasPermission(Permission permission) {
        return getCurrentRole().hasPermission(permission);
    }

    public static ResultBox<Void> requirePermission(Permission permission) {
        if (hasPermission(permission)) {
            return ResultBox.success(null);
        }
        String msg = Localized.tr(
                "Từ chối truy cập: Yêu cầu quyền [" + permission.titleVi + "]. Vai trò hiện tại: " + getCurrentRole().titleVi,
                "Access denied: Missing permission [" + permission.titleEn + "]. Active role: " + getCurrentRole().titleEn
        );
        return ResultBox.failure(new SecurityException(msg));
    }

    public static synchronized boolean isRememberMeEnabled() {
        return rememberMeEnabled;
    }

    public static synchronized void setRememberMeEnabled(boolean enabled) {
        rememberMeEnabled = enabled;
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            db.putSetting(KEY_REMEMBER_ME_ENABLED, String.valueOf(enabled));
        }
    }

    public static synchronized List<SavedLoginItem> getSavedLogins() {
        List<SavedLoginItem> out = new ArrayList<>();
        for (RawSavedCredential raw : savedCredentials) {
            UserAccount acc = findUserById(raw.userId);
            if (acc != null && !acc.isLocked) {
                out.add(new SavedLoginItem(acc, raw.username, raw.password, raw.savedAt));
            }
        }
        return Collections.unmodifiableList(out);
    }

    public static synchronized SavedLoginItem getPrimarySavedLogin() {
        List<SavedLoginItem> list = getSavedLogins();
        return list.isEmpty() ? null : list.get(0);
    }

    public static synchronized void removeSavedLogin(String userId) {
        if (userId == null) return;
        boolean changed = savedCredentials.removeIf(c -> userId.equals(c.userId));
        if (changed) {
            persistSavedCredentials();
            notifyListeners();
        }
    }

    public static synchronized ResultBox<UserAccount> loginWithSavedAccount(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return ResultBox.failureMessage(Localized.tr("Tài khoản đã lưu không tồn tại", "Saved login not found"));
        }
        RawSavedCredential matched = null;
        for (RawSavedCredential c : savedCredentials) {
            if (userId.equals(c.userId)) {
                matched = c;
                break;
            }
        }
        if (matched == null) {
            return ResultBox.failureMessage(Localized.tr("Thông tin đăng nhập đã hết hạn", "Saved login expired"));
        }
        return login(matched.username, matched.password, true);
    }

    public static synchronized ResultBox<UserAccount> login(String usernameOrEmail, String password) {
        return login(usernameOrEmail, password, rememberMeEnabled);
    }

    public static synchronized ResultBox<UserAccount> login(
            String usernameOrEmail,
            String password,
            boolean rememberLogin
    ) {
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()
                || password == null || password.isEmpty()) {
            return ResultBox.failureMessage(Localized.tr(
                    "Vui lòng nhập đầy đủ tài khoản và mật khẩu",
                    "Please enter both username and password"));
        }
        String key = usernameOrEmail.trim().toLowerCase(Locale.ROOT);
        UserAccount found = null;
        for (UserAccount acc : users) {
            if (acc.username.equalsIgnoreCase(key)
                    || (!acc.email.isEmpty() && acc.email.equalsIgnoreCase(key))) {
                found = acc;
                break;
            }
        }
        if (found == null) {
            return ResultBox.failureMessage(Localized.tr(
                    "Không tìm thấy tài khoản phù hợp",
                    "Account not found"));
        }
        if (found.isLocked) {
            return ResultBox.failureMessage(Localized.tr(
                    "Tài khoản này đã bị Quản trị viên khóa",
                    "This account has been locked by Administrator"));
        }
        String computed = hashPasswordHex(password, found.passwordSalt);
        if (!constantTimeEquals(computed, found.passwordHash)) {
            return ResultBox.failureMessage(Localized.tr(
                    "Mật khẩu không chính xác",
                    "Incorrect password"));
        }
        setRememberMeEnabled(rememberLogin);
        if (rememberLogin) {
            upsertSavedCredential(found.id, found.username, password);
        } else {
            removeSavedLogin(found.id);
        }
        setActiveSession(found, rememberLogin);
        recordAudit(
                "Đăng nhập thành công với vai trò " + found.role.shortVi,
                "Signed in as " + found.role.shortEn
        );
        return ResultBox.success(found);
    }

    public static synchronized ResultBox<UserAccount> register(
            String fullName,
            String username,
            String email,
            String password
    ) {
        return register(fullName, username, email, password, rememberMeEnabled);
    }

    public static synchronized ResultBox<UserAccount> register(
            String fullName,
            String username,
            String email,
            String password,
            boolean rememberLogin
    ) {
        String cleanUser = username != null ? username.trim().toLowerCase(Locale.ROOT) : "";
        String cleanName = fullName != null ? fullName.trim() : "";
        String cleanEmail = email != null ? email.trim().toLowerCase(Locale.ROOT) : "";
        if (cleanName.length() < 2) {
            return ResultBox.failureMessage(Localized.tr("Họ tên phải có ít nhất 2 ký tự", "Full name must be at least 2 characters"));
        }
        if (!cleanUser.matches("^[a-z0-9_\\.]{3,24}$")) {
            return ResultBox.failureMessage(Localized.tr("Username gồm 3-24 ký tự chữ thường, số hoặc dấu _", "Username must be 3-24 lowercase letters, digits, or _"));
        }
        if (password == null || password.length() < 6) {
            return ResultBox.failureMessage(Localized.tr("Mật khẩu phải từ 6 ký tự trở lên", "Password must be at least 6 characters"));
        }
        for (UserAccount acc : users) {
            if (acc.username.equalsIgnoreCase(cleanUser)) {
                return ResultBox.failureMessage(Localized.tr("Tên đăng nhập đã tồn tại", "Username already taken"));
            }
            if (!cleanEmail.isEmpty() && acc.email.equalsIgnoreCase(cleanEmail)) {
                return ResultBox.failureMessage(Localized.tr("Email này đã được đăng ký", "Email already registered"));
            }
        }

        UserAccount created = buildLocalAccount(
                "u_" + UUID.randomUUID().toString().substring(0, 8),
                cleanUser,
                cleanName,
                cleanEmail.isEmpty() ? cleanUser + "@guest.vn" : cleanEmail,
                password,
                UserRole.MEMBER,
                "🧳",
                System.currentTimeMillis()
        );
        users.add(created);
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) db.upsertUser(created);

        setRememberMeEnabled(rememberLogin);
        if (rememberLogin) {
            upsertSavedCredential(created.id, created.username, password);
        }
        setActiveSession(created, rememberLogin);
        recordAudit(
                "Đăng ký tài khoản Du khách mới @" + created.username,
                "Registered new Tourist Member @" + created.username
        );
        return ResultBox.success(created);
    }

    public static synchronized ResultBox<UserAccount> signInOrRegisterSocial(
            AuthProvider provider,
            String identifier,
            String fullName
    ) {
        return signInOrRegisterSocial(provider, identifier, fullName, null);
    }

    /**
     * Đăng nhập hoặc tự động đăng ký nhanh bằng Google (Gmail) / Facebook / Zalo / Apple ID.
     * - Kiểm tra định dạng email/số điện thoại theo từng nhà cung cấp.
     * - Bảo vệ RBAC: ngăn giả mạo email của tài khoản Quản trị viên / Biên tập viên nội bộ.
     * - Tài khoản mới qua mạng xã hội tự động nhận vai trò MEMBER và lưu trữ vào SQLite (saigon_travel.db).
     */
    public static synchronized ResultBox<UserAccount> signInOrRegisterSocial(
            AuthProvider provider,
            String identifier,
            String fullName,
            String externalId
    ) {
        if (provider == null || provider == AuthProvider.LOCAL) {
            return ResultBox.failure(new IllegalArgumentException(
                    "Provider không hợp lệ / Invalid social provider"));
        }

        String cleanId = identifier != null ? identifier.trim().toLowerCase(Locale.ROOT) : "";
        if (cleanId.isEmpty()) {
            return ResultBox.failure(new IllegalArgumentException(Localized.tr(
                    "Vui lòng nhập email hoặc tài khoản " + provider.badgeLabel,
                    "Enter your " + provider.badgeLabel + " email or handle")));
        }

        boolean isEmail = cleanId.contains("@") && cleanId.contains(".");
        boolean isPhone = cleanId.replace(" ", "").matches("^(\\+?84|0)[0-9]{8,10}$");

        switch (provider) {
            case GOOGLE:
                if (!isEmail) {
                    return ResultBox.failure(new IllegalArgumentException(Localized.tr(
                            "Vui lòng nhập địa chỉ Gmail / Email hợp lệ (vd: ten@gmail.com)",
                            "Please enter a valid Gmail / Email address (e.g. name@gmail.com)")));
                }
                break;
            case ZALO:
                if (!isPhone && !isEmail) {
                    return ResultBox.failure(new IllegalArgumentException(Localized.tr(
                            "Zalo yêu cầu số điện thoại Việt Nam (vd: 09xx...) hoặc email hợp lệ",
                            "Zalo requires a Vietnam phone number (e.g. 09xx...) or valid email")));
                }
                break;
            case FACEBOOK:
            case APPLE:
                if (!isEmail && cleanId.length() < 3) {
                    return ResultBox.failure(new IllegalArgumentException(Localized.tr(
                            "Vui lòng nhập email hoặc tên tài khoản " + provider.badgeLabel + " hợp lệ",
                            "Please enter a valid " + provider.badgeLabel + " email or handle")));
                }
                break;
            case LOCAL:
            default:
                break;
        }

        final String normalizedEmail;
        if (isEmail) {
            normalizedEmail = cleanId;
        } else if (isPhone) {
            normalizedEmail = cleanId.replaceAll("[^0-9]", "") + "@zalo.local";
        } else {
            normalizedEmail = cleanId.replaceAll("[^a-z0-9_.]", "") + "@" + provider.code + ".social";
        }

        String normExt = (externalId != null && !externalId.trim().isEmpty())
                ? externalId.trim().toLowerCase(Locale.ROOT)
                : null;

        UserAccount existing = null;
        for (UserAccount candidate : users) {
            boolean sameProviderExt = normExt != null
                    && candidate.authProvider == provider
                    && normExt.equalsIgnoreCase(candidate.providerAccountId);
            boolean sameEmail = candidate.email.equalsIgnoreCase(normalizedEmail);
            if (sameProviderExt || sameEmail) {
                existing = candidate;
                break;
            }
        }

        if (existing != null) {
            if (existing.isLocked) {
                return ResultBox.failure(new SecurityException(Localized.tr(
                        "Tài khoản đã bị khóa bởi Quản trị viên",
                        "Account is locked by Administrator")));
            }
            if (existing.role != UserRole.MEMBER && existing.authProvider == AuthProvider.LOCAL) {
                return ResultBox.failure(new SecurityException(Localized.tr(
                        "Tài khoản " + existing.role.shortVi + " nội bộ yêu cầu đăng nhập bằng mật khẩu",
                        "Privileged local account requires password")));
            }
            setActiveSession(existing);
            recordAudit(
                    "Đăng nhập qua " + provider.badgeLabel + ": " + existing.email,
                    "Signed in via " + provider.badgeLabel + ": " + existing.email
            );
            return ResultBox.success(existing);
        }

        String rawName = fullName != null ? fullName.trim() : "";
        String resolvedName = rawName.length() >= 2
                ? rawName
                : deriveDisplayNameFromIdentifier(normalizedEmail);

        String uniqueUsername = buildUniqueSocialUsername(provider, normalizedEmail);
        final String providerEmoji;
        switch (provider) {
            case GOOGLE:
                providerEmoji = "📧";
                break;
            case FACEBOOK:
                providerEmoji = "📘";
                break;
            case ZALO:
                providerEmoji = "💬";
                break;
            case APPLE:
                providerEmoji = "";
                break;
            case LOCAL:
            default:
                providerEmoji = "🧳";
                break;
        }

        String salt = generateSaltHex();
        String oauthPlaceholderSecret = "oauth$" + provider.code + "$" + UUID.randomUUID();
        UserAccount created = new UserAccount(
                "u_" + provider.code + "_" + System.currentTimeMillis(),
                uniqueUsername,
                resolvedName,
                normalizedEmail,
                hashPasswordHex(oauthPlaceholderSecret, salt),
                salt,
                UserRole.MEMBER,
                providerEmoji,
                false,
                System.currentTimeMillis(),
                provider,
                normExt != null ? normExt : normalizedEmail
        );
        users.add(created);
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) db.upsertUser(created);

        setActiveSession(created);
        recordAudit(
                "Đăng ký mới qua " + provider.badgeLabel + ": " + created.email + " (@" + created.username + ")",
                "Registered via " + provider.badgeLabel + ": " + created.email + " (@" + created.username + ")"
        );
        return ResultBox.success(created);
    }

    /**
     * Trả về toàn bộ tài khoản mạng xã hội/Gmail đã từng đăng nhập trên thiết bị (hỗ trợ chọn nhanh 1 chạm).
     */
    public static synchronized List<UserAccount> getSavedSocialAccounts() {
        return getSavedSocialAccounts(null);
    }

    public static synchronized List<UserAccount> getSavedSocialAccounts(AuthProvider provider) {
        List<UserAccount> result = new ArrayList<>();
        for (UserAccount u : users) {
            if (u.authProvider != AuthProvider.LOCAL
                    && !u.isLocked
                    && (provider == null || u.authProvider == provider)) {
                result.add(u);
            }
        }
        return result;
    }

    private static String deriveDisplayNameFromIdentifier(String email) {
        int at = email != null ? email.indexOf('@') : -1;
        String local = (at > 0) ? email.substring(0, at) : (email != null ? email : "");
        if (local.trim().isEmpty()) {
            return "Du khách";
        }
        String[] parts = local.split("[._\\-]+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part == null || part.trim().isEmpty()) continue;
            String p = part.trim();
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(p.charAt(0)));
            if (p.length() > 1) {
                sb.append(p.substring(1));
            }
        }
        return sb.length() > 0 ? sb.toString() : "Du khách";
    }

    private static String buildUniqueSocialUsername(AuthProvider provider, String email) {
        int at = email != null ? email.indexOf('@') : -1;
        String rawPrefix = (at > 0) ? email.substring(0, at) : (email != null ? email : "");
        String cleaned = rawPrefix.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9_]", "_")
                .replaceAll("^_+|_+$", "");
        if (cleaned.length() > 14) {
            cleaned = cleaned.substring(0, 14);
        }
        if (cleaned.isEmpty()) {
            cleaned = provider.code;
        }
        String base = provider.code.charAt(0) + "_" + cleaned;
        if (base.length() > 20) {
            base = base.substring(0, 20);
        }
        String candidate = base.length() >= 3 ? base : (provider.code + "_user");
        int suffix = 1;
        while (usernameExists(candidate)) {
            String tag = "_" + suffix;
            int maxBase = Math.max(1, 24 - tag.length());
            String prefixPart = base.length() > maxBase ? base.substring(0, maxBase) : base;
            candidate = prefixPart + tag;
            suffix++;
        }
        return candidate;
    }

    public static synchronized ResultBox<UserAccount> quickDemoLogin(UserRole role) {
        if (role == null || role == UserRole.GUEST) {
            logout();
            return ResultBox.success(null);
        }
        for (UserAccount acc : users) {
            if (acc.role == role && !acc.isLocked) {
                setActiveSession(acc);
                recordAudit(
                        "Chuyển nhanh sang vai trò mẫu: " + role.titleVi,
                        "Switched to demo role: " + role.titleEn
                );
                return ResultBox.success(acc);
            }
        }
        return ResultBox.failureMessage("No active demo account available for " + role.name());
    }

    public static synchronized void logout() {
        UserAccount prev = currentUser;
        currentUser = null;
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            db.removeSetting(KEY_CURRENT_USER_ID);
        }
        if (prev != null) {
            AuditLogEntry entry = new AuditLogEntry(
                    "log_" + UUID.randomUUID().toString().substring(0, 8),
                    prev.username,
                    prev.role,
                    "Đăng xuất về chế độ Khách vãng lai",
                    "Signed out to Guest mode",
                    System.currentTimeMillis()
            );
            auditLogs.add(0, entry);
            if (db != null) db.insertAuditLog(entry);
        }
        notifyDependentScopes();
        notifyListeners();
    }

    public static synchronized ResultBox<UserAccount> updateCurrentUserProfile(String newFullName, String newEmoji) {
        UserAccount cur = currentUser;
        if (cur == null) {
            return ResultBox.failureMessage(Localized.tr("Bạn chưa đăng nhập", "Not signed in"));
        }
        return updateCurrentUserProfile(newFullName, cur.email, newEmoji);
    }

    public static synchronized ResultBox<UserAccount> updateCurrentUserProfile(
            String newFullName,
            String newEmail,
            String newEmoji
    ) {
        UserAccount cur = currentUser;
        if (cur == null) {
            return ResultBox.failureMessage(Localized.tr("Bạn chưa đăng nhập", "Not signed in"));
        }
        return updateCurrentUserProfile(newFullName, newEmail, newEmoji, cur.avatarPhotoPath);
    }

    public static synchronized ResultBox<UserAccount> updateCurrentUserProfile(
            String newFullName,
            String newEmail,
            String newEmoji,
            String newAvatarPhotoPath
    ) {
        UserAccount cur = currentUser;
        if (cur == null) {
            return ResultBox.failureMessage(Localized.tr("Bạn chưa đăng nhập", "Not signed in"));
        }
        String cleanName = newFullName != null ? newFullName.trim() : "";
        if (cleanName.length() < 2) {
            return ResultBox.failureMessage(Localized.tr("Tên hiển thị tối thiểu 2 ký tự", "Display name must be at least 2 characters"));
        }
        String cleanEmail = newEmail != null ? newEmail.trim().toLowerCase(Locale.ROOT) : "";
        if (!cleanEmail.isEmpty()) {
            if (!cleanEmail.contains("@") || cleanEmail.length() < 5) {
                return ResultBox.failureMessage(Localized.tr("Định dạng email không hợp lệ", "Invalid email address"));
            }
            for (UserAccount acc : users) {
                if (!acc.id.equals(cur.id) && cleanEmail.equalsIgnoreCase(acc.email)) {
                    return ResultBox.failureMessage(Localized.tr("Email này đã thuộc về tài khoản khác", "Email is already used by another account"));
                }
            }
        }
        String resolvedEmail = cleanEmail.isEmpty() ? cur.email : cleanEmail;
        String cleanEmoji = (newEmoji != null && !newEmoji.trim().isEmpty()) ? newEmoji.trim() : cur.avatarEmoji;
        String cleanPhotoPath = newAvatarPhotoPath != null ? newAvatarPhotoPath.trim() : "";
        UserAccount updated = cur.withProfileDetails(cleanName, resolvedEmail, cleanEmoji, cleanPhotoPath);
        replaceUserInList(updated);
        currentUser = updated;
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) db.upsertUser(updated);
        recordAudit(
                "Cập nhật hồ sơ cá nhân @" + updated.username,
                "Updated personal profile @" + updated.username
        );
        notifyListeners();
        return ResultBox.success(updated);
    }

    public static synchronized ResultBox<UserAccount> updateCurrentUserAvatarPhoto(String newAvatarPhotoPath) {
        UserAccount cur = currentUser;
        if (cur == null) {
            return ResultBox.failureMessage(Localized.tr("Vui lòng đăng nhập để đổi ảnh đại diện", "Please sign in to set an avatar photo"));
        }
        String cleanPath = newAvatarPhotoPath != null ? newAvatarPhotoPath.trim() : "";
        UserAccount updated = cur.withAvatarPhoto(cleanPath);
        replaceUserInList(updated);
        currentUser = updated;
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) db.upsertUser(updated);
        recordAudit(
                cleanPath.isEmpty()
                        ? "Gỡ ảnh đại diện tùy chỉnh @" + updated.username
                        : "Tải lên ảnh đại diện mới @" + updated.username,
                cleanPath.isEmpty()
                        ? "Removed custom avatar photo @" + updated.username
                        : "Uploaded new avatar photo @" + updated.username
        );
        notifyListeners();
        return ResultBox.success(updated);
    }

    public static synchronized ResultBox<UserAccount> changeCurrentUserPassword(
            String currentPassword,
            String newPassword
    ) {
        UserAccount cur = currentUser;
        if (cur == null) {
            return ResultBox.failureMessage(Localized.tr("Bạn chưa đăng nhập", "Not signed in"));
        }
        if (cur.authProvider == AuthProvider.LOCAL) {
            if (currentPassword == null || currentPassword.isEmpty()) {
                return ResultBox.failureMessage(Localized.tr("Vui lòng nhập mật khẩu hiện tại", "Please enter your current password"));
            }
            String computed = hashPasswordHex(currentPassword, cur.passwordSalt);
            if (!constantTimeEquals(computed, cur.passwordHash)) {
                return ResultBox.failureMessage(Localized.tr("Mật khẩu hiện tại không chính xác", "Current password is incorrect"));
            }
        }
        if (newPassword == null || newPassword.length() < 6) {
            return ResultBox.failureMessage(Localized.tr("Mật khẩu mới phải từ 6 ký tự trở lên", "New password must be at least 6 characters"));
        }
        String newSalt = generateSaltHex();
        String newHash = hashPasswordHex(newPassword, newSalt);
        UserAccount updated = cur.withPassword(newHash, newSalt);
        replaceUserInList(updated);
        currentUser = updated;
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) db.upsertUser(updated);
        syncSavedCredentialPasswordIfPresent(updated.id, updated.username, newPassword);
        recordAudit(
                "Đổi mật khẩu bảo mật PBKDF2 @" + updated.username,
                "Changed PBKDF2 password @" + updated.username
        );
        notifyListeners();
        return ResultBox.success(updated);
    }

    public static synchronized ResultBox<UserAccount> changeUserRole(String targetUserId, UserRole newRole) {
        ResultBox<Void> perm = requirePermission(Permission.MANAGE_USERS);
        if (perm.isFailure()) return ResultBox.failure(perm.exceptionOrNull());
        if (newRole == null || newRole == UserRole.GUEST) {
            return ResultBox.failureMessage("Invalid assignable role");
        }

        UserAccount target = findUserById(targetUserId);
        if (target == null) return ResultBox.failureMessage("User not found");

        if (target.role == UserRole.ADMIN && newRole != UserRole.ADMIN && countActiveAdmins() <= 1) {
            return ResultBox.failureMessage(Localized.tr(
                    "Không thể hạ quyền Quản trị viên cuối cùng của hệ thống!",
                    "Cannot demote the last active System Administrator!"));
        }

        UserAccount updated = target.withRole(newRole);
        replaceUserInList(updated);
        if (currentUser != null && currentUser.id.equals(updated.id)) {
            currentUser = updated;
        }
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) db.upsertUser(updated);

        recordAudit(
                "Đổi vai trò @" + updated.username + " → " + newRole.shortVi,
                "Changed role of @" + updated.username + " → " + newRole.shortEn
        );
        notifyListeners();
        return ResultBox.success(updated);
    }

    public static synchronized ResultBox<UserAccount> toggleUserLock(String targetUserId) {
        ResultBox<Void> perm = requirePermission(Permission.MANAGE_USERS);
        if (perm.isFailure()) return ResultBox.failure(perm.exceptionOrNull());

        UserAccount target = findUserById(targetUserId);
        if (target == null) return ResultBox.failureMessage("User not found");
        if (currentUser != null && currentUser.id.equals(target.id)) {
            return ResultBox.failureMessage(Localized.tr(
                    "Không thể tự khóa tài khoản đang đăng nhập!",
                    "Cannot lock your own active account!"));
        }
        if (!target.isLocked && target.role == UserRole.ADMIN && countActiveAdmins() <= 1) {
            return ResultBox.failureMessage(Localized.tr(
                    "Không thể khóa Quản trị viên cuối cùng!",
                    "Cannot lock the last active Administrator!"));
        }

        UserAccount updated = target.withLocked(!target.isLocked);
        replaceUserInList(updated);
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) db.upsertUser(updated);

        recordAudit(
                (updated.isLocked ? "Khóa tài khoản @" : "Mở khóa tài khoản @") + updated.username,
                (updated.isLocked ? "Locked account @" : "Unlocked account @") + updated.username
        );
        notifyListeners();
        return ResultBox.success(updated);
    }

    public static synchronized ResultBox<Void> deleteUser(String targetUserId) {
        ResultBox<Void> perm = requirePermission(Permission.MANAGE_USERS);
        if (perm.isFailure()) return perm;

        UserAccount target = findUserById(targetUserId);
        if (target == null) return ResultBox.failureMessage("User not found");
        if (currentUser != null && currentUser.id.equals(target.id)) {
            return ResultBox.failureMessage(Localized.tr(
                    "Không thể tự xóa tài khoản đang đăng nhập!",
                    "Cannot delete your own active session!"));
        }
        if (target.role == UserRole.ADMIN && countActiveAdmins() <= 1) {
            return ResultBox.failureMessage(Localized.tr(
                    "Phải giữ lại ít nhất 1 Quản trị viên!",
                    "Must retain at least one System Administrator!"));
        }

        users.remove(target);
        savedCredentials.removeIf(c -> target.id.equals(c.userId));
        persistSavedCredentials();
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) db.deleteUser(target.id);

        recordAudit(
                "Xóa tài khoản @" + target.username,
                "Deleted user account @" + target.username
        );
        notifyListeners();
        return ResultBox.success(null);
    }

    public static synchronized ResultBox<UserAccount> createUserByAdmin(
            String fullName,
            String username,
            String email,
            String password,
            UserRole role
    ) {
        ResultBox<Void> perm = requirePermission(Permission.MANAGE_USERS);
        if (perm.isFailure()) return ResultBox.failure(perm.exceptionOrNull());
        if (role == null || role == UserRole.GUEST) {
            return ResultBox.failureMessage("Invalid role");
        }
        String cleanUser = username != null ? username.trim().toLowerCase(Locale.ROOT) : "";
        String cleanName = fullName != null ? fullName.trim() : "";
        String cleanEmail = email != null ? email.trim().toLowerCase(Locale.ROOT) : "";
        if (cleanName.length() < 2) return ResultBox.failureMessage("Full name too short");
        if (!cleanUser.matches("^[a-z0-9_\\.]{3,24}$")) return ResultBox.failureMessage("Invalid username");
        if (password == null || password.length() < 6) return ResultBox.failureMessage("Password must be >= 6 chars");
        if (usernameExists(cleanUser)) return ResultBox.failureMessage("Username already exists");

        String emoji = role == UserRole.ADMIN ? "🛡️" : (role == UserRole.EDITOR ? "✍️" : "🧳");
        UserAccount created = buildLocalAccount(
                "u_" + UUID.randomUUID().toString().substring(0, 8),
                cleanUser,
                cleanName,
                cleanEmail.isEmpty() ? cleanUser + "@dulichtphcm.vn" : cleanEmail,
                password,
                role,
                emoji,
                System.currentTimeMillis()
        );
        users.add(created);
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) db.upsertUser(created);

        recordAudit(
                "Tạo tài khoản @" + created.username + " (" + role.shortVi + ")",
                "Created account @" + created.username + " (" + role.shortEn + ")"
        );
        notifyListeners();
        return ResultBox.success(created);
    }

    public static synchronized void recordAudit(String actionVi, String actionEn) {
        UserAccount actor = currentUser;
        AuditLogEntry entry = new AuditLogEntry(
                "log_" + UUID.randomUUID().toString().substring(0, 8),
                actor != null ? actor.username : "guest",
                actor != null ? actor.role : UserRole.GUEST,
                actionVi,
                actionEn,
                System.currentTimeMillis()
        );
        auditLogs.add(0, entry);
        if (auditLogs.size() > 100) {
            auditLogs.remove(auditLogs.size() - 1);
        }
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            db.insertAuditLog(entry);
        }
    }

    private static void setActiveSession(UserAccount account) {
        setActiveSession(account, true);
    }

    private static void setActiveSession(UserAccount account, boolean persistSessionAcrossRestart) {
        currentUser = account;
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db != null) {
            if (account != null && persistSessionAcrossRestart) {
                db.putSetting(KEY_CURRENT_USER_ID, account.id);
            } else {
                db.removeSetting(KEY_CURRENT_USER_ID);
            }
        }
        notifyDependentScopes();
        notifyListeners();
    }

    private static void upsertSavedCredential(String userId, String username, String rawPassword) {
        savedCredentials.removeIf(c -> userId.equals(c.userId));
        savedCredentials.add(0, new RawSavedCredential(userId, username, rawPassword, System.currentTimeMillis()));
        while (savedCredentials.size() > 8) {
            savedCredentials.remove(savedCredentials.size() - 1);
        }
        persistSavedCredentials();
    }

    private static void syncSavedCredentialPasswordIfPresent(String userId, String username, String newPassword) {
        for (int i = 0; i < savedCredentials.size(); i++) {
            RawSavedCredential c = savedCredentials.get(i);
            if (userId.equals(c.userId)) {
                savedCredentials.set(i, new RawSavedCredential(userId, username, newPassword, c.savedAt));
                persistSavedCredentials();
                return;
            }
        }
    }

    private static void persistSavedCredentials() {
        SaigonTravelDatabaseHelper db = SaigonTravelDatabaseHelper.peekInstance();
        if (db == null) return;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < savedCredentials.size(); i++) {
            RawSavedCredential c = savedCredentials.get(i);
            if (i > 0) sb.append('\n');
            sb.append(obfuscateHex(c.userId)).append(':')
              .append(obfuscateHex(c.username)).append(':')
              .append(obfuscateHex(c.password)).append(':')
              .append(c.savedAt);
        }
        db.putSetting(KEY_SAVED_LOGINS_RAW, sb.toString());
    }

    private static void deserializeSavedCredentials(String raw) {
        savedCredentials.clear();
        if (raw == null || raw.trim().isEmpty()) return;
        String[] lines = raw.split("\n");
        for (String line : lines) {
            String[] parts = line.trim().split(":");
            if (parts.length != 4) continue;
            try {
                String uid = deobfuscateHex(parts[0]);
                String uname = deobfuscateHex(parts[1]);
                String pwd = deobfuscateHex(parts[2]);
                long ts = Long.parseLong(parts[3]);
                if (!uid.isEmpty() && !uname.isEmpty() && !pwd.isEmpty()) {
                    savedCredentials.add(new RawSavedCredential(uid, uname, pwd, ts));
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static String obfuscateHex(String plain) {
        byte[] bytes = (plain != null ? plain : "").getBytes(StandardCharsets.UTF_8);
        byte[] out = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            out[i] = (byte) (bytes[i] ^ OBFUSCATION_MASK[i % OBFUSCATION_MASK.length]);
        }
        return toHex(out);
    }

    private static String deobfuscateHex(String hex) {
        byte[] enc = fromHex(hex);
        byte[] out = new byte[enc.length];
        for (int i = 0; i < enc.length; i++) {
            out[i] = (byte) (enc[i] ^ OBFUSCATION_MASK[i % OBFUSCATION_MASK.length]);
        }
        return new String(out, StandardCharsets.UTF_8);
    }

    private static void notifyDependentScopes() {
        FavoritesManager.reloadActiveScope();
        TripPlanManager.reloadActiveScope();
        TravelPassportManager.reloadActiveScope();
        TripExpenseManager.reloadActiveScope();
        TicketWalletManager.reloadActiveScope();
        TravelJournalManager.reloadActiveScope();
        PackingListManager.reloadActiveScope();
    }

    public static void addListener(Runnable listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public static void removeListener(Runnable listener) {
        listeners.remove(listener);
    }

    private static void notifyListeners() {
        for (Runnable r : listeners) {
            r.run();
        }
    }

    private static boolean usernameExists(String u) {
        for (UserAccount acc : users) {
            if (acc.username.equalsIgnoreCase(u)) return true;
        }
        return false;
    }

    private static UserAccount findUserById(String id) {
        if (id == null) return null;
        for (UserAccount acc : users) {
            if (acc.id.equals(id)) return acc;
        }
        return null;
    }

    private static void replaceUserInList(UserAccount updated) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).id.equals(updated.id)) {
                users.set(i, updated);
                return;
            }
        }
    }

    private static int countActiveAdmins() {
        int c = 0;
        for (UserAccount acc : users) {
            if (acc.role == UserRole.ADMIN && !acc.isLocked) c++;
        }
        return c;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return "Traveler";
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static String generateSaltHex() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return toHex(salt);
    }

    public static String hashPasswordHex(String password, String saltHex) {
        try {
            byte[] salt = fromHex(saltHex);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return toHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("PBKDF2 error", e);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            diff |= a.charAt(i) ^ b.charAt(i);
        }
        return diff == 0;
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format(Locale.US, "%02x", b));
        }
        return sb.toString();
    }

    private static byte[] fromHex(String hex) {
        int len = hex.length();
        byte[] out = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            out[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return out;
    }
}
