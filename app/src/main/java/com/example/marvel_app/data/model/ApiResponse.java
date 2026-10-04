package com.example.marvel_app.data.model;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {

    private String error;
    private int limit;
    private int offset;

    @SerializedName("number_of_page_results")
    private int numberOfPageResults;

    @SerializedName("number_of_total_results")
    private int numberOfTotalResults;

    @SerializedName("status_code")
    private int statusCode;

    private T results;

    public String getError() {
        return error;
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    public int getNumberOfPageResults() {
        return numberOfPageResults;
    }

    public int getNumberOfTotalResults() {
        return numberOfTotalResults;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public T getResults() {
        return results;
    }

    public boolean isSuccessful() {
        return statusCode == 1 && results != null;
    }
}
