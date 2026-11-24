package com.example.nhom_15_dulichtphcm.data.ai;

import com.example.nhom_15_dulichtphcm.data.model.AiChatMessage;
import com.example.nhom_15_dulichtphcm.util.AppLanguage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * In-session multi-turn conversation state for the Smart Travel Guide screen.
 */
public final class AiChatSession {

    private static final List<AiChatMessage> messages = new ArrayList<>();
    private static volatile boolean isThinking = false;
    private static AppLanguage greetedLanguage = null;

    private AiChatSession() {}

    public static synchronized void ensureGreeting(AppLanguage lang) {
        if (messages.isEmpty()) {
            messages.add(AiTravelEngine.buildWelcomeMessage(lang));
            greetedLanguage = lang;
        } else if (messages.size() == 1 && !messages.get(0).isUser && greetedLanguage != lang) {
            messages.set(0, AiTravelEngine.buildWelcomeMessage(lang));
            greetedLanguage = lang;
        }
    }

    public static synchronized List<AiChatMessage> getMessages() {
        return Collections.unmodifiableList(new ArrayList<>(messages));
    }

    public static boolean isThinking() {
        return isThinking;
    }

    public static synchronized void send(String query, AppLanguage lang, Runnable onUpdate) {
        if (query == null || query.trim().isEmpty() || isThinking) return;
        String clean = query.trim();
        List<AiChatMessage> snapshotBefore = new ArrayList<>(messages);
        messages.add(AiChatMessage.userMessage(clean));
        isThinking = true;
        if (onUpdate != null) onUpdate.run();

        GeminiService.generateReplyAsync(clean, snapshotBefore, lang, reply -> {
            synchronized (AiChatSession.class) {
                messages.add(reply);
                isThinking = false;
            }
            if (onUpdate != null) onUpdate.run();
        });
    }

    public static synchronized void clear(AppLanguage lang) {
        messages.clear();
        isThinking = false;
        ensureGreeting(lang);
    }
}
