package com.example.marvel_app.data.repository;

import android.content.Context;
import android.util.LruCache;

import com.example.marvel_app.data.api.ApiConfiguration;
import com.example.marvel_app.data.api.ComicVineApiClient;
import com.example.marvel_app.data.api.ComicVineService;
import com.example.marvel_app.data.model.ApiResponse;
import com.example.marvel_app.data.model.CharacterDto;
import com.example.marvel_app.data.model.CharacterCardData;
import com.example.marvel_app.data.model.PowerDto;
import com.example.marvel_app.data.model.ResourceReference;
import com.example.marvel_app.domain.search.CharacterSearchQueryExpander;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class CharacterRepository {

    public static final class SmartSearchPage {
        private final List<CharacterDto> characters;
        private final boolean hasMore;

        private SmartSearchPage(List<CharacterDto> characters, boolean hasMore) {
            this.characters = Collections.unmodifiableList(new ArrayList<>(characters));
            this.hasMore = hasMore;
        }

        public List<CharacterDto> getCharacters() {
            return characters;
        }

        public boolean hasMore() {
            return hasMore;
        }
    }

    public static final class SmartSearchRequest {
        private final List<Call<?>> calls = new ArrayList<>();
        private boolean canceled;

        private synchronized void add(Call<?> call) {
            if (canceled) {
                call.cancel();
            } else {
                calls.add(call);
            }
        }

        public synchronized void cancel() {
            canceled = true;
            for (Call<?> call : calls) {
                call.cancel();
            }
            calls.clear();
        }

        public synchronized boolean isCanceled() {
            return canceled;
        }
    }

    public static final int PAGE_SIZE = 20;
    public static final int SEARCH_PAGE_SIZE = 10;

    private static final String CARD_FIELDS =
            "id,name,real_name,deck,api_detail_url,site_detail_url," +
            "count_of_issue_appearances,image,publisher";

    private static final String DETAIL_FIELDS =
            "id,name,real_name,deck,description,aliases,birth,gender," +
            "api_detail_url,site_detail_url,count_of_issue_appearances,image,publisher," +
            "origin,first_appeared_in_issue,powers,teams,character_friends,character_enemies";

    private static final String FEATURED_MARVEL_IDS =
            "1443|1455|79420|1477|1472|2268|2267|1440|1442|3200";

    private static volatile CharacterRepository instance;

    private final ComicVineService service;
    private final LruCache<String, CharacterDto> detailCache = new LruCache<>(30);

    private CharacterRepository(Context context) {
        service = ComicVineApiClient.getService(context);
    }

    public static CharacterRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (CharacterRepository.class) {
                if (instance == null) {
                    instance = new CharacterRepository(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public boolean isConfigured() {
        return ApiConfiguration.hasComicVineApiKey();
    }

    public Call<ApiResponse<List<CharacterDto>>> loadCharacters(
            int offset,
            RepositoryCallback<List<CharacterDto>> callback
    ) {
        if (!isConfigured()) {
            callback.onResult(RepositoryResult.notConfigured(
                    "Adicione COMIC_VINE_API_KEY ao arquivo local.properties."
            ));
            return null;
        }

        Call<ApiResponse<List<CharacterDto>>> call = service.getCharacters(
                CARD_FIELDS,
                PAGE_SIZE,
                Math.max(0, offset),
                "count_of_issue_appearances:desc",
                "id:" + FEATURED_MARVEL_IDS
        );
        enqueueCharacterList(call, callback);
        return call;
    }

    public Call<ApiResponse<List<CharacterDto>>> loadCharactersByIds(
            List<Long> characterIds,
            RepositoryCallback<List<CharacterDto>> callback
    ) {
        if (characterIds == null || characterIds.isEmpty()) {
            callback.onResult(RepositoryResult.success(Collections.emptyList()));
            return null;
        }
        if (!isConfigured()) {
            callback.onResult(RepositoryResult.notConfigured(
                    "Adicione COMIC_VINE_API_KEY ao arquivo local.properties."
            ));
            return null;
        }

        LinkedHashSet<Long> uniqueIds = new LinkedHashSet<>();
        for (Long id : characterIds) {
            if (id != null && id > 0) {
                uniqueIds.add(id);
            }
        }
        if (uniqueIds.isEmpty()) {
            callback.onResult(RepositoryResult.success(Collections.emptyList()));
            return null;
        }

        StringBuilder filter = new StringBuilder("id:");
        for (Long id : uniqueIds) {
            if (filter.length() > 3) {
                filter.append('|');
            }
            filter.append(id);
        }
        Call<ApiResponse<List<CharacterDto>>> call = service.getCharacters(
                CARD_FIELDS,
                Math.min(uniqueIds.size(), 100),
                0,
                null,
                filter.toString()
        );
        enqueueCharacterList(call, callback);
        return call;
    }

    public Call<ApiResponse<List<CharacterDto>>> searchCharacters(
            String query,
            int page,
            RepositoryCallback<List<CharacterDto>> callback
    ) {
        String cleanQuery = query == null ? "" : query.trim();
        if (cleanQuery.isEmpty()) {
            callback.onResult(RepositoryResult.success(Collections.emptyList()));
            return null;
        }
        if (!isConfigured()) {
            callback.onResult(RepositoryResult.notConfigured(
                    "Adicione COMIC_VINE_API_KEY ao arquivo local.properties."
            ));
            return null;
        }

        Call<ApiResponse<List<CharacterDto>>> call = service.searchCharacters(
                cleanQuery,
                "character",
                CARD_FIELDS,
                SEARCH_PAGE_SIZE,
                (Math.max(1, page) - 1) * SEARCH_PAGE_SIZE
        );
        enqueueCharacterList(call, callback);
        return call;
    }

    public SmartSearchRequest searchCharactersSmart(
            String userQuery,
            int page,
            RepositoryCallback<SmartSearchPage> callback
    ) {
        SmartSearchRequest request = new SmartSearchRequest();
        List<String> expandedQueries = CharacterSearchQueryExpander.expand(userQuery);
        if (expandedQueries.isEmpty()) {
            callback.onResult(RepositoryResult.success(
                    new SmartSearchPage(Collections.emptyList(), false)
            ));
            return request;
        }
        if (!isConfigured()) {
            callback.onResult(RepositoryResult.notConfigured(
                    "Adicione COMIC_VINE_API_KEY ao arquivo local.properties."
            ));
            return request;
        }

        List<List<CharacterDto>> resultsByQuery = new ArrayList<>();
        for (int index = 0; index < expandedQueries.size(); index++) {
            resultsByQuery.add(null);
        }
        int[] remaining = {expandedQueries.size()};
        int[] successfulQueries = {0};
        boolean[] hasMoreByQuery = new boolean[expandedQueries.size()];
        String[] lastError = {"Não foi possível consultar os arquivos da Comic Vine."};
        int safePage = Math.max(1, page);

        for (int index = 0; index < expandedQueries.size(); index++) {
            final int resultIndex = index;
            Call<ApiResponse<List<CharacterDto>>> call = service.searchCharacters(
                    expandedQueries.get(index),
                    "character",
                    CARD_FIELDS,
                    SEARCH_PAGE_SIZE,
                    (safePage - 1) * SEARCH_PAGE_SIZE
            );
            request.add(call);
            call.enqueue(new Callback<ApiResponse<List<CharacterDto>>>() {
                @Override
                public void onResponse(
                        Call<ApiResponse<List<CharacterDto>>> currentCall,
                        Response<ApiResponse<List<CharacterDto>>> response
                ) {
                    ApiResponse<List<CharacterDto>> body = response.body();
                    synchronized (request) {
                        if (request.isCanceled()) {
                            return;
                        }
                        if (response.isSuccessful() && body != null && body.isSuccessful()) {
                            resultsByQuery.set(resultIndex, mergeWithoutDuplicates(body.getResults()));
                            hasMoreByQuery[resultIndex] = body.getOffset()
                                    + body.getNumberOfPageResults()
                                    < body.getNumberOfTotalResults();
                            successfulQueries[0]++;
                        } else {
                            lastError[0] = apiErrorMessage(response, body);
                        }
                        remaining[0]--;
                        if (remaining[0] == 0) {
                            finishSmartSearch(
                                    resultsByQuery,
                                    hasMoreByQuery,
                                    successfulQueries[0],
                                    lastError[0],
                                    callback
                            );
                        }
                    }
                }

                @Override
                public void onFailure(
                        Call<ApiResponse<List<CharacterDto>>> currentCall,
                        Throwable cause
                ) {
                    synchronized (request) {
                        if (request.isCanceled()) {
                            return;
                        }
                        lastError[0] = "Não foi possível consultar os arquivos da Comic Vine.";
                        remaining[0]--;
                        if (remaining[0] == 0) {
                            finishSmartSearch(
                                    resultsByQuery,
                                    hasMoreByQuery,
                                    successfulQueries[0],
                                    lastError[0],
                                    callback
                            );
                        }
                    }
                }
            });
        }
        return request;
    }

    private void finishSmartSearch(
            List<List<CharacterDto>> resultsByQuery,
            boolean[] hasMoreByQuery,
            int successfulQueries,
            String errorMessage,
            RepositoryCallback<SmartSearchPage> callback
    ) {
        if (successfulQueries == 0) {
            callback.onResult(RepositoryResult.error(errorMessage, null));
            return;
        }
        Map<Long, CharacterDto> unique = new LinkedHashMap<>();
        for (List<CharacterDto> queryResults : resultsByQuery) {
            if (queryResults == null) {
                continue;
            }
            for (CharacterDto character : queryResults) {
                unique.putIfAbsent(character.getId(), character);
            }
        }
        boolean hasMore = false;
        for (boolean queryHasMore : hasMoreByQuery) {
            hasMore |= queryHasMore;
        }
        callback.onResult(RepositoryResult.success(
                new SmartSearchPage(new ArrayList<>(unique.values()), hasMore)
        ));
    }

    public Call<ApiResponse<CharacterDto>> loadCharacter(
            String objectId,
            RepositoryCallback<CharacterDto> callback
    ) {
        String cleanObjectId = objectId == null ? "" : objectId.trim();
        if (cleanObjectId.isEmpty()) {
            callback.onResult(RepositoryResult.error("Identificador de personagem inválido.", null));
            return null;
        }

        CharacterDto cached = detailCache.get(cleanObjectId);
        if (cached != null) {
            callback.onResult(RepositoryResult.success(cached));
            return null;
        }

        if (!isConfigured()) {
            callback.onResult(RepositoryResult.notConfigured(
                    "Adicione COMIC_VINE_API_KEY ao arquivo local.properties."
            ));
            return null;
        }

        Call<ApiResponse<CharacterDto>> call = service.getCharacter(cleanObjectId, DETAIL_FIELDS);
        call.enqueue(new Callback<ApiResponse<CharacterDto>>() {
            @Override
            public void onResponse(
                    Call<ApiResponse<CharacterDto>> request,
                    Response<ApiResponse<CharacterDto>> response
            ) {
                ApiResponse<CharacterDto> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccessful()) {
                    callback.onResult(RepositoryResult.error(apiErrorMessage(response, body), null));
                    return;
                }
                CharacterDto character = body.getResults();
                detailCache.put(cleanObjectId, character);
                callback.onResult(RepositoryResult.success(character));
            }

            @Override
            public void onFailure(Call<ApiResponse<CharacterDto>> request, Throwable cause) {
                if (!request.isCanceled()) {
                    callback.onResult(RepositoryResult.error(
                            "Não foi possível consultar o dossiê do personagem.",
                            cause
                    ));
                }
            }
        });
        return call;
    }

    public SmartSearchRequest searchCharactersByPowers(
            List<String> powerNames,
            RepositoryCallback<List<CharacterCardData>> callback
    ) {
        SmartSearchRequest request = new SmartSearchRequest();
        if (!isConfigured()) {
            callback.onResult(RepositoryResult.notConfigured(
                    "Adicione COMIC_VINE_API_KEY ao arquivo local.properties."
            ));
            return request;
        }
        List<String> cleanNames = new ArrayList<>();
        for (String name : powerNames) {
            if (name != null && !name.trim().isEmpty()) cleanNames.add(name.trim());
        }
        if (cleanNames.isEmpty()) {
            callback.onResult(RepositoryResult.success(Collections.emptyList()));
            return request;
        }
        loadPowerCharacters(cleanNames, 0, new ArrayList<>(), request, callback);
        return request;
    }

    private void loadPowerCharacters(
            List<String> names,
            int index,
            List<Map<Long, ResourceReference>> groups,
            SmartSearchRequest smartRequest,
            RepositoryCallback<List<CharacterCardData>> callback
    ) {
        Call<ApiResponse<List<PowerDto>>> call = service.searchPowers(
                names.get(index), "power", "id,name,characters", 10, 0);
        smartRequest.add(call);
        call.enqueue(new Callback<ApiResponse<List<PowerDto>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PowerDto>>> request,
                                   Response<ApiResponse<List<PowerDto>>> response) {
                if (smartRequest.isCanceled()) return;
                ApiResponse<List<PowerDto>> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccessful()) {
                    callback.onResult(RepositoryResult.error(apiErrorMessage(response, body), null));
                    return;
                }
                PowerDto selected = selectPower(body.getResults(), names.get(index));
                if (selected == null) {
                    callback.onResult(RepositoryResult.success(Collections.emptyList()));
                    return;
                }
                Map<Long, ResourceReference> group = new LinkedHashMap<>();
                for (ResourceReference character : selected.getCharacters()) {
                    group.put(character.getId(), character);
                }
                groups.add(group);
                if (index + 1 < names.size()) {
                    loadPowerCharacters(names, index + 1, groups, smartRequest, callback);
                } else {
                    callback.onResult(RepositoryResult.success(intersectPowerGroups(groups)));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<PowerDto>>> request, Throwable cause) {
                if (!request.isCanceled() && !smartRequest.isCanceled()) callback.onResult(RepositoryResult.error(
                        "Não foi possível cruzar os poderes selecionados.", cause));
            }
        });
    }

    private PowerDto selectPower(List<PowerDto> powers, String requested) {
        if (powers == null || powers.isEmpty()) return null;
        for (PowerDto power : powers) {
            if (power.getName().equalsIgnoreCase(requested)) return power;
        }
        return powers.get(0);
    }

    private List<CharacterCardData> intersectPowerGroups(List<Map<Long, ResourceReference>> groups) {
        if (groups.isEmpty()) return Collections.emptyList();
        Set<Long> ids = new HashSet<>(groups.get(0).keySet());
        for (int i = 1; i < groups.size(); i++) ids.retainAll(groups.get(i).keySet());
        List<CharacterCardData> cards = new ArrayList<>();
        for (Long id : ids) {
            ResourceReference ref = groups.get(0).get(id);
            if (ref == null) continue;
            String url = ref.getApiDetailUrl();
            if (url.endsWith("/")) url = url.substring(0, url.length() - 1);
            String objectId = url.substring(url.lastIndexOf('/') + 1);
            cards.add(new CharacterCardData(id, objectId, ref.getName(),
                    "Poderes selecionados", "", "", 0));
            if (cards.size() == PAGE_SIZE) break;
        }
        return cards;
    }

    public List<CharacterDto> mergeWithoutDuplicates(List<CharacterDto> characters) {
        Map<Long, CharacterDto> uniqueCharacters = new LinkedHashMap<>();
        if (characters != null) {
            for (CharacterDto character : characters) {
                if (character != null && character.isMarvelCharacter()) {
                    uniqueCharacters.put(character.getId(), character);
                }
            }
        }
        return new ArrayList<>(uniqueCharacters.values());
    }

    private void enqueueCharacterList(
            Call<ApiResponse<List<CharacterDto>>> call,
            RepositoryCallback<List<CharacterDto>> callback
    ) {
        call.enqueue(new Callback<ApiResponse<List<CharacterDto>>>() {
            @Override
            public void onResponse(
                    Call<ApiResponse<List<CharacterDto>>> request,
                    Response<ApiResponse<List<CharacterDto>>> response
            ) {
                ApiResponse<List<CharacterDto>> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccessful()) {
                    callback.onResult(RepositoryResult.error(apiErrorMessage(response, body), null));
                    return;
                }
                callback.onResult(RepositoryResult.success(
                        mergeWithoutDuplicates(body.getResults())
                ));
            }

            @Override
            public void onFailure(Call<ApiResponse<List<CharacterDto>>> request, Throwable cause) {
                if (!request.isCanceled()) {
                    callback.onResult(RepositoryResult.error(
                            "Não foi possível consultar os arquivos da Comic Vine.",
                            cause
                    ));
                }
            }
        });
    }

    private String apiErrorMessage(Response<?> response, ApiResponse<?> body) {
        if (body != null && body.getError() != null && !body.getError().trim().isEmpty()) {
            return body.getError();
        }
        return String.format(
                Locale.US,
                "A Comic Vine respondeu com o código HTTP %d.",
                response.code()
        );
    }
}
