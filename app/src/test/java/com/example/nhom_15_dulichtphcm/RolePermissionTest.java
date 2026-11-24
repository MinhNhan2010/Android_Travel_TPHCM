package com.example.nhom_15_dulichtphcm;

import com.example.nhom_15_dulichtphcm.data.local.AuthManager;
import com.example.nhom_15_dulichtphcm.data.local.ReviewManager;
import com.example.nhom_15_dulichtphcm.data.model.AuthProvider;
import com.example.nhom_15_dulichtphcm.data.model.Permission;
import com.example.nhom_15_dulichtphcm.data.model.PlaceReview;
import com.example.nhom_15_dulichtphcm.data.model.ResultBox;
import com.example.nhom_15_dulichtphcm.data.model.UserAccount;
import com.example.nhom_15_dulichtphcm.data.model.UserRole;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RolePermissionTest {

    @Before
    public void setUp() {
        AuthManager.resetForTesting();
        ReviewManager.resetForTesting();
    }

    @Test
    public void fourTierRbacMatrix_matchesSpecification() {
        assertEquals(UserRole.GUEST, AuthManager.getCurrentRole());
        assertTrue(UserRole.GUEST.hasPermission(Permission.VIEW_PUBLIC_GUIDE));
        assertFalse(UserRole.GUEST.hasPermission(Permission.SAVE_PERSONAL_COLLECTION));

        assertTrue(UserRole.MEMBER.hasPermission(Permission.SAVE_PERSONAL_COLLECTION));
        assertTrue(UserRole.MEMBER.hasPermission(Permission.POST_REVIEW));
        assertFalse(UserRole.MEMBER.hasPermission(Permission.MANAGE_CONTENT));

        assertTrue(UserRole.EDITOR.hasPermission(Permission.MANAGE_CONTENT));
        assertTrue(UserRole.EDITOR.hasPermission(Permission.MODERATE_REVIEWS));
        assertFalse(UserRole.EDITOR.hasPermission(Permission.MANAGE_USERS));

        assertTrue(UserRole.ADMIN.hasPermission(Permission.MANAGE_USERS));
        assertTrue(UserRole.ADMIN.hasPermission(Permission.DELETE_CONTENT));
        assertTrue(UserRole.ADMIN.hasPermission(Permission.CONFIGURE_SYSTEM));
    }

    @Test
    public void loginPbkdf2AndLastAdminProtection_workSecurely() {
        assertFalse(AuthManager.login("admin", "wrong_pass").isSuccess());
        ResultBox<UserAccount> adminLogin = AuthManager.login("admin", "admin123");
        assertTrue(adminLogin.isSuccess());
        assertEquals(UserRole.ADMIN, AuthManager.getCurrentRole());

        // Cannot demote the sole active administrator
        ResultBox<UserAccount> demoteAttempt = AuthManager.changeUserRole("u_admin", UserRole.MEMBER);
        assertFalse(demoteAttempt.isSuccess());
    }

    @Test
    public void reviewModeration_enforcesAuthorOrEditorRights() {
        assertTrue(AuthManager.login("tourist", "user123").isSuccess());
        ResultBox<PlaceReview> posted = ReviewManager.addReview("notre_dame", 5, "Kiến trúc tuyệt đẹp giữa trung tâm Sài Gòn!");
        assertTrue(posted.isSuccess());

        // Tourist cannot delete Editor's seeded review rev_1
        assertFalse(ReviewManager.deleteReview("rev_1").isSuccess());

        // Editor CAN moderate tourist's review
        assertTrue(AuthManager.login("guide", "guide123").isSuccess());
        assertTrue(ReviewManager.deleteReview(posted.getOrNull().id).isSuccess());
    }

    @Test
    public void googleAndSocialAuth_validatesProvidersSavesLinkedAccountsAndBlocksPrivilegeHijack() {
        assertEquals(0, AuthManager.getSavedSocialAccounts().size());

        // Google requires a valid email address
        assertFalse(AuthManager.signInOrRegisterSocial(AuthProvider.GOOGLE, "not_an_email", "").isSuccess());

        // Google valid registration derives display name & g_ username
        ResultBox<UserAccount> gmailReg = AuthManager.signInOrRegisterSocial(
                AuthProvider.GOOGLE,
                "minh.anh@gmail.com",
                ""
        );
        assertTrue(gmailReg.isSuccess());
        UserAccount gUser = gmailReg.getOrNull();
        assertNotNull(gUser);
        assertEquals(AuthProvider.GOOGLE, gUser.authProvider);
        assertEquals(UserRole.MEMBER, gUser.role);
        assertEquals("Minh Anh", gUser.fullName);
        assertTrue(gUser.username.startsWith("g_"));
        assertEquals("📧", gUser.avatarEmoji);

        // Zalo supports Vietnam phone number and normalizes to @zalo.local
        assertFalse(AuthManager.signInOrRegisterSocial(AuthProvider.ZALO, "abc", "").isSuccess());
        ResultBox<UserAccount> zaloReg = AuthManager.signInOrRegisterSocial(
                AuthProvider.ZALO,
                "0909123456",
                "Hoàng Nam"
        );
        assertTrue(zaloReg.isSuccess());
        assertEquals("0909123456@zalo.local", zaloReg.getOrNull().email);
        assertTrue(zaloReg.getOrNull().username.startsWith("z_"));

        // Facebook handle & Apple ID email work and appear in saved social accounts
        assertTrue(AuthManager.signInOrRegisterSocial(AuthProvider.FACEBOOK, "saigon.explorer", "").isSuccess());
        assertTrue(AuthManager.signInOrRegisterSocial(AuthProvider.APPLE, "visitor@icloud.com", "Alex").isSuccess());
        assertEquals(4, AuthManager.getSavedSocialAccounts().size());
        assertEquals(1, AuthManager.getSavedSocialAccounts(AuthProvider.GOOGLE).size());

        // Re-signing in with the same Gmail returns the existing account without duplicating
        ResultBox<UserAccount> gmailRelogin = AuthManager.signInOrRegisterSocial(
                AuthProvider.GOOGLE,
                "minh.anh@gmail.com",
                ""
        );
        assertTrue(gmailRelogin.isSuccess());
        assertEquals(gUser.id, gmailRelogin.getOrNull().id);
        assertEquals(4, AuthManager.getSavedSocialAccounts().size());

        // Cannot hijack local ADMIN or EDITOR accounts via social sign-in without password
        ResultBox<UserAccount> adminHijack = AuthManager.signInOrRegisterSocial(
                AuthProvider.GOOGLE,
                "admin@dulichtphcm.vn",
                "Fake Admin"
        );
        assertFalse(adminHijack.isSuccess());
        assertTrue(adminHijack.exceptionOrNull() instanceof SecurityException);
    }

    @Test
    public void avatarPhotoUpload_updatesPreservesAndClearsAcrossProfileOperations() {
        assertFalse(AuthManager.updateCurrentUserAvatarPhoto("/data/user/0/app/files/avatars/a.jpg").isSuccess());

        assertTrue(AuthManager.login("tourist", "user123").isSuccess());
        assertFalse(AuthManager.getCurrentUser().hasAvatarPhoto());

        String samplePath = "/data/user/0/com.example.nhom_15_dulichtphcm/files/avatars/avatar_u_tourist_1.jpg";
        ResultBox<UserAccount> uploaded = AuthManager.updateCurrentUserAvatarPhoto(samplePath);
        assertTrue(uploaded.isSuccess());
        assertTrue(AuthManager.getCurrentUser().hasAvatarPhoto());
        assertEquals(samplePath, AuthManager.getCurrentUser().avatarPhotoPath);

        // Editing display name/email preserves uploaded avatar photo unless explicitly changed
        ResultBox<UserAccount> renamed = AuthManager.updateCurrentUserProfile("Du Khách Mới", "tourist@dulichtphcm.vn", "🌴");
        assertTrue(renamed.isSuccess());
        assertEquals(samplePath, AuthManager.getCurrentUser().avatarPhotoPath);

        // Password change also preserves avatarPhotoPath
        assertTrue(AuthManager.changeCurrentUserPassword("user123", "newpass123").isSuccess());
        assertEquals(samplePath, AuthManager.getCurrentUser().avatarPhotoPath);

        // Removing custom photo reverts cleanly to emoji avatar
        assertTrue(AuthManager.updateCurrentUserAvatarPhoto("").isSuccess());
        assertFalse(AuthManager.getCurrentUser().hasAvatarPhoto());
    }

    @Test
    public void savedLoginCredentials_rememberAutoLoginSyncPasswordAndForget() {
        assertEquals(0, AuthManager.getSavedLogins().size());
        assertTrue(AuthManager.isRememberMeEnabled());

        // Login with rememberLogin = true saves credentials and sets primary saved login
        assertTrue(AuthManager.login("tourist", "user123", true).isSuccess());
        assertEquals(1, AuthManager.getSavedLogins().size());
        AuthManager.SavedLoginItem primary = AuthManager.getPrimarySavedLogin();
        assertNotNull(primary);
        assertEquals("tourist", primary.username);
        assertEquals("user123", primary.savedPassword);

        // Logout clears active session, but keeps remembered login for auto-fill & 1-tap login
        AuthManager.logout();
        assertEquals(UserRole.GUEST, AuthManager.getCurrentRole());
        assertNotNull(AuthManager.getPrimarySavedLogin());
        assertTrue(AuthManager.loginWithSavedAccount("u_tourist").isSuccess());
        assertEquals("u_tourist", AuthManager.getCurrentUser().id);

        // Changing password automatically updates saved credential so 1-tap login keeps working
        assertTrue(AuthManager.changeCurrentUserPassword("user123", "updated456").isSuccess());
        assertEquals("updated456", AuthManager.getPrimarySavedLogin().savedPassword);
        AuthManager.logout();
        assertTrue(AuthManager.loginWithSavedAccount("u_tourist").isSuccess());

        // Logging in with rememberLogin = false forgets saved credentials for that account
        AuthManager.logout();
        assertTrue(AuthManager.login("tourist", "updated456", false).isSuccess());
        assertFalse(AuthManager.isRememberMeEnabled());
        assertEquals(0, AuthManager.getSavedLogins().size());

        // Registering with rememberLogin = true also persists saved login, and removeSavedLogin removes it
        AuthManager.logout();
        ResultBox<UserAccount> reg = AuthManager.register("Lan Phương", "lanphuong", "lan@saigon.vn", "pass789", true);
        assertTrue(reg.isSuccess());
        assertEquals(1, AuthManager.getSavedLogins().size());
        AuthManager.removeSavedLogin(reg.getOrNull().id);
        assertEquals(0, AuthManager.getSavedLogins().size());
    }
}
