package com.example.marvel_app.domain.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class CardCollectionStore {
    public static final int MAX_DECK_SIZE = 3;

    public enum DeckChange {
        ADDED,
        REMOVED,
        DECK_FULL,
        NOT_OWNED,
        UNKNOWN_CARD
    }

    private final CardCollectionStorage storage;

    public CardCollectionStore(CardCollectionStorage storage) {
        this.storage = storage;
    }

    public synchronized CardCollectionState load() {
        String raw = storage.read();
        if (raw == null || raw.trim().isEmpty()) {
            return CardCollectionState.empty();
        }

        Set<String> owned = new LinkedHashSet<>();
        List<String> deck = new ArrayList<>();
        String[] lines = raw.split("\\n");
        for (String line : lines) {
            if (line.startsWith("owned=")) {
                addValidIds(owned, line.substring("owned=".length()));
            } else if (line.startsWith("deck=")) {
                addValidDeckIds(deck, line.substring("deck=".length()));
            }
        }
        deck.removeIf(id -> !owned.contains(id));
        if (deck.size() > MAX_DECK_SIZE) {
            deck = new ArrayList<>(deck.subList(0, MAX_DECK_SIZE));
        }
        return new CardCollectionState(owned, deck);
    }

    public synchronized int addPack(List<CollectibleCard> cards) {
        CardCollectionState current = load();
        Set<String> owned = new LinkedHashSet<>(current.getOwnedIds());
        int previousSize = owned.size();
        if (cards != null) {
            for (CollectibleCard card : cards) {
                if (card != null && CardCatalog.find(card.getId()) != null) {
                    owned.add(card.getId());
                }
            }
        }
        save(new CardCollectionState(owned, current.getDeckIds()));
        return owned.size() - previousSize;
    }

    public synchronized void fillDeckFromCollection() {
        CardCollectionState current = load();
        List<String> deck = new ArrayList<>(current.getDeckIds());
        for (String id : current.getOwnedIds()) {
            if (deck.size() >= MAX_DECK_SIZE) {
                break;
            }
            if (!deck.contains(id)) {
                deck.add(id);
            }
        }
        save(new CardCollectionState(current.getOwnedIds(), deck));
    }

    public synchronized DeckChange toggleDeck(String id) {
        if (CardCatalog.find(id) == null) {
            return DeckChange.UNKNOWN_CARD;
        }
        CardCollectionState current = load();
        if (!current.getOwnedIds().contains(id)) {
            return DeckChange.NOT_OWNED;
        }
        List<String> deck = new ArrayList<>(current.getDeckIds());
        if (deck.remove(id)) {
            save(new CardCollectionState(current.getOwnedIds(), deck));
            return DeckChange.REMOVED;
        }
        if (deck.size() >= MAX_DECK_SIZE) {
            return DeckChange.DECK_FULL;
        }
        deck.add(id);
        save(new CardCollectionState(current.getOwnedIds(), deck));
        return DeckChange.ADDED;
    }

    public List<CollectibleCard> ownedCards() {
        List<CollectibleCard> cards = new ArrayList<>();
        for (String id : load().getOwnedIds()) {
            CollectibleCard card = CardCatalog.find(id);
            if (card != null) {
                cards.add(card);
            }
        }
        return Collections.unmodifiableList(cards);
    }

    public List<CollectibleCard> deckCards() {
        List<CollectibleCard> cards = new ArrayList<>();
        for (String id : load().getDeckIds()) {
            CollectibleCard card = CardCatalog.find(id);
            if (card != null && card.getDuelCard().isPlayable()) {
                cards.add(card);
            }
        }
        return Collections.unmodifiableList(cards);
    }

    private void save(CardCollectionState state) {
        storage.write("owned=" + String.join(",", state.getOwnedIds())
                + "\ndeck=" + String.join(",", state.getDeckIds()));
    }

    private static void addValidIds(Set<String> target, String encoded) {
        for (String id : splitIds(encoded)) {
            if (CardCatalog.find(id) != null) {
                target.add(id);
            }
        }
    }

    private static void addValidDeckIds(List<String> target, String encoded) {
        for (String id : splitIds(encoded)) {
            if (CardCatalog.find(id) != null && !target.contains(id)) {
                target.add(id);
            }
        }
    }

    private static List<String> splitIds(String encoded) {
        if (encoded == null || encoded.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<String> ids = new ArrayList<>();
        for (String value : encoded.split(",")) {
            String id = value.trim();
            if (!id.isEmpty()) {
                ids.add(id);
            }
        }
        return ids;
    }
}
