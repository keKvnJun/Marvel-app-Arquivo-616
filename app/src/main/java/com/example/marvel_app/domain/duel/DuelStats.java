package com.example.marvel_app.domain.duel;

import java.util.Objects;

public final class DuelStats {
    private final int versatility;
    private final int editorialHistory;
    private final int presence;
    private final int alliances;
    private final int multiverseIndex;

    public DuelStats(int versatility, int editorialHistory, int presence, int alliances) {
        this.versatility = requireRange(versatility);
        this.editorialHistory = requireRange(editorialHistory);
        this.presence = requireRange(presence);
        this.alliances = requireRange(alliances);
        this.multiverseIndex = Math.round(
                versatility * 0.30f
                        + editorialHistory * 0.20f
                        + presence * 0.30f
                        + alliances * 0.20f
        );
    }

    public int getVersatility() {
        return versatility;
    }

    public int getEditorialHistory() {
        return editorialHistory;
    }

    public int getPresence() {
        return presence;
    }

    public int getAlliances() {
        return alliances;
    }

    public int getMultiverseIndex() {
        return multiverseIndex;
    }

    private static int requireRange(int value) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("Duel stat must be between 0 and 100");
        }
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DuelStats)) {
            return false;
        }
        DuelStats that = (DuelStats) other;
        return versatility == that.versatility
                && editorialHistory == that.editorialHistory
                && presence == that.presence
                && alliances == that.alliances
                && multiverseIndex == that.multiverseIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(versatility, editorialHistory, presence, alliances, multiverseIndex);
    }
}
