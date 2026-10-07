package com.example.marvel_app;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.example.marvel_app.ui.widget.CinematicSplashView;
import com.example.marvel_app.ui.widget.ComicTransitionView;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {
    private static final long TRANSITION_DELAY_MS = 2680L;
    private static final long REDUCED_MOTION_DELAY_MS = 360L;

    private View splashRoot;
    private CinematicSplashView cinematicLayer;
    private ComicTransitionView transition;
    private AnimatorSet openingAnimation;
    private boolean navigating;

    private final Runnable openMain = () -> {
        if (navigating || isFinishing() || isDestroyed()) return;
        navigating = true;
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(0, 0);
        finish();
    };
    private final Runnable revealTransition = () -> transition.play(openMain);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        splashRoot = findViewById(R.id.splash_root);
        cinematicLayer = findViewById(R.id.splash_cinematic_layer);
        transition = findViewById(R.id.splash_transition);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!navigating) startOpening();
    }

    @Override
    protected void onPause() {
        stopOpening();
        super.onPause();
    }

    private void startOpening() {
        stopOpening();
        cinematicLayer.start();

        if (!ValueAnimator.areAnimatorsEnabled()) {
            showFinalState();
            splashRoot.postDelayed(revealTransition, REDUCED_MOTION_DELAY_MS);
            return;
        }

        View background = findViewById(R.id.splash_comic_background);
        View issue = findViewById(R.id.splash_issue_label);
        View orbit = findViewById(R.id.splash_shield_orbit);
        View title = findViewById(R.id.splash_title);
        View subtitle = findViewById(R.id.splash_subtitle);
        View progress = findViewById(R.id.splash_progress);
        View pow = findViewById(R.id.splash_fx_pow);

        background.setScaleX(1.13f);
        background.setScaleY(1.13f);
        issue.setAlpha(0f);
        issue.setTranslationY(-dp(20f));
        orbit.setAlpha(0f);
        orbit.setScaleX(0.42f);
        orbit.setScaleY(0.42f);
        orbit.setRotation(-24f);
        title.setAlpha(0f);
        title.setScaleX(0.76f);
        title.setScaleY(0.76f);
        subtitle.setAlpha(0f);
        subtitle.setTranslationY(dp(14f));
        progress.setAlpha(0f);
        pow.setAlpha(0f);
        pow.setScaleX(0.18f);
        pow.setScaleY(0.18f);
        pow.setRotation(18f);

        List<Animator> animators = new ArrayList<>();
        animators.add(timed(ObjectAnimator.ofFloat(background, View.SCALE_X, 1.13f, 1.02f), 0L, 2550L));
        animators.add(timed(ObjectAnimator.ofFloat(background, View.SCALE_Y, 1.13f, 1.02f), 0L, 2550L));
        animators.add(timed(ObjectAnimator.ofFloat(issue, View.ALPHA, 0f, 1f), 140L, 460L));
        animators.add(timed(ObjectAnimator.ofFloat(issue, View.TRANSLATION_Y, -dp(20f), 0f), 140L, 620L));
        animators.add(timed(ObjectAnimator.ofFloat(orbit, View.ALPHA, 0f, 1f), 460L, 420L));
        animators.add(timed(ObjectAnimator.ofFloat(orbit, View.SCALE_X, 0.42f, 1f), 460L, 760L));
        animators.add(timed(ObjectAnimator.ofFloat(orbit, View.SCALE_Y, 0.42f, 1f), 460L, 760L));
        animators.add(timed(ObjectAnimator.ofFloat(orbit, View.ROTATION, -24f, 0f), 460L, 900L));
        animators.add(timed(ObjectAnimator.ofFloat(title, View.ALPHA, 0f, 1f), 900L, 420L));
        animators.add(timed(ObjectAnimator.ofFloat(title, View.SCALE_X, 0.76f, 1f), 900L, 650L));
        animators.add(timed(ObjectAnimator.ofFloat(title, View.SCALE_Y, 0.76f, 1f), 900L, 650L));
        animators.add(timed(ObjectAnimator.ofFloat(subtitle, View.ALPHA, 0f, 1f), 1320L, 450L));
        animators.add(timed(ObjectAnimator.ofFloat(subtitle, View.TRANSLATION_Y, dp(14f), 0f), 1320L, 520L));
        animators.add(timed(ObjectAnimator.ofFloat(progress, View.ALPHA, 0f, 1f), 1660L, 420L));
        animators.add(timed(ObjectAnimator.ofFloat(pow, View.ALPHA, 0f, 1f), 1840L, 280L));

        ObjectAnimator powScaleX = timed(ObjectAnimator.ofFloat(pow, View.SCALE_X, 0.18f, 1.08f, 1f), 1840L, 600L);
        ObjectAnimator powScaleY = timed(ObjectAnimator.ofFloat(pow, View.SCALE_Y, 0.18f, 1.08f, 1f), 1840L, 600L);
        powScaleX.setInterpolator(new OvershootInterpolator(0.7f));
        powScaleY.setInterpolator(new OvershootInterpolator(0.7f));
        animators.add(powScaleX);
        animators.add(powScaleY);
        animators.add(timed(ObjectAnimator.ofFloat(pow, View.ROTATION, 18f, 8f), 1840L, 600L));

        openingAnimation = new AnimatorSet();
        openingAnimation.playTogether(animators);
        openingAnimation.setInterpolator(new DecelerateInterpolator(1.35f));
        openingAnimation.start();
        splashRoot.postDelayed(revealTransition, TRANSITION_DELAY_MS);
    }

    private ObjectAnimator timed(ObjectAnimator animator, long delay, long duration) {
        animator.setStartDelay(delay);
        animator.setDuration(duration);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        return animator;
    }

    private void showFinalState() {
        int[] ids = {
                R.id.splash_issue_label,
                R.id.splash_shield_orbit,
                R.id.splash_title,
                R.id.splash_subtitle,
                R.id.splash_progress,
                R.id.splash_fx_pow
        };
        for (int id : ids) {
            View view = findViewById(id);
            view.setAlpha(1f);
            view.setScaleX(1f);
            view.setScaleY(1f);
            view.setTranslationY(0f);
        }
    }

    private void stopOpening() {
        if (splashRoot != null) splashRoot.removeCallbacks(revealTransition);
        if (openingAnimation != null) {
            openingAnimation.cancel();
            openingAnimation = null;
        }
        if (cinematicLayer != null) cinematicLayer.stop();
        if (transition != null) transition.cancel();
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onDestroy() {
        stopOpening();
        super.onDestroy();
    }
}
