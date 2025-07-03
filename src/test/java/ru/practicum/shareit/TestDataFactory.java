package ru.practicum.shareit;

import ru.practicum.shareit.item.dto.ItemDtoChange;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

public class TestDataFactory {

    public static ItemRequest validItemRequest() {
        User user = new User();
        user.setId(1L);
        user.setName("Тестовый пользователь");
        user.setEmail("test@example.com");

        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Нужна дрель");
        request.setRequestor(user);
        request.setCreated(LocalDateTime.now());

        return request;
    }

    // Невалидный ItemRequest
    public static ItemRequest invalidItemRequest() {
        // Не устанавливаем обязательные поля
        return new ItemRequest();
    }

    // Валидный DTO для создания
    public static ItemDtoChange validCreateDto() {
        return ItemDtoChange.builder()
                .name("Дрель ударная")
                .description("Мощная дрель для бетона")
                .available(true)
                .request(validItemRequest()) // валидный вложенный объект
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
                .request(validItemRequest()) // запрещенное поле
                .build();
    }
}