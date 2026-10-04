package com.example.marvel_app.data.repository;

public interface RepositoryCallback<T> {
    void onResult(RepositoryResult<T> result);
}
