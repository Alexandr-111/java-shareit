package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingInfoDto;

@Data
@Builder
public class ItemDtoShort {
    private String name;
    private String description;
    private BookingInfoDto lastBooking;
    private BookingInfoDto nextBooking;
}