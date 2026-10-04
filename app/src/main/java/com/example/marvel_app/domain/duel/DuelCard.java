package com.example.marvel_app.domain.duel;

import java.util.Objects;

public final class DuelCard {
    private final String id;
    private final String name;
    private final String rarity;
    private final DuelStats stats;
    private final boolean playable;
    private final String disclosure;

    public DuelCard(
            String id,
            String name,
            String rarity,
            DuelStats stats,
            boolean playable,
            String disclosure
    ) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.rarity = Objects.requireNonNull(rarity);
        this.stats = Objects.requireNonNull(stats);
        this.playable = playable;
        this.disclosure = Objects.requireNonNull(disclosure);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRarity() {
        return rarity;
    }

    public DuelStats getStats() {
        return stats;
    }

    public boolean isPlayable() {
        return playable;
    }

    public String getDisclosure() {
        return disclosure;
    }
}
