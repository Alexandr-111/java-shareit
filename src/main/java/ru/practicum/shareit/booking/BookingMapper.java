package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDtoChange;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.dto.BookingInfoDto;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.dto.ItemDtoInternal;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingMapper {
    private final UserMapper userMapper;

    public BookingDtoResponse toBookingDtoResponse(Booking booking) {
        Objects.requireNonNull(booking, "Бронирование (Booking) не должно быть null");

        BookingDtoResponse.BookingDtoResponseBuilder builder = BookingDtoResponse.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus().name());


        // Делаем ДТО вручную без ItemMapper, иначе возникает цикл между ItemMapper и UserMapper
        if (booking.getItem() != null) {
            ItemDtoInternal itemDtoInternal = ItemDtoInternal.builder()
                    .id(booking.getItem().getId())
                    .name(booking.getItem().getName())
                    .build();
            builder.item(itemDtoInternal);
        }
        if (booking.getBooker() != null) {
            builder.booker(userMapper.toUserDtoInternal(booking.getBooker()));
        }
        return builder.build();
    }

    public Booking toBooking(Item item, User user, BookingDtoChange bookingDtoChange) {
        Objects.requireNonNull(user, "Вещь (Item) не должна быть null");
        Objects.requireNonNull(bookingDtoChange, "ДТО (BookingDtoChange) не должен быть null");
        Objects.requireNonNull(user, "Пользователь (User) не должен быть null");


        return Booking.builder()
                .start(bookingDtoChange.getStart())
                .end(bookingDtoChange.getEnd())
                .item(item)
                .booker(user)
                .status(Status.WAITING)
                .build();
    }

    public BookingInfoDto toBookingInfoDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        return BookingInfoDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .build();
    }
}