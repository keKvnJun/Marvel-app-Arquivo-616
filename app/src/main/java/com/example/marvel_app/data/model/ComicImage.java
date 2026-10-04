package com.example.marvel_app.data.model;

import com.google.gson.annotations.SerializedName;

public class ComicImage {

    @SerializedName("icon_url")
    private String iconUrl;

    @SerializedName("medium_url")
    private String mediumUrl;

    @SerializedName("screen_url")
    private String screenUrl;

    @SerializedName("screen_large_url")
    private String screenLargeUrl;

    @SerializedName("small_url")
    private String smallUrl;

    @SerializedName("super_url")
    private String superUrl;

    @SerializedName("thumb_url")
    private String thumbUrl;

    @SerializedName("tiny_url")
    private String tinyUrl;

    @SerializedName("original_url")
    private String originalUrl;

    public String getIconUrl() {
        return secureUrl(iconUrl);
    }

    public String getMediumUrl() {
        return secureUrl(mediumUrl);
    }

    public String getScreenUrl() {
        return secureUrl(screenUrl);
    }

    public String getScreenLargeUrl() {
        return secureUrl(screenLargeUrl);
    }

    public String getSmallUrl() {
        return secureUrl(smallUrl);
    }

    public String getSuperUrl() {
        return secureUrl(superUrl);
    }

    public String getThumbUrl() {
        return secureUrl(thumbUrl);
    }

    public String getTinyUrl() {
        return secureUrl(tinyUrl);
    }

    public String getOriginalUrl() {
        return secureUrl(originalUrl);
    }

    public String getCardUrl() {
        String url = firstNotBlank(mediumUrl, screenUrl, smallUrl, thumbUrl, iconUrl);
        return secureUrl(url);
    }

    public String getDetailUrl() {
        String url = firstNotBlank(screenLargeUrl, superUrl, originalUrl, screenUrl, mediumUrl);
        return secureUrl(url);
    }

    private String firstNotBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return "";
    }

    private String secureUrl(String value) {
        if (value == null) {
            return "";
        }
        if (value.startsWith("http://")) {
            return "https://" + value.substring("http://".length());
        }
        return value;
    }
}
