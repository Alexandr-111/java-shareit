package ru.practicum.shareit.exception;

import org.springframework.http.HttpStatus;

public class CustomApiException extends RuntimeException {
    private final HttpStatus status;

    public CustomApiException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getHttpStatus() {
        return status;
    }
}