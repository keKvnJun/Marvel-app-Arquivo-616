package com.example.marvel_app.data.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CharacterCardDataTest {

    @Test
    public void equalityUsesComicVineId() {
        CharacterCardData first = new CharacterCardData(
                1L,
                "4005-1",
                "Hero",
                "First name",
                "https://example.com/one.jpg",
                "Marvel Comics",
                10
        );
        CharacterCardData second = new CharacterCardData(
                1L,
                "4005-1",
                "Hero renamed",
                "Second name",
                "https://example.com/two.jpg",
                "Marvel Comics",
                20
        );

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}
