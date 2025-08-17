package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoChange;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.BadInputException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.ItemUnavailableException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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
        if (bookingDtoChange.getStart().isAfter(bookingDtoChange.getEnd())) {
            throw new BadInputException("Дата начала бронирования не может быть позже даты окончания");
        }
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
            throw new BadInputException("Пользователя с id " + userId + " нет в базе");
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
        return bookingMapper.toBookingDtoResponse(existingBooking);
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
    public Page<BookingDtoResponse> getBookingsByUser(Long userId, BookingState state, int from, int size) {
        if (!userRepository.existsById(userId)) {
            throw new DataNotFoundException("Пользователь с id " + userId + " не найден");
        }
        LocalDateTime now = LocalDateTime.now();
        log.info("Пользователь userId={}, Статус бронирования state={}, текущее время now={}", userId, state, now);
        Page<Booking> bookings;
        Pageable pageable = PageRequest.of(from / size, size,
                Sort.by(Sort.Direction.DESC, "start"));
        switch (state) {
            case ALL:
                bookings = bookingRepository.findByBookerIdOrderByStartDesc(userId, pageable);
                break;
            case CURRENT:
                bookings = bookingRepository.findCurrentByBooker(userId, now, pageable);
                break;
            case PAST:
                bookings = bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(userId, now, pageable);
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId, now, pageable);
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, Status.WAITING, pageable);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, Status.REJECTED, pageable);
                break;
            default:
                throw new IllegalArgumentException("Несуществующий статус бронирования");
        }
        return bookings.map(bookingMapper::toBookingDtoResponse);
    }

    @Override
    public Page<BookingDtoResponse> getBookingsForItems(Long userId, BookingState state, int from, int size) {
        if (!userRepository.existsById(userId)) {
            throw new DataNotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (!userRepository.existsByIdAndItemsIsNotEmpty(userId)) {
            throw new DataNotFoundException("Пользователь с id " + userId + " не является владельцем ни одной вещи");
        }
        LocalDateTime now = LocalDateTime.now();
        Page<Booking> bookings;
        Pageable pageable = PageRequest.of(from / size, size,
                Sort.by(Sort.Direction.DESC, "start"));

        switch (state) {
            case ALL:
                bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(userId, pageable);
                break;
            case CURRENT:
                bookings = bookingRepository.findCurrentByItemOwner(userId, now, pageable);
                break;
            case PAST:
                bookings = bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, now, pageable);
                break;
            case FUTURE:
                bookings = bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(userId, now, pageable);
                break;
            case WAITING:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.WAITING, pageable);
                break;
            case REJECTED:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.REJECTED, pageable);
                break;
            default:
                throw new IllegalArgumentException("Несуществующий статус бронирования");
        }

        return bookings.map(bookingMapper::toBookingDtoResponse);
    }
}