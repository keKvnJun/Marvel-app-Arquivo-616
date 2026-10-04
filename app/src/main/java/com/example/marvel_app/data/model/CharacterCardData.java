package com.example.marvel_app.data.model;

import java.util.Objects;

public class CharacterCardData {

    private long id;
    private String apiObjectId;
    private String name;
    private String realName;
    private String imageUrl;
    private String publisherName;
    private int issueAppearances;

    public CharacterCardData() {
    }

    public CharacterCardData(
            long id,
            String apiObjectId,
            String name,
            String realName,
            String imageUrl,
            String publisherName,
            int issueAppearances
    ) {
        this.id = id;
        this.apiObjectId = valueOrEmpty(apiObjectId);
        this.name = valueOrEmpty(name);
        this.realName = valueOrEmpty(realName);
        this.imageUrl = valueOrEmpty(imageUrl);
        this.publisherName = valueOrEmpty(publisherName);
        this.issueAppearances = issueAppearances;
    }

    public static CharacterCardData from(CharacterDto character) {
        String publisher = character.getPublisher() == null
                ? ""
                : character.getPublisher().getName();
        return new CharacterCardData(
                character.getId(),
                character.getApiObjectId(),
                character.getName(),
                character.getRealName(),
                character.getCardImageUrl(),
                publisher,
                character.getCountOfIssueAppearances()
        );
    }

    public long getId() {
        return id;
    }

    public String getApiObjectId() {
        return valueOrEmpty(apiObjectId);
    }

    public String getName() {
        return valueOrEmpty(name);
    }

    public String getRealName() {
        return valueOrEmpty(realName);
    }

    public String getImageUrl() {
        return valueOrEmpty(imageUrl);
    }

    public String getPublisherName() {
        return valueOrEmpty(publisherName);
    }

    public int getIssueAppearances() {
        return issueAppearances;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CharacterCardData)) {
            return false;
        }
        CharacterCardData that = (CharacterCardData) other;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
