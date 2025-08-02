package ru.practicum.shareit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.request.dto.ItemRequestDtoChange;
import ru.practicum.shareit.user.dto.UserDtoChange;
import ru.practicum.shareit.validate.OnCreate;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ItemRequestDtoChangeValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    // Валидные данные при создании
    @Test
    void validCreateRequest_shouldPassValidation() {
        ItemRequestDtoChange dto = new ItemRequestDtoChange(
                "Valid description",
                new UserDtoChange("User Name", "user@example.com"),
                LocalDateTime.now()
        );

        Set<ConstraintViolation<ItemRequestDtoChange>> violations =
                validator.validate(dto, OnCreate.class);

        assertThat(violations).isEmpty();
    }

    // Невалидные данные при создании
    @Test
    void nullDescriptionOnCreate_shouldFailValidation() {
        ItemRequestDtoChange dto = new ItemRequestDtoChange(
                null,
                new UserDtoChange("User Name", "user@example.com"),
                LocalDateTime.now()
        );

        Set<ConstraintViolation<ItemRequestDtoChange>> violations =
                validator.validate(dto, OnCreate.class);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Описание запроса обязательно");
    }

    @Test
    void blankDescriptionOnCreate_shouldFailValidation() {
        ItemRequestDtoChange dto = new ItemRequestDtoChange(
                "   ",
                new UserDtoChange("User Name", "user@example.com"),
                LocalDateTime.now()
        );

        Set<ConstraintViolation<ItemRequestDtoChange>> violations =
                validator.validate(dto, OnCreate.class);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Описание запроса обязательно");
    }

    @Test
    void emptyDescriptionOnCreate_shouldFailValidation() {
        ItemRequestDtoChange dto = new ItemRequestDtoChange(
                "",
                new UserDtoChange("User Name", "user@example.com"),
                LocalDateTime.now()
        );

        Set<ConstraintViolation<ItemRequestDtoChange>> violations =
                validator.validate(dto, OnCreate.class);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Описание запроса обязательно");
    }

    @Test
    void tooLongDescription_shouldFailValidation() {
        // Генерируем строку длиной 1001 символ
        String longDescription = "a".repeat(1001);

        ItemRequestDtoChange dto = new ItemRequestDtoChange(
                longDescription,
                new UserDtoChange("User Name", "user@example.com"),
                LocalDateTime.now()
        );

        Set<ConstraintViolation<ItemRequestDtoChange>> violations =
                validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Размер должен быть не больше 1000");
    }

    // Проверка без группы OnCreate
    @Test
    void withoutGroups_shouldIgnoreCreateConstraints() {
        ItemRequestDtoChange dto = new ItemRequestDtoChange(
                null, // Должно игнорироваться без OnCreate
                new UserDtoChange("User Name", "user@example.com"),
                LocalDateTime.now()
        );

        Set<ConstraintViolation<ItemRequestDtoChange>> violations =
                validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void exactly1000CharactersDescription_shouldPassValidation() {
        String validDescription = "a".repeat(1000);

        ItemRequestDtoChange dto = new ItemRequestDtoChange(
                validDescription,
                new UserDtoChange("User Name", "user@example.com"),
                LocalDateTime.now()
        );

        Set<ConstraintViolation<ItemRequestDtoChange>> violations =
                validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void nullRequestor_shouldPassValidation() {
        ItemRequestDtoChange dto = new ItemRequestDtoChange(
                "Valid description",
                null, // requestor может быть null
                LocalDateTime.now()
        );

        Set<ConstraintViolation<ItemRequestDtoChange>> violations =
                validator.validate(dto);

        assertThat(violations).isEmpty();
    }
}