package com.example.marvel_app.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.marvel_app.domain.cards.CardCollectionStorage;

public final class SharedPreferencesCardStorage implements CardCollectionStorage {
    private static final String FILE_NAME = "card_collection";
    private static final String STORAGE_KEY = "state";

    private final SharedPreferences preferences;

    public SharedPreferencesCardStorage(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public String read() {
        return preferences.getString(STORAGE_KEY, "");
    }

    @Override
    public void write(String value) {
        preferences.edit().putString(STORAGE_KEY, value).apply();
    }
}
