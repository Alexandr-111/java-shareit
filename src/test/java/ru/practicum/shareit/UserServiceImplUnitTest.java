package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserServiceImpl;
import ru.practicum.shareit.user.UserStorage;
import ru.practicum.shareit.user.dto.UserDtoChange;
import ru.practicum.shareit.user.dto.UserDtoResponse;
import ru.practicum.shareit.user.User;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplUnitTest {

    @Mock
    private UserStorage userStorage;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void create_shouldCreateUserSuccessfully() {
        // Подготовка входных данных
        UserDtoChange requestDto = new UserDtoChange("Кузьма Кузьмин", "kuzma@example.com");
        User userEntity = new User("Кузьма Кузьмин", "kuzma@example.com");
        User savedUser = new User("Кузьма Кузьмин", "kuzma@example.com");
        savedUser.setId(1L);
        UserDtoResponse expectedResponse = new UserDtoResponse(1L, "Кузьма Кузьмин",
                "kuzma@example.com");

        // Мокирование поведения
        when(userStorage.findEmail("kuzma@example.com")).thenReturn(false);
        when(userMapper.toUser(requestDto)).thenReturn(userEntity);
        when(userStorage.create(userEntity)).thenReturn(savedUser);
        when(userMapper.toUserDtoResponse(savedUser)).thenReturn(expectedResponse);

        // Вызов метода
        UserDtoResponse actualResponse = userService.create(requestDto);

        // Проверки
        assertNotNull(actualResponse);
        assertEquals(1L, actualResponse.getId());
        assertEquals("Кузьма Кузьмин", actualResponse.getName());
        assertEquals("kuzma@example.com", actualResponse.getEmail());

        // Проверка вызовов
        verify(userStorage).findEmail("kuzma@example.com");
        verify(userMapper).toUser(requestDto);
        verify(userStorage).create(userEntity);
        verify(userMapper).toUserDtoResponse(savedUser);
    }

    @Test
    void create_shouldThrowConflictExceptionWhenEmailExists() {
        // Подготовка данных
        UserDtoChange requestDto = new UserDtoChange("Кузьма Кузьмин", "existing@example.com");
        // Мокирование поведения
        when(userStorage.findEmail("existing@example.com")).thenReturn(true);
        // Проверка исключения
        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.create(requestDto));
        assertEquals("Такой email уже зарегистрирован, необходимо использовать другой.",
                exception.getMessage());
        // Проверка, что методы не вызывались
        verify(userStorage, never()).create(any());
        verify(userMapper, never()).toUser(any());
        verify(userMapper, never()).toUserDtoResponse(any());
    }

    @Test
    void create_shouldUseMapperCorrectly() {
        // Подготовка данных
        UserDtoChange requestDto = new UserDtoChange("Кузьма Кузьмин", "kuzma@example.com");
        User userEntity = new User("Кузьма Кузьмин", "kuzma@example.com");
        User savedUser = new User("Кузьма Кузьмин", "kuzma@example.com");
        savedUser.setId(1L);
        UserDtoResponse expectedResponse = new UserDtoResponse(1L, "Кузьма Кузьмин",
                "kuzma@example.com");

        // Мокирование
        when(userStorage.findEmail("kuzma@example.com")).thenReturn(false);
        when(userMapper.toUser(requestDto)).thenReturn(userEntity);
        when(userStorage.create(userEntity)).thenReturn(savedUser);
        when(userMapper.toUserDtoResponse(savedUser)).thenReturn(expectedResponse);

        userService.create(requestDto);

        verify(userMapper).toUser(requestDto);
        verify(userMapper).toUserDtoResponse(savedUser);

        assertEquals("Кузьма Кузьмин", userEntity.getName());
        assertEquals("kuzma@example.com", userEntity.getEmail());
    }

    @Test
    void create_shouldPassCorrectEntityToStorage() {
        // Подготовка данных
        UserDtoChange requestDto = new UserDtoChange("Кузьма Кузьмин", "kuzma@example.com");
        User userEntity = new User("Кузьма Кузьмин", "kuzma@example.com");
        User savedUser = new User("Кузьма Кузьмин", "kuzma@example.com");
        savedUser.setId(1L);

        // Мокирование
        when(userStorage.findEmail("kuzma@example.com")).thenReturn(false);
        when(userMapper.toUser(requestDto)).thenReturn(userEntity);
        when(userStorage.create(userEntity)).thenReturn(savedUser);
        when(userMapper.toUserDtoResponse(any())).thenReturn(new UserDtoResponse(1L, "Кузьма Кузьмин",
                "kuzma@example.com"));

        userService.create(requestDto);

        // Проверка передачи в хранилище
        verify(userStorage).create(userEntity);

        // Проверка, что ID установлен при сохранении
        assertNotNull(savedUser.getId());
        assertEquals(1L, savedUser.getId());
    }

    @Test
    void create_shouldHandleNullEmailInRequest() {
        UserDtoChange requestDto = new UserDtoChange("Кузьма Кузьмин", null);

        when(userStorage.findEmail(null)).thenThrow(new IllegalArgumentException("Email cannot be null"));

        assertThrows(IllegalArgumentException.class,
                () -> userService.create(requestDto));
    }

    @Test
    void update_shouldUpdateUserSuccessfully() {
        // Подготовка входных данных
        Long userId = 1L;
        UserDtoChange updateDto = new UserDtoChange("Савва Саввин", "savva@example.com");
        User existingUser = new User("Кузьма Кузьмин", "kuzma@example.com");
        existingUser.setId(userId);
        User updatedUser = new User("Савва Саввин", "savva@example.com");
        updatedUser.setId(userId);
        UserDtoResponse expectedResponse = new UserDtoResponse(userId, "Савва Саввин",
                "savva@example.com");

        // Мокирование поведения
        when(userStorage.getUserById(userId)).thenReturn(Optional.of(existingUser));
        when(userStorage.findEmail("savva@example.com")).thenReturn(false);
        when(userStorage.update(updatedUser)).thenReturn(updatedUser);
        when(userMapper.toUserDtoResponse(updatedUser)).thenReturn(expectedResponse);

        // Вызов и проверка
        UserDtoResponse result = userService.update(userId, updateDto);
        assertEquals(expectedResponse, result);

        // Создаем объект для проверки порядка вызовов
        InOrder inOrder = inOrder(userStorage, userMapper);

        inOrder.verify(userStorage, times(1)).getUserById(userId);
        inOrder.verify(userStorage).findEmail("savva@example.com");
        inOrder.verify(userStorage).update(argThat(user ->
                user.getName().equals("Савва Саввин") &&
                        user.getEmail().equals("savva@example.com")
        ));
        inOrder.verify(userMapper).toUserDtoResponse(any(User.class));
    }

    @Test
    void update_shouldThrowConflictWhenEmailExists() {
        // Подготовка входных данных
        Long userId = 1L;
        UserDtoChange updateDto = new UserDtoChange("Савва Саввин", "existing@example.com");
        User existingUser = new User("Кузьма Кузьмин", "kuzma@example.com");
        existingUser.setId(userId);

        // Мокирование поведения
        when(userStorage.getUserById(userId)).thenReturn(Optional.of(existingUser));
        when(userStorage.findEmail("existing@example.com")).thenReturn(true);

        // Вызов и проверка
        assertThrows(ConflictException.class, () -> userService.update(userId, updateDto));
    }

    @Test
    void getById_shouldReturnUserSuccessfully() {
        // Подготовка входных данных
        Long userId = 1L;
        User existingUser = new User("Кузьма Кузьмин", "kuzma@example.com");
        existingUser.setId(userId);
        UserDtoResponse expectedResponse = new UserDtoResponse(userId, "Кузьма Кузьмин",
                "kuzma@example.com");

        // Мокирование поведения
        when(userStorage.getUserById(userId)).thenReturn(Optional.of(existingUser));
        when(userMapper.toUserDtoResponse(existingUser)).thenReturn(expectedResponse);

        // Вызов и проверка
        UserDtoResponse result = userService.getUserById(userId);
        assertEquals(expectedResponse, result);
    }

    @Test
    void getById_shouldThrowNotFoundWhenUserNotExists() {
        // Подготовка входных данных
        Long userId = 9999L;
        // Мокирование поведения
        when(userStorage.getUserById(userId)).thenReturn(Optional.empty());
        // Вызов и проверка
        assertThrows(DataNotFoundException.class, () -> userService.getUserById(userId));
    }

    @Test
    void getAll_shouldReturnListOfUsers() {
        // Подготовка входных данных
        User user1 = new User("Кузьма Кузьмин", "kuzma@example.com");
        user1.setId(1L);
        User user2 = new User("Савва Саввин", "savva@example.com");
        user2.setId(2L);

        UserDtoResponse dto1 = new UserDtoResponse(1L, "Кузьма Кузьмин", "kuzma@example.com");
        UserDtoResponse dto2 = new UserDtoResponse(2L, "Савва Саввин", "savva@example.com");

        // Мокирование поведения
        when(userStorage.getAll()).thenReturn(List.of(user1, user2));
        when(userMapper.toUserDtoResponse(user1)).thenReturn(dto1);
        when(userMapper.toUserDtoResponse(user2)).thenReturn(dto2);

        // Вызов и проверка
        List<UserDtoResponse> result = userService.getAll();
        assertEquals(2, result.size());
        assertTrue(result.containsAll(List.of(dto1, dto2)));
    }

    @Test
    void delete_shouldDeleteUserSuccessfully() {
        // Подготовка входных данных
        Long userId = 1L;
        // Мокирование поведения
        when(userStorage.userNotExists(userId)).thenReturn(false);
        // Вызов и проверка
        assertDoesNotThrow(() -> userService.deleteUser(userId));
        verify(userStorage).deleteUser(userId);
    }

    @Test
    void delete_shouldThrowNotFoundWhenUserNotExists() {
        // Подготовка входных данных
        Long userId = 9999L;
        // Мокирование поведения
        when(userStorage.userNotExists(userId)).thenReturn(true);
        // Вызов и проверка
        assertThrows(DataNotFoundException.class, () -> userService.deleteUser(userId));
    }
}