package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.comment.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDtoChange;
import ru.practicum.shareit.item.dto.ItemDtoInternal;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoWithDetails;
import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class ItemMapper {
    private final BookingMapper bookingMapper;
    private final ItemRequestRepository itemRequestRepository;

    public ItemDtoResponse toItemDtoResponse(Item item) {
        Objects.requireNonNull(item, "Вещь (Item) не должна быть null");

        return ItemDtoResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }

    public Item toItem(User owner, ItemDtoChange itemDtoChange) {
        Objects.requireNonNull(itemDtoChange, "ДТО (ItemDtoChange) не должен быть null");
        Objects.requireNonNull(owner, "Владелец (User) не должен быть null");

        Item.ItemBuilder builder = Item.builder()
                .name(itemDtoChange.getName())
                .description(itemDtoChange.getDescription())
                .available(itemDtoChange.getAvailable())
                .owner(owner);

        if (itemDtoChange.getRequestId() != null) {
            ItemRequest itemRequest = itemRequestRepository.findById(itemDtoChange.getRequestId())
                    .orElse(null);
            builder.request(itemRequest);
        }
        return builder.build();
    }

    public ItemDtoInternal toItemDtoInternal(Item item) {
        Objects.requireNonNull(item, "Вещь (Item) не должна быть null");

        return ItemDtoInternal.builder()
                .id(item.getId())
                .name(item.getName())
                .build();
    }

    public ItemDtoWithDetails toItemDtoWithDetails(User user, Item item) {
        Objects.requireNonNull(item, "Вещь (Item) не должна быть null");
        Objects.requireNonNull(user, "Пользователь (User) не должен быть null");

        ItemDtoWithDetails.ItemDtoWithDetailsBuilder builder = ItemDtoWithDetails.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .comments(item.getComments().stream()
                        .map(comment -> CommentMapper.toCommentDtoResponse(user, comment))
                        .toList());

        // Сведения о бронированиях предоставляем только владельцу вещи
        if (item.getOwner().getId().equals(user.getId())) {
            // Получаем последнее бронирование
            Booking last = item.getBookings().stream()
                    .filter(booking -> booking.getStart().isBefore(LocalDateTime.now()))
                    .max(Comparator.comparing(Booking::getStart))
                    .orElse(null);

            // Получаем следующее бронирование
            Booking next = item.getBookings().stream()
                    .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                    .min(Comparator.comparing(Booking::getStart))
                    .orElse(null);

            if (last != null) {
                builder.lastBooking(bookingMapper.toBookingInfoDto(last));
            }
            if (next != null) {
                builder.nextBooking(bookingMapper.toBookingInfoDto(next));
            }
        }
        return builder.build();
    }

    public ItemForRequestDto toItemForRequestDto(Item item) {
        ItemForRequestDto.ItemForRequestDtoBuilder builder = ItemForRequestDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable());

        if (item.getRequest() != null) {
            builder.requestId(item.getRequest().getId());
        } else {
            builder.requestId(null);
        }
        return builder.build();
    }
}