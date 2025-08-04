package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import ru.practicum.shareit.request.dto.ItemRequestDtoChange;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ItemDtoChange {
    private String name;
    private String description;
    private Boolean available;
    private ItemRequestDtoChange requestDto;
    private Long requestId;
}