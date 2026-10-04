package com.example.marvel_app.data.api;

import com.example.marvel_app.BuildConfig;

public final class ApiConfiguration {

    public static final String COMIC_VINE_BASE_URL = "https://comicvine.gamespot.com/api/";
    public static final String USER_AGENT = "MarvelApp-SchoolProject/1.0 Android";

    private ApiConfiguration() {
    }

    public static String getComicVineApiKey() {
        return BuildConfig.COMIC_VINE_API_KEY.trim();
    }

    public static String getGeminiApiKey() {
        return BuildConfig.GEMINI_API_KEY.trim();
    }

    public static boolean hasComicVineApiKey() {
        return !getComicVineApiKey().isEmpty();
    }

    public static boolean hasGeminiApiKey() {
        return !getGeminiApiKey().isEmpty();
    }
}
