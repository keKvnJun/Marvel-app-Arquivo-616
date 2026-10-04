package com.example.marvel_app.data.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;

import org.junit.Test;

public class CharacterDtoTest {

    @Test
    public void parsesComicVineCardDataAndNormalizesImageUrl() {
        String json = "{"
                + "\"id\":1443,"
                + "\"name\":\"Spider-Man\","
                + "\"real_name\":\"Peter Parker\","
                + "\"api_detail_url\":\"https://comicvine.gamespot.com/api/character/4005-1443/\","
                + "\"publisher\":{\"id\":31,\"name\":\"Marvel Comics\"},"
                + "\"image\":{\"medium_url\":\"http://example.com/spider-man.jpg\"}"
                + "}";

        CharacterDto character = new Gson().fromJson(json, CharacterDto.class);

        assertEquals("4005-1443", character.getApiObjectId());
        assertEquals("https://example.com/spider-man.jpg", character.getCardImageUrl());
        assertTrue(character.isMarvelCharacter());
    }
}
