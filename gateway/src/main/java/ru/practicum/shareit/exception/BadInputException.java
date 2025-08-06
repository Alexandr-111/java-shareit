package ru.practicum.shareit.exception;

public class BadInputException extends RuntimeException {
    public BadInputException(String message) {
        super(message);
    }
}