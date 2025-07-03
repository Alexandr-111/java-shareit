package ru.practicum.shareit.user;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.dto.UserDtoChange;
import ru.practicum.shareit.user.dto.UserDtoResponse;

import java.util.Objects;

@Component
public class UserMapper {

    public User toUser(UserDtoChange userDtoChange) {
        Objects.requireNonNull(userDtoChange, "ДТО (UserDtoChange) не должен быть null");
        return new User(
                userDtoChange.getName(),
                userDtoChange.getEmail()
        );
    }

    public UserDtoResponse toUserDtoResponse(User user) {
        Objects.requireNonNull(user, "Пользователь (User) не должен быть null");
        return new UserDtoResponse(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}