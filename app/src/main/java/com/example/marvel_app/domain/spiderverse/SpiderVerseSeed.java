package com.example.marvel_app.domain.spiderverse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class SpiderVerseSeed {
    private final String key;
    private final long comicVineId;
    private final String displayName;
    private final String realName;
    private final String universe;
    private final List<String> searchTerms;

    public SpiderVerseSeed(
            String key,
            long comicVineId,
            String displayName,
            String realName,
            String universe,
            List<String> searchTerms
    ) {
        this.key = requireText(key, "key");
        if (comicVineId <= 0) {
            throw new IllegalArgumentException("comicVineId must be positive");
        }
        this.comicVineId = comicVineId;
        this.displayName = requireText(displayName, "displayName");
        this.realName = requireText(realName, "realName");
        this.universe = requireText(universe, "universe");
        this.searchTerms = Collections.unmodifiableList(new ArrayList<>(searchTerms));
    }

    public String getKey() {
        return key;
    }

    public long getComicVineId() {
        return comicVineId;
    }

    public String getApiObjectId() {
        return "4005-" + comicVineId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRealName() {
        return realName;
    }

    public String getUniverse() {
        return universe;
    }

    public List<String> getSearchTerms() {
        return searchTerms;
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field);
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }
}
