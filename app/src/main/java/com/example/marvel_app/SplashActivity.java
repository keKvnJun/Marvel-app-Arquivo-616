package com.example.marvel_app;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    private VideoView introVideo;
    private boolean navigating;
    private boolean foreground;
    private boolean prepared;
    private boolean completed;
    private final Runnable reducedMotionExit = this::openMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(getColor(R.color.marvel_red));
        getWindow().setNavigationBarColor(getColor(R.color.marvel_red));
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(R.layout.activity_splash);

        introVideo = findViewById(R.id.splash_video);
        findViewById(R.id.splash_skip).setOnClickListener(view -> openMain());
        introVideo.setOnPreparedListener(player -> {
            prepared = true;
            if (foreground) introVideo.start();
        });
        introVideo.setOnCompletionListener(player -> {
            completed = true;
            if (foreground) openMain();
        });
        introVideo.setOnErrorListener((player, what, extra) -> {
            completed = true;
            if (foreground) openMain();
            return true;
        });
        if (ValueAnimator.areAnimatorsEnabled()) {
            Uri source = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.marvel_comics_intro);
            introVideo.setVideoURI(source);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        foreground = true;
        if (completed) {
            openMain();
        } else if (!ValueAnimator.areAnimatorsEnabled()) {
            findViewById(R.id.splash_root).postDelayed(reducedMotionExit, 400L);
        } else if (prepared) {
            introVideo.start();
        }
    }

    @Override
    protected void onPause() {
        foreground = false;
        findViewById(R.id.splash_root).removeCallbacks(reducedMotionExit);
        if (introVideo != null && introVideo.isPlaying()) introVideo.pause();
        super.onPause();
    }

    private void openMain() {
        if (navigating || isFinishing() || isDestroyed()) return;
        navigating = true;
        if (introVideo != null) introVideo.stopPlayback();
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        findViewById(R.id.splash_root).removeCallbacks(reducedMotionExit);
        if (!navigating && introVideo != null) introVideo.stopPlayback();
        super.onDestroy();
    }
}
