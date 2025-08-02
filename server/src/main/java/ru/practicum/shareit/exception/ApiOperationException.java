package ru.practicum.shareit.exception;

import org.springframework.http.HttpStatusCode;

public class ApiOperationException extends RuntimeException {
    private final HttpStatusCode status;

    public ApiOperationException(String message, HttpStatusCode status) {
        super(message);
        this.status = status;
    }

    public HttpStatusCode getHttpStatusCode() {
        return status;
    }
}