package com.example.marvel_app.domain.cards;

public interface CardCollectionStorage {
    String read();

    void write(String value);
}
