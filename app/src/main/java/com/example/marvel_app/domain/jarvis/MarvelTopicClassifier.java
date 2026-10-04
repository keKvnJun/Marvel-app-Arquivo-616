package com.example.marvel_app.domain.jarvis;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class MarvelTopicClassifier {
    public enum Result {
        MARVEL,
        OUT_OF_SCOPE,
        EMPTY
    }

    private static final Set<String> TERMS = new HashSet<>(Arrays.asList(
            "marvel", "vingadores", "avengers", "homem aranha", "spider man", "miles morales",
            "peter parker", "gwen stacy", "ghost spider", "hulk", "thor", "loki", "thanos",
            "homem de ferro", "iron man", "tony stark", "capitao america", "captain america",
            "pantera negra", "black panther", "viuva negra", "black widow", "x men", "wolverine",
            "wanda maximoff", "feiticeira escarlate", "scarlet witch",
            "deadpool", "demolidor", "daredevil", "quarteto fantastico", "fantastic four",
            "doutor estranho", "doctor strange", "guardioes da galaxia", "guardians of the galaxy",
            "shield", "hydra", "jarvis", "ultron", "wakanda", "asgard", "mutante marvel",
            "quadrinhos marvel", "hq marvel", "comic vine", "simbionte",
            "venom", "carnificina", "carnage", "aranhaverso", "spider verse", "multiverso",
            "galactus", "surfista prateado", "silver surfer", "nick fury", "stan lee"
    ));

    public Result classify(String question) {
        String normalized = normalize(question);
        if (normalized.isEmpty()) {
            return Result.EMPTY;
        }
        for (String term : TERMS) {
            if (containsTerm(normalized, term)) {
                return Result.MARVEL;
            }
        }
        return Result.OUT_OF_SCOPE;
    }

    private static boolean containsTerm(String text, String term) {
        return (" " + text + " ").contains(" " + term + " ");
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
    }
}
