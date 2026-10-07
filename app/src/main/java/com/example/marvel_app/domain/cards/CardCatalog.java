package com.example.marvel_app.domain.cards;

import com.example.marvel_app.data.model.CharacterCardData;
import com.example.marvel_app.domain.duel.DuelCard;
import com.example.marvel_app.domain.duel.DuelStats;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CardCatalog {
    private static final String DISCLOSURE =
            "Índices lúdicos criados para o Arquivo 616. Não são níveis oficiais da Marvel.";

    private static final List<CollectibleCard> CARDS = Collections.unmodifiableList(Arrays.asList(
            card(616001, "spider", "Spider-Man", "Peter Parker", "Rara", 82, 78, 95, 72),
            card(616002, "iron", "Iron Man", "Tony Stark", "Rara", 88, 76, 96, 68),
            card(616003, "storm", "Storm", "Ororo Munroe", "Épica", 92, 70, 84, 80),
            card(616004, "hulk", "Hulk", "Bruce Banner", "Rara", 76, 79, 97, 60),
            card(616005, "captain", "Captain America", "Steve Rogers", "Épica", 54, 100, 98, 94),
            card(616006, "strange", "Doctor Strange", "Stephen Strange", "Épica", 100, 71, 82, 66),
            card(616007, "panther", "Black Panther", "T'Challa", "Rara", 74, 69, 82, 88),
            card(616008, "scarlet-witch", "Scarlet Witch", "Wanda Maximoff", "Épica", 98, 72, 87, 75),
            card(616009, "thor", "Thor", "Thor Odinson", "Lendária", 90, 100, 96, 78),
            card(616010, "black-widow", "Black Widow", "Natasha Romanoff", "Incomum", 48, 66, 78, 92),
            card(616011, "miles", "Spider-Man", "Miles Morales", "Rara", 86, 18, 72, 64),
            card(616012, "captain-marvel", "Captain Marvel", "Carol Danvers", "Épica", 94, 58, 80, 74)
    ));

    private static final Map<String, CollectibleCard> BY_ID;

    static {
        Map<String, CollectibleCard> cardsById = new LinkedHashMap<>();
        for (CollectibleCard card : CARDS) {
            cardsById.put(card.getId(), card);
        }
        BY_ID = Collections.unmodifiableMap(cardsById);
    }

    private CardCatalog() {
    }

    public static List<CollectibleCard> all() {
        return CARDS;
    }

    public static CollectibleCard find(String id) {
        return BY_ID.get(id);
    }

    private static CollectibleCard card(
            long localId,
            String id,
            String name,
            String realName,
            String rarity,
            int versatility,
            int editorialHistory,
            int presence,
            int alliances
    ) {
        CharacterCardData character = new CharacterCardData(
                localId,
                "",
                name,
                realName,
                "",
                "Marvel Comics",
                0
        );
        DuelCard duelCard = new DuelCard(
                id,
                name,
                rarity,
                new DuelStats(versatility, editorialHistory, presence, alliances),
                true,
                DISCLOSURE
        );
        return new CollectibleCard(character, duelCard);
    }
}
