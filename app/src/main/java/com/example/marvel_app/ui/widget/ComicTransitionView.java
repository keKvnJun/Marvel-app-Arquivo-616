package com.example.marvel_app.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.marvel_app.R;

public final class ComicTransitionView extends FrameLayout {
    private static final int[] BURSTS = {
            R.drawable.comic_burst_yellow,
            R.drawable.comic_burst_red,
            R.drawable.comic_burst_blue,
            R.drawable.comic_burst_cream
    };
    private static final int[] WORDS = {
            R.string.splash_pow,
            R.string.splash_boom,
            R.string.splash_zap,
            R.string.splash_kapow
    };

    private final ImageView burst;
    private final TextView word;
    private AnimatorSet running;
    private Runnable pendingAnimation;
    private int nextEffect;

    public ComicTransitionView(Context context) {
        this(context, null);
    }

    public ComicTransitionView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ComicTransitionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundResource(R.drawable.bg_comic_transition);
        setClickable(true);
        setFocusable(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        setVisibility(GONE);

        burst = new ImageView(context);
        burst.setContentDescription(null);
        LayoutParams burstParams = new LayoutParams(dp(250), dp(175), Gravity.CENTER);
        addView(burst, burstParams);

        word = new TextView(context);
        word.setGravity(Gravity.CENTER);
        word.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD_ITALIC));
        word.setTextSize(TypedValue.COMPLEX_UNIT_SP, 46);
        word.setLetterSpacing(0.02f);
        word.setShadowLayer(dp(2), dp(3), dp(3), Color.WHITE);
        LayoutParams wordParams = new LayoutParams(dp(220), dp(120), Gravity.CENTER);
        addView(word, wordParams);
    }

    public void play(@Nullable Runnable endAction) {
        cancel();
        int index = nextEffect++ % BURSTS.length;
        burst.setImageResource(BURSTS[index]);
        word.setText(WORDS[index]);
        word.setTextColor(index == 1 ? Color.WHITE : Color.BLACK);
        setVisibility(VISIBLE);
        bringToFront();

        if (!android.animation.ValueAnimator.areAnimatorsEnabled()) {
            setVisibility(GONE);
            if (endAction != null) endAction.run();
            return;
        }

        pendingAnimation = () -> {
            pendingAnimation = null;
            animateEffect(index, endAction);
        };
        post(pendingAnimation);
    }

    public static ComicTransitionView attachTo(Activity activity) {
        ViewGroup content = activity.findViewById(android.R.id.content);
        ComicTransitionView transition = new ComicTransitionView(activity);
        content.addView(transition, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        return transition;
    }

    public void cancel() {
        if (pendingAnimation != null) {
            removeCallbacks(pendingAnimation);
            pendingAnimation = null;
        }
        if (running != null) running.cancel();
        setVisibility(GONE);
    }

    private void animateEffect(int index, @Nullable Runnable endAction) {
        float width = Math.max(getWidth(), getResources().getDisplayMetrics().widthPixels);
        setTranslationX(width);
        burst.setScaleX(0.25f);
        burst.setScaleY(0.25f);
        burst.setRotation(index % 2 == 0 ? -14f : 14f);
        word.setScaleX(0.55f);
        word.setScaleY(0.55f);

        AnimatorSet enter = new AnimatorSet();
        enter.playTogether(
                ObjectAnimator.ofFloat(this, View.TRANSLATION_X, width, 0f),
                ObjectAnimator.ofFloat(burst, View.SCALE_X, 0.25f, 1.16f),
                ObjectAnimator.ofFloat(burst, View.SCALE_Y, 0.25f, 1.16f),
                ObjectAnimator.ofFloat(burst, View.ROTATION, burst.getRotation(), 2f),
                ObjectAnimator.ofFloat(word, View.SCALE_X, 0.55f, 1.08f),
                ObjectAnimator.ofFloat(word, View.SCALE_Y, 0.55f, 1.08f)
        );
        enter.setDuration(120L);

        AnimatorSet pulse = new AnimatorSet();
        pulse.playTogether(
                ObjectAnimator.ofFloat(burst, View.SCALE_X, 1.16f, 0.98f, 1.05f),
                ObjectAnimator.ofFloat(burst, View.SCALE_Y, 1.16f, 0.98f, 1.05f),
                ObjectAnimator.ofFloat(word, View.ROTATION, 0f, index % 2 == 0 ? 3f : -3f, 0f)
        );
        pulse.setDuration(100L);

        AnimatorSet exit = new AnimatorSet();
        exit.playTogether(
                ObjectAnimator.ofFloat(this, View.TRANSLATION_X, 0f, -width),
                ObjectAnimator.ofFloat(burst, View.SCALE_X, 1.05f, 1.35f),
                ObjectAnimator.ofFloat(burst, View.SCALE_Y, 1.05f, 1.35f)
        );
        exit.setDuration(100L);

        running = new AnimatorSet();
        running.playSequentially(enter, pulse, exit);
        running.addListener(new AnimatorListenerAdapter() {
            private boolean cancelled;

            @Override
            public void onAnimationCancel(Animator animation) {
                cancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(GONE);
                setTranslationX(0f);
                if (!cancelled && endAction != null) endAction.run();
            }
        });
        running.start();
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
