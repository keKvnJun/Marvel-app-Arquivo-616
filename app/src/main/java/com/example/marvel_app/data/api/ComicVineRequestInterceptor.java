package com.example.marvel_app.data.api;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

final class ComicVineRequestInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        HttpUrl originalUrl = originalRequest.url();
        HttpUrl.Builder urlBuilder = originalUrl.newBuilder();

        if (originalUrl.queryParameter("format") == null) {
            urlBuilder.addQueryParameter("format", "json");
        }
        if (originalUrl.queryParameter("api_key") == null
                && ApiConfiguration.hasComicVineApiKey()) {
            urlBuilder.addQueryParameter("api_key", ApiConfiguration.getComicVineApiKey());
        }

        Request request = originalRequest.newBuilder()
                .url(urlBuilder.build())
                .header("Accept", "application/json")
                .header("User-Agent", ApiConfiguration.USER_AGENT)
                .build();
        return chain.proceed(request);
    }
}
