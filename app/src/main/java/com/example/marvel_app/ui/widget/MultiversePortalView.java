package com.example.marvel_app.ui.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.marvel_app.R;

public final class MultiversePortalView extends View {
    private final Paint cyan = strokePaint();
    private final Paint magenta = strokePaint();
    private final Paint violet = strokePaint();
    private final Paint core = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF ring = new RectF();
    private final RectF innerRing = new RectF();
    private ValueAnimator animator;
    private float rotation;

    public MultiversePortalView(Context context) {
        this(context, null);
    }

    public MultiversePortalView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        cyan.setColor(ContextCompat.getColor(context, R.color.spider_cyan));
        magenta.setColor(ContextCompat.getColor(context, R.color.spider_magenta));
        violet.setColor(ContextCompat.getColor(context, R.color.spider_portal_violet));
        cyan.setStrokeWidth(dp(4.5f));
        magenta.setStrokeWidth(dp(4.5f));
        violet.setStrokeWidth(dp(4.5f));
        core.setColor(0xD9000208);
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
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float radius = Math.min(cx, cy) - dp(7);
        ring.set(cx - radius, cy - radius, cx + radius, cy + radius);
        canvas.drawCircle(cx, cy, radius * 0.58f, core);
        canvas.save();
        canvas.rotate(rotation, cx, cy);
        canvas.drawArc(ring, 4f, 104f, false, cyan);
        canvas.drawArc(ring, 132f, 76f, false, magenta);
        canvas.drawArc(ring, 232f, 112f, false, violet);
        innerRing.set(ring);
        innerRing.inset(dp(10), dp(10));
        canvas.drawArc(innerRing, 24f, 145f, false, magenta);
        canvas.drawArc(innerRing, 204f, 130f, false, cyan);
        canvas.restore();

        for (int i = 0; i < 10; i++) {
            double angle = Math.toRadians(rotation * (i % 2 == 0 ? 1 : -1) + i * 36f);
            float sparkRadius = radius * (0.72f + (i % 3) * 0.08f);
            float x = cx + (float) Math.cos(angle) * sparkRadius;
            float y = cy + (float) Math.sin(angle) * sparkRadius;
            canvas.drawCircle(x, y, dp(i % 2 == 0 ? 2.2f : 1.4f), i % 2 == 0 ? cyan : magenta);
        }
    }

    private void start() {
        if (!ValueAnimator.areAnimatorsEnabled() || animator != null) return;
        animator = ValueAnimator.ofFloat(0f, 360f);
        animator.setDuration(4200L);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(value -> {
            rotation = (float) value.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    private void stop() {
        if (animator != null) animator.cancel();
        animator = null;
    }

    private static Paint strokePaint() {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        return paint;
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
