package com.example.nhom_15_dulichtphcm.ui.components;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.nhom_15_dulichtphcm.R;
import com.example.nhom_15_dulichtphcm.data.db.SaigonTravelDatabaseHelper;
import com.example.nhom_15_dulichtphcm.data.local.TicketWalletManager;
import com.example.nhom_15_dulichtphcm.data.local.TravelJournalManager;
import com.example.nhom_15_dulichtphcm.data.local.UserDataBackupManager;
import com.example.nhom_15_dulichtphcm.data.model.Attraction;
import com.example.nhom_15_dulichtphcm.data.model.ResultBox;
import com.example.nhom_15_dulichtphcm.ui.AppNavigator;
import com.example.nhom_15_dulichtphcm.util.Localized;
import com.example.nhom_15_dulichtphcm.util.MapLauncher;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Reusable pure Java + XML dialogs for QR E-Ticket Booking, Ticket Wallet,
 * Travel Memory Journal / Postcard, and SQLite JSON Backup / Trip Report.
 */
public final class ExperienceDialogs {

    private static final String[] TIME_SLOTS = {
            "08:30 - 10:30",
            "10:30 - 13:00",
            "13:30 - 16:00",
            "16:00 - 18:30"
    };

    private ExperienceDialogs() {}

    // =========================================================================
    // 1. BOOK QR E-TICKET DIALOG
    // =========================================================================

    public static void showBookTicketDialog(
            Context context,
            Attraction attraction,
            AppNavigator nav,
            Runnable onBooked
    ) {
        if (context == null || attraction == null) return;
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_book_ticket, null);
        AlertDialog dialog = DialogHelper.showCustomCardDialog(context, view);

        TextView tvTitle = view.findViewById(R.id.tv_ticket_dialog_title);
        TextView tvPlace = view.findViewById(R.id.tv_ticket_place_name);
        TextView tvBasePrice = view.findViewById(R.id.tv_ticket_base_price);
        EditText etDate = view.findViewById(R.id.et_ticket_date);
        LinearLayout slotBox = view.findViewById(R.id.container_ticket_slots);
        TextView tvAdultQty = view.findViewById(R.id.tv_adult_qty);
        TextView tvStudentQty = view.findViewById(R.id.tv_student_qty);
        TextView tvChildQty = view.findViewById(R.id.tv_child_qty);
        TextView tvTotal = view.findViewById(R.id.tv_ticket_total_vnd);
        TextView btnConfirm = view.findViewById(R.id.btn_ticket_confirm);

        tvTitle.setText(Localized.tr("🎫 Đặt Vé Tham quan QR", "🎫 Book QR E-Ticket"));
        tvPlace.setText(Localized.tr(attraction.nameVi, attraction.nameEn));

        final long baseAdultPrice = TicketWalletManager.parseAdultPriceVnd(attraction.ticketPrice);
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        tvBasePrice.setText(baseAdultPrice == 0L
                ? Localized.tr("✨ Điểm tham quan miễn phí — Cấp thẻ E-Pass QR 0đ",
                        "✨ Free admission — Complimentary QR E-Pass")
                : Localized.tr("Giá vé tiêu chuẩn: " + nf.format(baseAdultPrice) + "đ / người lớn",
                        "Standard adult fare: " + nf.format(baseAdultPrice) + " VND"));

        String defaultDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        etDate.setText(defaultDate);

        final int[] slotIdx = {0};
        final int[] counts = {1, 0, 0}; // adult, student, child

        Runnable updateSummary = () -> {
            tvAdultQty.setText(String.valueOf(counts[0]));
            tvStudentQty.setText(String.valueOf(counts[1]));
            tvChildQty.setText(String.valueOf(counts[2]));
            long total = TicketWalletManager.calculateTotalVnd(baseAdultPrice, counts[0], counts[1], counts[2]);
            if (total == 0L) {
                tvTotal.setText(Localized.tr("Tổng cộng: Miễn phí (0đ)", "Total: Free (0 VND)"));
            } else {
                tvTotal.setText(Localized.tr(
                        "Tổng thanh toán: " + nf.format(total) + "đ",
                        "Total Amount: " + nf.format(total) + " VND"));
            }
        };

        Runnable renderSlots = new Runnable() {
            @Override
            public void run() {
                slotBox.removeAllViews();
                for (int i = 0; i < TIME_SLOTS.length; i++) {
                    final int idx = i;
                    slotBox.addView(ChipViewFactory.createChip(
                            context,
                            "🕒 " + TIME_SLOTS[i],
                            idx == slotIdx[0],
                            0,
                            () -> {
                                slotIdx[0] = idx;
                                run();
                            }
                    ));
                }
            }
        };
        renderSlots.run();
        updateSummary.run();

        view.findViewById(R.id.btn_ticket_close).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btn_adult_minus).setOnClickListener(v -> {
            counts[0] = Math.max(0, counts[0] - 1);
            updateSummary.run();
        });
        view.findViewById(R.id.btn_adult_plus).setOnClickListener(v -> {
            counts[0] = Math.min(20, counts[0] + 1);
            updateSummary.run();
        });
        view.findViewById(R.id.btn_student_minus).setOnClickListener(v -> {
            counts[1] = Math.max(0, counts[1] - 1);
            updateSummary.run();
        });
        view.findViewById(R.id.btn_student_plus).setOnClickListener(v -> {
            counts[1] = Math.min(20, counts[1] + 1);
            updateSummary.run();
        });
        view.findViewById(R.id.btn_child_minus).setOnClickListener(v -> {
            counts[2] = Math.max(0, counts[2] - 1);
            updateSummary.run();
        });
        view.findViewById(R.id.btn_child_plus).setOnClickListener(v -> {
            counts[2] = Math.min(20, counts[2] + 1);
            updateSummary.run();
        });

        btnConfirm.setText(Localized.tr(
                "🎫 Phát hành Vé Điện tử QR & Ghi Chi tiêu",
                "🎫 Issue QR E-Ticket & Log Expense"));
        btnConfirm.setOnClickListener(v -> {
            ResultBox<SaigonTravelDatabaseHelper.TicketRow> res = TicketWalletManager.bookTicket(
                    attraction,
                    etDate.getText().toString(),
                    TIME_SLOTS[slotIdx[0]],
                    counts[0],
                    counts[1],
                    counts[2],
                    true
            );
            if (!res.isSuccess()) {
                if (res.exceptionOrNull() instanceof SecurityException) {
                    dialog.dismiss();
                    DialogHelper.showPermissionRequired(context, res.getErrorMessage(), nav);
                } else {
                    Toast.makeText(context, res.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
                return;
            }
            dialog.dismiss();
            if (onBooked != null) onBooked.run();
            SaigonTravelDatabaseHelper.TicketRow created = res.getOrNull();
            if (created != null) {
                showQrTicketPassDialog(context, created);
            }
        });
    }

    // =========================================================================
    // 2. QR E-TICKET BOARDING PASS VIEWER
    // =========================================================================

    public static void showQrTicketPassDialog(Context context, SaigonTravelDatabaseHelper.TicketRow ticket) {
        if (context == null || ticket == null) return;
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_qr_ticket_view, null);
        AlertDialog dialog = DialogHelper.showCustomCardDialog(context, view);

        TextView tvPlace = view.findViewById(R.id.tv_qr_place_name);
        TextView tvCode = view.findViewById(R.id.tv_qr_code_id);
        ImageView ivQr = view.findViewById(R.id.iv_qr_bitmap);
        TextView tvDateTime = view.findViewById(R.id.tv_qr_datetime);
        TextView tvPax = view.findViewById(R.id.tv_qr_pax_total);
        TextView btnShare = view.findViewById(R.id.btn_qr_share);
        TextView btnDone = view.findViewById(R.id.btn_qr_done);

        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        int pax = ticket.adultQty + ticket.studentQty + ticket.childQty;

        tvPlace.setText(ticket.attractionName);
        tvCode.setText(Localized.tr("Mã vé: ", "Pass ID: ") + ticket.id);
        tvDateTime.setText("📅 " + ticket.visitDate + "  •  🕒 " + ticket.timeSlot);
        tvPax.setText(Localized.tr(
                "👥 " + pax + " khách (NL:" + ticket.adultQty + " SV:" + ticket.studentQty + " TE:" + ticket.childQty
                        + ") • " + nf.format(ticket.totalVnd) + "đ",
                "👥 " + pax + " guests • " + nf.format(ticket.totalVnd) + " VND"));

        Bitmap qrBmp = QrBitmapGenerator.generateQrBitmap(ticket.qrPayload, 520, 0xFF0F172A);
        ivQr.setImageBitmap(qrBmp);

        btnShare.setText(Localized.tr("📤 Chia sẻ vé", "📤 Share Pass"));
        btnDone.setText(Localized.tr("Hoàn tất", "Done"));

        btnShare.setOnClickListener(v -> {
            String text = "🎫 VÉ ĐIỆN TỬ QR — EXPLORE SAIGON\n"
                    + "📍 Điểm đến: " + ticket.attractionName + "\n"
                    + "🆔 Mã vé: " + ticket.id + "\n"
                    + "📅 Ngày giờ: " + ticket.visitDate + " (" + ticket.timeSlot + ")\n"
                    + "👥 Số khách: " + pax + " | Tổng: " + nf.format(ticket.totalVnd) + "đ\n"
                    + "🔐 QR Payload: " + ticket.qrPayload;
            MapLauncher.shareText(context, ticket.id, text);
        });
        btnDone.setOnClickListener(v -> dialog.dismiss());
    }

    // =========================================================================
    // 3. MY QR TICKET WALLET DIALOG
    // =========================================================================

    public static void showTicketWalletDialog(Context context, Runnable onChanged) {
        if (context == null) return;
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_ticket_wallet, null);
        AlertDialog dialog = DialogHelper.showCustomCardDialog(context, view);

        TextView tvTitle = view.findViewById(R.id.tv_wallet_title);
        TextView tvSub = view.findViewById(R.id.tv_wallet_subtitle);
        LinearLayout container = view.findViewById(R.id.container_wallet_tickets);
        TextView tvEmpty = view.findViewById(R.id.tv_wallet_empty);

        tvTitle.setText(Localized.tr("🎫 Ví Vé Điện tử QR của tôi", "🎫 My QR E-Ticket Wallet"));
        tvSub.setText(Localized.tr(
                "Lưu trữ ngoại tuyến an toàn trên bảng SQLite tickets",
                "Stored offline in SQLite table tickets"));
        view.findViewById(R.id.btn_wallet_close).setOnClickListener(v -> dialog.dismiss());

        float d = context.getResources().getDisplayMetrics().density;
        NumberFormat nf = NumberFormat.getInstance(Locale.US);

        Runnable renderList = new Runnable() {
            @Override
            public void run() {
                container.removeAllViews();
                List<SaigonTravelDatabaseHelper.TicketRow> list = TicketWalletManager.getTickets();
                tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);

                for (SaigonTravelDatabaseHelper.TicketRow t : list) {
                    LinearLayout row = new LinearLayout(context);
                    row.setOrientation(LinearLayout.VERTICAL);
                    row.setBackgroundResource(R.drawable.bg_stat_tile);
                    int p = (int) (12 * d);
                    row.setPadding(p, p, p, p);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lp.bottomMargin = (int) (8 * d);
                    row.setLayoutParams(lp);

                    TextView tvName = new TextView(context);
                    tvName.setText("🎫 " + t.attractionName + " (" + t.id + ")");
                    tvName.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
                    tvName.setTextSize(13.5f);
                    tvName.setTypeface(null, Typeface.BOLD);
                    row.addView(tvName);

                    int pax = t.adultQty + t.studentQty + t.childQty;
                    TextView tvMeta = new TextView(context);
                    tvMeta.setText("📅 " + t.visitDate + " • " + t.timeSlot + " • " + pax + " khách • " + nf.format(t.totalVnd) + "đ");
                    tvMeta.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
                    tvMeta.setTextSize(12f);
                    row.addView(tvMeta);

                    LinearLayout actions = new LinearLayout(context);
                    actions.setOrientation(LinearLayout.HORIZONTAL);
                    actions.setPadding(0, (int) (8 * d), 0, 0);

                    TextView btnQr = new TextView(context);
                    btnQr.setText(Localized.tr("🔍 Mở Mã QR", "🔍 Show QR"));
                    btnQr.setBackgroundResource(R.drawable.bg_button_teal);
                    btnQr.setTextColor(ContextCompat.getColor(context, R.color.white));
                    btnQr.setTextSize(11.5f);
                    btnQr.setTypeface(null, Typeface.BOLD);
                    btnQr.setGravity(Gravity.CENTER);
                    btnQr.setPadding((int) (12 * d), (int) (6 * d), (int) (12 * d), (int) (6 * d));
                    btnQr.setOnClickListener(v -> showQrTicketPassDialog(context, t));
                    actions.addView(btnQr);

                    TextView btnCancel = new TextView(context);
                    btnCancel.setText(Localized.tr("🗑️ Hủy vé", "🗑️ Cancel"));
                    btnCancel.setBackgroundResource(R.drawable.bg_button_outlined);
                    btnCancel.setTextColor(ContextCompat.getColor(context, R.color.danger_red));
                    btnCancel.setTextSize(11.5f);
                    btnCancel.setTypeface(null, Typeface.BOLD);
                    btnCancel.setGravity(Gravity.CENTER);
                    btnCancel.setPadding((int) (12 * d), (int) (6 * d), (int) (12 * d), (int) (6 * d));
                    LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    clp.setMarginStart((int) (8 * d));
                    btnCancel.setLayoutParams(clp);
                    btnCancel.setOnClickListener(v -> {
                        TicketWalletManager.cancelTicket(t.id);
                        if (onChanged != null) onChanged.run();
                        run();
                    });
                    actions.addView(btnCancel);

                    row.addView(actions);
                    container.addView(row);
                }
            }
        };
        renderList.run();
    }

    // =========================================================================
    // 4. TRAVEL MEMORY JOURNAL & SAIGON POSTCARD DIALOG
    // =========================================================================

    public static void showTravelJournalDialog(
            Context context,
            Attraction attraction,
            AppNavigator nav,
            Runnable onSaved
    ) {
        if (context == null || attraction == null) return;
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_travel_journal, null);
        AlertDialog dialog = DialogHelper.showCustomCardDialog(context, view);

        TextView tvTitle = view.findViewById(R.id.tv_journal_title);
        TextView tvPlace = view.findViewById(R.id.tv_journal_place);
        LinearLayout boxMoods = view.findViewById(R.id.container_journal_moods);
        EditText etDate = view.findViewById(R.id.et_journal_date);
        EditText etNote = view.findViewById(R.id.et_journal_note);
        TextView btnSave = view.findViewById(R.id.btn_journal_save);
        TextView btnPostcard = view.findViewById(R.id.btn_journal_postcard);

        tvTitle.setText(Localized.tr("📸 Nhật ký Kỷ niệm & Bưu thiếp", "📸 Travel Journal & Postcard"));
        tvPlace.setText("📍 " + Localized.tr(attraction.nameVi, attraction.nameEn));

        SaigonTravelDatabaseHelper.JournalRow existing = TravelJournalManager.getJournalForAttraction(attraction.id);
        final String[] selectedMood = {existing != null ? existing.moodEmoji : TravelJournalManager.MOOD_EMOJIS[0]};
        etDate.setText(existing != null
                ? existing.visitDate
                : new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));
        if (existing != null) {
            etNote.setText(existing.note);
        }

        float d = context.getResources().getDisplayMetrics().density;
        Runnable renderMoods = new Runnable() {
            @Override
            public void run() {
                boxMoods.removeAllViews();
                for (String em : TravelJournalManager.MOOD_EMOJIS) {
                    TextView chip = new TextView(context);
                    int sz = (int) (38 * d);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(sz, sz);
                    lp.setMarginEnd((int) (6 * d));
                    chip.setLayoutParams(lp);
                    chip.setGravity(Gravity.CENTER);
                    chip.setText(em);
                    chip.setTextSize(18f);
                    chip.setBackgroundResource(em.equals(selectedMood[0])
                            ? R.drawable.bg_profile_avatar_ring
                            : R.drawable.bg_oauth_button);
                    chip.setOnClickListener(v -> {
                        selectedMood[0] = em;
                        run();
                    });
                    boxMoods.addView(chip);
                }
            }
        };
        renderMoods.run();

        view.findViewById(R.id.btn_journal_close).setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            ResultBox<SaigonTravelDatabaseHelper.JournalRow> res = TravelJournalManager.saveJournal(
                    attraction,
                    selectedMood[0],
                    etNote.getText().toString(),
                    etDate.getText().toString()
            );
            if (!res.isSuccess()) {
                if (res.exceptionOrNull() instanceof SecurityException) {
                    dialog.dismiss();
                    DialogHelper.showPermissionRequired(context, res.getErrorMessage(), nav);
                } else {
                    Toast.makeText(context, res.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
                return;
            }
            if (onSaved != null) onSaved.run();
            Toast.makeText(context,
                    Localized.tr("Đã lưu nhật ký kỷ niệm vào SQLite!", "Memory saved to SQLite!"),
                    Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        btnPostcard.setOnClickListener(v -> {
            ResultBox<SaigonTravelDatabaseHelper.JournalRow> res = TravelJournalManager.saveJournal(
                    attraction,
                    selectedMood[0],
                    etNote.getText().toString(),
                    etDate.getText().toString()
            );
            if (!res.isSuccess()) {
                if (res.exceptionOrNull() instanceof SecurityException) {
                    dialog.dismiss();
                    DialogHelper.showPermissionRequired(context, res.getErrorMessage(), nav);
                } else {
                    Toast.makeText(context, res.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
                return;
            }
            if (onSaved != null) onSaved.run();
            SaigonTravelDatabaseHelper.JournalRow saved = res.getOrNull();
            if (saved != null) {
                MapLauncher.shareText(
                        context,
                        "Postcard - " + attraction.nameVi,
                        TravelJournalManager.formatPostcardText(attraction, saved)
                );
            }
        });
    }

    // =========================================================================
    // 5. SQLITE JSON BACKUP / RESTORE & PRINTABLE TRIP REPORT DIALOG
    // =========================================================================

    public static void showBackupAndReportDialog(Context context, Runnable onRestored) {
        if (context == null) return;
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_backup_restore, null);
        AlertDialog dialog = DialogHelper.showCustomCardDialog(context, view);

        TextView tvTitle = view.findViewById(R.id.tv_backup_title);
        TextView tabReport = view.findViewById(R.id.tab_backup_report);
        TextView tabExport = view.findViewById(R.id.tab_backup_export);
        TextView tabRestore = view.findViewById(R.id.tab_backup_restore);
        EditText etPayload = view.findViewById(R.id.et_backup_payload);
        TextView btnCopy = view.findViewById(R.id.btn_backup_copy);
        TextView btnShare = view.findViewById(R.id.btn_backup_share);
        TextView btnApplyRestore = view.findViewById(R.id.btn_backup_apply_restore);

        tvTitle.setText(Localized.tr("💾 Sao lưu & Hồ sơ Hành trình", "💾 SQLite Backup & Trip Dossier"));
        view.findViewById(R.id.btn_backup_close).setOnClickListener(v -> dialog.dismiss());

        final int[] mode = {0}; // 0 = report, 1 = export JSON, 2 = restore JSON

        Runnable syncMode = () -> {
            styleTab(context, tabReport, mode[0] == 0);
            styleTab(context, tabExport, mode[0] == 1);
            styleTab(context, tabRestore, mode[0] == 2);

            btnApplyRestore.setVisibility(mode[0] == 2 ? View.VISIBLE : View.GONE);
            btnShare.setVisibility(mode[0] == 2 ? View.GONE : View.VISIBLE);

            if (mode[0] == 0) {
                etPayload.setText(UserDataBackupManager.buildShareableTripReport());
            } else if (mode[0] == 1) {
                etPayload.setText(UserDataBackupManager.exportPersonalBackupJson());
            } else {
                etPayload.setHint(Localized.tr(
                        "Dán chuỗi JSON sao lưu vào đây rồi bấm [♻️ Nạp SQLite]...",
                        "Paste your backup JSON here and tap [♻️ Restore SQLite]..."));
                etPayload.setText(UserDataBackupManager.exportPersonalBackupJson());
            }
        };

        tabReport.setOnClickListener(v -> { mode[0] = 0; syncMode.run(); });
        tabExport.setOnClickListener(v -> { mode[0] = 1; syncMode.run(); });
        tabRestore.setOnClickListener(v -> { mode[0] = 2; syncMode.run(); });
        syncMode.run();

        btnCopy.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText("ExploreSaigonData", etPayload.getText().toString()));
                Toast.makeText(context,
                        Localized.tr("📋 Đã sao chép vào bộ nhớ tạm!", "📋 Copied to clipboard!"),
                        Toast.LENGTH_SHORT).show();
            }
        });

        btnShare.setOnClickListener(v -> MapLauncher.shareText(
                context,
                mode[0] == 0 ? "ExploreSaigon_TripReport" : "ExploreSaigon_Backup.json",
                etPayload.getText().toString()
        ));

        btnApplyRestore.setOnClickListener(v -> {
            ResultBox<Integer> res = UserDataBackupManager.importPersonalBackupJson(etPayload.getText().toString());
            if (!res.isSuccess()) {
                Toast.makeText(context, res.getErrorMessage(), Toast.LENGTH_LONG).show();
                return;
            }
            if (onRestored != null) onRestored.run();
            Toast.makeText(context,
                    Localized.tr("♻️ Đã khôi phục " + res.getOrNull() + " mục vào SQLite!",
                            "♻️ Restored " + res.getOrNull() + " items into SQLite!"),
                    Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
    }

    private static void styleTab(Context context, TextView tv, boolean active) {
        tv.setBackgroundResource(active ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        tv.setTextColor(ContextCompat.getColor(context, active ? R.color.white : R.color.text_primary));
    }
}
