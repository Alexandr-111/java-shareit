package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingInfoDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDtoShort {
    private String name;
    private String description;
    private BookingInfoDto lastBooking;
    private BookingInfoDto nextBooking;
}