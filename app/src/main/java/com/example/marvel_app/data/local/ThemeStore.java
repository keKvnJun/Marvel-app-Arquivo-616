package com.example.marvel_app.data.local;

import android.content.Context;
import android.content.SharedPreferences;

public final class ThemeStore {

    private static final String FILE_NAME = "appearance_preferences";
    private static final String THEME_KEY = "theme_mode";

    public enum ThemeMode {
        SYSTEM,
        LIGHT,
        DARK
    }

    private final SharedPreferences preferences;

    public ThemeStore(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    public ThemeMode getThemeMode() {
        String storedValue = preferences.getString(THEME_KEY, ThemeMode.SYSTEM.name());
        try {
            return ThemeMode.valueOf(storedValue);
        } catch (IllegalArgumentException exception) {
            return ThemeMode.SYSTEM;
        }
    }

    public void setThemeMode(ThemeMode mode) {
        ThemeMode safeMode = mode == null ? ThemeMode.SYSTEM : mode;
        preferences.edit().putString(THEME_KEY, safeMode.name()).apply();
    }
}
