package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import ru.practicum.shareit.user.dto.UserDtoChange;

import java.time.LocalDateTime;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ItemRequestDtoChange {
    private String description;
    private UserDtoChange requestorDto;
    private LocalDateTime created;
}