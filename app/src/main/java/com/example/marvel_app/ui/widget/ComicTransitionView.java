package com.example.marvel_app.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.marvel_app.R;

public final class ComicTransitionView extends FrameLayout {
    private static final int INK = Color.rgb(23, 23, 23);
    private final FrameLayout[] panels = new FrameLayout[3];
    private final FrameLayout caption;
    private final TextView bubble;
    private final TextView impact;
    private AnimatorSet running;
    private Runnable pendingAnimation;

    public ComicTransitionView(Context context) {
        this(context, null);
    }

    public ComicTransitionView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ComicTransitionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundColor(Color.rgb(244, 235, 221));
        setClickable(true);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        setVisibility(GONE);

        LinearLayout page = new LinearLayout(context);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(8), dp(8), dp(8), dp(8));
        addView(page, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        int[] artwork = {
                R.drawable.comic_collage_splash,
                R.drawable.comic_iron_man,
                R.drawable.comic_collage_halftone
        };
        for (int index = 0; index < panels.length; index++) {
            FrameLayout panel = new FrameLayout(context);
            panel.setBackgroundColor(INK);
            panel.setPadding(dp(4), dp(4), dp(4), dp(4));
            panel.setClipChildren(true);
            LinearLayout.LayoutParams panelParams = new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, 0, index == 1 ? 1.3f : 1f);
            panelParams.setMargins(0, dp(3), 0, dp(3));
            page.addView(panel, panelParams);

            ImageView image = new ImageView(context);
            image.setImageResource(artwork[index]);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            image.setContentDescription(null);
            panel.addView(image, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            panels[index] = panel;
        }

        caption = new FrameLayout(context);
        LayoutParams captionParams = new LayoutParams(dp(230), dp(112), Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        captionParams.topMargin = dp(82);
        addView(caption, captionParams);

        View tail = new View(context);
        tail.setBackground(makeBubbleBackground());
        tail.setRotation(45f);
        FrameLayout.LayoutParams tailParams = new FrameLayout.LayoutParams(dp(24), dp(24), Gravity.BOTTOM | Gravity.LEFT);
        tailParams.setMargins(dp(34), 0, 0, dp(8));
        caption.addView(tail, tailParams);

        bubble = new TextView(context);
        bubble.setGravity(Gravity.CENTER);
        bubble.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        bubble.setTextColor(INK);
        bubble.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        bubble.setBackground(makeBubbleBackground());
        bubble.setPadding(dp(12), dp(8), dp(12), dp(8));
        FrameLayout.LayoutParams bubbleParams = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, dp(78), Gravity.TOP);
        caption.addView(bubble, bubbleParams);

        impact = new TextView(context);
        impact.setBackgroundResource(R.drawable.comic_burst_yellow);
        impact.setGravity(Gravity.CENTER);
        impact.setText(R.string.splash_zap);
        impact.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD_ITALIC));
        impact.setTextColor(INK);
        impact.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        LayoutParams impactParams = new LayoutParams(dp(120), dp(88), Gravity.END | Gravity.CENTER_VERTICAL);
        impactParams.rightMargin = dp(7);
        impactParams.topMargin = dp(180);
        addView(impact, impactParams);
    }

    private GradientDrawable makeBubbleBackground() {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(Color.WHITE);
        shape.setCornerRadius(dp(28));
        shape.setStroke(dp(3), INK);
        return shape;
    }

    public void play(int destinationId, @Nullable Runnable coveredAction) {
        cancel();
        bubble.setText(destinationId == R.id.nav_home ? R.string.transition_home
                : destinationId == R.id.nav_explore ? R.string.transition_explore
                : destinationId == R.id.nav_duel ? R.string.transition_duel
                : destinationId == R.id.nav_jarvis ? R.string.transition_jarvis
                : R.string.transition_archive);
        impact.setVisibility(destinationId == R.id.nav_duel ? VISIBLE : GONE);
        setBackgroundColor(Color.rgb(244, 235, 221));
        setVisibility(VISIBLE);
        bringToFront();
        if (!ValueAnimator.areAnimatorsEnabled()) {
            if (coveredAction != null) coveredAction.run();
            setVisibility(GONE);
            return;
        }
        pendingAnimation = () -> {
            pendingAnimation = null;
            animatePage(coveredAction);
        };
        post(pendingAnimation);
    }

    public static ComicTransitionView attachTo(Activity activity) {
        ViewGroup content = activity.findViewById(android.R.id.content);
        ComicTransitionView transition = new ComicTransitionView(activity);
        content.addView(transition, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return transition;
    }

    public void cancel() {
        if (pendingAnimation != null) {
            removeCallbacks(pendingAnimation);
            pendingAnimation = null;
        }
        if (running != null) {
            running.cancel();
            running = null;
        }
        setVisibility(GONE);
        setTranslationX(0f);
        setAlpha(1f);
        setBackgroundColor(Color.rgb(244, 235, 221));
    }

    private void animatePage(@Nullable Runnable coveredAction) {
        float width = Math.max(getWidth(), getResources().getDisplayMetrics().widthPixels);
        setTranslationX(0f);
        for (int index = 0; index < panels.length; index++) {
            panels[index].setTranslationX(index % 2 == 0 ? -width : width);
        }
        caption.setScaleX(0.72f);
        caption.setScaleY(0.72f);
        caption.setAlpha(0f);
        impact.setScaleX(0.45f);
        impact.setScaleY(0.45f);
        impact.setAlpha(0f);

        AnimatorSet enter = new AnimatorSet();
        enter.playTogether(
                timed(panels[0], View.TRANSLATION_X, 0L, 240L, -width, 0f),
                timed(panels[1], View.TRANSLATION_X, 60L, 240L, width, 0f),
                timed(panels[2], View.TRANSLATION_X, 120L, 240L, -width, 0f),
                timed(caption, View.ALPHA, 170L, 160L, 0f, 1f),
                timed(caption, View.SCALE_X, 170L, 160L, 0.72f, 1f),
                timed(caption, View.SCALE_Y, 170L, 160L, 0.72f, 1f),
                timed(impact, View.ALPHA, 210L, 150L, 0f, 1f),
                timed(impact, View.SCALE_X, 210L, 150L, 0.45f, 1f),
                timed(impact, View.SCALE_Y, 210L, 150L, 0.45f, 1f)
        );
        enter.setInterpolator(new DecelerateInterpolator(1.5f));
        enter.addListener(new AnimatorListenerAdapter() {
            private boolean cancelled;

            @Override public void onAnimationCancel(Animator animation) { cancelled = true; }
            @Override public void onAnimationEnd(Animator animation) {
                if (!cancelled) {
                    if (coveredAction != null) coveredAction.run();
                    setBackgroundColor(Color.TRANSPARENT);
                }
            }
        });

        AnimatorSet leave = new AnimatorSet();
        leave.playTogether(
                timed(panels[0], View.TRANSLATION_X, 80L, 220L, 0f, -width),
                timed(panels[1], View.TRANSLATION_X, 125L, 220L, 0f, width),
                timed(panels[2], View.TRANSLATION_X, 170L, 220L, 0f, -width),
                timed(caption, View.ALPHA, 80L, 140L, 1f, 0f),
                timed(impact, View.ALPHA, 80L, 140L, 1f, 0f)
        );
        running = new AnimatorSet();
        running.playSequentially(enter, leave);
        running.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                setVisibility(GONE);
                setAlpha(1f);
                setBackgroundColor(Color.rgb(244, 235, 221));
                running = null;
            }
        });
        running.start();
    }

    private ObjectAnimator timed(View target, String property, long delay,
                                 long duration, float from, float to) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(target, property, from, to);
        animator.setStartDelay(delay);
        animator.setDuration(duration);
        return animator;
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
