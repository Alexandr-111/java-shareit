package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingInfoDto;
import ru.practicum.shareit.comment.Comment;
import ru.practicum.shareit.comment.CommentMapper;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.comment.dto.CommentDtoChange;
import ru.practicum.shareit.comment.dto.CommentDtoResponse;
import ru.practicum.shareit.exception.BadInputException;
import ru.practicum.shareit.exception.CommentNotAllowedException;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.item.dto.ItemDtoChange;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoShort;
import ru.practicum.shareit.item.dto.ItemDtoWithDetails;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final ItemMapper itemMapper;

    @Override
    @Transactional
    public ItemDtoResponse create(Long ownerId, ItemDtoChange itemDtoChange) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new DataNotFoundException("Пользователь с id " + ownerId + " не найден"));
        Item item = itemMapper.toItem(owner, itemDtoChange);
        Item createdItem = itemRepository.save(item);
        return itemMapper.toItemDtoResponse(createdItem);
    }

    @Override
    @Transactional
    public ItemDtoResponse update(Long userId, Long itemId, ItemDtoChange itemDtoChange) {
        if (!userRepository.existsById(userId)) {
            throw new DataNotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (itemDtoChange.getName() == null
                && itemDtoChange.getDescription() == null
                && itemDtoChange.getAvailable() == null) {
            throw new BadInputException("Должно быть задано хотя бы одно поле для обновления вещи");
        }
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new DataNotFoundException("Вещь с id " + itemId + " не найдена"));
        Long ownerId = existingItem.getOwner().getId();

        if (!userId.equals(ownerId)) {
            throw new BadInputException("Обновить вещь может только владелец вещи");
        }
        if (itemDtoChange.getName() != null) {
            existingItem.setName(itemDtoChange.getName());
        }
        if (itemDtoChange.getDescription() != null) {
            existingItem.setDescription(itemDtoChange.getDescription());
        }
        if (itemDtoChange.getAvailable() != null) {
            existingItem.setAvailable(itemDtoChange.getAvailable());
        }
        return itemMapper.toItemDtoResponse(existingItem);
    }

    @Override
    public ItemDtoWithDetails getItemById(Long userId, Long itemId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("Пользователь с id " + userId + " не найден"));
        Item itemFound = itemRepository.findById(itemId)
                .orElseThrow(() -> new DataNotFoundException("Вещь с id " + itemId + " не найдена"));
        return itemMapper.toItemDtoWithDetails(user, itemFound);
    }

    @Override
    public List<ItemDtoShort> getItemsByOwner(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new DataNotFoundException("Пользователь с id " + userId + " не найден");
        }
        List<Item> items = itemRepository.findByOwnerId(userId);
        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .toList();

        // Получаем бронирования для всех вещей одним запросом
        Map<Long, List<Booking>> bookingsMap = bookingRepository.findByItemIdIn(itemIds).stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));

        return items.stream()
                .map(item -> {
                    ItemDtoShort dto = ItemDtoShort.builder()
                            .name(item.getName())
                            .description(item.getDescription())
                            .build();

                    List<Booking> itemBookings = bookingsMap.getOrDefault(item.getId(), Collections.emptyList());

                    // Находим последнее завершенное бронирование
                    Optional<Booking> lastBooking = itemBookings.stream()
                            .filter(booking -> booking.getEnd().isBefore(LocalDateTime.now())
                                    && booking.getStatus() == Status.APPROVED)
                            .max(Comparator.comparing(Booking::getEnd));

                    // Находим ближайшее будущее бронирование
                    Optional<Booking> nextBooking = itemBookings.stream()
                            .filter(booking -> booking.getStart().isAfter(LocalDateTime.now())
                                    && booking.getStatus() == Status.APPROVED)
                            .min(Comparator.comparing(Booking::getStart));

                    lastBooking.ifPresent(booking -> dto.setLastBooking(
                            new BookingInfoDto(booking.getId(), booking.getBooker().getId())
                    ));

                    nextBooking.ifPresent(booking -> dto.setNextBooking(
                            new BookingInfoDto(booking.getId(), booking.getBooker().getId())
                    ));
                    return dto;
                })
                .toList();
    }

    @Override
    public List<ItemDtoResponse> searchItems(Long userId, String text) {
        if (!userRepository.existsById(userId)) {
            throw new DataNotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        String searchText = text.toLowerCase();
        List<Item> items = itemRepository.searchAvailableItems(searchText);
        log.debug("Получено {} вещей из ItemStorage", items.size());
        return items.stream()
                .map(itemMapper::toItemDtoResponse)
                .toList();
    }

    @Override
    @Transactional
    public CommentDtoResponse createComment(Long itemId, Long userId, CommentDtoChange commentDtoChange) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("Пользователь с id " + userId + " не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new DataNotFoundException("Вещь с id " + itemId + " не найдена"));

        if (item.getOwner().getId().equals(userId)) {
            throw new CommentNotAllowedException("Владелец не может оставлять отзывы на свою вещь");
        }
        List<Booking> bookings = bookingRepository.findBookingsForCommentCheck(
                userId, itemId, Status.APPROVED);
        log.info("Найдено бронирований: {}", bookings.size());

        boolean isCompletedBooking = bookings.stream()
                .anyMatch(booking ->
                        Instant.now().isAfter(
                                booking.getEnd().atOffset(ZoneOffset.UTC).toInstant()));

        if (!isCompletedBooking) {
            throw new CommentNotAllowedException("Пользователь не брал вещь в аренду или аренда еще не завершена");
        }
        Comment comment = CommentMapper.toComment(item, user, commentDtoChange);
        Comment createdComment = commentRepository.save(comment);
        return CommentMapper.toCommentDtoResponse(user, createdComment);
    }
}