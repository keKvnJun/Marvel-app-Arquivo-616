package com.example.marvel_app.domain.spiderverse;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SpiderVerseCatalogTest {
    @Test
    public void seedsContainBroadCuratedSelection() {
        assertTrue(SpiderVerseCatalog.seeds().size() >= 18);
        assertTrue(SpiderVerseCatalog.seeds().stream()
                .anyMatch(seed -> seed.getRealName().equals("Pavitr Prabhakar")));
    }

    @Test
    public void everySeedHasUniqueComicVineDestination() {
        Set<Long> ids = new HashSet<>();
        for (SpiderVerseSeed seed : SpiderVerseCatalog.seeds()) {
            assertTrue(seed.getComicVineId() > 0);
            assertEquals("4005-" + seed.getComicVineId(), seed.getApiObjectId());
            assertTrue("ID duplicado no Spider Verso", ids.add(seed.getComicVineId()));
        }
    }

    @Test
    public void keepsMarvelResultsAndDeduplicatesByComicVineId() {
        List<FakeResult> results = Arrays.asList(
                new FakeResult(10, "Spider-Man", "Peter Parker", "Marvel Comics"),
                new FakeResult(10, "Spider-Man", "Peter Parker", "Marvel"),
                new FakeResult(20, "Spider-Man", "Peter Parker", "Other Comics")
        );

        List<FakeResult> filtered = SpiderVerseCatalog.keepMarvelAndDeduplicate(results);

        assertEquals(1, filtered.size());
        assertEquals(10, filtered.get(0).getComicVineId());
    }

    @Test
    public void deduplicatesMissingIdsByNormalizedIdentity() {
        List<FakeResult> results = Arrays.asList(
                new FakeResult(0, "Spider Man", "Péter Parker", "Marvel Comics"),
                new FakeResult(0, " spider   man ", "Peter Parker", "Marvel")
        );

        assertEquals(1, SpiderVerseCatalog.keepMarvelAndDeduplicate(results).size());
    }

    private static final class FakeResult implements SpiderVerseResult {
        private final long id;
        private final String name;
        private final String realName;
        private final String publisher;

        private FakeResult(long id, String name, String realName, String publisher) {
            this.id = id;
            this.name = name;
            this.realName = realName;
            this.publisher = publisher;
        }

        @Override
        public long getComicVineId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getRealName() {
            return realName;
        }

        @Override
        public String getPublisherName() {
            return publisher;
        }
    }
}
