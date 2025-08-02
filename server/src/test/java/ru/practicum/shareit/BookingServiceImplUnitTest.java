package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingServiceImpl;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingDtoChange;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.exception.BadInputException;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.ItemUnavailableException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDtoInternal;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDtoInternal;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplUnitTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private final LocalDateTime now = LocalDateTime.now();
    private final User user = User.builder().id(1L).name("user").email("user@mail.com").build();
    private final User owner = User.builder().id(2L).name("owner").email("owner@mail.com").build();
    private final Item item = Item.builder()
            .id(1L)
            .name("Item")
            .description("Description")
            .available(true)
            .owner(owner)
            .build();
    private final Booking booking = Booking.builder()
            .id(1L)
            .start(now.plusHours(1))
            .end(now.plusHours(2))
            .item(item)
            .booker(user)
            .status(Status.WAITING)
            .build();

    @Test
    @Transactional
    void create_shouldCreateBookingWhenValid() {
        BookingDtoChange dto = BookingDtoChange.builder()
                .itemId(1L)
                .start(now.plusHours(1))
                .end(now.plusHours(2))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingMapper.toBooking(item, user, dto)).thenReturn(booking);
        when(bookingRepository.save(booking)).thenReturn(booking);

        BookingDtoResponse responseDto = BookingDtoResponse.builder()
                .id(1L)
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(new ItemDtoInternal(item.getId(), item.getName()))
                .booker(new UserDtoInternal(user.getId()))
                .status(booking.getStatus().name())
                .build();
        when(bookingMapper.toBookingDtoResponse(booking)).thenReturn(responseDto);

        BookingDtoResponse result = bookingService.create(1L, dto);

        assertNotNull(result);
        verify(bookingRepository).save(booking);
    }

    @Test
    void create_shouldThrowWhenUserNotFound() {
        BookingDtoChange dto = BookingDtoChange.builder()
                .itemId(1L)
                .start(now.plusHours(1))
                .end(now.plusHours(2))
                .build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(DataNotFoundException.class, () -> bookingService.create(1L, dto));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenItemUnavailable() {
        BookingDtoChange dto = BookingDtoChange.builder()
                .itemId(1L)
                .start(now.plusHours(1))
                .end(now.plusHours(2))
                .build();

        Item unavailableItem = Item.builder()
                .id(1L)
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(false)
                .owner(owner)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(unavailableItem));

        assertThrows(ItemUnavailableException.class, () -> bookingService.create(1L, dto));
        verify(bookingRepository, never()).save(any());
        verify(bookingMapper, never()).toBooking(any(), any(), any());
    }

    @Test
    void update_shouldThrowWhenNotOwner() {

        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(BadInputException.class, () -> bookingService.update(1L, 1L, true));
    }

    @Test
    void getBookingById_shouldReturnWhenOwner() {
        when(userRepository.existsById(owner.getId())).thenReturn(true);
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        BookingDtoResponse responseDto = BookingDtoResponse.builder()
                .id(1L)
                .booker(new UserDtoInternal(1L))
                .build();
        when(bookingMapper.toBookingDtoResponse(booking)).thenReturn(responseDto);

        BookingDtoResponse result = bookingService.getBookingById(owner.getId(), booking.getId());

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
        assertEquals(user.getId(), result.getBooker().getId());
        verify(bookingRepository).findById(booking.getId());
    }
}