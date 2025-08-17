package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import ru.practicum.shareit.validate.OnCreate;

import java.time.LocalDateTime;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class BookingDtoChange {
    @NotNull(groups = {OnCreate.class}, message = "Поле Начало аренды - обязательно")
    @FutureOrPresent(message = "Дата начала бронирования должна быть в будущем или настоящем")
    private LocalDateTime start;

    @NotNull(groups = {OnCreate.class}, message = "Поле Конец аренды - обязательно")
    @Future(message = "Дата окончания бронирования должна быть в будущем")
    private LocalDateTime end;

    @NotNull(groups = {OnCreate.class}, message = "ID вещи должен быть указан")
    @Positive(message = "ID вещи должен быть положительным числом")
    private Long itemId;
}