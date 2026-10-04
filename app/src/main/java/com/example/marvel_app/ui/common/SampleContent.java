package com.example.marvel_app.ui.common;

import com.example.marvel_app.data.model.CharacterCardData;

import java.util.Arrays;
import java.util.List;

public final class SampleContent {
    private SampleContent() {
    }

    public static List<CharacterCardData> characters() {
        return Arrays.asList(
                new CharacterCardData(-1, "", "Spider-Man", "Peter Parker", "", "Marvel Comics", 1643),
                new CharacterCardData(-2, "", "Iron Man", "Tony Stark", "", "Marvel Comics", 1274),
                new CharacterCardData(-3, "", "Miles Morales", "Spider-Man", "", "Marvel Comics", 386),
                new CharacterCardData(-4, "", "Black Panther", "T'Challa", "", "Marvel Comics", 742),
                new CharacterCardData(-5, "", "Captain Marvel", "Carol Danvers", "", "Marvel Comics", 923)
        );
    }
}
