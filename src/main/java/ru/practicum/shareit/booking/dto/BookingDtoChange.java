package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import ru.practicum.shareit.validate.OnCreate;

import java.time.LocalDateTime;

@Data
@Jacksonized
@Builder(toBuilder = true)
public class BookingDtoChange {
    @NotNull(groups = {OnCreate.class}, message = "Поле Начало аренды - обязательно")
    private LocalDateTime start;

    @NotNull(groups = {OnCreate.class}, message = "Поле Конец аренды - обязательно")
    private LocalDateTime end;

    @NotNull(groups = {OnCreate.class}, message = "ID вещи должен быть указан")
    @Positive(message = "ID вещи должен быть положительным числом")
    private Long itemId;
}