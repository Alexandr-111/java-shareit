package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import ru.practicum.shareit.validate.OnCreate;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserDtoChange {
    @NotEmpty(groups = {OnCreate.class}, message = "Имя обязательно при создании")
    private String name;

    @NotEmpty(groups = OnCreate.class, message = "Email обязателен при создании")
    @Email(message = "Email является некорректным")
    private String email;
}