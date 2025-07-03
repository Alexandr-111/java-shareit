package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.request.ItemRequest;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemDtoResponse {
    Long id;
    String name;
    String description;
    Boolean available;
    ItemRequest request;

    @Builder
    public ItemDtoResponse( Long id, String name, String description, Boolean available, ItemRequest request) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.available = available;
        this.request = request;
    }
}