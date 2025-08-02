package ru.practicum.shareit.booking;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
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
import ru.practicum.shareit.validate.OnCreate;

import java.net.URI;
import java.util.List;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingGatewayController {
    public static final String USER_ID = "X-Sharer-User-Id";
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<BookingDtoResponse> createBooking(
            @Positive @RequestHeader(USER_ID) Long userId,
            @Validated({OnCreate.class, Default.class}) @RequestBody BookingDtoChange bookingDtoChange) {
        log.debug("BookingGatewayController. Создание бронирования пользователем с ID {}. Получен BookingDtoChange {}",
                userId, bookingDtoChange);
        BookingDtoResponse readyDto = bookingClient.create(userId, bookingDtoChange);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(readyDto.getId())
                .toUri();
        return ResponseEntity.created(location).body(readyDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingDtoResponse> approveBooking(
            @Positive(message = "ID должен быть положительным") @RequestHeader(USER_ID) Long userId,
            @Positive(message = "ID должен быть положительным") @PathVariable Long bookingId,
            @NotNull(message = "Статус обязателен") @RequestParam(name = "approved") Boolean confirmation) {
        log.debug("BookingGatewayController. Подтверждение запроса на бронирование с ID {}. Пользователем с ID {}.",
                bookingId, userId);
        BookingDtoResponse readyDto = bookingClient.update(userId, bookingId, confirmation);
        return ResponseEntity.ok(readyDto);
    }

    @GetMapping
    public ResponseEntity<List<BookingDtoResponse>> getBookingsByUser(
            @Positive(message = "ID должен быть положительным") @RequestHeader(USER_ID) Long userId,
            @RequestParam(name = "state", required = false, defaultValue = "ALL") String stateParam,
            @PositiveOrZero @RequestParam(name = "from", required = false, defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", required = false, defaultValue = "10") Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new DataNotFoundException("Не найден статус: " + stateParam));
        log.info("BookingGatewayController. Получение списка бронирований: state {}, userId={}, from={}, size={}",
                stateParam, userId, from, size);
        return bookingClient.getBookings(userId, state, from, size);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDtoResponse> foundBooking(
            @Positive(message = "ID должен быть положительным") @RequestHeader(USER_ID) Long userId,
            @Positive(message = "ID должен быть положительным") @PathVariable Long bookingId) {
        log.debug("BookingGatewayController. Получение информации о бронировании с ID {}. Пользователем с ID {}",
                bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingDtoResponse>> getBookingsForItems(
            @Positive(message = "ID должен быть положительным") @RequestHeader(USER_ID) Long userId,
            @RequestParam(name = "state", required = false, defaultValue = "ALL") String stateParam,
            @PositiveOrZero @RequestParam(name = "from", required = false, defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", required = false, defaultValue = "10") Integer size) {

            log.debug("BookingGatewayController. Получение бронирований для всех вещей пользователя с ID {}", userId);
            BookingState state = BookingState.from(stateParam)
                    .orElseThrow(() -> new DataNotFoundException("Не найден статус: " + stateParam));
            return bookingClient.getBookingsForItems(userId, state, from, size);
        }
}