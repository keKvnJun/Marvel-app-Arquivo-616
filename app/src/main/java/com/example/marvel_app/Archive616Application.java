package com.example.marvel_app;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.marvel_app.data.local.ThemeStore;

public final class Archive616Application extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ThemeStore.ThemeMode mode = new ThemeStore(this).getThemeMode();
        if (mode == ThemeStore.ThemeMode.DARK) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if (mode == ThemeStore.ThemeMode.LIGHT) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }
}
