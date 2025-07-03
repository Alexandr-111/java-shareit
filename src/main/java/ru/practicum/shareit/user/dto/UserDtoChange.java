package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import ru.practicum.shareit.validate.OnCreate;
import lombok.Data;

@Data
public class UserDtoChange {
    @NotEmpty(groups = {OnCreate.class}, message = "Имя обязательно при создании")
    String name;

    @NotEmpty(groups = OnCreate.class, message = "Email обязателен при создании")
    @Email(message = "Email является некорректным")
    String email;

    public UserDtoChange(String name, String email) {
        this.name = name;
        this.email = email;
    }
}