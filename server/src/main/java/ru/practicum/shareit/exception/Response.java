package ru.practicum.shareit.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class Response {
    private final String error;
    private final String description;
    private final Map<String, String> errors;

    public Response(String error, String description) {
        this.error = error;
        this.description = description;
        this.errors = null;
    }

    public Response(String error, Map<String, String> errors) {
        this.error = error;
        this.description = null;
        this.errors = errors;
    }
}