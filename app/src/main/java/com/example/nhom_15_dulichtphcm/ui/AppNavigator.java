package com.example.nhom_15_dulichtphcm.ui;

import com.example.nhom_15_dulichtphcm.util.SpeechHelper;

/**
 * Pure Java navigation & host services contract implemented by MainActivity.
 */
public interface AppNavigator {
    void openAttractionDetail(String attractionId);
    void openFoodDetail(String foodId);
    void openMapAt(double lat, double lng, String title);
    void openRideBooking(String destName, double destLat, double destLng);
    void openAiAssistant();
    void openFavorites();
    void openTripPlanner();
    void openTravelToolkit();
    void openMetroGuide();
    void openSafetyHub();
    void openDiscoveryQuiz();
    void openAuthProfile();
    void openAdminDashboard();
    void navigateBack();
    void showPermissionRequiredDialog(String message);
    void showLanguagePickerDialog();
    SpeechHelper getSpeechHelper();
}
