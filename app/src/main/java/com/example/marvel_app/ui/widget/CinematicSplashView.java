package com.example.marvel_app.ui.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

/**
 * Lightweight procedural layer for the opening screen. It keeps motion in vectors and Canvas
 * primitives so startup does not depend on a video or on multiple full-screen bitmaps.
 */
public final class CinematicSplashView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private ValueAnimator animator;
    private float phase;

    public CinematicSplashView(Context context) {
        this(context, null);
    }

    public CinematicSplashView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    public void start() {
        stop();
        if (!ValueAnimator.areAnimatorsEnabled()) {
            phase = 0.58f;
            invalidate();
            return;
        }
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(2850L);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(value -> {
            phase = (float) value.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    public void stop() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        stop();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();
        if (width <= 0f || height <= 0f) return;

        drawRedCurtains(canvas, width, height);
        drawPanelCuts(canvas, width, height);
        drawHalftone(canvas, width, height);
        drawLightSweep(canvas, width, height);
    }

    private void drawRedCurtains(Canvas canvas, float width, float height) {
        float drift = (phase - 0.5f) * width * 0.18f;
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(Color.argb(205, 193, 12, 28));
        path.reset();
        path.moveTo(-width * 0.28f + drift, 0f);
        path.lineTo(width * 0.36f + drift, 0f);
        path.lineTo(width * 0.78f + drift, height);
        path.lineTo(width * 0.08f + drift, height);
        path.close();
        canvas.drawPath(path, paint);

        paint.setColor(Color.argb(150, 239, 28, 43));
        path.reset();
        path.moveTo(width * 0.74f - drift, 0f);
        path.lineTo(width * 1.20f - drift, 0f);
        path.lineTo(width * 0.83f - drift, height);
        path.lineTo(width * 0.43f - drift, height);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawPanelCuts(Canvas canvas, float width, float height) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(5f));
        paint.setColor(Color.argb(190, 0, 0, 0));
        float slide = phase * width * 0.08f;
        canvas.drawLine(-width * 0.05f + slide, height * 0.27f,
                width * 1.05f + slide, height * 0.12f, paint);
        canvas.drawLine(-width * 0.10f - slide, height * 0.76f,
                width * 1.10f - slide, height * 0.88f, paint);
    }

    private void drawHalftone(Canvas canvas, float width, float height) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(90, 255, 244, 199));
        float spacing = dp(18f);
        float offset = (phase * spacing * 2f) % spacing;
        for (float y = height * 0.08f; y < height * 0.34f; y += spacing) {
            for (float x = -spacing + offset; x < width + spacing; x += spacing) {
                float radius = dp(1.5f) + (y / height) * dp(2f);
                canvas.drawCircle(x, y, radius, paint);
            }
        }
    }

    private void drawLightSweep(Canvas canvas, float width, float height) {
        float center = -width * 0.35f + phase * width * 1.7f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(35, 255, 255, 255));
        path.reset();
        path.moveTo(center - width * 0.16f, 0f);
        path.lineTo(center + width * 0.04f, 0f);
        path.lineTo(center + width * 0.30f, height);
        path.lineTo(center + width * 0.10f, height);
        path.close();
        canvas.drawPath(path, paint);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
