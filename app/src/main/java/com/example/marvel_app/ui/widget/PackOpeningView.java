package com.example.marvel_app.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.marvel_app.R;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * User-controlled pack ritual: tap to arm the seal, drag to tear it, then swipe each card.
 * Game rules remain outside this view so it can be reused without coupling animation to domain data.
 */
public final class PackOpeningView extends FrameLayout {
    private static final int MAX_CARDS = 5;
    private static final float TEAR_THRESHOLD = 0.68f;

    public interface Listener {
        void onOpeningCompleted();
    }

    private enum State {
        IDLE,
        WAITING_TO_START,
        WAITING_TO_TEAR,
        ANIMATING,
        READY_TO_REVEAL,
        COMPLETED
    }

    private final FrameLayout cardStage;
    private final View cardBack;
    private final View aura;
    private final View envelope;
    private final View envelopeGlint;
    private final View tearStrip;
    private final TextView hint;
    private final TextView counter;
    private final int touchSlop;
    private final Map<View, Integer> hiddenAccessibilitySiblings = new IdentityHashMap<>();
    private AnimatorSet running;
    private Listener listener;
    private Runnable dismissListener;
    private State state = State.IDLE;
    private float downX;
    private float downY;
    private float tearProgress;
    private float revealProgress;
    private int tearDirection = 1;
    private int nextCardIndex;
    private boolean dragging;
    private boolean thresholdHapticSent;

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
        cardBack = findViewById(R.id.pack_card_back);
        aura = findViewById(R.id.pack_opening_aura);
        envelope = findViewById(R.id.pack_envelope);
        envelopeGlint = findViewById(R.id.pack_envelope_glint);
        tearStrip = findViewById(R.id.pack_tear_strip);
        hint = findViewById(R.id.pack_opening_hint);
        counter = findViewById(R.id.pack_opening_counter);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        setVisibility(GONE);
        setClickable(true);
        setFocusable(true);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    public void setCards(@NonNull List<? extends View> cards) {
        cancelAnimation();
        listener = null;
        state = State.IDLE;
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
            holder.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
            FrameLayout.LayoutParams holderParams = new FrameLayout.LayoutParams(
                    dp(172f),
                    dp(258f),
                    Gravity.CENTER
            );
            cardStage.addView(holder, holderParams);
        }
    }

    public void play(@Nullable Listener completionListener) {
        cancelAnimation();
        listener = completionListener;
        state = State.WAITING_TO_START;
        nextCardIndex = 0;
        setVisibility(VISIBLE);
        bringToFront();
        hideSiblingContentFromAccessibility();
        resetVisualState();
        updatePrompt(R.string.pack_opening_hint, true);
        requestFocus();
    }

    public void setOnDismissListener(@Nullable Runnable dismissListener) {
        this.dismissListener = dismissListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (state == State.IDLE) return false;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                dragging = false;
                thresholdHapticSent = false;
                if ((state == State.WAITING_TO_TEAR || state == State.READY_TO_REVEAL)
                        && getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (state == State.WAITING_TO_TEAR) {
                    float delta = event.getX() - downX;
                    if (Math.abs(delta) > touchSlop) dragging = true;
                    if (dragging) updateTearProgress(delta);
                } else if (state == State.READY_TO_REVEAL) {
                    float delta = event.getY() - downY;
                    if (-delta > touchSlop) dragging = true;
                    if (dragging) updateRevealProgress(delta);
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (state == State.WAITING_TO_TEAR && dragging) {
                    if (tearProgress >= TEAR_THRESHOLD) finishTear(tearDirection);
                    else resetIncompleteTear();
                } else if (state == State.READY_TO_REVEAL && dragging) {
                    if (revealProgress >= 0.46f) finishRevealGesture();
                    else resetIncompleteReveal();
                } else {
                    super.performClick();
                    handleActivation(false);
                }
                releaseParentIntercept();
                dragging = false;
                return true;
            case MotionEvent.ACTION_CANCEL:
                if (state == State.WAITING_TO_TEAR && dragging) resetIncompleteTear();
                else if (state == State.READY_TO_REVEAL && dragging) resetIncompleteReveal();
                releaseParentIntercept();
                dragging = false;
                return true;
            default:
                return true;
        }
    }

    @Override
    public boolean performClick() {
        super.performClick();
        handleActivation(true);
        return true;
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(Button.class.getName());
        info.removeAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK);
        boolean interactive = state == State.WAITING_TO_START
                || state == State.WAITING_TO_TEAR
                || state == State.READY_TO_REVEAL
                || state == State.COMPLETED;
        info.setClickable(interactive);
        if (interactive) {
            info.addAction(new AccessibilityNodeInfo.AccessibilityAction(
                    AccessibilityNodeInfo.ACTION_CLICK,
                    getResources().getString(actionLabelForState())
            ));
        }
    }

    @Override
    public boolean performAccessibilityAction(int action, Bundle arguments) {
        if (action == AccessibilityNodeInfo.ACTION_CLICK) return performClick();
        return super.performAccessibilityAction(action, arguments);
    }

    public void dismiss() {
        cancelAnimation();
        listener = null;
        state = State.IDLE;
        restoreSiblingAccessibility();
        setVisibility(GONE);
        if (dismissListener != null) dismissListener.run();
    }

    private void handleActivation(boolean fromAccessibility) {
        if (state == State.WAITING_TO_START) {
            haptic(HapticFeedbackConstants.VIRTUAL_KEY);
            armSeal();
        } else if (state == State.WAITING_TO_TEAR) {
            if (fromAccessibility) {
                haptic(HapticFeedbackConstants.CLOCK_TICK);
                finishTear(1);
            } else {
                updatePrompt(R.string.pack_opening_drag_hint, true);
            }
        } else if (state == State.READY_TO_REVEAL) {
            if (fromAccessibility) {
                haptic(HapticFeedbackConstants.VIRTUAL_KEY);
                revealNextCard();
            } else {
                updatePrompt(R.string.pack_opening_reveal_again, true);
            }
        } else if (state == State.COMPLETED) {
            haptic(HapticFeedbackConstants.VIRTUAL_KEY);
            dismiss();
        }
    }

    private void armSeal() {
        state = State.ANIMATING;
        updatePrompt(R.string.pack_opening_drag_hint, false);
        if (!ValueAnimator.areAnimatorsEnabled()) {
            state = State.WAITING_TO_TEAR;
            refreshAccessibilityAction();
            return;
        }

        running = new AnimatorSet();
        running.playTogether(
                ObjectAnimator.ofFloat(envelope, View.SCALE_X, 1f, 1.035f, 1f),
                ObjectAnimator.ofFloat(envelope, View.SCALE_Y, 1f, 1.035f, 1f),
                ObjectAnimator.ofFloat(envelope, View.ROTATION, 0f, -1.5f, 1.5f, 0f),
                ObjectAnimator.ofFloat(envelopeGlint, View.TRANSLATION_X, -dp(180f), dp(180f))
        );
        running.setDuration(420L);
        running.setInterpolator(new AccelerateDecelerateInterpolator());
        startAnimation(State.WAITING_TO_TEAR, null);
    }

    private void updateTearProgress(float delta) {
        float maxDistance = dp(220f);
        tearDirection = delta < 0f ? -1 : 1;
        tearProgress = Math.min(1f, Math.abs(delta) / maxDistance);
        tearStrip.setTranslationX(tearDirection * tearProgress * dp(270f));
        envelope.setRotation(tearDirection * tearProgress * 2.5f);
        aura.setAlpha(tearProgress * 0.78f);
        aura.setScaleX(0.45f + tearProgress * 0.62f);
        aura.setScaleY(0.45f + tearProgress * 0.62f);
        if (tearProgress >= TEAR_THRESHOLD && !thresholdHapticSent) {
            thresholdHapticSent = true;
            haptic(HapticFeedbackConstants.CLOCK_TICK);
        }
    }

    private void resetIncompleteTear() {
        state = State.ANIMATING;
        updatePrompt(R.string.pack_opening_drag_again, true);
        if (!ValueAnimator.areAnimatorsEnabled()) {
            resetTearVisuals();
            state = State.WAITING_TO_TEAR;
            refreshAccessibilityAction();
            return;
        }
        running = new AnimatorSet();
        running.playTogether(
                ObjectAnimator.ofFloat(tearStrip, View.TRANSLATION_X, tearStrip.getTranslationX(), 0f),
                ObjectAnimator.ofFloat(envelope, View.ROTATION, envelope.getRotation(), 0f),
                ObjectAnimator.ofFloat(aura, View.ALPHA, aura.getAlpha(), 0f),
                ObjectAnimator.ofFloat(aura, View.SCALE_X, aura.getScaleX(), 0.45f),
                ObjectAnimator.ofFloat(aura, View.SCALE_Y, aura.getScaleY(), 0.45f)
        );
        running.setDuration(180L);
        running.setInterpolator(new DecelerateInterpolator());
        startAnimation(State.WAITING_TO_TEAR, this::resetTearVisuals);
    }

    private void finishTear(int direction) {
        state = State.ANIMATING;
        tearDirection = direction == 0 ? 1 : direction;
        haptic(HapticFeedbackConstants.VIRTUAL_KEY);
        if (!ValueAnimator.areAnimatorsEnabled()) {
            applyTornState();
            becomeReadyToReveal();
            return;
        }

        running = new AnimatorSet();
        running.playTogether(
                ObjectAnimator.ofFloat(tearStrip, View.TRANSLATION_X,
                        tearStrip.getTranslationX(), tearDirection * dp(290f)),
                ObjectAnimator.ofFloat(aura, View.ALPHA, aura.getAlpha(), 0.74f),
                ObjectAnimator.ofFloat(aura, View.SCALE_X, aura.getScaleX(), 1.12f),
                ObjectAnimator.ofFloat(aura, View.SCALE_Y, aura.getScaleY(), 1.12f),
                ObjectAnimator.ofFloat(envelope, View.ALPHA, 1f, 0f),
                ObjectAnimator.ofFloat(envelope, View.TRANSLATION_Y, 0f, dp(72f)),
                ObjectAnimator.ofFloat(envelope, View.SCALE_X, 1f, 0.90f),
                ObjectAnimator.ofFloat(envelope, View.SCALE_Y, 1f, 0.90f)
        );
        running.setDuration(300L);
        running.setInterpolator(new DecelerateInterpolator(1.35f));
        startAnimation(State.READY_TO_REVEAL, () -> {
            applyTornState();
            becomeReadyToReveal();
        });
    }

    private void becomeReadyToReveal() {
        state = State.READY_TO_REVEAL;
        updatePrompt(R.string.pack_opening_reveal_first, true);
        if (cardStage.getChildCount() == 0) {
            completeOpening();
            return;
        }
        prepareCurrentCard();
    }

    private void revealNextCard() {
        if (nextCardIndex >= cardStage.getChildCount()) {
            completeOpening();
            return;
        }
        revealProgress = 1f;
        finishRevealGesture();
    }

    private void prepareCurrentCard() {
        if (nextCardIndex >= cardStage.getChildCount()) return;
        View current = cardStage.getChildAt(nextCardIndex);
        current.setAlpha(1f);
        current.setScaleX(0.94f);
        current.setScaleY(0.94f);
        current.setTranslationX(0f);
        current.setTranslationY(dp(10f));
        current.setRotation(0f);
        current.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        revealProgress = 0f;
        cardBack.setVisibility(VISIBLE);
        cardBack.bringToFront();
        cardBack.setAlpha(1f);
        cardBack.setTranslationX(0f);
        cardBack.setTranslationY(0f);
        cardBack.setRotation(0f);
        cardBack.setScaleX(1f);
        cardBack.setScaleY(1f);
        counter.setText(getResources().getString(
                R.string.pack_opening_counter,
                nextCardIndex + 1,
                cardStage.getChildCount()
        ));
    }

    private void updateRevealProgress(float deltaY) {
        float maxDistance = dp(300f);
        revealProgress = Math.min(1f, Math.max(0f, -deltaY / maxDistance));
        cardBack.setTranslationY(-revealProgress * dp(330f));
        cardBack.setRotation(revealProgress * -5f);
        cardBack.setAlpha(1f - revealProgress * 0.36f);
        cardBack.setScaleX(1f - revealProgress * 0.04f);
        cardBack.setScaleY(1f - revealProgress * 0.04f);
        View current = cardStage.getChildAt(nextCardIndex);
        current.setScaleX(0.94f + revealProgress * 0.06f);
        current.setScaleY(0.94f + revealProgress * 0.06f);
        current.setTranslationY(dp(10f) * (1f - revealProgress));
        aura.setAlpha(0.34f + revealProgress * 0.5f);
        if (revealProgress >= 0.46f && !thresholdHapticSent) {
            thresholdHapticSent = true;
            haptic(HapticFeedbackConstants.CLOCK_TICK);
        }
    }

    private void resetIncompleteReveal() {
        state = State.ANIMATING;
        updatePrompt(R.string.pack_opening_reveal_again, true);
        View current = cardStage.getChildAt(nextCardIndex);
        if (!ValueAnimator.areAnimatorsEnabled()) {
            prepareCurrentCard();
            state = State.READY_TO_REVEAL;
            refreshAccessibilityAction();
            return;
        }
        running = new AnimatorSet();
        running.playTogether(
                ObjectAnimator.ofFloat(cardBack, View.TRANSLATION_Y,
                        cardBack.getTranslationY(), 0f),
                ObjectAnimator.ofFloat(cardBack, View.ROTATION, cardBack.getRotation(), 0f),
                ObjectAnimator.ofFloat(cardBack, View.ALPHA, cardBack.getAlpha(), 1f),
                ObjectAnimator.ofFloat(cardBack, View.SCALE_X, cardBack.getScaleX(), 1f),
                ObjectAnimator.ofFloat(cardBack, View.SCALE_Y, cardBack.getScaleY(), 1f),
                ObjectAnimator.ofFloat(current, View.SCALE_X, current.getScaleX(), 0.94f),
                ObjectAnimator.ofFloat(current, View.SCALE_Y, current.getScaleY(), 0.94f),
                ObjectAnimator.ofFloat(current, View.TRANSLATION_Y,
                        current.getTranslationY(), dp(10f))
        );
        running.setDuration(190L);
        running.setInterpolator(new DecelerateInterpolator());
        startAnimation(State.READY_TO_REVEAL, () -> revealProgress = 0f);
    }

    private void finishRevealGesture() {
        if (nextCardIndex >= cardStage.getChildCount()) {
            completeOpening();
            return;
        }
        state = State.ANIMATING;
        View current = cardStage.getChildAt(nextCardIndex);
        current.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);

        AnimatorSet reveal = new AnimatorSet();
        reveal.playTogether(
                ObjectAnimator.ofFloat(cardBack, View.TRANSLATION_Y,
                        cardBack.getTranslationY(), -dp(430f)),
                ObjectAnimator.ofFloat(cardBack, View.ALPHA, cardBack.getAlpha(), 0f),
                ObjectAnimator.ofFloat(cardBack, View.ROTATION, cardBack.getRotation(), -9f),
                ObjectAnimator.ofFloat(current, View.SCALE_X, current.getScaleX(), 1.04f, 1f),
                ObjectAnimator.ofFloat(current, View.SCALE_Y, current.getScaleY(), 1.04f, 1f),
                ObjectAnimator.ofFloat(current, View.TRANSLATION_Y, current.getTranslationY(), 0f),
                ObjectAnimator.ofFloat(aura, View.SCALE_X, aura.getScaleX(), 1.2f),
                ObjectAnimator.ofFloat(aura, View.SCALE_Y, aura.getScaleY(), 1.2f)
        );
        reveal.setDuration(ValueAnimator.areAnimatorsEnabled() ? 320L : 0L);
        reveal.setInterpolator(new OvershootInterpolator(0.45f));
        running = reveal;
        int revealedIndex = nextCardIndex;
        startAnimation(State.READY_TO_REVEAL, () -> onCardRevealed(revealedIndex));
    }

    private void onCardRevealed(int revealedIndex) {
        nextCardIndex = revealedIndex + 1;
        View card = cardStage.getChildAt(revealedIndex);
        CharSequence description = card.getContentDescription();
        if (description == null && card instanceof ViewGroup
                && ((ViewGroup) card).getChildCount() > 0) {
            description = ((ViewGroup) card).getChildAt(0).getContentDescription();
        }
        String announcement = getResources().getString(
                R.string.pack_opening_card_revealed,
                nextCardIndex,
                cardStage.getChildCount(),
                description == null ? "" : description
        );
        announceForAccessibility(announcement);

        if (nextCardIndex >= cardStage.getChildCount()) {
            cardBack.setVisibility(INVISIBLE);
            arrangeFinalCards();
        } else {
            moveRevealedCardAside(card, revealedIndex);
            state = State.READY_TO_REVEAL;
            updatePrompt(R.string.pack_opening_reveal_next, false);
            prepareCurrentCard();
        }
    }

    private void moveRevealedCardAside(View card, int index) {
        float[] target = finalTransform(index, cardStage.getChildCount());
        card.animate()
                .translationX(target[0])
                .rotation(target[1])
                .scaleX(0.82f)
                .scaleY(0.82f)
                .setDuration(ValueAnimator.areAnimatorsEnabled() ? 180L : 0L)
                .start();
    }

    private void arrangeFinalCards() {
        state = State.ANIMATING;
        AnimatorSet finalFan = new AnimatorSet();
        Animator[] animators = new Animator[cardStage.getChildCount() * 4];
        int animatorIndex = 0;
        for (int index = 0; index < cardStage.getChildCount(); index++) {
            View card = cardStage.getChildAt(index);
            float[] target = finalTransform(index, cardStage.getChildCount());
            animators[animatorIndex++] = ObjectAnimator.ofFloat(card, View.TRANSLATION_X,
                    card.getTranslationX(), target[0]);
            animators[animatorIndex++] = ObjectAnimator.ofFloat(card, View.ROTATION,
                    card.getRotation(), target[1]);
            animators[animatorIndex++] = ObjectAnimator.ofFloat(card, View.SCALE_X,
                    card.getScaleX(), 0.82f);
            animators[animatorIndex++] = ObjectAnimator.ofFloat(card, View.SCALE_Y,
                    card.getScaleY(), 0.82f);
        }
        finalFan.playTogether(animators);
        finalFan.setDuration(ValueAnimator.areAnimatorsEnabled() ? 220L : 0L);
        finalFan.setInterpolator(new DecelerateInterpolator());
        running = finalFan;
        startAnimation(State.COMPLETED, this::completeOpening);
    }

    private void completeOpening() {
        state = State.COMPLETED;
        cardBack.setVisibility(INVISIBLE);
        counter.setText(R.string.pack_opening_counter_ready);
        updatePrompt(R.string.pack_opening_continue, false);
        setContentDescription(getResources().getString(R.string.pack_opening_revealed));
        announceForAccessibility(getResources().getString(R.string.pack_opening_revealed));
        Listener completion = listener;
        listener = null;
        if (completion != null) completion.onOpeningCompleted();
    }

    private void startAnimation(State endState, @Nullable Runnable endAction) {
        AnimatorSet animation = running;
        if (animation == null) return;
        animation.addListener(new AnimatorListenerAdapter() {
            private boolean cancelled;

            @Override
            public void onAnimationCancel(Animator animator) {
                cancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (running == animation) running = null;
                if (cancelled) return;
                state = endState;
                if (endAction != null) endAction.run();
                refreshAccessibilityAction();
            }
        });
        animation.start();
    }

    private void resetVisualState() {
        envelope.setVisibility(VISIBLE);
        envelope.setAlpha(1f);
        envelope.setScaleX(1f);
        envelope.setScaleY(1f);
        envelope.setRotation(0f);
        envelope.setTranslationY(0f);
        envelopeGlint.setTranslationX(-dp(180f));
        tearProgress = 0f;
        revealProgress = 0f;
        tearStrip.setTranslationX(0f);
        cardBack.setVisibility(INVISIBLE);
        cardBack.setAlpha(1f);
        cardBack.setTranslationX(0f);
        cardBack.setTranslationY(0f);
        cardBack.setRotation(0f);
        counter.setText(R.string.pack_opening_counter_ready);
        hint.setAlpha(1f);
        aura.setAlpha(0f);
        aura.setScaleX(0.45f);
        aura.setScaleY(0.45f);
        for (int index = 0; index < cardStage.getChildCount(); index++) {
            View card = cardStage.getChildAt(index);
            card.animate().cancel();
            card.setAlpha(0f);
            card.setScaleX(0.82f);
            card.setScaleY(0.82f);
            card.setTranslationX(0f);
            card.setTranslationY(dp(68f));
            card.setRotation(0f);
            card.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        }
    }

    private void resetTearVisuals() {
        tearProgress = 0f;
        tearStrip.setTranslationX(0f);
        envelope.setRotation(0f);
        aura.setAlpha(0f);
        aura.setScaleX(0.45f);
        aura.setScaleY(0.45f);
    }

    private void applyTornState() {
        envelope.setAlpha(0f);
        envelope.setVisibility(INVISIBLE);
        hint.setAlpha(1f);
        aura.setAlpha(0.34f);
        aura.setScaleX(1.18f);
        aura.setScaleY(1.18f);
    }

    private float[] finalTransform(int index, int count) {
        float center = (count - 1) / 2f;
        float step = count <= 3 ? dp(86f) : dp(58f);
        float distance = index - center;
        return new float[]{distance * step, distance * 4f};
    }

    private void updatePrompt(int textRes, boolean announce) {
        hint.animate().cancel();
        hint.setAlpha(1f);
        hint.setText(textRes);
        setContentDescription(getResources().getString(textRes));
        refreshAccessibilityAction();
        if (announce) announceForAccessibility(getResources().getString(textRes));
    }

    private int actionLabelForState() {
        if (state == State.WAITING_TO_TEAR) return R.string.pack_opening_tear_action;
        if (state == State.READY_TO_REVEAL) return R.string.pack_opening_reveal_action;
        if (state == State.COMPLETED) return R.string.pack_opening_continue;
        return R.string.pack_opening_hint;
    }

    private void refreshAccessibilityAction() {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
    }

    private void releaseParentIntercept() {
        if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(false);
    }

    private void haptic(int feedbackConstant) {
        if (isHapticFeedbackEnabled()) performHapticFeedback(feedbackConstant);
    }

    private void hideSiblingContentFromAccessibility() {
        restoreSiblingAccessibility();
        ViewParent parent = getParent();
        if (!(parent instanceof ViewGroup)) return;
        ViewGroup group = (ViewGroup) parent;
        for (int index = 0; index < group.getChildCount(); index++) {
            View sibling = group.getChildAt(index);
            if (sibling == this) continue;
            hiddenAccessibilitySiblings.put(sibling, sibling.getImportantForAccessibility());
            sibling.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        }
    }

    private void restoreSiblingAccessibility() {
        for (Map.Entry<View, Integer> entry : hiddenAccessibilitySiblings.entrySet()) {
            entry.getKey().setImportantForAccessibility(entry.getValue());
        }
        hiddenAccessibilitySiblings.clear();
    }

    private void cancelAnimation() {
        hint.animate().cancel();
        for (int index = 0; index < cardStage.getChildCount(); index++) {
            cardStage.getChildAt(index).animate().cancel();
        }
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
        state = State.IDLE;
        restoreSiblingAccessibility();
        if (dismissListener != null) dismissListener.run();
        super.onDetachedFromWindow();
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
