package com.example.nhom_15_dulichtphcm.util;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.Locale;

/**
 * Bilingual Vietnamese & English TTS Audio Guide & Pronunciation Helper.
 */
public final class SpeechHelper {

    private TextToSpeech tts;
    private boolean isReady = false;
    private final Context appContext;

    public SpeechHelper(Context context) {
        this.appContext = context != null ? context.getApplicationContext() : null;
        if (this.appContext != null) {
            try {
                this.tts = new TextToSpeech(this.appContext, status -> {
                    if (status == TextToSpeech.SUCCESS && tts != null) {
                        Locale vi = Locale.forLanguageTag("vi-VN");
                        int res = tts.setLanguage(vi);
                        if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                            tts.setLanguage(Locale.US);
                        }
                        tts.setSpeechRate(0.92f);
                        isReady = true;
                    }
                });
            } catch (Exception ignored) {
            }
        }
    }

    public void speakVietnamese(String text) {
        speakWithLocale(text, Locale.forLanguageTag("vi-VN"), 0.94f);
    }

    public void speakVietnameseSlow(String text) {
        speakWithLocale(text, Locale.forLanguageTag("vi-VN"), 0.62f);
    }

    public void speakGuide(String viText, String enText, float speedMultiplier) {
        boolean isVi = LanguageManager.getCurrentLanguage() == AppLanguage.VIETNAMESE;
        String chosen = isVi ? viText : (enText != null && !enText.isEmpty() ? enText : viText);
        Locale targetLocale = isVi ? Locale.forLanguageTag("vi-VN") : Locale.US;
        float clampedRate = Math.max(0.55f, Math.min(1.5f, speedMultiplier));
        speakWithLocale(chosen, targetLocale, clampedRate);
    }

    public boolean isSpeaking() {
        return tts != null && isReady && tts.isSpeaking();
    }

    public void stopSpeaking() {
        if (tts != null) {
            try {
                tts.stop();
            } catch (Exception ignored) {
            }
        }
    }

    private void speakWithLocale(String text, Locale preferredLocale, float rate) {
        if (text == null || text.trim().isEmpty()) return;
        if (tts != null && isReady) {
            try {
                int res = tts.setLanguage(preferredLocale);
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.setLanguage(Locale.US);
                }
            } catch (Exception ignored) {
            }
            tts.setSpeechRate(rate);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "guide_" + System.currentTimeMillis());
        } else if (appContext != null) {
            Toast.makeText(appContext,
                    Localized.tr("Đang khởi tạo trình đọc thuyết minh...", "Initializing audio narrator..."),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void shutdown() {
        if (tts != null) {
            try {
                tts.stop();
                tts.shutdown();
            } catch (Exception ignored) {
            }
            tts = null;
            isReady = false;
        }
    }
}
