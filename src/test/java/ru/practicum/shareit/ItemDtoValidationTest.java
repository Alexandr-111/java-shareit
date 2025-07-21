package ru.practicum.shareit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDtoChange;
import ru.practicum.shareit.validate.OnCreate;
import ru.practicum.shareit.validate.OnUpdate;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

class ItemDtoValidationTest {
    private static Validator validator;

    @BeforeAll
    static void setup() {
        // Создаем валидатор для всех тестов
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    // Валидный объект при создании
    @Test
    void whenCreateDtoIsValid_thenNoViolations() {
        ItemDtoChange dto = TestDataFactory.validCreateDto();

        Set<ConstraintViolation<ItemDtoChange>> violations =
                validator.validate(dto, OnCreate.class);

        assertThat(violations).isEmpty();
    }

    // Невалидный объект при создании
    @Test
    void whenCreateDtoIsInvalid_thenThreeViolations() {

        ItemDtoChange dto = TestDataFactory.invalidCreateDto();

        Set<ConstraintViolation<ItemDtoChange>> violations =
                validator.validate(dto, OnCreate.class);

        assertThat(violations).hasSize(3);

        List<String> messages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .toList();

        assertThat(messages).containsExactlyInAnyOrder(
                "Название обязательно",
                "Описание обязательно",
                "Поле обязательно при создании"
        );
    }

    // Валидный объект при обновлении
    @Test
    void whenUpdateDtoIsValid_thenNoViolations() {

        ItemDtoChange dto = TestDataFactory.validUpdateDto();

        Set<ConstraintViolation<ItemDtoChange>> violations =
                validator.validate(dto, OnUpdate.class);

        assertThat(violations).isEmpty();
    }

    // Невалидный объект при обновлении (запрещенное поле)
    @Test
    void whenUpdateDtoHasForbiddenField_thenViolation() {

        ItemDtoChange dto = TestDataFactory.invalidUpdateDto();

        Set<ConstraintViolation<ItemDtoChange>> violations =
                validator.validate(dto, OnUpdate.class);

        assertThat(violations).hasSize(1);

        ConstraintViolation<ItemDtoChange> violation = violations.iterator().next();
        assertThat(violation.getMessage()).isEqualTo("Поле недоступно при обновлении");
        assertThat(violation.getPropertyPath().toString()).isEqualTo("requestDto");
    }

    // Каскадная валидация при создании
    @Test
    void whenNestedRequestIsInvalid_thenCascadeValidationFails() {

        ItemDtoChange dto = TestDataFactory.validCreateDto()
                .toBuilder()
                .requestDto(TestDataFactory.invalidItemRequestDtoChange())
                .build();

        Set<ConstraintViolation<ItemDtoChange>> violations =
                validator.validate(dto, OnCreate.class);

        assertThat(violations).isNotEmpty();

        // Проверяем что ошибки связаны с вложенным объектом
        boolean hasRequestErrors = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().startsWith("request"));
        assertThat(hasRequestErrors).isTrue();
    }
}