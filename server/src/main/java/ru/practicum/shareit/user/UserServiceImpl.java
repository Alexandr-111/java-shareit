package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.user.dto.UserDtoChange;
import ru.practicum.shareit.user.dto.UserDtoResponse;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
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
    @Transactional
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
        return userMapper.toUserDtoResponse(existingUser);
    }

    @Override
    public Page<UserDtoResponse> getAll(int from, int size) {
        log.debug("Вызван метод UserService.getAll()");
        int pageNumber = from / size;
        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.map(userMapper::toUserDtoResponse);
    }

    @Override
    public UserDtoResponse getUserById(long id) {
        log.debug("Вызван метод UserService.getUserById() c ID = {}", id);
        User userFound = userRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Пользователь с id " + id + " не найден"));
        return userMapper.toUserDtoResponse(userFound);
    }

    @Override
    @Transactional
    public void deleteUser(long id) {
        log.debug("Вызван метод UserService.deleteUser() c ID = {}", id);
        if (!userRepository.existsById(id)) {
            throw new DataNotFoundException("Пользователь с id " + id + " не найден");
        }
        userRepository.deleteById(id);
    }
}