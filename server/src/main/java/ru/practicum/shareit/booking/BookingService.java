package ru.practicum.shareit.booking;

import org.springframework.data.domain.Page;
import ru.practicum.shareit.booking.dto.BookingDtoChange;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.dto.BookingState;

public interface BookingService {
    BookingDtoResponse create(Long userId, BookingDtoChange bookingDtoChange);

    BookingDtoResponse update(Long userId, Long bookingId, Boolean confirmation);

    BookingDtoResponse getBookingById(Long userId, Long bookingId);

    Page<BookingDtoResponse> getBookingsByUser(Long userId, BookingState state, int from, int size);

    Page<BookingDtoResponse> getBookingsForItems(Long userId, BookingState state, int from, int size);
}