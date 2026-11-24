package com.example.nhom_15_dulichtphcm.ui.screens;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.nhom_15_dulichtphcm.R;
import com.example.nhom_15_dulichtphcm.data.db.SaigonTravelDatabaseHelper;
import com.example.nhom_15_dulichtphcm.data.local.AuthManager;
import com.example.nhom_15_dulichtphcm.data.local.FavoritesManager;
import com.example.nhom_15_dulichtphcm.data.local.ReviewManager;
import com.example.nhom_15_dulichtphcm.data.local.TravelJournalManager;
import com.example.nhom_15_dulichtphcm.data.local.TravelPassportManager;
import com.example.nhom_15_dulichtphcm.data.local.TripPlanManager;
import com.example.nhom_15_dulichtphcm.data.model.Attraction;
import com.example.nhom_15_dulichtphcm.data.model.Permission;
import com.example.nhom_15_dulichtphcm.data.model.PlaceReview;
import com.example.nhom_15_dulichtphcm.data.model.ResultBox;
import com.example.nhom_15_dulichtphcm.data.model.UserAccount;
import com.example.nhom_15_dulichtphcm.data.repository.AttractionRepository;
import com.example.nhom_15_dulichtphcm.ui.AppNavigator;
import com.example.nhom_15_dulichtphcm.ui.components.ExperienceDialogs;
import com.example.nhom_15_dulichtphcm.util.ImageLoader;
import com.example.nhom_15_dulichtphcm.util.Localized;
import com.example.nhom_15_dulichtphcm.util.MapLauncher;
import com.example.nhom_15_dulichtphcm.util.SpeechHelper;

import java.util.List;

/**
 * Pure Java + XML detail screen for an Attraction with Passport Check-in and SQLite Community Reviews.
 */
public final class AttractionDetailFragment extends Fragment {

    private static final String ARG_ID = "attraction_id";

    public static AttractionDetailFragment newInstance(String attractionId) {
        AttractionDetailFragment f = new AttractionDetailFragment();
        Bundle b = new Bundle();
        b.putString(ARG_ID, attractionId);
        f.setArguments(b);
        return f;
    }

    private static final float[] AUDIO_SPEEDS = {0.8f, 1.0f, 1.25f};

    private int selectedRating = 5;
    private int selectedAudioTrack = 0; // 0=history, 1=culture, 2=tips
    private int speedIndex = 1; // 1.0x
    private boolean isAudioPlayerVisible = false;
    private Attraction attraction;
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_attraction_detail, container, false);
        String id = getArguments() != null ? getArguments().getString(ARG_ID) : null;
        attraction = AttractionRepository.getAttractionById(id);
        if (attraction == null && !AttractionRepository.getAttractions().isEmpty()) {
            attraction = AttractionRepository.getAttractions().get(0);
        }
        if (attraction != null) {
            bindContent();
        }
        return rootView;
    }

    private void bindContent() {
        AppNavigator nav = getNavigator();
        boolean isFav = FavoritesManager.isAttractionFavorite(attraction.id);
        boolean inTrip = TripPlanManager.contains(attraction.id);

        TextView tvToolbarTitle = rootView.findViewById(R.id.tv_toolbar_title);
        TextView tvToolbarSub = rootView.findViewById(R.id.tv_toolbar_subtitle);
        TextView btnFavTop = rootView.findViewById(R.id.btn_toolbar_action);
        TextView btnTripTop = rootView.findViewById(R.id.btn_detail_trip_top);
        TextView btnShareTop = rootView.findViewById(R.id.btn_detail_share);

        tvToolbarTitle.setText(Localized.tr(attraction.nameVi, attraction.nameEn));
        tvToolbarSub.setText(attraction.district + " • " + attraction.province);
        btnFavTop.setText(isFav ? "❤️" : "🤍");
        btnTripTop.setText(inTrip ? "✓" : "📅");

        rootView.findViewById(R.id.btn_toolbar_back).setOnClickListener(v -> {
            if (nav != null) nav.navigateBack();
        });

        btnFavTop.setOnClickListener(v -> {
            ResultBox<Boolean> res = FavoritesManager.toggleAttraction(attraction.id);
            if (!res.isSuccess()) {
                if (nav != null) nav.showPermissionRequiredDialog(res.getErrorMessage());
                return;
            }
            bindContent();
        });

        btnTripTop.setOnClickListener(v -> {
            ResultBox<Boolean> res = TripPlanManager.toggle(attraction.id);
            if (!res.isSuccess()) {
                if (nav != null) nav.showPermissionRequiredDialog(res.getErrorMessage());
                return;
            }
            bindContent();
        });

        btnShareTop.setOnClickListener(v -> MapLauncher.shareText(
                requireContext(),
                attraction.nameVi,
                attraction.nameVi + " (" + attraction.nameEn + ")\n📍 " + attraction.address));

        ImageView ivHero = rootView.findViewById(R.id.iv_detail_hero);
        ImageLoader.loadAttraction(ivHero, attraction);

        TextView tvBadge = rootView.findViewById(R.id.tv_detail_badge);
        TextView tvMain = rootView.findViewById(R.id.tv_detail_name_main);
        TextView tvSub = rootView.findViewById(R.id.tv_detail_name_sub);
        TextView tvAddr = rootView.findViewById(R.id.tv_detail_address);
        TextView tvHours = rootView.findViewById(R.id.tv_detail_hours);
        TextView tvTicket = rootView.findViewById(R.id.tv_detail_ticket);
        TextView tvHistory = rootView.findViewById(R.id.tv_detail_history);
        TextView tvCulture = rootView.findViewById(R.id.tv_detail_culture);
        TextView tvTips = rootView.findViewById(R.id.tv_detail_tips);

        tvBadge.setText(Localized.tr(attraction.category.displayNameVi, attraction.category.displayName));
        tvMain.setText(Localized.tr(attraction.nameVi, attraction.nameEn));
        tvSub.setText(Localized.tr(attraction.nameEn, attraction.nameVi));
        tvAddr.setText(attraction.address);
        tvHours.setText(attraction.openingHours);
        tvTicket.setText(attraction.ticketPrice);
        tvHistory.setText(attraction.history);
        tvCulture.setText(attraction.culture);
        tvTips.setText(attraction.foreignerTips);

        View.OnClickListener openPhotos = v -> MapLauncher.openPhotoSearch(requireContext(), attraction.nameVi);
        rootView.findViewById(R.id.btn_detail_google_photos).setOnClickListener(openPhotos);
        rootView.findViewById(R.id.btn_detail_google_gallery).setOnClickListener(openPhotos);

        TextView btnMap = rootView.findViewById(R.id.btn_detail_map);
        TextView btnRide = rootView.findViewById(R.id.btn_detail_ride);
        TextView btnCheckIn = rootView.findViewById(R.id.btn_detail_checkin);
        TextView btnTrip = rootView.findViewById(R.id.btn_detail_trip);
        TextView btnAudio = rootView.findViewById(R.id.btn_detail_audio);

        btnMap.setText(Localized.tr("🗺️ Bản đồ", "🗺️ Map"));
        btnRide.setText(Localized.tr("🚕 Đặt xe 🚖", "🚕 Book Ride 🚖"));
        btnAudio.setText(Localized.tr("🔊 Thuyết minh", "🔊 Audio Guide"));

        btnMap.setOnClickListener(v -> {
            if (nav != null) nav.openMapAt(attraction.latitude, attraction.longitude, attraction.nameVi);
        });
        btnMap.setOnLongClickListener(v -> {
            MapLauncher.openInGoogleMaps(requireContext(), attraction.latitude, attraction.longitude, attraction.nameVi);
            return true;
        });

        btnRide.setOnClickListener(v -> {
            if (nav != null) nav.openRideBooking(attraction.nameVi, attraction.latitude, attraction.longitude);
        });

        boolean stamped = TravelPassportManager.isCheckedIn(attraction.id);
        btnCheckIn.setText(stamped
                ? Localized.tr("✅ Đã đóng dấu", "✅ Stamped")
                : Localized.tr("🛂 Đóng dấu", "🛂 Check-in"));
        btnCheckIn.setOnClickListener(v -> {
            ResultBox<Boolean> res = TravelPassportManager.toggleCheckIn(attraction);
            if (!res.isSuccess()) {
                if (nav != null) nav.showPermissionRequiredDialog(res.getErrorMessage());
                return;
            }
            Toast.makeText(requireContext(),
                    Boolean.TRUE.equals(res.getOrNull())
                            ? Localized.tr("🎉 Đã đóng dấu vào Hộ chiếu Du lịch Số!", "🎉 Added stamp to Travel Passport!")
                            : Localized.tr("Đã gỡ dấu Hộ chiếu", "Removed stamp"),
                    Toast.LENGTH_SHORT).show();
            bindContent();
        });

        btnTrip.setText(inTrip
                ? Localized.tr("✓ Trong Lịch trình", "✓ In My Trip")
                : Localized.tr("📅 Thêm vào Lịch trình", "📅 Add to My Trip"));
        btnTrip.setOnClickListener(v -> {
            ResultBox<Boolean> res = TripPlanManager.toggle(attraction.id);
            if (!res.isSuccess()) {
                if (nav != null) nav.showPermissionRequiredDialog(res.getErrorMessage());
                return;
            }
            bindContent();
        });

        btnAudio.setText(Localized.tr("🎧 Thuyết minh Số", "🎧 Audio Guide"));
        btnAudio.setOnClickListener(v -> {
            isAudioPlayerVisible = !isAudioPlayerVisible;
            renderAudioGuidePlayer();
            if (isAudioPlayerVisible) {
                playCurrentAudioTrack();
            } else {
                SpeechHelper sh = nav != null ? nav.getSpeechHelper() : null;
                if (sh != null) sh.stopSpeaking();
            }
        });

        TextView btnBookTicket = rootView.findViewById(R.id.btn_detail_book_ticket);
        TextView btnJournal = rootView.findViewById(R.id.btn_detail_journal);
        btnBookTicket.setText(Localized.tr("🎫 Đặt vé QR", "🎫 Book QR Pass"));

        SaigonTravelDatabaseHelper.JournalRow journal = TravelJournalManager.getJournalForAttraction(attraction.id);
        btnJournal.setText(journal != null
                ? journal.moodEmoji + " " + Localized.tr("Đã lưu Nhật ký", "Saved Memory")
                : Localized.tr("📸 Nhật ký & Bưu thiếp", "📸 Journal & Postcard"));

        btnBookTicket.setOnClickListener(v -> ExperienceDialogs.showBookTicketDialog(
                requireContext(), attraction, nav, this::bindContent));
        btnJournal.setOnClickListener(v -> ExperienceDialogs.showTravelJournalDialog(
                requireContext(), attraction, nav, this::bindContent));

        renderAudioGuidePlayer();
        renderReviewsSection();
    }

    private void renderAudioGuidePlayer() {
        if (!isAdded() || rootView == null) return;
        View card = rootView.findViewById(R.id.card_audio_guide_player);
        card.setVisibility(isAudioPlayerVisible ? View.VISIBLE : View.GONE);

        TextView btnHistory = rootView.findViewById(R.id.btn_track_history);
        TextView btnCulture = rootView.findViewById(R.id.btn_track_culture);
        TextView btnTips = rootView.findViewById(R.id.btn_track_tips);
        TextView btnPlayStop = rootView.findViewById(R.id.btn_audio_play_stop);
        TextView btnSpeed = rootView.findViewById(R.id.btn_audio_speed);

        styleTrackChip(btnHistory, selectedAudioTrack == 0);
        styleTrackChip(btnCulture, selectedAudioTrack == 1);
        styleTrackChip(btnTips, selectedAudioTrack == 2);

        btnHistory.setOnClickListener(v -> { selectedAudioTrack = 0; renderAudioGuidePlayer(); playCurrentAudioTrack(); });
        btnCulture.setOnClickListener(v -> { selectedAudioTrack = 1; renderAudioGuidePlayer(); playCurrentAudioTrack(); });
        btnTips.setOnClickListener(v -> { selectedAudioTrack = 2; renderAudioGuidePlayer(); playCurrentAudioTrack(); });

        AppNavigator nav = getNavigator();
        SpeechHelper sh = nav != null ? nav.getSpeechHelper() : null;
        boolean playing = sh != null && sh.isSpeaking();
        btnPlayStop.setText(playing
                ? Localized.tr("⏹️ Dừng thuyết minh", "⏹️ Stop Narration")
                : Localized.tr("▶️ Phát thuyết minh", "▶️ Play Narration"));
        btnSpeed.setText("⚡ " + AUDIO_SPEEDS[speedIndex] + "x");

        btnPlayStop.setOnClickListener(v -> {
            if (sh == null) return;
            if (sh.isSpeaking()) {
                sh.stopSpeaking();
                renderAudioGuidePlayer();
            } else {
                playCurrentAudioTrack();
            }
        });

        btnSpeed.setOnClickListener(v -> {
            speedIndex = (speedIndex + 1) % AUDIO_SPEEDS.length;
            renderAudioGuidePlayer();
            if (sh != null && sh.isSpeaking()) {
                playCurrentAudioTrack();
            }
        });
    }

    private void playCurrentAudioTrack() {
        AppNavigator nav = getNavigator();
        SpeechHelper sh = nav != null ? nav.getSpeechHelper() : null;
        if (sh == null || attraction == null) return;

        String body;
        if (selectedAudioTrack == 1) {
            body = attraction.culture;
        } else if (selectedAudioTrack == 2) {
            body = attraction.foreignerTips;
        } else {
            body = attraction.shortSummary + " " + attraction.history;
        }
        String vi = attraction.nameVi + ". " + body;
        String en = attraction.nameEn + ". " + body;
        sh.speakGuide(vi, en, AUDIO_SPEEDS[speedIndex]);
        renderAudioGuidePlayer();
    }

    private void styleTrackChip(TextView tv, boolean active) {
        tv.setBackgroundResource(active ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        tv.setTextColor(requireContext().getColor(active ? R.color.white : R.color.text_primary));
    }

    @Override
    public void onDestroyView() {
        AppNavigator nav = getNavigator();
        if (nav != null && nav.getSpeechHelper() != null) {
            nav.getSpeechHelper().stopSpeaking();
        }
        super.onDestroyView();
    }

    private void renderReviewsSection() {
        AppNavigator nav = getNavigator();
        LinearLayout starsRow = rootView.findViewById(R.id.container_rating_stars);
        EditText etComment = rootView.findViewById(R.id.et_review_comment);
        TextView btnSubmit = rootView.findViewById(R.id.btn_submit_review);
        TextView tvHeader = rootView.findViewById(R.id.tv_reviews_header);
        LinearLayout listContainer = rootView.findViewById(R.id.container_reviews_list);

        starsRow.removeAllViews();
        for (int s = 1; s <= 5; s++) {
            final int starVal = s;
            TextView star = new TextView(requireContext());
            star.setText(s <= selectedRating ? "★ " : "☆ ");
            star.setTextSize(22f);
            star.setTextColor(requireContext().getColor(R.color.saigon_gold));
            star.setOnClickListener(v -> {
                selectedRating = starVal;
                renderReviewsSection();
            });
            starsRow.addView(star);
        }

        btnSubmit.setOnClickListener(v -> {
            String comment = etComment.getText().toString();
            ResultBox<PlaceReview> res = ReviewManager.addReview(attraction.id, selectedRating, comment);
            if (!res.isSuccess()) {
                if (res.exceptionOrNull() instanceof SecurityException) {
                    if (nav != null) nav.showPermissionRequiredDialog(res.getErrorMessage());
                } else {
                    Toast.makeText(requireContext(), res.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
                return;
            }
            etComment.setText("");
            Toast.makeText(requireContext(),
                    Localized.tr("Đã lưu đánh giá!", "Review saved!"),
                    Toast.LENGTH_SHORT).show();
            renderReviewsSection();
        });

        List<PlaceReview> reviews = ReviewManager.getReviewsForTarget(attraction.id);
        double avg = ReviewManager.averageRating(attraction.id);
        tvHeader.setText(reviews.isEmpty()
                ? Localized.tr("⭐ Đánh giá Cộng đồng (Chưa có)", "⭐ Community Reviews (0)")
                : Localized.tr("⭐ Đánh giá Cộng đồng (" + avg + "★ • " + reviews.size() + ")",
                        "⭐ Reviews (" + avg + "★ • " + reviews.size() + ")"));

        listContainer.removeAllViews();
        UserAccount curUser = AuthManager.getCurrentUser();
        boolean canModerate = AuthManager.hasPermission(Permission.MODERATE_REVIEWS);

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (PlaceReview r : reviews) {
            View row = inflater.inflate(R.layout.item_review, listContainer, false);
            TextView tvAuthor = row.findViewById(R.id.tv_review_author);
            TextView tvStars = row.findViewById(R.id.tv_review_stars);
            TextView tvComment = row.findViewById(R.id.tv_review_comment);
            TextView btnDel = row.findViewById(R.id.btn_delete_review);

            tvAuthor.setText(r.authorName + " (" + r.authorRole.shortVi + ")");
            tvStars.setText("★★★★★".substring(0, Math.min(5, Math.max(1, r.rating))));
            tvComment.setText(r.comment);

            boolean canDel = canModerate || (curUser != null && curUser.id.equals(r.authorId));
            btnDel.setVisibility(canDel ? View.VISIBLE : View.GONE);
            btnDel.setOnClickListener(v -> {
                ReviewManager.deleteReview(r.id);
                renderReviewsSection();
            });
            listContainer.addView(row);
        }
    }

    private AppNavigator getNavigator() {
        return getActivity() instanceof AppNavigator ? (AppNavigator) getActivity() : null;
    }
}
