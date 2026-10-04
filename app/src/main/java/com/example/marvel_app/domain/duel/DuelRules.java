package com.example.marvel_app.domain.duel;

public final class DuelRules {
    public enum Category {
        VERSATILITY,
        EDITORIAL_HISTORY,
        PRESENCE,
        ALLIANCES,
        MULTIVERSE_INDEX
    }

    public enum Outcome {
        FIRST_WINS,
        SECOND_WINS,
        TIE,
        INVALID_CARD
    }

    private DuelRules() {
    }

    public static Outcome compare(DuelCard first, DuelCard second, Category category) {
        if (first == null || second == null || category == null
                || !first.isPlayable() || !second.isPlayable()) {
            return Outcome.INVALID_CARD;
        }
        int firstValue = value(first.getStats(), category);
        int secondValue = value(second.getStats(), category);
        if (firstValue > secondValue) {
            return Outcome.FIRST_WINS;
        }
        if (secondValue > firstValue) {
            return Outcome.SECOND_WINS;
        }
        return Outcome.TIE;
    }

    private static int value(DuelStats stats, Category category) {
        switch (category) {
            case VERSATILITY:
                return stats.getVersatility();
            case EDITORIAL_HISTORY:
                return stats.getEditorialHistory();
            case PRESENCE:
                return stats.getPresence();
            case ALLIANCES:
                return stats.getAlliances();
            case MULTIVERSE_INDEX:
                return stats.getMultiverseIndex();
            default:
                throw new IllegalArgumentException("Unsupported duel category");
        }
    }
}
