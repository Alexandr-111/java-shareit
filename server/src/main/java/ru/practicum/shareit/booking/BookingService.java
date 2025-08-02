package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDtoChange;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.dto.BookingState;

import java.util.List;

public interface BookingService {
    BookingDtoResponse create(Long userId, BookingDtoChange bookingDtoChange);

    BookingDtoResponse update(Long userId, Long bookingId, Boolean confirmation);

    BookingDtoResponse getBookingById(Long userId, Long bookingId);

    List<BookingDtoResponse> getBookingsByUser(Long userId, BookingState bookingState);

    List<BookingDtoResponse> getBookingsForItems(Long userId, BookingState bookingState);
}