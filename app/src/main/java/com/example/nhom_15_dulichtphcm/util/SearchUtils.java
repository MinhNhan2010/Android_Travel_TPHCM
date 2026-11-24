package com.example.nhom_15_dulichtphcm.util;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Accent-insensitive Vietnamese & multilingual fuzzy search helper.
 */
public final class SearchUtils {

    private SearchUtils() {}

    public static String normalize(String input) {
        if (input == null || input.isEmpty()) return "";
        String decomposed = Normalizer.normalize(input, Normalizer.Form.NFD);
        StringBuilder sb = new StringBuilder(decomposed.length());
        for (int i = 0; i < decomposed.length(); i++) {
            char c = decomposed.charAt(i);
            int type = Character.getType(c);
            if (type == Character.NON_SPACING_MARK) {
                continue;
            }
            if (c == 'đ' || c == 'Đ') {
                sb.append('d');
            } else {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString().replaceAll("\\s+", " ").trim();
    }

    public static boolean fuzzyContains(String haystack, String query) {
        if (query == null || query.trim().isEmpty()) return true;
        if (haystack == null || haystack.isEmpty()) return false;
        String normHaystack = normalize(haystack);
        String normQuery = normalize(query);
        if (normQuery.isEmpty()) return true;
        return normHaystack.contains(normQuery);
    }

    public static boolean matchesAllTokens(String combinedFields, String query) {
        if (query == null || query.trim().isEmpty()) return true;
        if (combinedFields == null) return false;
        String normFields = normalize(combinedFields);
        String normQuery = normalize(query);
        if (normQuery.isEmpty()) return true;
        String[] tokens = normQuery.split(" ");
        for (String token : tokens) {
            if (!token.isEmpty() && !normFields.contains(token)) {
                return false;
            }
        }
        return true;
    }

    public static String safeLower(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT);
    }
}
