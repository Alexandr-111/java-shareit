package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemServiceImpl;
import ru.practicum.shareit.item.ItemStorage;
import ru.practicum.shareit.item.dto.ItemDtoChange;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserStorage;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplUnitTest {

    @Mock
    private ItemStorage itemStorage;

    @Mock
    private UserStorage userStorage;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void create_shouldCreateItemSuccessfully() {
        // Подготовка данных
        Long ownerId = 1L;
        User owner = new User(ownerId, "Кузьма Кузьмин", "kuzma@example.com");
        ItemDtoChange requestDto = new ItemDtoChange("Пылесос", "Мощный пылесос",
                true, null);

        // Создаем объекты Item
        Item newItem = new Item("Пылесос", "Мощный пылесос", true, owner, null);
        Item savedItem = new Item("Пылесос", "Мощный пылесос", true, owner, null);
        savedItem.setId(1L);

        ItemDtoResponse expectedResponse = new ItemDtoResponse(1L, "Пылесос", "Мощный пылесос",
                true, null);

        // Мокирование поведения
        when(userStorage.getUserById(ownerId)).thenReturn(Optional.of(owner));
        when(itemMapper.toItem(owner, requestDto)).thenReturn(newItem);
        when(itemStorage.create(newItem)).thenReturn(savedItem);
        when(itemMapper.toItemDtoResponse(savedItem)).thenReturn(expectedResponse);

        // Вызов метода и проверки
        ItemDtoResponse actualResponse = itemService.create(ownerId, requestDto);
        assertNotNull(actualResponse);
        assertEquals(1L, actualResponse.getId());
        assertEquals("Пылесос", actualResponse.getName());
        assertEquals("Мощный пылесос", actualResponse.getDescription());
        assertTrue(actualResponse.getAvailable());
        assertNull(actualResponse.getRequest());

        // Проверка вызовов
        verify(userStorage).getUserById(ownerId);
        verify(itemMapper).toItem(owner, requestDto);
        verify(itemStorage).create(newItem);
        verify(itemMapper).toItemDtoResponse(savedItem);
    }

    @Test
    void create_shouldThrowExceptionWhenUserNotFound() {
        // Подготовка данных
        Long ownerId = 9999L;
        ItemDtoChange requestDto = new ItemDtoChange("Холодильник",
                "Двухкамерный", true, null);

        when(userStorage.getUserById(ownerId)).thenReturn(Optional.empty());

        // Проверка исключения
        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> itemService.create(ownerId, requestDto)
        );

        assertEquals("Пользователь с id 9999 не найден", exception.getMessage());
        verify(userStorage).getUserById(ownerId);
        verifyNoInteractions(itemMapper, itemStorage);
    }

    @Test
    void create_shouldHandleDifferentItemsCorrectly() {
        // Подготовка данных
        Long ownerId = 3L;
        User owner = new User(ownerId, "Кузьма Кузьмин", "kuzma@example.com");
        owner.setId(ownerId);
        ItemDtoChange requestDto = new ItemDtoChange("Блендер", "Мощность 1000W",
                true, null);
        Item newItem = new Item("Блендер", "Мощность 1000W", true, owner, null);
        Item savedItem = new Item("Блендер", "Мощность 1000W", true, owner, null);
        savedItem.setId(4L);
        ItemDtoResponse expectedResponse = new ItemDtoResponse(4L, "Блендер", "Мощность 1000W",
                true, null);

        when(userStorage.getUserById(ownerId)).thenReturn(Optional.of(owner));
        when(itemMapper.toItem(owner, requestDto)).thenReturn(newItem);
        when(itemStorage.create(newItem)).thenReturn(savedItem);
        when(itemMapper.toItemDtoResponse(savedItem)).thenReturn(expectedResponse);

        // Вызов метода
        ItemDtoResponse actualResponse = itemService.create(ownerId, requestDto);

        // Проверки
        assertNotNull(actualResponse);
        assertEquals(4L, actualResponse.getId());
        assertEquals("Блендер", actualResponse.getName());
        assertEquals("Мощность 1000W", actualResponse.getDescription());
        assertTrue(actualResponse.getAvailable());

        // Верификация
        verify(itemMapper).toItem(eq(owner), eq(requestDto));
    }

    @Test
    void getItemById_shouldReturnItemSuccessfully() {
        // Подготовка данных
        Long userId = 1L;
        Long itemId = 10L;
        User owner = new User(userId, "Иван", "ivan@example.com");
        Item item = new Item("Пылесос", "Мощный пылесос", true, owner, null);
        item.setId(itemId);
        ItemDtoResponse expectedResponse = new ItemDtoResponse(itemId, "Пылесос", "Мощный пылесос",
                true, null);

        // Мокирование поведения
        when(userStorage.userNotExists(userId)).thenReturn(false);
        when(itemStorage.getItemById(itemId)).thenReturn(Optional.of(item));
        when(itemMapper.toItemDtoResponse(item)).thenReturn(expectedResponse);

        // Вызов метода
        ItemDtoResponse actualResponse = itemService.getItemById(userId, itemId);

        // Проверки
        assertNotNull(actualResponse);
        assertEquals(itemId, actualResponse.getId());
        assertEquals("Пылесос", actualResponse.getName());
        assertEquals("Мощный пылесос", actualResponse.getDescription());
        assertTrue(actualResponse.getAvailable());
        assertNull(actualResponse.getRequest());

        // Проверка вызовов
        verify(userStorage).userNotExists(userId);
        verify(itemStorage).getItemById(itemId);
        verify(itemMapper).toItemDtoResponse(item);
    }

    @Test
    void getItemById_shouldThrowWhenItemNotFound() {
        // Подготовка данных
        Long userId = 2L;
        Long itemId = 9999L;

        // Мокирование поведения
        when(userStorage.userNotExists(userId)).thenReturn(false);
        when(itemStorage.getItemById(itemId)).thenReturn(Optional.empty());

        // Проверка исключения
        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> itemService.getItemById(userId, itemId)
        );

        // Проверка сообщения
        assertEquals("Вещь с id 9999 не найдена", exception.getMessage());

        // Проверка вызовов
        verify(userStorage).userNotExists(userId);
        verify(itemStorage).getItemById(itemId);
        verifyNoInteractions(itemMapper);
    }

    @Test
    void getItemById_shouldThrowWhenUserNotFound() {
        // Подготовка данных
        Long userId = 9999L;
        Long itemId = 20L;

        // Мокирование поведения
        when(userStorage.userNotExists(userId)).thenReturn(true);

        // Проверка исключения
        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> itemService.getItemById(userId, itemId)
        );

        // Проверка сообщения
        assertEquals("Пользователь с id 9999 не найден", exception.getMessage());

        // Проверка вызовов
        verify(userStorage).userNotExists(userId);
        verifyNoInteractions(itemStorage, itemMapper);
    }
}