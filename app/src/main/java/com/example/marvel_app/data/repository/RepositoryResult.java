package com.example.marvel_app.data.repository;

public final class RepositoryResult<T> {

    public enum Status {
        SUCCESS,
        ERROR,
        NOT_CONFIGURED
    }

    private final Status status;
    private final T data;
    private final String message;
    private final Throwable cause;

    private RepositoryResult(Status status, T data, String message, Throwable cause) {
        this.status = status;
        this.data = data;
        this.message = message;
        this.cause = cause;
    }

    public static <T> RepositoryResult<T> success(T data) {
        return new RepositoryResult<>(Status.SUCCESS, data, "", null);
    }

    public static <T> RepositoryResult<T> error(String message, Throwable cause) {
        return new RepositoryResult<>(Status.ERROR, null, message, cause);
    }

    public static <T> RepositoryResult<T> notConfigured(String message) {
        return new RepositoryResult<>(Status.NOT_CONFIGURED, null, message, null);
    }

    public Status getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getCause() {
        return cause;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }
}
