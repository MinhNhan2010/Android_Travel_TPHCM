package com.example.nhom_15_dulichtphcm.data.model;

import java.util.function.Consumer;

/**
 * Pure Java generic result container replacing Kotlin Result<T>.
 */
public final class ResultBox<T> {
    private final T value;
    private final Throwable error;

    private ResultBox(T value, Throwable error) {
        this.value = value;
        this.error = error;
    }

    public static <T> ResultBox<T> success(T value) {
        return new ResultBox<>(value, null);
    }

    public static <T> ResultBox<T> failure(Throwable error) {
        return new ResultBox<>(null, error != null ? error : new RuntimeException("Unknown error"));
    }

    public static <T> ResultBox<T> failureMessage(String message) {
        return new ResultBox<>(null, new IllegalArgumentException(message));
    }

    public boolean isSuccess() {
        return error == null;
    }

    public boolean isFailure() {
        return error != null;
    }

    public T getOrNull() {
        return value;
    }

    public T getOrThrow() {
        if (error != null) {
            if (error instanceof RuntimeException) {
                throw (RuntimeException) error;
            }
            throw new RuntimeException(error);
        }
        return value;
    }

    public Throwable exceptionOrNull() {
        return error;
    }

    public Throwable getError() {
        return error;
    }

    public String getErrorMessage() {
        return error != null && error.getMessage() != null ? error.getMessage() : "";
    }

    public ResultBox<T> onSuccess(Consumer<T> action) {
        if (error == null && action != null) {
            action.accept(value);
        }
        return this;
    }

    public ResultBox<T> onFailure(Consumer<Throwable> action) {
        if (error != null && action != null) {
            action.accept(error);
        }
        return this;
    }
}
