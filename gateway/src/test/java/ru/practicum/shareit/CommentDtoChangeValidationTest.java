package ru.practicum.shareit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.comment.dto.CommentDtoChange;
import ru.practicum.shareit.validate.OnCreate;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CommentDtoChangeValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }


    @Test
    void validCreateRequest_shouldPassValidation() {
        CommentDtoChange dto = CommentDtoChange.builder()
                .text("Valid comment text")
                .build();

        Set<ConstraintViolation<CommentDtoChange>> violations =
                validator.validate(dto, OnCreate.class);

        assertThat(violations).isEmpty();
    }

    @Test
    void nullTextOnCreate_shouldFailValidation() {
        CommentDtoChange dto = CommentDtoChange.builder()
                .text(null)
                .build();

        assertSingleViolation(dto);
    }

    @Test
    void emptyTextOnCreate_shouldFailValidation() {
        CommentDtoChange dto = CommentDtoChange.builder()
                .text("")
                .build();

        assertSingleViolation(dto);
    }

    @Test
    void blankTextOnCreate_shouldFailValidation() {
        CommentDtoChange dto = CommentDtoChange.builder()
                .text("      ")
                .build();

        assertSingleViolation(dto);
    }

    // Вспомогательный метод
    private void assertSingleViolation(CommentDtoChange dto) {
        Set<ConstraintViolation<CommentDtoChange>> violations =
                validator.validate(dto, OnCreate.class);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Отзыв не должен быть пустым");
    }
}