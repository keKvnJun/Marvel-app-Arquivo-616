package com.example.marvel_app.domain.cards;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class PackOpeningServiceTest {
    @Test
    public void packContainsUniqueCardsAndPrioritizesUndiscoveredCards() {
        Set<String> owned = new HashSet<>();
        owned.add("spider");
        owned.add("iron");
        PackOpeningService service = new PackOpeningService(CardCatalog.all(), new Random(7));

        List<CollectibleCard> pack = service.openPack(owned, 3);

        assertEquals(3, pack.size());
        Set<String> ids = new HashSet<>();
        for (CollectibleCard card : pack) {
            ids.add(card.getId());
            assertFalse(owned.contains(card.getId()));
        }
        assertEquals(3, ids.size());
    }

    @Test
    public void packStillOpensWhenCollectionIsComplete() {
        Set<String> owned = new HashSet<>();
        for (CollectibleCard card : CardCatalog.all()) {
            owned.add(card.getId());
        }

        List<CollectibleCard> pack = new PackOpeningService(CardCatalog.all(), new Random(3))
                .openPack(owned, 3);

        assertEquals(3, pack.size());
    }
}
