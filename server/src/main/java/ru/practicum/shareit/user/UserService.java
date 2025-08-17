package ru.practicum.shareit.user;

import org.springframework.data.domain.Page;
import ru.practicum.shareit.user.dto.UserDtoChange;
import ru.practicum.shareit.user.dto.UserDtoResponse;

public interface UserService {
    UserDtoResponse create(UserDtoChange userDtoChange);

    UserDtoResponse update(Long userId, UserDtoChange userDtoChange);

    Page<UserDtoResponse> getAll(int from, int size);

    UserDtoResponse getUserById(long id);

    void deleteUser(long id);
}