package com.example.marvel_app.domain.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;

public class CharacterSearchQueryExpanderTest {

    @Test
    public void expandsPortugueseExactAlias() {
        assertEquals(List.of("Spider-Man"), CharacterSearchQueryExpander.expand("Homem-Aranha"));
        assertEquals(List.of("Black Panther"), CharacterSearchQueryExpander.expand("pantera negra"));
    }

    @Test
    public void expandsPortuguesePrefixToSeveralRelevantCharacters() {
        List<String> queries = CharacterSearchQueryExpander.expand("ho");

        assertTrue(queries.contains("Spider-Man"));
        assertTrue(queries.contains("Iron Man"));
    }

    @Test
    public void ignoresAccentsWhenExpandingPrefix() {
        List<String> captainQueries = CharacterSearchQueryExpander.expand("capitão");
        List<String> captainMarvelQueries = CharacterSearchQueryExpander.expand("capitã");

        assertTrue(captainQueries.contains("Captain America"));
        assertTrue(captainMarvelQueries.contains("Captain Marvel"));
    }

    @Test
    public void keepsUnknownQueryForComicVineSearch() {
        assertEquals(List.of("Thanos"), CharacterSearchQueryExpander.expand("Thanos"));
    }

    @Test
    public void acceptsCommonPortugueseTypingMistake() {
        assertEquals(List.of("Spider-Man"), CharacterSearchQueryExpander.expand("homen aranha"));
    }

    @Test
    public void returnsNoQueriesForBlankText() {
        assertTrue(CharacterSearchQueryExpander.expand("   ").isEmpty());
    }

    @Test
    public void matchesOfflineSuggestionsUsingExpandedQueries() {
        assertTrue(CharacterSearchQueryExpander.matchesAnyExpansion("ho", "Spider-Man"));
        assertTrue(CharacterSearchQueryExpander.matchesAnyExpansion("ho", "Iron Man"));
    }
}
