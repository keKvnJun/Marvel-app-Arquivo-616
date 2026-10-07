package com.example.marvel_app.ui.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.marvel_app.R;

public final class GlitchOverlayView extends View {
    private final Paint cyan = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint magenta = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint scanline = new Paint(Paint.ANTI_ALIAS_FLAG);
    private ValueAnimator animator;
    private float phase;

    public GlitchOverlayView(Context context) {
        this(context, null);
    }

    public GlitchOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        cyan.setColor(ContextCompat.getColor(context, R.color.spider_cyan));
        cyan.setAlpha(88);
        magenta.setColor(ContextCompat.getColor(context, R.color.spider_magenta));
        magenta.setAlpha(76);
        scanline.setColor(0x28FFFFFF);
        scanline.setStrokeWidth(dp(1));
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        start();
    }

    @Override
    protected void onDetachedFromWindow() {
        stop();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE) {
            start();
        } else {
            stop();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        for (int y = 0; y < height; y += dp(7)) {
            canvas.drawLine(0, y, width, y, scanline);
        }

        float wave = (float) Math.sin(phase * Math.PI * 2f);
        float firstY = height * (0.24f + 0.08f * wave);
        float secondY = height * (0.67f - 0.06f * wave);
        canvas.drawRect(dp(12) + wave * dp(10), firstY, width * 0.66f, firstY + dp(5), cyan);
        canvas.drawRect(width * 0.30f - wave * dp(12), secondY, width - dp(10), secondY + dp(4), magenta);
        canvas.drawRect(width * 0.72f, height * 0.43f, width, height * 0.43f + dp(2), cyan);
    }

    private void start() {
        if (!ValueAnimator.areAnimatorsEnabled() || animator != null) return;
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(820L);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(value -> {
            phase = (float) value.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    private void stop() {
        if (animator != null) animator.cancel();
        animator = null;
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
