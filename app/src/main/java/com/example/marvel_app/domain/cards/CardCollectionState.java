package com.example.marvel_app.domain.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class CardCollectionState {
    private final Set<String> ownedIds;
    private final List<String> deckIds;

    public CardCollectionState(Set<String> ownedIds, List<String> deckIds) {
        this.ownedIds = Collections.unmodifiableSet(new LinkedHashSet<>(ownedIds));
        this.deckIds = Collections.unmodifiableList(new ArrayList<>(deckIds));
    }

    public static CardCollectionState empty() {
        return new CardCollectionState(Collections.emptySet(), Collections.emptyList());
    }

    public Set<String> getOwnedIds() {
        return ownedIds;
    }

    public List<String> getDeckIds() {
        return deckIds;
    }
}
