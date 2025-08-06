package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class BookingDtoChange {
    private LocalDateTime start;
    private LocalDateTime end;
    private Long itemId;
}