package com.example.marvel_app.domain.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public final class PackOpeningService {
    private final List<CollectibleCard> catalog;
    private final Random random;

    public PackOpeningService(List<CollectibleCard> catalog, Random random) {
        Map<String, CollectibleCard> uniqueCards = new LinkedHashMap<>();
        for (CollectibleCard card : catalog) {
            if (card != null) {
                uniqueCards.putIfAbsent(card.getId(), card);
            }
        }
        this.catalog = new ArrayList<>(uniqueCards.values());
        this.random = random;
    }

    public List<CollectibleCard> openPack(Set<String> ownedIds, int size) {
        if (size <= 0 || catalog.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> owned = ownedIds == null ? Collections.emptySet() : new HashSet<>(ownedIds);
        List<CollectibleCard> undiscovered = new ArrayList<>();
        List<CollectibleCard> discovered = new ArrayList<>();
        for (CollectibleCard card : catalog) {
            if (owned.contains(card.getId())) {
                discovered.add(card);
            } else {
                undiscovered.add(card);
            }
        }
        List<CollectibleCard> pack = new ArrayList<>();
        takeWeightedUntilFull(pack, undiscovered, size);
        takeWeightedUntilFull(pack, discovered, Math.min(size, catalog.size()));
        return Collections.unmodifiableList(pack);
    }

    private void takeWeightedUntilFull(
            List<CollectibleCard> target,
            List<CollectibleCard> source,
            int size
    ) {
        while (target.size() < size && !source.isEmpty()) {
            int totalWeight = 0;
            for (CollectibleCard card : source) {
                totalWeight += rarityWeight(card);
            }
            int draw = random.nextInt(totalWeight);
            int selectedIndex = 0;
            for (int index = 0; index < source.size(); index++) {
                draw -= rarityWeight(source.get(index));
                if (draw < 0) {
                    selectedIndex = index;
                    break;
                }
            }
            target.add(source.remove(selectedIndex));
        }
    }

    private static int rarityWeight(CollectibleCard card) {
        String rarity = card.getDuelCard().getRarity();
        if ("Lendária".equalsIgnoreCase(rarity)) return 5;
        if ("Épica".equalsIgnoreCase(rarity)) return 15;
        if ("Rara".equalsIgnoreCase(rarity)) return 30;
        return 50;
    }
}
