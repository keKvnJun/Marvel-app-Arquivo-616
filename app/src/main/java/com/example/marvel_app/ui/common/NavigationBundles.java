package com.example.marvel_app.ui.common;

import android.os.Bundle;

import com.example.marvel_app.data.model.CharacterCardData;

public final class NavigationBundles {
    private NavigationBundles() {
    }

    public static Bundle forCharacter(CharacterCardData character) {
        Bundle bundle = new Bundle();
        bundle.putString("objectId", character.getApiObjectId());
        bundle.putLong("characterId", character.getId());
        bundle.putString("name", character.getName());
        bundle.putString("realName", character.getRealName());
        bundle.putString("imageUrl", character.getImageUrl());
        bundle.putInt("appearances", character.getIssueAppearances());
        return bundle;
    }
}
