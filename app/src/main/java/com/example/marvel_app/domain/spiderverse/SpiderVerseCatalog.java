package com.example.marvel_app.domain.spiderverse;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class SpiderVerseCatalog {
    private static final List<SpiderVerseSeed> SEEDS = Collections.unmodifiableList(Arrays.asList(
            seed("peter-parker", "Spider-Man", "Peter Parker", "Terra 616", "Spider-Man", "Peter Parker"),
            seed("miles-morales", "Spider-Man", "Miles Morales", "Terra 1610", "Miles Morales", "Ultimate Spider-Man"),
            seed("ghost-spider", "Ghost-Spider", "Gwen Stacy", "Terra 65", "Ghost-Spider", "Spider-Gwen", "Gwen Stacy"),
            seed("spider-man-2099", "Spider-Man 2099", "Miguel O'Hara", "Terra 928", "Spider-Man 2099", "Miguel O'Hara"),
            seed("spider-man-noir", "Spider-Man Noir", "Peter Parker", "Terra 90214", "Spider-Man Noir"),
            seed("spider-ham", "Spider-Ham", "Peter Porker", "Terra 8311", "Spider-Ham", "Peter Porker"),
            seed("spdr", "SP//dr", "Peni Parker", "Terra 14512", "SP//dr", "Peni Parker"),
            seed("spider-punk", "Spider-Punk", "Hobie Brown", "Terra 138", "Spider-Punk", "Hobie Brown"),
            seed("silk", "Silk", "Cindy Moon", "Terra 616", "Silk", "Cindy Moon"),
            seed("scarlet-spider-ben", "Scarlet Spider", "Ben Reilly", "Terra 616", "Scarlet Spider", "Ben Reilly"),
            seed("scarlet-spider-kaine", "Scarlet Spider", "Kaine Parker", "Terra 616", "Kaine Parker", "Scarlet Spider Kaine"),
            seed("spider-girl", "Spider-Girl", "May Parker", "Terra 982", "Spider-Girl", "Mayday Parker"),
            seed("superior-spider-man", "Superior Spider-Man", "Otto Octavius", "Terra 616", "Superior Spider-Man"),
            seed("spider-uk", "Spider-UK", "Billy Braddock", "Terra 833", "Spider-UK", "Billy Braddock"),
            seed("spider-man-1602", "The Spider", "Peter Parquagh", "Terra 311", "Spider-Man 1602", "Peter Parquagh"),
            seed("spider-woman", "Spider-Woman", "Jessica Drew", "Terra 616", "Spider-Woman", "Jessica Drew"),
            seed("spider-man-india", "Spider-Man India", "Pavitr Prabhakar", "Terra 50101", "Spider-Man India", "Pavitr Prabhakar"),
            seed("spider-rex", "Spider-Rex", "Pter Ptarker", "Terra 66", "Spider-Rex"),
            seed("spiders-man", "Spiders-Man", "Peter Parker", "Terra 11580", "Spiders-Man"),
            seed("japanese-spider-man", "Japanese Spider-Man", "Takuya Yamashiro", "Terra 51778", "Japanese Spider-Man", "Takuya Yamashiro")
    ));

    private SpiderVerseCatalog() {
    }

    public static List<SpiderVerseSeed> seeds() {
        return SEEDS;
    }

    public static <T extends SpiderVerseResult> List<T> keepMarvelAndDeduplicate(List<T> results) {
        Map<String, T> unique = new LinkedHashMap<>();
        for (T result : results) {
            if (result == null || !isMarvelPublisher(result.getPublisherName())) {
                continue;
            }
            String identity = result.getComicVineId() > 0
                    ? "id:" + result.getComicVineId()
                    : "name:" + normalize(result.getName()) + "|" + normalize(result.getRealName());
            unique.putIfAbsent(identity, result);
        }
        return Collections.unmodifiableList(new ArrayList<>(unique.values()));
    }

    public static boolean isMarvelPublisher(String publisherName) {
        String normalized = normalize(publisherName);
        return normalized.equals("marvel") || normalized.equals("marvel comics");
    }

    private static SpiderVerseSeed seed(
            String key,
            String displayName,
            String realName,
            String universe,
            String... searchTerms
    ) {
        return new SpiderVerseSeed(key, displayName, realName, universe, Arrays.asList(searchTerms));
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String withoutMarks = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return withoutMarks.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }
}
