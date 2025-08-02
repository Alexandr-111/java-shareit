package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDtoChange;
import ru.practicum.shareit.user.dto.UserDtoResponse;

import java.util.List;

public interface UserService {
    UserDtoResponse create(UserDtoChange userDtoChange);

    UserDtoResponse update(Long userId, UserDtoChange userDtoChange);

    List<UserDtoResponse> getAll();

    UserDtoResponse getUserById(long id);

    void deleteUser(long id);
}