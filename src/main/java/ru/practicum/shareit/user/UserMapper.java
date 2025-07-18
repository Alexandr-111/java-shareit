package ru.practicum.shareit.user;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.dto.UserDtoChange;
import ru.practicum.shareit.user.dto.UserDtoResponse;

import java.util.Objects;

@Component
public class UserMapper {

    public User toUser(UserDtoChange userDtoChange) {
        Objects.requireNonNull(userDtoChange, "ДТО (UserDtoChange) не должен быть null");
        return User.builder()
                .name(userDtoChange.getName())
                .email(userDtoChange.getEmail())
                .build();
    }

    public UserDtoChange toUserDtoChange(User user) {
        Objects.requireNonNull(user, "Пользователь (User) не должен быть null");
        return UserDtoChange.builder()
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public UserDtoResponse toUserDtoResponse(User user) {
        Objects.requireNonNull(user, "Пользователь (User) не должен быть null");
        return UserDtoResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}