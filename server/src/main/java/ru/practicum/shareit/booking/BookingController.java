package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.practicum.shareit.booking.dto.BookingDtoChange;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.DataNotFoundException;

import java.net.URI;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private static final String USER_ID = "X-Sharer-User-Id";
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingDtoResponse> createBooking(@RequestHeader(USER_ID) Long userId,
                                                            @RequestBody BookingDtoChange bookingDtoChange) {
        log.debug("Создание бронирования пользователем с ID {}. Получен объект BookingDtoChange {}",
                userId, bookingDtoChange);
        BookingDtoResponse readyDto = bookingService.create(userId, bookingDtoChange);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(readyDto.getId())
                .toUri();
        return ResponseEntity.created(location).body(readyDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingDtoResponse> approveBooking(
            @RequestHeader(USER_ID) Long userId,
            @PathVariable Long bookingId,
            @RequestParam(name = "approved") Boolean confirmation) {
        log.debug("Подтверждение запроса на бронирование с ID {}. Пользователем с ID {}.", bookingId, userId);
        BookingDtoResponse readyDto = bookingService.update(userId, bookingId, confirmation);
        return ResponseEntity.ok(readyDto);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDtoResponse> foundBooking(@RequestHeader(USER_ID) Long userId,
                                                           @PathVariable Long bookingId) {
        log.debug("Получение информации о бронировании с ID {}. Пользователем с ID {}", bookingId, userId);
        BookingDtoResponse readyDto = bookingService.getBookingById(userId, bookingId);
        return ResponseEntity.ok(readyDto);
    }

    @GetMapping
    public ResponseEntity<List<BookingDtoResponse>> getBookingsByUser(
            @RequestHeader(USER_ID) Long userId,
            @RequestParam(name = "state", required = false, defaultValue = "ALL") String stateParam) {
        log.debug("Получение списка бронирований пользователя с ID {}", userId);
        BookingState bookingState;
        try {
            bookingState = BookingState.valueOf(stateParam.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DataNotFoundException("Не найден статус: " + stateParam);
        }
        List<BookingDtoResponse> bookings = bookingService.getBookingsByUser(userId, bookingState);
        System.out.println("Found bookings: " + bookings.size());
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingDtoResponse>> getBookingsForItems(
            @RequestHeader(USER_ID) Long userId,
            @RequestParam(name = "state", required = false, defaultValue = "ALL") String stateParam) {
        log.debug("Получение бронирований для всех вещей пользователя с ID {}", userId);
        BookingState bookingState;
        try {
            bookingState = BookingState.valueOf(stateParam.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DataNotFoundException("Не найден статус: " + stateParam);
        }
        List<BookingDtoResponse> bookings = bookingService.getBookingsForItems(userId, bookingState);
        return ResponseEntity.ok(bookings);
    }
}