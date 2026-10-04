package com.example.marvel_app.data.api;

import com.example.marvel_app.data.model.ApiResponse;
import com.example.marvel_app.data.model.CharacterDto;
import com.example.marvel_app.data.model.PowerDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ComicVineService {

    @GET("characters/")
    Call<ApiResponse<List<CharacterDto>>> getCharacters(
            @Query("field_list") String fieldList,
            @Query("limit") int limit,
            @Query("offset") int offset,
            @Query("sort") String sort
    );

    @GET("search/")
    Call<ApiResponse<List<CharacterDto>>> searchCharacters(
            @Query("query") String query,
            @Query("resources") String resources,
            @Query("field_list") String fieldList,
            @Query("limit") int limit,
            @Query("offset") int offset
    );

    @GET("character/{objectId}/")
    Call<ApiResponse<CharacterDto>> getCharacter(
            @Path("objectId") String objectId,
            @Query("field_list") String fieldList
    );

    @GET("search/")
    Call<ApiResponse<List<PowerDto>>> searchPowers(
            @Query("query") String query,
            @Query("resources") String resources,
            @Query("field_list") String fieldList,
            @Query("limit") int limit,
            @Query("offset") int offset
    );
}
