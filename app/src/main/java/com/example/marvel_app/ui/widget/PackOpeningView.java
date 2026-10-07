package com.example.marvel_app.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.marvel_app.R;

import java.util.List;

/**
 * Reusable full-screen pack opening overlay. Call setCards with up to five ready-made card views,
 * then call play. The component owns presentation only and stays independent from game rules.
 */
public final class PackOpeningView extends FrameLayout {
    private static final int MAX_CARDS = 5;

    public interface Listener {
        void onOpeningCompleted();
    }

    private final FrameLayout cardStage;
    private final View aura;
    private final View envelope;
    private final View tearStrip;
    private final TextView hint;
    private AnimatorSet running;
    private Listener listener;
    private Runnable dismissListener;
    private boolean playing;

    public PackOpeningView(Context context) {
        this(context, null);
    }

    public PackOpeningView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PackOpeningView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.view_pack_opening, this, true);
        cardStage = findViewById(R.id.pack_card_stage);
        aura = findViewById(R.id.pack_opening_aura);
        envelope = findViewById(R.id.pack_envelope);
        tearStrip = findViewById(R.id.pack_tear_strip);
        hint = findViewById(R.id.pack_opening_hint);

        setVisibility(GONE);
        setClickable(true);
        setFocusable(true);
        setContentDescription(getResources().getString(R.string.pack_opening_description));
        setOnClickListener(view -> {
            if (playing) finishImmediately();
            else dismiss();
        });
    }

    public void setCards(@NonNull List<? extends View> cards) {
        cancelAnimation();
        listener = null;
        playing = false;
        cardStage.removeAllViews();
        int count = Math.min(cards.size(), MAX_CARDS);
        for (int index = 0; index < count; index++) {
            View card = cards.get(index);
            ViewParent parent = card.getParent();
            if (parent instanceof ViewGroup) ((ViewGroup) parent).removeView(card);

            FrameLayout holder = new FrameLayout(getContext());
            holder.setClipChildren(false);
            holder.setClipToPadding(false);
            holder.setElevation(dp(4f + index));
            holder.addView(card, new FrameLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
            ));
            FrameLayout.LayoutParams holderParams = new FrameLayout.LayoutParams(
                    dp(136f),
                    dp(230f),
                    Gravity.CENTER
            );
            cardStage.addView(holder, holderParams);
        }
    }

    public void play(@Nullable Listener completionListener) {
        cancelAnimation();
        listener = completionListener;
        playing = true;
        setVisibility(VISIBLE);
        bringToFront();
        setContentDescription(getResources().getString(R.string.pack_opening_description));
        resetVisualState();
        announceForAccessibility(getResources().getString(R.string.pack_opening_description));

        if (!ValueAnimator.areAnimatorsEnabled()) {
            showFinalState();
            complete();
            return;
        }

        AnimatorSet anticipation = new AnimatorSet();
        anticipation.playTogether(
                ObjectAnimator.ofFloat(envelope, View.SCALE_X, 1f, 1.04f),
                ObjectAnimator.ofFloat(envelope, View.SCALE_Y, 1f, 1.04f),
                ObjectAnimator.ofFloat(envelope, View.ROTATION, 0f, -2f, 2f, 0f)
        );
        anticipation.setDuration(280L);
        anticipation.setInterpolator(new AccelerateDecelerateInterpolator());

        AnimatorSet tear = new AnimatorSet();
        tear.playTogether(
                ObjectAnimator.ofFloat(tearStrip, View.TRANSLATION_X, 0f, dp(270f)),
                ObjectAnimator.ofFloat(aura, View.ALPHA, 0f, 0.92f),
                ObjectAnimator.ofFloat(aura, View.SCALE_X, 0.45f, 1.15f),
                ObjectAnimator.ofFloat(aura, View.SCALE_Y, 0.45f, 1.15f)
        );
        tear.setDuration(190L);
        tear.setInterpolator(new DecelerateInterpolator());

        AnimatorSet envelopeExit = new AnimatorSet();
        envelopeExit.playTogether(
                ObjectAnimator.ofFloat(envelope, View.ALPHA, 1f, 0f),
                ObjectAnimator.ofFloat(envelope, View.TRANSLATION_Y, 0f, dp(90f)),
                ObjectAnimator.ofFloat(envelope, View.SCALE_X, 1.04f, 0.88f),
                ObjectAnimator.ofFloat(envelope, View.SCALE_Y, 1.04f, 0.88f),
                ObjectAnimator.ofFloat(hint, View.ALPHA, 1f, 0f)
        );
        envelopeExit.setDuration(210L);
        envelopeExit.setInterpolator(new DecelerateInterpolator(1.4f));

        AnimatorSet reveals = buildCardReveals();
        running = new AnimatorSet();
        running.playSequentially(anticipation, tear, envelopeExit, reveals);
        running.addListener(new AnimatorListenerAdapter() {
            private boolean cancelled;

            @Override
            public void onAnimationCancel(Animator animation) {
                cancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                running = null;
                if (!cancelled) complete();
            }
        });
        running.start();
    }

    public void setOnDismissListener(@Nullable Runnable dismissListener) {
        this.dismissListener = dismissListener;
    }

    public void finishImmediately() {
        if (!playing) return;
        if (running != null) {
            running.removeAllListeners();
            running.cancel();
            running = null;
        }
        showFinalState();
        complete();
    }

    public void dismiss() {
        cancelAnimation();
        listener = null;
        playing = false;
        setVisibility(GONE);
        if (dismissListener != null) dismissListener.run();
    }

    private AnimatorSet buildCardReveals() {
        AnimatorSet reveals = new AnimatorSet();
        int count = cardStage.getChildCount();
        if (count == 0) return reveals;

        Animator[] cardAnimations = new Animator[count];
        for (int index = 0; index < count; index++) {
            View card = cardStage.getChildAt(index);
            float[] target = finalTransform(index, count);
            AnimatorSet reveal = new AnimatorSet();
            reveal.playTogether(
                    ObjectAnimator.ofFloat(card, View.ALPHA, 0f, 1f),
                    ObjectAnimator.ofFloat(card, View.TRANSLATION_Y, dp(94f), 0f),
                    ObjectAnimator.ofFloat(card, View.TRANSLATION_X, 0f, target[0]),
                    ObjectAnimator.ofFloat(card, View.ROTATION, 0f, target[1]),
                    ObjectAnimator.ofFloat(card, View.SCALE_X, 0.80f, 1f),
                    ObjectAnimator.ofFloat(card, View.SCALE_Y, 0.80f, 1f)
            );
            reveal.setDuration(220L);
            reveal.setInterpolator(new OvershootInterpolator(0.55f));
            cardAnimations[index] = reveal;
        }
        reveals.playSequentially(cardAnimations);
        return reveals;
    }

    private void resetVisualState() {
        envelope.setAlpha(1f);
        envelope.setScaleX(1f);
        envelope.setScaleY(1f);
        envelope.setRotation(0f);
        envelope.setTranslationY(0f);
        tearStrip.setTranslationX(0f);
        hint.setAlpha(1f);
        hint.setText(R.string.pack_opening_hint);
        aura.setAlpha(0f);
        aura.setScaleX(0.45f);
        aura.setScaleY(0.45f);
        for (int index = 0; index < cardStage.getChildCount(); index++) {
            View card = cardStage.getChildAt(index);
            card.setAlpha(0f);
            card.setScaleX(0.80f);
            card.setScaleY(0.80f);
            card.setTranslationX(0f);
            card.setTranslationY(dp(94f));
            card.setRotation(0f);
        }
    }

    private void showFinalState() {
        envelope.setAlpha(0f);
        hint.setAlpha(0f);
        aura.setAlpha(0.34f);
        aura.setScaleX(1.2f);
        aura.setScaleY(1.2f);
        int count = cardStage.getChildCount();
        for (int index = 0; index < count; index++) {
            View card = cardStage.getChildAt(index);
            float[] target = finalTransform(index, count);
            card.setAlpha(1f);
            card.setScaleX(1f);
            card.setScaleY(1f);
            card.setTranslationX(target[0]);
            card.setTranslationY(0f);
            card.setRotation(target[1]);
        }
    }

    private float[] finalTransform(int index, int count) {
        float center = (count - 1) / 2f;
        float step = count <= 3 ? dp(104f) : dp(66f);
        float distance = index - center;
        return new float[]{distance * step, distance * 4f};
    }

    private void complete() {
        playing = false;
        hint.setText(R.string.pack_opening_continue);
        hint.animate().alpha(1f).setDuration(180L).start();
        setContentDescription(getResources().getString(R.string.pack_opening_revealed));
        announceForAccessibility(getResources().getString(R.string.pack_opening_revealed));
        Listener completion = listener;
        listener = null;
        if (completion != null) completion.onOpeningCompleted();
    }

    private void cancelAnimation() {
        hint.animate().cancel();
        if (running != null) {
            running.removeAllListeners();
            running.cancel();
            running = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        cancelAnimation();
        listener = null;
        playing = false;
        super.onDetachedFromWindow();
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
