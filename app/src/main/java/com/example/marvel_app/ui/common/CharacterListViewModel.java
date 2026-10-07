package com.example.marvel_app.ui.common;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.marvel_app.data.model.CharacterCardData;
import com.example.marvel_app.data.model.CharacterDto;
import com.example.marvel_app.data.repository.CharacterRepository;
import com.example.marvel_app.data.repository.RepositoryResult;
import com.example.marvel_app.domain.search.CharacterSearchQueryExpander;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;

public final class CharacterListViewModel extends AndroidViewModel {
    public static final class State {
        public final List<CharacterCardData> items;
        public final boolean loading;
        public final String message;
        public final boolean configured;

        State(List<CharacterCardData> items, boolean loading, String message, boolean configured) {
            this.items = Collections.unmodifiableList(new ArrayList<>(items));
            this.loading = loading;
            this.message = message;
            this.configured = configured;
        }
    }

    private final CharacterRepository repository;
    private final MutableLiveData<State> state = new MutableLiveData<>(
            new State(Collections.emptyList(), false, "", true)
    );
    private final List<CharacterCardData> accumulated = new ArrayList<>();
    private Call<?> activeCall;
    private CharacterRepository.SmartSearchRequest activeSmartSearch;
    private String currentQuery = "";
    private int page = 1;
    private boolean hasMore = true;
    private int requestVersion;

    public CharacterListViewModel(@NonNull Application application) {
        super(application);
        repository = CharacterRepository.getInstance(application);
    }

    public LiveData<State> state() {
        return state;
    }

    public void loadFeatured() {
        cancel();
        int version = ++requestVersion;
        state.setValue(new State(accumulated, true, "", true));
        activeCall = repository.loadCharacters(0, result -> {
            if (version == requestVersion) consumeReplacing(result);
        });
    }

    public void search(String query) {
        requestVersion++;
        currentQuery = query == null ? "" : query.trim();
        page = 1;
        hasMore = true;
        accumulated.clear();
        if (currentQuery.isEmpty()) {
            loadFeatured();
            return;
        }
        requestSearch(false);
    }

    public void searchByPowers(List<String> powers) {
        cancel();
        int version = ++requestVersion;
        accumulated.clear();
        currentQuery = "";
        hasMore = false;
        state.setValue(new State(accumulated, true, "", true));
        activeSmartSearch = repository.searchCharactersByPowers(powers, result -> {
            if (version != requestVersion) return;
            if (result.isSuccess()) {
                accumulated.clear();
                accumulated.addAll(result.getData());
                state.postValue(new State(accumulated, false, "", true));
            } else {
                state.postValue(new State(accumulated, false, result.getMessage(),
                        result.getStatus() != RepositoryResult.Status.NOT_CONFIGURED));
            }
        });
    }

    public void loadNext() {
        State current = state.getValue();
        if (current == null || current.loading || currentQuery.isEmpty() || !hasMore) {
            return;
        }
        requestSearch(true);
    }

    private void requestSearch(boolean append) {
        cancel();
        int version = requestVersion;
        state.setValue(new State(accumulated, true, "", true));
        activeSmartSearch = repository.searchCharactersSmart(currentQuery, page, result -> {
            if (version == requestVersion) consumeSmartSearch(result, append);
        });
    }

    private void consumeReplacing(RepositoryResult<List<CharacterDto>> result) {
        accumulated.clear();
        if (result.isSuccess()) {
            consumeCharacters(result.getData(), false, false);
        } else {
            consumeFailure(result);
        }
    }

    private void consumeSmartSearch(
            RepositoryResult<CharacterRepository.SmartSearchPage> result,
            boolean append
    ) {
        if (result.isSuccess()) {
            CharacterRepository.SmartSearchPage searchPage = result.getData();
            consumeCharacters(searchPage.getCharacters(), append, searchPage.hasMore());
        } else {
            consumeFailure(result);
        }
    }

    private void consumeCharacters(
            List<CharacterDto> characters,
            boolean append,
            boolean sourceHasMore
    ) {
        int previousSize = accumulated.size();
        if (!append) {
            accumulated.clear();
            previousSize = 0;
        }
        for (CharacterDto character : characters) {
            CharacterCardData card = CharacterCardData.from(character);
            boolean exists = accumulated.stream().anyMatch(item -> item.getId() == card.getId());
            if (!exists) {
                accumulated.add(card);
            }
        }
        hasMore = sourceHasMore && (!append || accumulated.size() > previousSize);
        state.postValue(new State(new ArrayList<>(accumulated), false, "", true));
        page++;
    }

    private void consumeFailure(RepositoryResult<?> result) {
        hasMore = false;
        boolean configured = result.getStatus() != RepositoryResult.Status.NOT_CONFIGURED;
        List<CharacterCardData> fallback = accumulated.isEmpty()
                ? offlineSuggestions(currentQuery)
                : accumulated;
        state.postValue(new State(new ArrayList<>(fallback), false, result.getMessage(), configured));
    }

    private List<CharacterCardData> offlineSuggestions(String query) {
        if (query == null || query.trim().isEmpty()) {
            return SampleContent.characters();
        }
        List<CharacterCardData> matches = new ArrayList<>();
        for (CharacterCardData character : SampleContent.characters()) {
            if (CharacterSearchQueryExpander.matchesAnyExpansion(query, character.getName())) {
                matches.add(character);
            }
        }
        return matches;
    }

    private void cancel() {
        if (activeCall != null) {
            activeCall.cancel();
            activeCall = null;
        }
        if (activeSmartSearch != null) {
            activeSmartSearch.cancel();
            activeSmartSearch = null;
        }
    }

    @Override
    protected void onCleared() {
        cancel();
    }
}
