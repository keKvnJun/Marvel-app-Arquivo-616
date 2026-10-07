package com.example.marvel_app.domain.cards;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CardCollectionStoreTest {
    @Test
    public void collectionAndDeckSurviveNewStoreInstance() {
        MemoryStorage storage = new MemoryStorage();
        CardCollectionStore first = new CardCollectionStore(storage);
        first.addPack(CardCatalog.all().subList(0, 3));
        first.fillDeckFromCollection();

        CardCollectionStore restored = new CardCollectionStore(storage);

        assertEquals(3, restored.ownedCards().size());
        assertEquals(3, restored.deckCards().size());
    }

    @Test
    public void deckAcceptsOnlyOwnedCardsAndStopsAtThree() {
        CardCollectionStore store = new CardCollectionStore(new MemoryStorage());
        store.addPack(CardCatalog.all().subList(0, 4));

        assertEquals(CardCollectionStore.DeckChange.ADDED, store.toggleDeck("spider"));
        assertEquals(CardCollectionStore.DeckChange.ADDED, store.toggleDeck("iron"));
        assertEquals(CardCollectionStore.DeckChange.ADDED, store.toggleDeck("storm"));
        assertEquals(CardCollectionStore.DeckChange.DECK_FULL, store.toggleDeck("hulk"));
        assertEquals(3, store.load().getDeckIds().size());
        assertEquals(CardCollectionStore.DeckChange.NOT_OWNED, store.toggleDeck("thor"));
    }

    @Test
    public void corruptedAndUnknownIdsAreIgnored() {
        MemoryStorage storage = new MemoryStorage();
        storage.write("owned=spider,unknown\ndeck=unknown,spider,spider");

        CardCollectionState state = new CardCollectionStore(storage).load();

        assertEquals(1, state.getOwnedIds().size());
        assertEquals(Arrays.asList("spider"), state.getDeckIds());
    }

    @Test
    public void packAddsOnlyNewCardsToCollectionCount() {
        CardCollectionStore store = new CardCollectionStore(new MemoryStorage());
        assertEquals(2, store.addPack(CardCatalog.all().subList(0, 2)));
        assertEquals(1, store.addPack(CardCatalog.all().subList(1, 3)));
        assertEquals(3, store.ownedCards().size());
    }

    private static final class MemoryStorage implements CardCollectionStorage {
        private String value = "";

        @Override public String read() { return value; }

        @Override public void write(String value) { this.value = value; }
    }
}
