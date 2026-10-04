package com.example.marvel_app.data.model;

import com.google.gson.annotations.SerializedName;

public class ResourceReference {

    private long id;
    private String name;

    @SerializedName("api_detail_url")
    private String apiDetailUrl;

    @SerializedName("site_detail_url")
    private String siteDetailUrl;

    public long getId() {
        return id;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public String getApiDetailUrl() {
        return apiDetailUrl == null ? "" : apiDetailUrl;
    }

    public String getSiteDetailUrl() {
        return siteDetailUrl == null ? "" : siteDetailUrl;
    }
}
