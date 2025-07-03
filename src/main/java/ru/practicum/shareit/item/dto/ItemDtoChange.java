package ru.practicum.shareit.item.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.validate.OnCreate;
import ru.practicum.shareit.validate.OnUpdate;

@Data
public class ItemDtoChange {
    @NotEmpty(groups = {OnCreate.class}, message = "Название обязательно")
    String name;

    @NotEmpty(groups = {OnCreate.class}, message = "Описание обязательно")
    String description;

    @NotNull(groups = {OnCreate.class}, message = "Поле обязательно при создании")
    Boolean available;

    @Null(groups = OnUpdate.class, message = "Поле недоступно при обновлении")
    @Valid   // Для проверки вложенного объекта
    ItemRequest request;

    @Builder(toBuilder = true)
    public ItemDtoChange(String name, String description, Boolean available,
                         ItemRequest request) {
        this.name = name;
        this.description = description;
        this.available = available;
        this.request = request;
    }
}