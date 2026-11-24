package com.example.nhom_15_dulichtphcm.util;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;

/**
 * Formats lightweight Markdown (**bold**) into Android Spannable CharSequence for TextViews.
 */
public final class MarkdownFormatter {

    private MarkdownFormatter() {}

    public static CharSequence format(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        SpannableStringBuilder sb = new SpannableStringBuilder();
        int i = 0;
        int len = raw.length();
        while (i < len) {
            if (i + 1 < len && raw.charAt(i) == '*' && raw.charAt(i + 1) == '*') {
                int close = raw.indexOf("**", i + 2);
                if (close != -1) {
                    int startSpan = sb.length();
                    sb.append(raw, i + 2, close);
                    sb.setSpan(new StyleSpan(Typeface.BOLD), startSpan, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    i = close + 2;
                    continue;
                }
            }
            sb.append(raw.charAt(i));
            i++;
        }
        return sb;
    }
}
