package ru.practicum.shareit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDtoChange;
import ru.practicum.shareit.validate.OnCreate;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserDtoValidationTest {

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
        UserDtoChange dto = new UserDtoChange("Valid Name", "valid@example.com");
        Set<ConstraintViolation<UserDtoChange>> violations = validator.validate(dto, OnCreate.class);
        assertThat(violations).isEmpty();
    }

    // Невалидные данные
    @Test
    void nullNameOnCreate_shouldFailValidation() {
        UserDtoChange dto = new UserDtoChange(null, "test@example.com");
        assertSingleViolation(dto, "Имя обязательно при создании");
    }

    @Test
    void emptyNameOnCreate_shouldFailValidation() {
        UserDtoChange dto = new UserDtoChange("", "test@example.com");
        assertSingleViolation(dto, "Имя обязательно при создании");
    }

    @Test
    void nullEmailOnCreate_shouldFailValidation() {
        UserDtoChange dto = new UserDtoChange("Valid Name", null);
        assertSingleViolation(dto, "Email обязателен при создании");
    }

    @Test
    void emptyEmailOnCreate_shouldFailValidation() {
        UserDtoChange dto = new UserDtoChange("Valid Name", "");
        assertSingleViolation(dto, "Email обязателен при создании");
    }

    @Test
    void invalidEmailFormat_shouldFailValidation() {
        UserDtoChange dto = new UserDtoChange("Valid Name", "not-email");
        Set<ConstraintViolation<UserDtoChange>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Email является некорректным");
    }

    @Test
    void emailWithSpaces_shouldFailValidation() {
        UserDtoChange dto = new UserDtoChange("Valid Name", "email with@spaces.com");
        Set<ConstraintViolation<UserDtoChange>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Email является некорректным");
    }

    // Проверка отсутствия валидации при обновлении (без группы)
    @Test
    void withoutGroups_shouldIgnoreCreateConstraints() {
        UserDtoChange dto = new UserDtoChange(null, "email@spaces.com");
        Set<ConstraintViolation<UserDtoChange>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    // Вспомогательный метод
    private void assertSingleViolation(UserDtoChange dto, String expectedMessage) {
        Set<ConstraintViolation<UserDtoChange>> violations = validator.validate(dto, OnCreate.class);
        assertThat(violations).hasSize(1);
        ConstraintViolation<UserDtoChange> violation = violations.iterator().next();
        String actualMessage = violation.getMessage();
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }
}