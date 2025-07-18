package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoChange;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.exception.BadInputException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.ItemUnavailableException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingDtoResponse create(Long userId, BookingDtoChange bookingDtoChange) {
        log.info("Создание бронорования. Слой сервиса. Пользователь userId={}, ДТО bookingDtoChange={}",
                userId, bookingDtoChange);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("Пользователь с id " + userId + " не найден"));
        Long itemId = bookingDtoChange.getItemId();
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new DataNotFoundException("Вещь с id " + itemId + " не найдена"));
        if (!item.getAvailable()) {
            throw new ItemUnavailableException("Вещь с id " + itemId + " недоступна для бронирования");
        }
        Booking booking = bookingMapper.toBooking(item, user, bookingDtoChange);
        Booking createdBooking = bookingRepository.save(booking);
        return bookingMapper.toBookingDtoResponse(createdBooking);
    }

    @Override
    @Transactional
    public BookingDtoResponse update(Long userId, Long bookingId, Boolean confirmation) {
        if (!userRepository.existsById(userId)) {
            throw new BadInputException("Пользователя с id " + userId + "нет в базе");
        }

        Booking existingBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new DataNotFoundException("Бронирование с id " + bookingId + " не найдено"));

        Long ownerId = existingBooking.getItem().getOwner().getId();
        if (!userId.equals(ownerId)) {
            throw new BadInputException("Подтвердить бронирование может только владелец вещи");
        }

        if (existingBooking.getStatus() == Status.APPROVED || existingBooking.getStatus() == Status.REJECTED) {
            throw new ConflictException("Нельзя изменить статус бронирования, оно уже подтверждено или отклонено");
        }

        Status newStatus = confirmation ? Status.APPROVED : Status.REJECTED;
        existingBooking.setStatus(newStatus);

        Booking updateBooking = bookingRepository.save(existingBooking);
        return bookingMapper.toBookingDtoResponse(updateBooking);
    }

    @Override
    public BookingDtoResponse getBookingById(Long userId, Long bookingId) {
        if (!userRepository.existsById(userId)) {
            throw new DataNotFoundException("Пользователь с id " + userId + " не найден");
        }
        Booking existingBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new DataNotFoundException("Бронирование с id " + bookingId + " не найдено"));
        if (!(userId.equals(existingBooking.getItem().getOwner().getId())
                || userId.equals(existingBooking.getBooker().getId()))) {
            throw new BadInputException("Информация доступна только владелецу вещи или автору бронирования");
        }
        return bookingMapper.toBookingDtoResponse(existingBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDtoResponse> getBookingsByUser(Long userId, BookingState state) {
        if (!userRepository.existsById(userId)) {
            throw new DataNotFoundException("Пользователь с id " + userId + " не найден");
        }
        LocalDateTime now = LocalDateTime.now();
        log.info("Пользователь userId={}, Статус бронирования state={}, текущее время now={}", userId, state, now);
        List<Booking> bookings;
        switch (state) {
            case ALL:
                bookings = bookingRepository.findByBookerIdOrderByStartDesc(userId);
                break;
            case CURRENT:
                bookings = bookingRepository.findCurrentByBooker(userId);
                break;
            case PAST:
                bookings = bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(userId, now);
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId, now);
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, Status.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, Status.REJECTED);
                break;
            default:
                throw new IllegalArgumentException("Несуществующий статус бронирования");
        }
        log.info("Result bookings: {}", bookings);
        List<BookingDtoResponse> bookingsDtos = bookings.stream()
                .map(bookingMapper::toBookingDtoResponse)
                .toList();
        log.info("Result BookingDtoResponse: {}", bookingsDtos);
        return bookingsDtos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDtoResponse> getBookingsForItems(Long userId, BookingState bookingState) {
        if (!userRepository.existsById(userId)) {
            throw new DataNotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (!userRepository.existsByIdAndItemsIsNotEmpty(userId)) {
            throw new DataNotFoundException("Пользователь с id " + userId + " не является владельцем ни одной вещи");
        }
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (bookingState) {
            case ALL:
                bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(userId);
                break;
            case CURRENT:
                bookings = bookingRepository.findCurrentByItemOwner(userId);
                break;
            case PAST:
                bookings = bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, now);
                break;
            case FUTURE:
                bookings = bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(userId, now);
                break;
            case WAITING:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.REJECTED);
                break;
            default:
                throw new IllegalArgumentException("Несуществующий статус бронирования");
        }

        return bookings.stream()
                .map(bookingMapper::toBookingDtoResponse)
                .toList();
    }
}