package com.example.marvel_app.data.local;

import android.content.Context;

import com.example.marvel_app.data.model.CharacterCardData;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class HistoryStore extends CharacterListPreferences {

    private static final String FILE_NAME = "recent_characters";
    private static final String STORAGE_KEY = "history";
    private static final int MAX_ITEMS = 20;

    public HistoryStore(Context context) {
        super(context, FILE_NAME, STORAGE_KEY);
    }

    public synchronized void record(CharacterCardData character) {
        if (character == null) {
            return;
        }

        List<CharacterCardData> history = readItems();
        Iterator<CharacterCardData> iterator = history.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getId() == character.getId()) {
                iterator.remove();
                break;
            }
        }

        history.add(0, character);
        if (history.size() > MAX_ITEMS) {
            history = history.subList(0, MAX_ITEMS);
        }
        writeItems(history);
    }

    public synchronized List<CharacterCardData> getHistory() {
        return Collections.unmodifiableList(readItems());
    }

    public synchronized void clear() {
        clearItems();
    }
}
