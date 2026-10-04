package com.example.marvel_app.domain.duel;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DuelRulesTest {
    @Test
    public void calculatesBoundedStatsFromComicVineCounts() {
        DuelStats stats = DuelStatsCalculator.fromComicVine(14, 1962, 5000, 12, 2026);

        assertEquals(100, stats.getVersatility());
        assertEquals(80, stats.getEditorialHistory());
        assertEquals(100, stats.getPresence());
        assertEquals(100, stats.getAlliances());
        assertTrue(stats.getMultiverseIndex() <= 100);
    }

    @Test
    public void comparesOnlyPlayableCards() {
        DuelCard first = card("first", new DuelStats(80, 60, 50, 40));
        DuelCard second = card("second", new DuelStats(70, 90, 50, 30));

        assertEquals(
                DuelRules.Outcome.FIRST_WINS,
                DuelRules.compare(first, second, DuelRules.Category.VERSATILITY)
        );
        assertEquals(
                DuelRules.Outcome.TIE,
                DuelRules.compare(first, second, DuelRules.Category.PRESENCE)
        );
        assertEquals(
                DuelRules.Outcome.INVALID_CARD,
                DuelRules.compare(StanLeeCardFactory.create(), second, DuelRules.Category.MULTIVERSE_INDEX)
        );
    }

    @Test
    public void stanLeeCardIsClearlyCommemorativeAndNotPlayable() {
        DuelCard card = StanLeeCardFactory.create();

        assertFalse(card.isPlayable());
        assertEquals("Excelsior", card.getRarity());
        assertEquals(100, card.getStats().getMultiverseIndex());
        assertTrue(card.getDisclosure().contains("não vêm da Comic Vine"));
    }

    private static DuelCard card(String id, DuelStats stats) {
        return new DuelCard(id, id, "Comum", stats, true,
                "Mecânica do aplicativo baseada nos dados disponíveis.");
    }
}
