package ru.practicum.shareit;

import ru.practicum.shareit.item.dto.ItemDtoChange;
import ru.practicum.shareit.request.dto.ItemRequestDtoChange;
import ru.practicum.shareit.user.dto.UserDtoChange;

import java.time.LocalDateTime;

public class TestDataFactory {
    // Валидный ItemRequestDtoChange
    public static ItemRequestDtoChange validItemRequestDto() {
        UserDtoChange userDtoChange = new UserDtoChange();
        userDtoChange.setName("Тестовый пользователь");
        userDtoChange.setEmail("test@example.com");

        ItemRequestDtoChange requestDto = new ItemRequestDtoChange();
        requestDto.setDescription("Нужна дрель");
        requestDto.setRequestorDto(userDtoChange);
        requestDto.setCreated(LocalDateTime.now());

        return requestDto;
    }

    // Невалидный ItemRequestDtoChange
    public static ItemRequestDtoChange invalidItemRequestDtoChange() {
        // Не устанавливаем обязательные поля
        return new ItemRequestDtoChange();
    }

    // Валидный DTO для создания
    public static ItemDtoChange validCreateDto() {
        return ItemDtoChange.builder()
                .name("Дрель ударная")
                .description("Мощная дрель для бетона")
                .available(true)
                .requestDto(validItemRequestDto()) // валидный вложенный объект
                .build();
    }

    // Невалидный DTO для создания (пустые поля)
    public static ItemDtoChange invalidCreateDto() {
        return ItemDtoChange.builder()
                .name("")
                .description("")
                .available(null)
                .build();
    }

    // Валидный DTO для обновления
    public static ItemDtoChange validUpdateDto() {
        return ItemDtoChange.builder()
                .name("Новое название")
                .build();
    }

    // Невалидный DTO для обновления (с запрещенным полем)
    public static ItemDtoChange invalidUpdateDto() {
        return ItemDtoChange.builder()
                .name("Новое название")
                .requestDto(validItemRequestDto()) // запрещенное поле
                .build();
    }
}