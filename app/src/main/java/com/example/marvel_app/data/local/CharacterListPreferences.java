package com.example.marvel_app.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.marvel_app.data.model.CharacterCardData;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

abstract class CharacterListPreferences {

    private static final Type LIST_TYPE = new TypeToken<List<CharacterCardData>>() { }.getType();

    private final SharedPreferences preferences;
    private final Gson gson = new Gson();
    private final String storageKey;

    CharacterListPreferences(Context context, String fileName, String storageKey) {
        this.preferences = context.getApplicationContext()
                .getSharedPreferences(fileName, Context.MODE_PRIVATE);
        this.storageKey = storageKey;
    }

    protected synchronized List<CharacterCardData> readItems() {
        String json = preferences.getString(storageKey, "[]");
        try {
            List<CharacterCardData> items = gson.fromJson(json, LIST_TYPE);
            return items == null ? new ArrayList<>() : new ArrayList<>(items);
        } catch (JsonSyntaxException exception) {
            preferences.edit().remove(storageKey).apply();
            return new ArrayList<>();
        }
    }

    protected synchronized void writeItems(List<CharacterCardData> items) {
        preferences.edit().putString(storageKey, gson.toJson(items, LIST_TYPE)).apply();
    }

    protected synchronized void clearItems() {
        preferences.edit().remove(storageKey).apply();
    }
}
