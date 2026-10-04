package com.example.marvel_app.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

public class CharacterDto {

    private long id;
    private String name;

    @SerializedName("real_name")
    private String realName;

    private String deck;
    private String description;
    private String aliases;
    private String birth;
    private int gender;

    @SerializedName("api_detail_url")
    private String apiDetailUrl;

    @SerializedName("site_detail_url")
    private String siteDetailUrl;

    @SerializedName("count_of_issue_appearances")
    private int countOfIssueAppearances;

    private ComicImage image;
    private ResourceReference publisher;
    private ResourceReference origin;

    @SerializedName("first_appeared_in_issue")
    private ResourceReference firstAppearedInIssue;

    private List<ResourceReference> powers;
    private List<ResourceReference> teams;
    @SerializedName("character_friends")
    private List<ResourceReference> friends;

    @SerializedName("character_enemies")
    private List<ResourceReference> enemies;

    public long getId() {
        return id;
    }

    public String getName() {
        return valueOrEmpty(name);
    }

    public String getRealName() {
        return valueOrEmpty(realName);
    }

    public String getDeck() {
        return valueOrEmpty(deck);
    }

    public String getDescription() {
        return valueOrEmpty(description);
    }

    public String getAliases() {
        return valueOrEmpty(aliases);
    }

    public String getBirth() {
        return valueOrEmpty(birth);
    }

    public int getGender() {
        return gender;
    }

    public String getApiDetailUrl() {
        return valueOrEmpty(apiDetailUrl);
    }

    public String getSiteDetailUrl() {
        return valueOrEmpty(siteDetailUrl);
    }

    public int getCountOfIssueAppearances() {
        return countOfIssueAppearances;
    }

    public ComicImage getImage() {
        return image;
    }

    public ResourceReference getPublisher() {
        return publisher;
    }

    public ResourceReference getOrigin() {
        return origin;
    }

    public ResourceReference getFirstAppearedInIssue() {
        return firstAppearedInIssue;
    }

    public List<ResourceReference> getPowers() {
        return safeList(powers);
    }

    public List<ResourceReference> getTeams() {
        return safeList(teams);
    }

    public List<ResourceReference> getFriends() {
        return safeList(friends);
    }

    public List<ResourceReference> getEnemies() {
        return safeList(enemies);
    }

    public String getCardImageUrl() {
        return image == null ? "" : image.getCardUrl();
    }

    public String getDetailImageUrl() {
        return image == null ? "" : image.getDetailUrl();
    }

    public String getApiObjectId() {
        if (apiDetailUrl == null || apiDetailUrl.trim().isEmpty()) {
            return "4005-" + id;
        }
        String value = apiDetailUrl;
        if (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        int separator = value.lastIndexOf('/');
        return separator >= 0 ? value.substring(separator + 1) : "4005-" + id;
    }

    public boolean isMarvelCharacter() {
        if (publisher == null) {
            return false;
        }
        String publisherName = publisher.getName();
        return "Marvel".equalsIgnoreCase(publisherName)
                || "Marvel Comics".equalsIgnoreCase(publisherName);
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private List<ResourceReference> safeList(List<ResourceReference> values) {
        return values == null ? Collections.emptyList() : values;
    }
}
