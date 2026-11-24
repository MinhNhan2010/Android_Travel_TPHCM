package com.example.nhom_15_dulichtphcm.ui.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Pure Java interactive circular photo cropper & framing view supporting:
 * - 1-finger drag (pan) with automatic boundary clamping
 * - 2-finger pinch-to-zoom (1.0x – 4.5x)
 * - Programmatic zoom slider synchronization & 90-degree clockwise rotation
 * - Exact WYSIWYG square/circular bitmap export
 */
public final class AvatarCropperView extends View {

    public interface OnZoomChangedListener {
        void onZoomChanged(float zoomFactor);
    }

    private static final float MIN_ZOOM = 1.0f;
    private static final float MAX_ZOOM = 4.5f;

    private Bitmap sourceBitmap;
    private int rotationDegrees = 0;
    private float zoomFactor = 1.0f;
    private float offsetX = 0f;
    private float offsetY = 0f;

    private final Matrix drawMatrix = new Matrix();
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint scrimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderOuterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderInnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path scrimPath = new Path();
    private final Path circleClipPath = new Path();

    private final ScaleGestureDetector scaleDetector;
    private float lastTouchX = 0f;
    private float lastTouchY = 0f;
    private int activePointerId = MotionEvent.INVALID_POINTER_ID;
    private boolean isInteracting = false;
    private OnZoomChangedListener onZoomChangedListener;

    public AvatarCropperView(Context context) {
        this(context, null);
    }

    public AvatarCropperView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        float d = context.getResources().getDisplayMetrics().density;

        scrimPaint.setStyle(Paint.Style.FILL);
        scrimPaint.setColor(0xAD0F172A);

        borderOuterPaint.setStyle(Paint.Style.STROKE);
        borderOuterPaint.setStrokeWidth(2.5f * d);
        borderOuterPaint.setColor(0xFFD84315);

        borderInnerPaint.setStyle(Paint.Style.STROKE);
        borderInnerPaint.setStrokeWidth(1.2f * d);
        borderInnerPaint.setColor(0xDDFFFFFF);

        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1.0f * d);
        gridPaint.setColor(0x66FFFFFF);

        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float factor = detector.getScaleFactor();
                setZoomFactorInternal(zoomFactor * factor, true);
                return true;
            }
        });
    }

    public void setOnZoomChangedListener(@Nullable OnZoomChangedListener listener) {
        this.onZoomChangedListener = listener;
    }

    public void setImageBitmap(@Nullable Bitmap bitmap) {
        this.sourceBitmap = bitmap;
        this.rotationDegrees = 0;
        this.zoomFactor = 1.0f;
        this.offsetX = 0f;
        this.offsetY = 0f;
        if (onZoomChangedListener != null) {
            onZoomChangedListener.onZoomChanged(zoomFactor);
        }
        invalidate();
    }

    public float getZoomFactor() {
        return zoomFactor;
    }

    public float getMinZoom() {
        return MIN_ZOOM;
    }

    public float getMaxZoom() {
        return MAX_ZOOM;
    }

    public void setZoomFactor(float newZoom) {
        setZoomFactorInternal(newZoom, false);
    }

    private void setZoomFactorInternal(float newZoom, boolean notifyListener) {
        float clamped = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, newZoom));
        if (Math.abs(clamped - zoomFactor) < 0.001f) return;
        // Scale offsets proportionally around the circle center so zoom feels anchored
        float ratio = clamped / zoomFactor;
        offsetX *= ratio;
        offsetY *= ratio;
        zoomFactor = clamped;
        clampOffsets();
        if (notifyListener && onZoomChangedListener != null) {
            onZoomChangedListener.onZoomChanged(zoomFactor);
        }
        invalidate();
    }

    public void rotate90Clockwise() {
        rotationDegrees = (rotationDegrees + 90) % 360;
        // Rotate current pan vector 90 degrees clockwise so framing stays intuitive
        float oldX = offsetX;
        offsetX = -offsetY;
        offsetY = oldX;
        clampOffsets();
        invalidate();
    }

    public void resetTransform() {
        rotationDegrees = 0;
        zoomFactor = 1.0f;
        offsetX = 0f;
        offsetY = 0f;
        if (onZoomChangedListener != null) {
            onZoomChangedListener.onZoomChanged(zoomFactor);
        }
        invalidate();
    }

    private float getCropRadius() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return 120f;
        return Math.min(w, h) * 0.43f;
    }

    private float getRotatedWidth() {
        if (sourceBitmap == null) return 1f;
        return (rotationDegrees % 180 == 0) ? sourceBitmap.getWidth() : sourceBitmap.getHeight();
    }

    private float getRotatedHeight() {
        if (sourceBitmap == null) return 1f;
        return (rotationDegrees % 180 == 0) ? sourceBitmap.getHeight() : sourceBitmap.getWidth();
    }

    private float computeBaseScale(float diameter) {
        float rw = getRotatedWidth();
        float rh = getRotatedHeight();
        return Math.max(diameter / rw, diameter / rh);
    }

    private void clampOffsets() {
        if (sourceBitmap == null || getWidth() <= 0 || getHeight() <= 0) return;
        float radius = getCropRadius();
        float diameter = radius * 2f;
        float totalScale = computeBaseScale(diameter) * zoomFactor;

        float halfScaledW = (getRotatedWidth() * totalScale) * 0.5f;
        float halfScaledH = (getRotatedHeight() * totalScale) * 0.5f;

        float maxDx = Math.max(0f, halfScaledW - radius);
        float maxDy = Math.max(0f, halfScaledH - radius);

        offsetX = Math.max(-maxDx, Math.min(maxDx, offsetX));
        offsetY = Math.max(-maxDy, Math.min(maxDy, offsetY));
    }

    private void updateDrawMatrix(float centerX, float centerY, float diameter) {
        drawMatrix.reset();
        if (sourceBitmap == null) return;
        float bw = sourceBitmap.getWidth();
        float bh = sourceBitmap.getHeight();
        float totalScale = computeBaseScale(diameter) * zoomFactor;

        drawMatrix.postTranslate(-bw * 0.5f, -bh * 0.5f);
        drawMatrix.postRotate(rotationDegrees);
        drawMatrix.postScale(totalScale, totalScale);
        drawMatrix.postTranslate(centerX + offsetX, centerY + offsetY);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        clampOffsets();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        canvas.drawColor(0xFF0B1120);

        float cx = w * 0.5f;
        float cy = h * 0.5f;
        float radius = getCropRadius();
        float diameter = radius * 2f;

        if (sourceBitmap != null && !sourceBitmap.isRecycled()) {
            clampOffsets();
            updateDrawMatrix(cx, cy, diameter);
            canvas.drawBitmap(sourceBitmap, drawMatrix, bitmapPaint);
        }

        // Translucent dark overlay outside the circular aperture using EVEN_ODD fill rule
        scrimPath.reset();
        scrimPath.setFillType(Path.FillType.EVEN_ODD);
        scrimPath.addRect(0f, 0f, w, h, Path.Direction.CW);
        scrimPath.addCircle(cx, cy, radius, Path.Direction.CW);
        canvas.drawPath(scrimPath, scrimPaint);

        // Rule-of-thirds alignment grid inside the circle while dragging/zooming
        if (isInteracting) {
            canvas.save();
            circleClipPath.reset();
            circleClipPath.addCircle(cx, cy, radius, Path.Direction.CW);
            canvas.clipPath(circleClipPath);
            float step = diameter / 3f;
            float left = cx - radius;
            float top = cy - radius;
            for (int i = 1; i <= 2; i++) {
                canvas.drawLine(left + step * i, top, left + step * i, top + diameter, gridPaint);
                canvas.drawLine(left, top + step * i, left + diameter, top + step * i, gridPaint);
            }
            canvas.restore();
        }

        canvas.drawCircle(cx, cy, radius, borderOuterPaint);
        canvas.drawCircle(cx, cy, Math.max(1f, radius - borderOuterPaint.getStrokeWidth()), borderInnerPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (sourceBitmap == null) return false;
        getParent().requestDisallowInterceptTouchEvent(true);
        scaleDetector.onTouchEvent(event);

        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                activePointerId = event.getPointerId(0);
                lastTouchX = event.getX(0);
                lastTouchY = event.getY(0);
                isInteracting = true;
                invalidate();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (!scaleDetector.isInProgress()) {
                    int idx = event.findPointerIndex(activePointerId);
                    if (idx >= 0) {
                        float x = event.getX(idx);
                        float y = event.getY(idx);
                        offsetX += (x - lastTouchX);
                        offsetY += (y - lastTouchY);
                        clampOffsets();
                        lastTouchX = x;
                        lastTouchY = y;
                        invalidate();
                    }
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                int ptrIdx = event.getActionIndex();
                int ptrId = event.getPointerId(ptrIdx);
                if (ptrId == activePointerId) {
                    int newIdx = (ptrIdx == 0) ? 1 : 0;
                    if (newIdx < event.getPointerCount()) {
                        lastTouchX = event.getX(newIdx);
                        lastTouchY = event.getY(newIdx);
                        activePointerId = event.getPointerId(newIdx);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                activePointerId = MotionEvent.INVALID_POINTER_ID;
                isInteracting = false;
                invalidate();
                break;
            }
        }
        return true;
    }

    /**
     * Exports the exact framed square region corresponding to the circular crop window.
     */
    @Nullable
    public Bitmap exportCroppedSquareBitmap(int outputSizePx) {
        if (sourceBitmap == null || sourceBitmap.isRecycled()) return null;
        int outDim = Math.max(128, outputSizePx);
        Bitmap result = Bitmap.createBitmap(outDim, outDim, Bitmap.Config.ARGB_8888);
        Canvas outCanvas = new Canvas(result);
        outCanvas.drawColor(Color.BLACK);

        float viewRadius = getCropRadius();
        float viewDiameter = Math.max(1f, viewRadius * 2f);
        float exportRatio = outDim / viewDiameter;

        Matrix exportMatrix = new Matrix();
        float bw = sourceBitmap.getWidth();
        float bh = sourceBitmap.getHeight();
        float totalScale = computeBaseScale(outDim) * zoomFactor;

        exportMatrix.postTranslate(-bw * 0.5f, -bh * 0.5f);
        exportMatrix.postRotate(rotationDegrees);
        exportMatrix.postScale(totalScale, totalScale);
        exportMatrix.postTranslate(outDim * 0.5f + offsetX * exportRatio, outDim * 0.5f + offsetY * exportRatio);

        outCanvas.drawBitmap(sourceBitmap, exportMatrix, bitmapPaint);
        return result;
    }
}
