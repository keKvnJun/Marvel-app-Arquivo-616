package com.example.marvel_app.domain.cards;

import com.example.marvel_app.data.model.CharacterCardData;
import com.example.marvel_app.domain.duel.DuelCard;

import java.util.Objects;

public final class CollectibleCard {
    private final CharacterCardData character;
    private final DuelCard duelCard;

    public CollectibleCard(CharacterCardData character, DuelCard duelCard) {
        this.character = Objects.requireNonNull(character);
        this.duelCard = Objects.requireNonNull(duelCard);
    }

    public CharacterCardData getCharacter() {
        return character;
    }

    public DuelCard getDuelCard() {
        return duelCard;
    }

    public String getId() {
        return duelCard.getId();
    }
}
