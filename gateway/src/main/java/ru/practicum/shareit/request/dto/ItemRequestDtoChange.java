package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @Size(max = 1000, message = "Размер должен быть не больше 1000")
    private String description;
    private UserDtoChange requestorDto;
    private LocalDateTime created;
}