package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import ru.practicum.shareit.user.dto.UserDtoChange;
import ru.practicum.shareit.validate.OnCreate;

import java.time.LocalDateTime;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ItemRequestDtoChange {

    @NotBlank(groups = {OnCreate.class}, message = "Описание запроса обязательно")
    private String description;

    @NotNull(groups = {OnCreate.class}, message = "Пользователь, создавший запрос - обязательно")
    private UserDtoChange requestorDto;

    private LocalDateTime created;
}