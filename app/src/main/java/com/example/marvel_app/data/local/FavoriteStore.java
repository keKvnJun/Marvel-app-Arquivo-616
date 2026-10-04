package com.example.marvel_app.data.local;

import android.content.Context;

import com.example.marvel_app.data.model.CharacterCardData;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class FavoriteStore extends CharacterListPreferences {

    private static final String FILE_NAME = "favorite_characters";
    private static final String STORAGE_KEY = "favorites";

    public FavoriteStore(Context context) {
        super(context, FILE_NAME, STORAGE_KEY);
    }

    public synchronized List<CharacterCardData> getFavorites() {
        List<CharacterCardData> favorites = readItems();
        return Collections.unmodifiableList(favorites);
    }

    public synchronized boolean isFavorite(long characterId) {
        for (CharacterCardData favorite : readItems()) {
            if (favorite.getId() == characterId) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean toggle(CharacterCardData character) {
        if (character == null) {
            return false;
        }

        List<CharacterCardData> favorites = readItems();
        Iterator<CharacterCardData> iterator = favorites.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getId() == character.getId()) {
                iterator.remove();
                writeItems(favorites);
                return false;
            }
        }

        favorites.add(0, character);
        writeItems(favorites);
        return true;
    }

    public synchronized void clear() {
        clearItems();
    }
}
