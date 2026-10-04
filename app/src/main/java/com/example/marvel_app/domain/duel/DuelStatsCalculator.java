package com.example.marvel_app.domain.duel;

public final class DuelStatsCalculator {
    private static final int MAX_HISTORY_YEARS = 80;
    private static final int PRESENCE_REFERENCE = 2000;

    private DuelStatsCalculator() {
    }

    public static DuelStats fromComicVine(
            int powerCount,
            int firstAppearanceYear,
            int issueAppearanceCount,
            int teamCount,
            int currentYear
    ) {
        int versatility = clamp(powerCount * 10);
        int historyYears = firstAppearanceYear > 0 && currentYear >= firstAppearanceYear
                ? currentYear - firstAppearanceYear
                : 0;
        int editorialHistory = clamp(Math.round(historyYears * 100f / MAX_HISTORY_YEARS));
        int presence = logarithmicScore(issueAppearanceCount, PRESENCE_REFERENCE);
        int alliances = clamp(teamCount * 12);
        return new DuelStats(versatility, editorialHistory, presence, alliances);
    }

    private static int logarithmicScore(int value, int reference) {
        if (value <= 0) {
            return 0;
        }
        double score = Math.log1p(value) / Math.log1p(reference) * 100.0;
        return clamp((int) Math.round(score));
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
