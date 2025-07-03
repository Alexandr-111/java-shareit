package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.user.dto.UserDtoChange;
import ru.practicum.shareit.user.dto.UserDtoResponse;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;
    private final UserMapper userMapper;

    @Override
    public UserDtoResponse create(UserDtoChange userDtoChange) {
        log.debug("Вызван метод UserService.create(). Получен объект UserDtoChange {}", userDtoChange);

        if (userStorage.findEmail(userDtoChange.getEmail())) {
            throw new ConflictException("Такой email уже зарегистрирован, необходимо использовать другой.");
        }
        User user = userMapper.toUser(userDtoChange);
        User createdUser = userStorage.create(user);
        return userMapper.toUserDtoResponse(createdUser);
    }

    @Override
    public UserDtoResponse update(Long userId, UserDtoChange userDtoChange) {
        log.debug("Вызван метод UserService.update(). Получены объекты Long {} и UserDtoChange {}",
                userId, userDtoChange);

        User existingUser = userStorage.getUserById(userId)
                .orElseThrow(() -> new DataNotFoundException("Пользователь с id " + userId + " не найден"));

        if (userDtoChange.getEmail() != null) {
            if (userStorage.findEmail(userDtoChange.getEmail())) {
                throw new ConflictException("Такой email уже зарегистрирован, необходимо использовать другой.");
            }
        }

        if (userDtoChange.getName() != null) {
            existingUser.setName(userDtoChange.getName());
        }
        if (userDtoChange.getEmail() != null) {
            existingUser.setEmail(userDtoChange.getEmail());
        }
        User updatedUser = userStorage.update(existingUser);
        return userMapper.toUserDtoResponse(updatedUser);
    }

    @Override
    public List<UserDtoResponse> getAll() {
        log.debug("Вызван метод UserService.getAll()");
        List<User> users = userStorage.getAll();
        return users.stream()
                .map(userMapper::toUserDtoResponse)
                .toList();
    }

    @Override
    public UserDtoResponse getUserById(long id) {
        log.debug("Вызван метод UserService.getUserById() c ID = {}", id);
        User userFound = userStorage.getUserById(id)
                .orElseThrow(() -> new DataNotFoundException("Пользователь с id " + id + " не найден"));
        return userMapper.toUserDtoResponse(userFound);
    }

    @Override
    public void deleteUser(long id) {
        log.debug("Вызван метод UserService.deleteUser() c ID = {}", id);
        if (userStorage.userNotExists(id)) {
            throw new DataNotFoundException("Пользователь с id " + id + " не найден");
        }
        userStorage.deleteUser(id);
    }
}