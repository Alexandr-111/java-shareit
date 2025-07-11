package ru.practicum.shareit.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class AppExceptionHandler {
    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<Response> handleDataNotFoundException(DataNotFoundException ex) {
        log.warn("Выброшено исключение DataNotFoundException: {}", ex.getMessage());
        Response response = new Response("Не найден ресурс", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadInputException.class)
    public ResponseEntity<Response> handleBadInputException(BadInputException ex) {
        log.warn("Выброшено исключение BadInputException: {}", ex.getMessage());
        Response response = new Response("Некорректный запрос", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Response> handleConflictException(ConflictException ex) {
        log.warn("Выброшено исключение ConflictException: {}", ex.getMessage());
        Response response = new Response("Нарушение уникальности", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            log.error("Ошибка валидации поля {}: {}", error.getField(), error.getDefaultMessage());
            errors.put(error.getField(), error.getDefaultMessage());
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            log.error("Ошибка валидации параметра {}: {}", fieldName, message);
            errors.put(fieldName, message);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<Response> handleAllExceptions(Throwable ex) {
        log.error("Внутренняя ошибка сервера: ", ex);
        Response response = new Response("Ошибка сервера",
                "Операция не выполнена из-за ошибки на сервере");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}