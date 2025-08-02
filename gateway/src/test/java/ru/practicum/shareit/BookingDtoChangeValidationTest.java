package ru.practicum.shareit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDtoChange;
import ru.practicum.shareit.validate.OnCreate;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class BookingDtoChangeValidationTest {
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    // Валидные данные
    @Test
    void validCreateRequest_shouldPassValidation() {
        BookingDtoChange dto = BookingDtoChange.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(1L)
                .build();

        Set<ConstraintViolation<BookingDtoChange>> violations =
                validator.validate(dto, OnCreate.class);

        assertThat(violations).isEmpty();
    }

    @Test
    void nullStartOnCreate_shouldFailValidation() {
        BookingDtoChange dto = BookingDtoChange.builder()
                .start(null)
                .end(LocalDateTime.now().plusDays(1))
                .itemId(1L)
                .build();

        assertSingleViolation(dto, "Поле Начало аренды - обязательно");
    }

    @Test
    void nullEndOnCreate_shouldFailValidation() {
        BookingDtoChange dto = BookingDtoChange.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(null)
                .itemId(1L)
                .build();

        assertSingleViolation(dto, "Поле Конец аренды - обязательно");
    }

    @Test
    void nullItemIdOnCreate_shouldFailValidation() {
        BookingDtoChange dto = BookingDtoChange.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(null)
                .build();

        assertSingleViolation(dto, "ID вещи должен быть указан");
    }

    @Test
    void negativeItemIdOnCreate_shouldFailValidation() {
        BookingDtoChange dto = BookingDtoChange.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(-1L)
                .build();

        Set<ConstraintViolation<BookingDtoChange>> violations =
                validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("ID вещи должен быть положительным числом");
    }

    // Вспомогательный метод
    private void assertSingleViolation(BookingDtoChange dto, String expectedMessage) {
        Set<ConstraintViolation<BookingDtoChange>> violations =
                validator.validate(dto, OnCreate.class);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo(expectedMessage);
    }
}