package ru.practicum.shareit.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class ServerResponseException extends RuntimeException {
    private final HttpStatusCode status;

    public ServerResponseException(String message, HttpStatusCode status) {
        super(message);
        this. status =  status;
    }
}