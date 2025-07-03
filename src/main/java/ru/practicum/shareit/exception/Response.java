package ru.practicum.shareit.exception;

import lombok.Getter;

@Getter
public class Response {
    private final String name;
    private final String description;

    public Response(String name, String description) {
        this.name = name;
        this.description = description;
    }
}