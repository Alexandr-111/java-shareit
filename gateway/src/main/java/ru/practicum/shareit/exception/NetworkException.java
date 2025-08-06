package ru.practicum.shareit.exception;

import lombok.Getter;

@Getter
public class NetworkException extends RuntimeException {
    public NetworkException(String message) {
        super(message);
    }
}