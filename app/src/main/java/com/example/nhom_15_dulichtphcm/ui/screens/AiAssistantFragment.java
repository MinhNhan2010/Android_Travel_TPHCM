package com.example.nhom_15_dulichtphcm.ui.screens;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom_15_dulichtphcm.R;
import com.example.nhom_15_dulichtphcm.data.ai.AiChatSession;
import com.example.nhom_15_dulichtphcm.data.ai.GeminiService;
import com.example.nhom_15_dulichtphcm.data.model.AiChatMessage;
import com.example.nhom_15_dulichtphcm.ui.AppNavigator;
import com.example.nhom_15_dulichtphcm.ui.adapters.ChatMessageAdapter;
import com.example.nhom_15_dulichtphcm.util.LanguageManager;
import com.example.nhom_15_dulichtphcm.util.Localized;

import java.util.List;

/**
 * Pure Java + XML Smart Travel Guide Concierge screen backed by Hybrid Cloud Gemini + SQLite RAG.
 */
public final class AiAssistantFragment extends Fragment {

    private View rootView;
    private RecyclerView rvMessages;
    private TextView tvThinking;
    private TextView tvModeBadge;
    private EditText etInput;
    private ChatMessageAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_ai_assistant, container, false);
        AiChatSession.ensureGreeting(LanguageManager.getCurrentLanguage());

        rvMessages = rootView.findViewById(R.id.rv_chat_messages);
        tvThinking = rootView.findViewById(R.id.tv_ai_thinking);
        tvModeBadge = rootView.findViewById(R.id.tv_ai_mode_badge);
        etInput = rootView.findViewById(R.id.et_ai_input);
        TextView btnSend = rootView.findViewById(R.id.btn_ai_send);
        TextView tvTitle = rootView.findViewById(R.id.tv_toolbar_title);
        TextView tvSub = rootView.findViewById(R.id.tv_ai_header_sub);

        tvTitle.setText(Localized.tr("Trợ lý Du lịch AI ✨", "AI Travel Assistant ✨"));
        tvSub.setText(Localized.tr(
                "Hỏi đáp tự do lịch trình, giá vé, quán ngon, gọi xe & kinh nghiệm bản địa",
                "Ask anything: itineraries, tickets, eateries, ride fares & local tips"));
        etInput.setHint(Localized.tr(
                "Hỏi bất kỳ câu hỏi nào (giá vé, quán ngon, lộ trình...)",
                "Ask any question (tickets, food, routes, hotels...)"));
        etInput.setImeOptions(EditorInfo.IME_ACTION_SEND);

        updateModeBadge();

        rootView.findViewById(R.id.btn_toolbar_back).setOnClickListener(v -> {
            AppNavigator nav = getNavigator();
            if (nav != null) nav.navigateBack();
        });

        rootView.findViewById(R.id.btn_toolbar_action).setOnClickListener(v -> {
            AiChatSession.clear(LanguageManager.getCurrentLanguage());
            refreshMessages();
        });

        rootView.findViewById(R.id.btn_ai_api_key).setOnClickListener(v -> showApiKeyDialog());

        populateQuickPrompts();

        LinearLayoutManager lm = new LinearLayoutManager(requireContext());
        rvMessages.setLayoutManager(lm);
        adapter = new ChatMessageAdapter(getNavigator(), this::submitQuery);
        rvMessages.setAdapter(adapter);

        Runnable sendAction = () -> {
            String q = etInput.getText().toString();
            if (q.trim().isEmpty()) return;
            etInput.setText("");
            submitQuery(q);
        };

        btnSend.setOnClickListener(v -> sendAction.run());
        etInput.setOnEditorActionListener((v, actionId, event) -> {
            boolean enterPressed = event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN;
            if (actionId == EditorInfo.IME_ACTION_SEND || enterPressed) {
                sendAction.run();
                return true;
            }
            return false;
        });

        refreshMessages();
        return rootView;
    }

    private void updateModeBadge() {
        if (tvModeBadge == null) return;
        List<AiChatMessage> msgs = AiChatSession.getMessages();
        Boolean lastFromCloud = null;
        for (int i = msgs.size() - 1; i >= 1; i--) {
            AiChatMessage m = msgs.get(i);
            if (!m.isUser) {
                lastFromCloud = m.fromGemini;
                break;
            }
        }
        if (Boolean.TRUE.equals(lastFromCloud)) {
            tvModeBadge.setText("✨ GEMINI AI");
        } else if (Boolean.FALSE.equals(lastFromCloud)) {
            tvModeBadge.setText("⚡ SQLITE AI");
        } else {
            tvModeBadge.setText(GeminiService.isActive() ? "✨ GEMINI LIVE" : "⚡ SQLITE AI");
        }
    }

    private void populateQuickPrompts() {
        LinearLayout box = rootView.findViewById(R.id.container_ai_quick_prompts);
        if (box == null) return;
        box.removeAllViews();

        String[][] prompts = {
                {Localized.tr("🗺️ Lịch trình 3 ngày", "🗺️ 3-Day Itinerary"),
                        Localized.tr("Gợi ý lịch trình 3 ngày khám phá TP.HCM - Bình Dương - Vũng Tàu", "3-Day HCMC - Binh Duong - Vung Tau Itinerary")},
                {Localized.tr("🎟️ Giá vé Dinh Độc Lập", "🎟️ Palace Tickets"),
                        Localized.tr("Giá vé và giờ mở cửa Dinh Độc Lập là bao nhiêu?", "What are Independence Palace ticket prices and opening hours?")},
                {Localized.tr("🚕 Sân bay về Quận 1", "🚕 Airport to Dist. 1"),
                        Localized.tr("Từ sân bay Tân Sơn Nhất về Chợ Bến Thành bao xa và cước Grab/Xanh SM bao nhiêu?", "Distance and Grab/Xanh SM fare from Tan Son Nhat Airport to Ben Thanh Market?")},
                {Localized.tr("🌃 Ăn đêm & Phố Ốc", "🌃 Night Food & Snails"),
                        Localized.tr("Gợi ý quán ăn đêm, ốc ngon và chơi tối ở Sài Gòn", "Best late-night street food, snails, and nightlife in Saigon")},
                {Localized.tr("💰 Chi phí 3 ngày", "💰 3-Day Budget"),
                        Localized.tr("Ước tính chi phí du lịch Sài Gòn 3 ngày hết bao nhiêu tiền?", "Estimated budget for 3 days in Saigon")},
                {Localized.tr("☔ Trời mưa đi đâu?", "☔ Rainy Day Plan"),
                        Localized.tr("Trời mưa ở Sài Gòn nên đi chơi ở đâu trong nhà?", "Best indoor places to visit in Saigon when it rains?")},
                {Localized.tr("🏨 Khách sạn & Quà", "🏨 Hotels & Gifts"),
                        Localized.tr("Du lịch Sài Gòn nên ở khách sạn khu nào và mua quà lưu niệm gì?", "Best neighborhoods to stay in Saigon and top souvenirs to buy?")},
                {Localized.tr("🚇 Tàu Metro & Waterbus", "🚇 Metro & Waterbus"),
                        Localized.tr("Hướng dẫn đi Tàu Metro số 1, Saigon Waterbus và Tàu cao tốc Vũng Tàu", "Guide to Metro Line 1, Saigon Waterbus, and Vung Tau Fast Ferry")}
        };

        float d = getResources().getDisplayMetrics().density;
        for (String[] p : prompts) {
            TextView pill = new TextView(requireContext());
            pill.setText(p[0]);
            pill.setBackgroundResource(R.drawable.bg_chip_unselected);
            pill.setTextColor(ContextCompat.getColor(requireContext(), R.color.saigon_red));
            pill.setTextSize(12f);
            pill.setTypeface(null, Typeface.BOLD);
            pill.setGravity(Gravity.CENTER_VERTICAL);
            int ph = (int) (13 * d);
            int pv = (int) (7 * d);
            pill.setPadding(ph, pv, ph, pv);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd((int) (8 * d));
            pill.setLayoutParams(lp);
            final String queryText = p[1];
            pill.setOnClickListener(v -> submitQuery(queryText));
            box.addView(pill);
        }
    }

    private void showApiKeyDialog() {
        float d = getResources().getDisplayMetrics().density;
        LinearLayout wrap = new LinearLayout(requireContext());
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding((int) (20 * d), (int) (10 * d), (int) (20 * d), (int) (4 * d));

        EditText input = new EditText(requireContext());
        input.setHint(GeminiService.hasBuildKey()
                ? Localized.tr("Đang dùng khóa Cloud tích hợp sẵn (nhập khóa riêng nếu muốn)", "Built-in Cloud key active (optional override)")
                : "AIzaSy... (Gemini API Key - Optional)");
        input.setText(GeminiService.getUserApiKey());
        input.setBackgroundResource(R.drawable.bg_search_box);
        input.setPadding((int) (12 * d), (int) (10 * d), (int) (12 * d), (int) (10 * d));
        wrap.addView(input);

        new AlertDialog.Builder(requireContext())
                .setTitle(Localized.tr("🔑 Cấu hình Trợ lý AI (Hybrid Cloud + SQLite)", "🔑 Configure Hybrid AI Concierge"))
                .setMessage(Localized.tr(
                        "Hệ thống kết hợp Gemini Cloud (trả lời tự do mọi câu hỏi kèm dữ liệu thực tế của ứng dụng) và Bộ não Offline SQLite 16 chủ đề (hoạt động tức thì ngay cả khi mất mạng).",
                        "Combines Gemini Cloud (grounded in app data for open-ended questions) with a 16-topic SQLite Offline Engine for instant zero-network answers."))
                .setView(wrap)
                .setPositiveButton(Localized.tr("Lưu", "Save"), (dlg, w) -> {
                    GeminiService.savePersonalKey(input.getText().toString());
                    updateModeBadge();
                    Toast.makeText(requireContext(),
                            Localized.tr("Đã cập nhật cấu hình Trợ lý AI!", "Updated AI Concierge settings!"),
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(Localized.tr("Đóng", "Close"), null)
                .show();
    }

    private void submitQuery(String query) {
        if (query == null || query.trim().isEmpty()) return;
        AiChatSession.send(query, LanguageManager.getCurrentLanguage(), () -> {
            if (isAdded()) refreshMessages();
        });
        refreshMessages();
    }

    private void refreshMessages() {
        if (!isAdded()) return;
        adapter.submitList(AiChatSession.getMessages());
        tvThinking.setVisibility(AiChatSession.isThinking() ? View.VISIBLE : View.GONE);
        updateModeBadge();
        int count = adapter.getItemCount();
        if (count > 0) {
            rvMessages.scrollToPosition(count - 1);
        }
    }

    private AppNavigator getNavigator() {
        return getActivity() instanceof AppNavigator ? (AppNavigator) getActivity() : null;
    }
}
