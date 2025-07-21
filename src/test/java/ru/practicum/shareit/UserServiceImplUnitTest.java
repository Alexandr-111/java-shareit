package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserServiceImpl;
import ru.practicum.shareit.user.dto.UserDtoChange;
import ru.practicum.shareit.user.dto.UserDtoResponse;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void create_shouldCreateUserWhenEmailIsUnique() {
        // Подготовка данных
        UserDtoChange requestDto = new UserDtoChange("Ivan Ivanov", "ivan@example.com");
        User newUser = new User();
        newUser.setName("Ivan Ivanov");
        newUser.setEmail("ivan@example.com");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("Ivan Ivanov");
        savedUser.setEmail("ivan@example.com");

        UserDtoResponse expectedResponse = new UserDtoResponse(1L, "Ivan Ivanov", "ivan@example.com");

        // Мокирование
        when(userRepository.existsByEmail("ivan@example.com")).thenReturn(false);
        when(userMapper.toUser(requestDto)).thenReturn(newUser);
        when(userRepository.save(newUser)).thenReturn(savedUser);
        when(userMapper.toUserDtoResponse(savedUser)).thenReturn(expectedResponse);

        UserDtoResponse actualResponse = userService.create(requestDto);

        // Проверки
        assertNotNull(actualResponse);
        assertEquals(1L, actualResponse.getId());
        assertEquals("Ivan Ivanov", actualResponse.getName());
        assertEquals("ivan@example.com", actualResponse.getEmail());

        verify(userRepository).existsByEmail("ivan@example.com");
        verify(userMapper).toUser(requestDto);
        verify(userRepository).save(newUser);
        verify(userMapper).toUserDtoResponse(savedUser);
    }

    @Test
    void create_shouldThrowConflictExceptionWhenEmailExists() {
        UserDtoChange requestDto = new UserDtoChange("Ivan Ivanov", "ivan@example.com");
        // Мокирование
        when(userRepository.existsByEmail("ivan@example.com")).thenReturn(true);
        // Проверка
        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.create(requestDto));
        assertEquals("Такой email уже зарегистрирован, необходимо использовать другой.",
                exception.getMessage());

        verify(userRepository).existsByEmail("ivan@example.com");
        verifyNoInteractions(userMapper);
        verify(userRepository, never()).save(any());
    }

    @Test
    void update_shouldUpdateNameAndEmailWhenDataValid() {
        // Подготовка данных
        Long userId = 1L;
        UserDtoChange updateDto = new UserDtoChange("Ivan Updated", "updated@example.com");

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Ivan Original");
        existingUser.setEmail("original@example.com");

        UserDtoResponse expectedResponse = new UserDtoResponse(userId, "Ivan Updated", "updated@example.com");

        // Мокирование
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userMapper.toUserDtoResponse(existingUser)).thenReturn(expectedResponse);

        UserDtoResponse actualResponse = userService.update(userId, updateDto);

        assertNotNull(actualResponse);
        assertEquals(userId, actualResponse.getId());
        assertEquals("Ivan Updated", actualResponse.getName());
        assertEquals("updated@example.com", actualResponse.getEmail());

        verify(userRepository).findById(userId);
        verify(userRepository).existsByEmail("updated@example.com");
        verify(userMapper).toUserDtoResponse(existingUser);
    }

    @Test
    void update_shouldUpdateNameOnlyWhenEmailNotChanged() {
        Long userId = 1L;
        UserDtoChange updateDto = new UserDtoChange("Ivan Updated", null);

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Ivan Original");
        existingUser.setEmail("ivan@example.com");

        UserDtoResponse expectedResponse = new UserDtoResponse(userId, "Ivan Updated", "ivan@example.com");

        // Мокирование
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userMapper.toUserDtoResponse(existingUser)).thenReturn(expectedResponse);

        UserDtoResponse actualResponse = userService.update(userId, updateDto);

        assertNotNull(actualResponse);
        assertEquals(userId, actualResponse.getId());
        assertEquals("Ivan Updated", actualResponse.getName());
        assertEquals("ivan@example.com", actualResponse.getEmail());

        verify(userRepository).findById(userId);
        verify(userRepository, never()).existsByEmail(any());
        verify(userMapper).toUserDtoResponse(existingUser);
    }

    @Test
    void update_shouldThrowDataNotFoundExceptionWhenUserNotFound() {
        Long userId = 99L;
        UserDtoChange updateDto = new UserDtoChange("Ivan Updated", "updated@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> userService.update(userId, updateDto));
        assertEquals("Пользователь с id 99 не найден", exception.getMessage());

        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(userMapper);
    }

    @Test
    void update_shouldThrowConflictExceptionWhenEmailExists() {
        Long userId = 1L;
        UserDtoChange updateDto = new UserDtoChange("Ivan Updated", "existing@example.com");

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Ivan Original");
        existingUser.setEmail("original@example.com");

        // Мокирование
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Проверка исключения
        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.update(userId, updateDto));
        assertEquals("Такой email уже зарегистрирован, необходимо использовать другой.",
                exception.getMessage());

        verify(userRepository).findById(userId);
        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any());
        verifyNoInteractions(userMapper);
    }

    @Test
    void getAll_shouldReturnEmptyListWhenNoUsers() {

        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<UserDtoResponse> result = userService.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository).findAll();
        verifyNoInteractions(userMapper);
    }

    @Test
    void getAll_shouldReturnListOfUsers() {
        User user1 = new User();
        user1.setId(1L);
        user1.setName("Ivan Ivanov");
        user1.setEmail("ivan@example.com");

        User user2 = new User();
        user2.setId(2L);
        user2.setName("Petr Petrov");
        user2.setEmail("petr@example.com");

        UserDtoResponse dto1 = new UserDtoResponse(1L, "Ivan Ivanov", "ivan@example.com");
        UserDtoResponse dto2 = new UserDtoResponse(2L, "Petr Petrov", "petr@example.com");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        when(userMapper.toUserDtoResponse(user1)).thenReturn(dto1);
        when(userMapper.toUserDtoResponse(user2)).thenReturn(dto2);

        List<UserDtoResponse> result = userService.getAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(dto1, result.get(0));
        assertEquals(dto2, result.get(1));

        verify(userRepository).findAll();
        verify(userMapper, times(2)).toUserDtoResponse(any());
    }

    @Test
    void getUserById_shouldReturnUserWhenExists() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setName("Ivan Ivanov");
        user.setEmail("ivan@example.com");
        UserDtoResponse expectedResponse = new UserDtoResponse(userId, "Ivan Ivanov", "ivan@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toUserDtoResponse(user)).thenReturn(expectedResponse);

        UserDtoResponse actualResponse = userService.getUserById(userId);

        assertNotNull(actualResponse);
        assertEquals(userId, actualResponse.getId());
        assertEquals("Ivan Ivanov", actualResponse.getName());
        assertEquals("ivan@example.com", actualResponse.getEmail());

        verify(userRepository).findById(userId);
        verify(userMapper).toUserDtoResponse(user);
    }

    @Test
    void getUserById_shouldThrowExceptionWhenUserNotFound() {
        Long userId = 99L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> userService.getUserById(userId));
        assertEquals("Пользователь с id 99 не найден", exception.getMessage());

        verify(userRepository).findById(userId);
        verifyNoInteractions(userMapper);
    }

    @Test
    void deleteUser_shouldDeleteWhenUserExists() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);

        userService.deleteUser(userId);

        verify(userRepository).existsById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUser_shouldThrowExceptionWhenUserNotFound() {
        Long userId = 99L;
        when(userRepository.existsById(userId)).thenReturn(false);

        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> userService.deleteUser(userId));
        assertEquals("Пользователь с id 99 не найден", exception.getMessage());

        verify(userRepository).existsById(userId);
        verify(userRepository, never()).deleteById(any());
    }
}