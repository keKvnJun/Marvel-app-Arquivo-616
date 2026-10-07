package com.example.marvel_app.domain.cards;

import com.example.marvel_app.data.model.CharacterCardData;
import com.example.marvel_app.data.model.CharacterDto;
import com.example.marvel_app.domain.duel.DuelCard;
import com.example.marvel_app.domain.duel.DuelStats;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CardCatalog {
    private static final String DISCLOSURE =
            "Índices lúdicos criados para o Arquivo 616. Não são níveis oficiais da Marvel.";

    private static final List<CollectibleCard> CARDS = Collections.unmodifiableList(Arrays.asList(
            card(1443, "spider", "Spider-Man", "Peter Parker", "Rara", 82, 78, 95, 72),
            card(1455, "iron", "Iron Man", "Tony Stark", "Rara", 88, 76, 96, 68),
            card(1444, "storm", "Storm", "Ororo Munroe", "Épica", 92, 70, 84, 80),
            card(2267, "hulk", "Hulk", "Bruce Banner", "Rara", 76, 79, 97, 60),
            card(1442, "captain", "Captain America", "Steve Rogers", "Épica", 54, 100, 98, 94),
            card(1456, "strange", "Doctor Strange", "Stephen Strange", "Épica", 100, 71, 82, 66),
            card(1477, "panther", "Black Panther", "T'Challa", "Rara", 74, 69, 82, 88),
            card(1466, "scarlet-witch", "Scarlet Witch", "Wanda Maximoff", "Épica", 98, 72, 87, 75),
            card(2268, "thor", "Thor", "Thor Odinson", "Lendária", 90, 100, 96, 78),
            card(3200, "black-widow", "Black Widow", "Natasha Romanoff", "Incomum", 48, 66, 78, 92),
            card(79420, "miles", "Spider-Man", "Miles Morales", "Rara", 86, 18, 72, 64),
            card(21561, "captain-marvel", "Captain Marvel", "Carol Danvers", "Épica", 94, 58, 80, 74)
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

    public static List<Long> comicVineIds() {
        List<Long> ids = new ArrayList<>();
        for (CollectibleCard card : CARDS) {
            ids.add(card.getCharacter().getId());
        }
        return Collections.unmodifiableList(ids);
    }

    public static List<CollectibleCard> hydrate(
            List<CollectibleCard> cards,
            List<CharacterDto> characters
    ) {
        Map<Long, CharacterDto> byId = new LinkedHashMap<>();
        if (characters != null) {
            for (CharacterDto character : characters) {
                if (character != null) {
                    byId.put(character.getId(), character);
                }
            }
        }
        List<CollectibleCard> hydrated = new ArrayList<>();
        for (CollectibleCard card : cards) {
            CharacterCardData current = card.getCharacter();
            CharacterDto remote = byId.get(current.getId());
            if (remote == null) {
                hydrated.add(card);
                continue;
            }
            CharacterCardData character = new CharacterCardData(
                    remote.getId(),
                    remote.getApiObjectId(),
                    current.getName(),
                    current.getRealName(),
                    remote.getCardImageUrl(),
                    remote.getPublisher() == null ? "Marvel" : remote.getPublisher().getName(),
                    remote.getCountOfIssueAppearances()
            );
            hydrated.add(new CollectibleCard(character, card.getDuelCard()));
        }
        return Collections.unmodifiableList(hydrated);
    }

    private static CollectibleCard card(
            long comicVineId,
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
                comicVineId,
                "4005-" + comicVineId,
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
