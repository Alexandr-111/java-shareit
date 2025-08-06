package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.ItemForRequestDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemRequestDtoResponse {
    private Long id;
    private String description;
    private LocalDateTime created;
    private List<ItemForRequestDto> items; // Вещи, созданные в ответ на запрос
}