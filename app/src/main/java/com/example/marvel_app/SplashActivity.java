package com.example.marvel_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.marvel_app.ui.widget.ComicTransitionView;

public class SplashActivity extends AppCompatActivity {
    private static final long TRANSITION_DELAY_MS = 1280L;
    private View splashRoot;
    private ComicTransitionView transition;
    private final Runnable openMain = () -> {
        if (isFinishing() || isDestroyed()) return;
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
        transition = findViewById(R.id.splash_transition);
        animatePanel(R.id.splash_panel_left, -180f, 0L, 1100L);
        animatePanel(R.id.splash_panel_right, 190f, 80L, 1050L);
        animatePanel(R.id.splash_panel_center, 0f, 170L, 900L);
        animatePop(R.id.splash_fx_pow, 120L, -9f, -2f);
        animatePop(R.id.splash_fx_zap, 430L, 8f, 1f);

        View shield = findViewById(R.id.splash_shield_icon);
        shield.setScaleX(0.35f);
        shield.setScaleY(0.35f);
        shield.setAlpha(0f);
        shield.animate().alpha(1f).rotationBy(360f).scaleX(1f).scaleY(1f)
                .setStartDelay(260L).setDuration(720L).start();

        View title = findViewById(R.id.splash_title);
        title.setAlpha(0f);
        title.setScaleX(0.75f);
        title.setScaleY(0.75f);
        title.animate().alpha(1f).scaleX(1f).scaleY(1f)
                .setStartDelay(390L).setDuration(420L).start();
        splashRoot.postDelayed(revealTransition, TRANSITION_DELAY_MS);
    }

    private void animatePanel(int id, float startTranslation, long delay, long duration) {
        View panel = findViewById(id);
        panel.setAlpha(0f);
        panel.setTranslationX(startTranslation);
        panel.setScaleX(1.10f);
        panel.setScaleY(1.10f);
        panel.animate().alpha(1f).translationX(0f).scaleX(1.02f).scaleY(1.02f)
                .setStartDelay(delay).setDuration(duration).start();
    }

    private void animatePop(int id, long delay, float startRotation, float endRotation) {
        View effect = findViewById(id);
        effect.setAlpha(0f);
        effect.setScaleX(0.15f);
        effect.setScaleY(0.15f);
        effect.setRotation(startRotation);
        effect.animate().alpha(1f).scaleX(1f).scaleY(1f).rotation(endRotation)
                .setStartDelay(delay).setDuration(520L).start();
    }

    @Override
    protected void onDestroy() {
        if (splashRoot != null) splashRoot.removeCallbacks(revealTransition);
        if (transition != null) transition.cancel();
        super.onDestroy();
    }
}
