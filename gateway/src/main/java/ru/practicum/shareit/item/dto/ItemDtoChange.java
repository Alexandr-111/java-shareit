package ru.practicum.shareit.item.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import ru.practicum.shareit.request.dto.ItemRequestDtoChange;
import ru.practicum.shareit.validate.OnCreate;
import ru.practicum.shareit.validate.OnUpdate;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ItemDtoChange {
    @NotEmpty(groups = {OnCreate.class}, message = "Название обязательно")
    private String name;

    @NotEmpty(groups = {OnCreate.class}, message = "Описание обязательно")
    private String description;

    @NotNull(groups = {OnCreate.class}, message = "Поле обязательно при создании")
    private Boolean available;

    @Null(groups = OnUpdate.class, message = "Поле недоступно при обновлении")
    @Valid
    private ItemRequestDtoChange requestDto;

    @Positive
    private Long requestId;
}