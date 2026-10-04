package com.example.marvel_app.data.model;

import java.util.Collections;
import java.util.List;

public final class PowerDto {
    private long id;
    private String name;
    private List<ResourceReference> characters;

    public long getId() { return id; }
    public String getName() { return name == null ? "" : name; }
    public List<ResourceReference> getCharacters() {
        return characters == null ? Collections.emptyList() : characters;
    }
}
