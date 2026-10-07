package com.example.marvel_app.domain.cards;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.marvel_app.data.model.CharacterDto;
import com.google.gson.Gson;

import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CardCatalogImageTest {
    @Test
    public void catalogUsesUniqueComicVineIdsAndObjectIds() {
        Set<Long> ids = new HashSet<>();
        for (CollectibleCard card : CardCatalog.all()) {
            long id = card.getCharacter().getId();
            assertTrue(id > 0);
            assertTrue(ids.add(id));
            assertEquals("4005-" + id, card.getCharacter().getApiObjectId());
        }
    }

    @Test
    public void hydrateAddsRemoteImageWithoutReplacingCuratedIdentity() {
        String json = "{"
                + "\"id\":1443,"
                + "\"name\":\"Spider-Man\","
                + "\"real_name\":\"Peter Benjamin Parker\","
                + "\"api_detail_url\":\"https://comicvine.gamespot.com/api/character/4005-1443/\","
                + "\"publisher\":{\"name\":\"Marvel\"},"
                + "\"image\":{\"medium_url\":\"https://example.com/spider.jpg\"}"
                + "}";
        CharacterDto remote = new Gson().fromJson(json, CharacterDto.class);

        List<CollectibleCard> hydrated = CardCatalog.hydrate(
                Collections.singletonList(CardCatalog.find("spider")),
                Collections.singletonList(remote)
        );

        assertEquals("Peter Parker", hydrated.get(0).getCharacter().getRealName());
        assertFalse(hydrated.get(0).getCharacter().getImageUrl().isEmpty());
    }
}
