package com.example.marvel_app;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_DURATION_MS = 1400L;
    private final Runnable openMain = () -> {
        if (isFinishing() || isDestroyed()) return;
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(R.anim.screen_enter, R.anim.screen_exit);
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        findViewById(R.id.splash_shield_icon).animate()
                .rotationBy(360f).scaleX(1.08f).scaleY(1.08f).setDuration(900L).start();
        findViewById(R.id.splash_title).setAlpha(0f);
        findViewById(R.id.splash_title).animate()
                .alpha(1f).setStartDelay(180L).setDuration(500L).start();
        findViewById(R.id.splash_progress).postDelayed(openMain, SPLASH_DURATION_MS);
    }

    @Override
    protected void onDestroy() {
        findViewById(R.id.splash_progress).removeCallbacks(openMain);
        super.onDestroy();
    }
}
