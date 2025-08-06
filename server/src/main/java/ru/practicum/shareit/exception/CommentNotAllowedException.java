package ru.practicum.shareit.exception;

public class CommentNotAllowedException extends RuntimeException {
    public CommentNotAllowedException(String message) {
        super(message);
    }
}