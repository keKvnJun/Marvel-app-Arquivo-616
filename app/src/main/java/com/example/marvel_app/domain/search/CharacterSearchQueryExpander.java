package com.example.marvel_app.domain.search;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class CharacterSearchQueryExpander {

    private static final int MIN_PREFIX_LENGTH = 2;
    private static final int MAX_EXPANSIONS = 3;
    private static final Map<String, String> ALIASES = createAliases();

    private CharacterSearchQueryExpander() {
    }

    public static List<String> expand(String userQuery) {
        String original = userQuery == null ? "" : userQuery.trim();
        String normalized = normalize(original);
        if (normalized.isEmpty()) {
            return Collections.emptyList();
        }

        String exact = ALIASES.get(normalized);
        if (exact != null) {
            return Collections.singletonList(exact);
        }

        if (normalized.length() >= MIN_PREFIX_LENGTH) {
            Set<String> expansions = new LinkedHashSet<>();
            for (Map.Entry<String, String> alias : ALIASES.entrySet()) {
                if (alias.getKey().startsWith(normalized)) {
                    expansions.add(alias.getValue());
                    if (expansions.size() == MAX_EXPANSIONS) {
                        break;
                    }
                }
            }
            if (!expansions.isEmpty()) {
                return Collections.unmodifiableList(new ArrayList<>(expansions));
            }
        }

        return Collections.singletonList(original);
    }

    public static boolean matchesAnyExpansion(String userQuery, String characterName) {
        String normalizedName = normalize(characterName);
        if (normalizedName.isEmpty()) {
            return false;
        }
        for (String expandedQuery : expand(userQuery)) {
            String normalizedQuery = normalize(expandedQuery);
            if (normalizedName.startsWith(normalizedQuery)
                    || normalizedQuery.startsWith(normalizedName)) {
                return true;
            }
        }
        return false;
    }

    static String normalize(String value) {
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

    private static Map<String, String> createAliases() {
        Map<String, String> aliases = new LinkedHashMap<>();
        add(aliases, "homem aranha", "Spider-Man");
        add(aliases, "homem de ferro", "Iron Man");
        add(aliases, "homem formiga", "Ant-Man");
        add(aliases, "homem de gelo", "Iceman");
        add(aliases, "homen aranha", "Spider-Man");
        add(aliases, "homen de ferro", "Iron Man");
        add(aliases, "aranha", "Spider-Man");
        add(aliases, "ferro", "Iron Man");
        add(aliases, "miles morales", "Miles Morales");
        add(aliases, "capitao america", "Captain America");
        add(aliases, "capita marvel", "Captain Marvel");
        add(aliases, "pantera negra", "Black Panther");
        add(aliases, "viuva negra", "Black Widow");
        add(aliases, "feiticeira escarlate", "Scarlet Witch");
        add(aliases, "gaviao arqueiro", "Hawkeye");
        add(aliases, "doutor estranho", "Doctor Strange");
        add(aliases, "doutor destino", "Doctor Doom");
        add(aliases, "demolidor", "Daredevil");
        add(aliases, "justiceiro", "Punisher");
        add(aliases, "senhor fantastico", "Mister Fantastic");
        add(aliases, "mulher invisivel", "Invisible Woman");
        add(aliases, "tocha humana", "Human Torch");
        add(aliases, "surfista prateado", "Silver Surfer");
        add(aliases, "tempestade", "Storm");
        add(aliases, "vespa", "Wasp");
        add(aliases, "falcao", "Falcon");
        add(aliases, "cavaleiro da lua", "Moon Knight");
        add(aliases, "duende verde", "Green Goblin");
        add(aliases, "mistica", "Mystique");
        return Collections.unmodifiableMap(aliases);
    }

    private static void add(Map<String, String> aliases, String portugueseName, String apiName) {
        aliases.put(normalize(portugueseName), apiName);
    }
}
