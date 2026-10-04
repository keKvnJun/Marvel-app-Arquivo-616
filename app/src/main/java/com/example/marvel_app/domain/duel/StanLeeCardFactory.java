package com.example.marvel_app.domain.duel;

public final class StanLeeCardFactory {
    private StanLeeCardFactory() {
    }

    public static DuelCard create() {
        return new DuelCard(
                "commemorative-stan-lee",
                "Stan Lee",
                "Excelsior",
                new DuelStats(100, 100, 100, 100),
                false,
                "Carta comemorativa. Os atributos são uma homenagem editorial e não vêm da Comic Vine."
        );
    }
}
