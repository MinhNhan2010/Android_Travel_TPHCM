package com.example.nhom_15_dulichtphcm.ui.components;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Pure Java 2D Canvas QR Matrix Bitmap Renderer (offline, zero third-party dependencies).
 * Renders standard 25x25 QR-style finder patterns, alignment pattern, timing patterns,
 * and SHA-256 deterministic payload cells with a crisp rounded frame.
 */
public final class QrBitmapGenerator {

    private static final int GRID_SIZE = 25;

    private QrBitmapGenerator() {}

    public static Bitmap generateQrBitmap(String payload, int pixelSize, int darkColor) {
        int size = Math.max(240, pixelSize);
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);

        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.WHITE);
        canvas.drawRoundRect(new RectF(0, 0, size, size), size * 0.06f, size * 0.06f, bgPaint);

        boolean[][] matrix = new boolean[GRID_SIZE][GRID_SIZE];
        boolean[][] reserved = new boolean[GRID_SIZE][GRID_SIZE];

        // 3 standard 7x7 finder patterns (top-left, top-right, bottom-left)
        placeFinderPattern(matrix, reserved, 0, 0);
        placeFinderPattern(matrix, reserved, GRID_SIZE - 7, 0);
        placeFinderPattern(matrix, reserved, 0, GRID_SIZE - 7);

        // 5x5 alignment pattern at (16, 16)
        placeAlignmentPattern(matrix, reserved, 16, 16);

        // Timing patterns on row 6 & col 6
        for (int i = 8; i < GRID_SIZE - 8; i++) {
            reserved[6][i] = true;
            reserved[i][6] = true;
            matrix[6][i] = (i % 2 == 0);
            matrix[i][6] = (i % 2 == 0);
        }

        byte[] digest = sha256Twice(payload != null ? payload : "SAIGON_ETICKET");
        int bitIdx = 0;
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                if (reserved[r][c]) continue;
                int bytePos = (bitIdx / 8) % digest.length;
                int bitPos = bitIdx % 8;
                matrix[r][c] = ((digest[bytePos] >> bitPos) & 1) == 1;
                bitIdx++;
            }
        }

        float quietZone = size * 0.09f;
        float cell = (size - 2f * quietZone) / GRID_SIZE;

        Paint cellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cellPaint.setColor(darkColor);

        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                if (matrix[r][c]) {
                    float left = quietZone + c * cell;
                    float top = quietZone + r * cell;
                    float pad = cell * 0.06f;
                    canvas.drawRoundRect(
                            new RectF(left + pad, top + pad, left + cell - pad, top + cell - pad),
                            cell * 0.22f,
                            cell * 0.22f,
                            cellPaint
                    );
                }
            }
        }
        return bmp;
    }

    private static void placeFinderPattern(boolean[][] m, boolean[][] res, int startR, int startC) {
        for (int dr = -1; dr <= 7; dr++) {
            for (int dc = -1; dc <= 7; dc++) {
                int r = startR + dr;
                int c = startC + dc;
                if (r < 0 || r >= GRID_SIZE || c < 0 || c >= GRID_SIZE) continue;
                res[r][c] = true;
                if (dr >= 0 && dr <= 6 && dc >= 0 && dc <= 6) {
                    boolean border = (dr == 0 || dr == 6 || dc == 0 || dc == 6);
                    boolean inner = (dr >= 2 && dr <= 4 && dc >= 2 && dc <= 4);
                    m[r][c] = border || inner;
                } else {
                    m[r][c] = false;
                }
            }
        }
    }

    private static void placeAlignmentPattern(boolean[][] m, boolean[][] res, int startR, int startC) {
        for (int dr = 0; dr < 5; dr++) {
            for (int dc = 0; dc < 5; dc++) {
                int r = startR + dr;
                int c = startC + dc;
                res[r][c] = true;
                boolean border = (dr == 0 || dr == 4 || dc == 0 || dc == 4);
                boolean center = (dr == 2 && dc == 2);
                m[r][c] = border || center;
            }
        }
    }

    private static byte[] sha256Twice(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] first = md.digest(input.getBytes(StandardCharsets.UTF_8));
            byte[] second = md.digest((input + "#QR2").getBytes(StandardCharsets.UTF_8));
            byte[] out = new byte[first.length + second.length];
            System.arraycopy(first, 0, out, 0, first.length);
            System.arraycopy(second, 0, out, first.length, second.length);
            return out;
        } catch (Exception e) {
            return input.getBytes(StandardCharsets.UTF_8);
        }
    }
}
