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
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDtoResponse create(UserDtoChange userDtoChange) {
        log.debug("Вызван метод UserService.create(). Получен объект UserDtoChange {}", userDtoChange);

        if (userRepository.existsByEmail(userDtoChange.getEmail())) {
            throw new ConflictException("Такой email уже зарегистрирован, необходимо использовать другой.");
        }
        User user = userMapper.toUser(userDtoChange);
        User createdUser = userRepository.save(user);
        return userMapper.toUserDtoResponse(createdUser);
    }

    @Override
    public UserDtoResponse update(Long userId, UserDtoChange userDtoChange) {
        log.debug("Вызван метод UserService.update(). Получены объекты Long {} и UserDtoChange {}",
                userId, userDtoChange);

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("Пользователь с id " + userId + " не найден"));

        if (userDtoChange.getEmail() != null && !userDtoChange.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.existsByEmail(userDtoChange.getEmail())) {
                throw new ConflictException("Такой email уже зарегистрирован, необходимо использовать другой.");
            }
            existingUser.setEmail(userDtoChange.getEmail());
        }

        if (userDtoChange.getName() != null) {
            existingUser.setName(userDtoChange.getName());
        }
        User updatedUser = userRepository.save(existingUser);
        return userMapper.toUserDtoResponse(updatedUser);
    }

    @Override
    public List<UserDtoResponse> getAll() {
        log.debug("Вызван метод UserService.getAll()");
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toUserDtoResponse)
                .toList();
    }

    @Override
    public UserDtoResponse getUserById(long id) {
        log.debug("Вызван метод UserService.getUserById() c ID = {}", id);
        User userFound = userRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Пользователь с id " + id + " не найден"));
        return userMapper.toUserDtoResponse(userFound);
    }

    @Override
    public void deleteUser(long id) {
        log.debug("Вызван метод UserService.deleteUser() c ID = {}", id);
        if (!userRepository.existsById(id)) {
            throw new DataNotFoundException("Пользователь с id " + id + " не найден");
        }
        userRepository.deleteById(id);
    }
}