package com.example.marvel_app.data.api;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ComicVineApiClient {

    private static final long CACHE_SIZE_BYTES = 10L * 1024L * 1024L;
    private static volatile ComicVineService service;

    private ComicVineApiClient() {
    }

    public static ComicVineService getService(Context context) {
        if (service == null) {
            synchronized (ComicVineApiClient.class) {
                if (service == null) {
                    service = createService(context.getApplicationContext());
                }
            }
        }
        return service;
    }

    private static ComicVineService createService(Context context) {
        File cacheDirectory = new File(context.getCacheDir(), "comic_vine_http");
        Cache cache = new Cache(cacheDirectory, CACHE_SIZE_BYTES);

        OkHttpClient client = new OkHttpClient.Builder()
                .cache(cache)
                .addInterceptor(new ComicVineRequestInterceptor())
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .callTimeout(25, TimeUnit.SECONDS)
                .build();

        Gson gson = new GsonBuilder().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiConfiguration.COMIC_VINE_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        return retrofit.create(ComicVineService.class);
    }
}
