package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingInfoDto;
import ru.practicum.shareit.exception.BadInputException;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemServiceImpl;
import ru.practicum.shareit.item.dto.ItemDtoChange;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoShort;
import ru.practicum.shareit.item.dto.ItemDtoWithDetails;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplUnitTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void create_shouldCreateItemWhenUserExists() {
        Long ownerId = 1L;
        User owner = new User();
        owner.setId(ownerId);

        ItemDtoChange requestDto = new ItemDtoChange("Дрель", "Аккумуляторная дрель",
                true, null, null);
        Item newItem = new Item();
        Item savedItem = new Item();
        savedItem.setId(1L);
        ItemDtoResponse expectedResponse = new ItemDtoResponse(1L, "Дрель",
                "Аккумуляторная дрель", true, null);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemMapper.toItem(owner, requestDto)).thenReturn(newItem);
        when(itemRepository.save(newItem)).thenReturn(savedItem);
        when(itemMapper.toItemDtoResponse(savedItem)).thenReturn(expectedResponse);

        ItemDtoResponse actualResponse = itemService.create(ownerId, requestDto);

        assertNotNull(actualResponse);
        assertEquals(1L, actualResponse.getId());
        assertEquals("Дрель", actualResponse.getName());

        verify(userRepository).findById(ownerId);
        verify(itemMapper).toItem(owner, requestDto);
        verify(itemRepository).save(newItem);
        verify(itemMapper).toItemDtoResponse(savedItem);
    }

    @Test
    void create_shouldThrowWhenUserNotFound() {
        Long ownerId = 9999L;
        ItemDtoChange requestDto = new ItemDtoChange("Дрель", "Аккумуляторная дрель",
                true, null, null);

        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> itemService.create(ownerId, requestDto)
        );

        assertEquals("Пользователь с id 9999 не найден", exception.getMessage());
        verify(userRepository).findById(ownerId);
        verifyNoInteractions(itemRepository, itemMapper);
    }

    @Test
    void update_shouldUpdateExistingItem() {
        Long userId = 1L;
        Long itemId = 1L;
        User owner = new User();
        owner.setId(userId);

        Item existingItem = new Item();
        existingItem.setId(itemId);
        existingItem.setName("Старое название");
        existingItem.setDescription("Старое описание");
        existingItem.setAvailable(true);
        existingItem.setOwner(owner);

        ItemDtoChange updateDto = new ItemDtoChange();
        updateDto.setName("Новое название");

        ItemDtoResponse expectedResponse = new ItemDtoResponse(itemId, "Новое название",
                "Старое описание", true, null);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(itemMapper.toItemDtoResponse(existingItem)).thenReturn(expectedResponse);

        ItemDtoResponse actualResponse = itemService.update(userId, itemId, updateDto);

        assertNotNull(actualResponse);
        assertEquals("Новое название", actualResponse.getName());
        assertEquals("Старое описание", actualResponse.getDescription());

        verify(itemRepository).findById(itemId);
    }

    @Test
    void update_shouldThrowWhenNoFieldsToUpdate() {
        Long userId = 1L;
        Long itemId = 1L;
        ItemDtoChange updateDto = new ItemDtoChange();

        when(userRepository.existsById(userId)).thenReturn(true);

        BadInputException exception = assertThrows(
                BadInputException.class,
                () -> itemService.update(userId, itemId, updateDto)
        );
        assertEquals("Должно быть задано хотя бы одно поле для обновления вещи",
                exception.getMessage());
        verifyNoInteractions(itemRepository);
    }

    @Test
    void update_shouldThrowWhenUserNotOwner() {
        Long userId = 1L;
        Long ownerId = 2L; // Другой владелец
        Long itemId = 1L;

        User owner = new User();
        owner.setId(ownerId);

        Item existingItem = new Item();
        existingItem.setId(itemId);
        existingItem.setOwner(owner);

        ItemDtoChange updateDto = new ItemDtoChange();
        updateDto.setName("Новое название");

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));

        BadInputException exception = assertThrows(
                BadInputException.class,
                () -> itemService.update(userId, itemId, updateDto)
        );
        assertEquals("Обновить вещь может только владелец вещи", exception.getMessage());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void getItemById_shouldReturnItemWithDetails() {
        Long userId = 1L;
        Long itemId = 1L;

        User user = new User();
        user.setId(userId);

        Item item = new Item();
        item.setId(itemId);

        ItemDtoWithDetails expectedDto = new ItemDtoWithDetails();
        expectedDto.setId(itemId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemMapper.toItemDtoWithDetails(user, item)).thenReturn(expectedDto);

        ItemDtoWithDetails actualDto = itemService.getItemById(userId, itemId);

        assertNotNull(actualDto);
        assertEquals(itemId, actualDto.getId());

        verify(userRepository).findById(userId);
        verify(itemRepository).findById(itemId);
        verify(itemMapper).toItemDtoWithDetails(user, item);
    }

    @Test
    void getItemById_shouldThrowWhenUserNotFound() {
        Long userId = 9999L;
        Long itemId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> itemService.getItemById(userId, itemId)
        );

        assertEquals("Пользователь с id 9999 не найден", exception.getMessage());
        verify(userRepository).findById(userId);
        verifyNoInteractions(itemRepository);
    }

    @Test
    void getItemById_shouldThrowWhenItemNotFound() {
        Long userId = 1L;
        Long itemId = 9999L;

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> itemService.getItemById(userId, itemId)
        );

        assertEquals("Вещь с id 9999 не найдена", exception.getMessage());
        verify(itemRepository).findById(itemId);
    }

    @Test
    void getItemsByOwner_whenUserNotFound_throwsException() {
        Long userId = 99L;
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(DataNotFoundException.class, () -> itemService.getItemsByOwner(userId));
        verify(userRepository).existsById(userId);
    }

    @Test
    void getItemsByOwner_whenNoItems_returnsEmptyList() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.findByOwnerId(userId)).thenReturn(Collections.emptyList());

        List<ItemDtoShort> result = itemService.getItemsByOwner(userId);

        assertTrue(result.isEmpty());
        verify(userRepository).existsById(userId);
        verify(itemRepository).findByOwnerId(userId);
    }

    @Test
    void getItemsByOwner_whenItemsWithoutBookings_returnsDtosWithNullBookings() {
        Long userId = 1L;
        Item item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Аккумуляторная дрель");

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.findByOwnerId(userId)).thenReturn(List.of(item));
        when(bookingRepository.findByItemIdIn(any())).thenReturn(Collections.emptyList());

        List<ItemDtoShort> result = itemService.getItemsByOwner(userId);

        assertEquals(1, result.size());
        ItemDtoShort dto = result.getFirst();
        assertNull(dto.getLastBooking());
        assertNull(dto.getNextBooking());
        verify(bookingRepository).findByItemIdIn(List.of(1L));
    }

    @Test
    void getItemsByOwner_whenItemsWithBookings_returnsDtosWithBookingInfo() {
        Long userId = 1L;
        User owner = new User();
        owner.setId(userId);
        owner.setName("Owner");
        owner.setEmail("owner@mail.com");

        Item item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setOwner(owner);

        User booker = new User();
        booker.setId(2L);

        // Создаем временные метки
        LocalDateTime now = LocalDateTime.now();

        // Прошедшее бронирование
        Booking lastBooking = new Booking();
        lastBooking.setId(10L);
        lastBooking.setBooker(booker);
        lastBooking.setItem(item);
        lastBooking.setStart(now.minusDays(2));
        lastBooking.setEnd(now.minusDays(1));
        lastBooking.setStatus(Status.APPROVED);

        // Будущее бронирование
        Booking nextBooking = new Booking();
        nextBooking.setId(20L);
        nextBooking.setBooker(booker);
        nextBooking.setItem(item);
        nextBooking.setStart(now.plusDays(1));
        nextBooking.setEnd(now.plusDays(2));
        nextBooking.setStatus(Status.APPROVED);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.findByOwnerId(userId)).thenReturn(List.of(item));
        when(bookingRepository.findByItemIdIn(List.of(1L))).thenReturn(List.of(lastBooking, nextBooking));

        List<ItemDtoShort> result = itemService.getItemsByOwner(userId);

        assertEquals(1, result.size());
        ItemDtoShort dto = result.get(0);

        // Проверка lastBooking
        BookingInfoDto lastBookingDto = dto.getLastBooking();
        assertNotNull(lastBookingDto);
        assertEquals(lastBooking.getId(), lastBookingDto.getId());
        assertEquals(booker.getId(), lastBookingDto.getBookerId());

        // Проверка nextBooking
        BookingInfoDto nextBookingDto = dto.getNextBooking();
        assertNotNull(nextBookingDto);
        assertEquals(nextBooking.getId(), nextBookingDto.getId());
        assertEquals(booker.getId(), nextBookingDto.getBookerId());
    }

    @Test
    void searchItems_whenTextIsBlank_returnsEmptyList() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);

        List<ItemDtoResponse> result = itemService.searchItems(userId, "   ");
        assertTrue(result.isEmpty());
    }

    @Test
    void searchItems_whenNoMatchingItems_returnsEmptyList() {
        Long userId = 1L;
        String text = "дрель";
        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.searchAvailableItems(text.toLowerCase())).thenReturn(Collections.emptyList());

        List<ItemDtoResponse> result = itemService.searchItems(userId, text);
        assertTrue(result.isEmpty());
        verify(itemRepository).searchAvailableItems(text.toLowerCase());
    }

    @Test
    void searchItems_whenItemsFound_returnsDtoList() {
        Long userId = 1L;
        String text = "дрель";
        Item item1 = new Item();
        item1.setId(1L);
        Item item2 = new Item();
        item2.setId(2L);

        ItemDtoResponse dto1 = new ItemDtoResponse(1L, "Дрель", "Профессиональная",
                true, null);
        ItemDtoResponse dto2 = new ItemDtoResponse(2L, "Дрель-шуруповерт", "Аккумуляторная",
                true, null);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.searchAvailableItems(text.toLowerCase())).thenReturn(List.of(item1, item2));
        when(itemMapper.toItemDtoResponse(item1)).thenReturn(dto1);
        when(itemMapper.toItemDtoResponse(item2)).thenReturn(dto2);

        List<ItemDtoResponse> result = itemService.searchItems(userId, text);

        assertEquals(2, result.size());
        assertEquals(dto1, result.get(0));
        assertEquals(dto2, result.get(1));
        verify(itemMapper, times(2)).toItemDtoResponse(any());
    }
}