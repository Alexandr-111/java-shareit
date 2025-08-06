package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoChange;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;
import ru.practicum.shareit.user.User;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ItemRequestMapper {
    private final ItemMapper itemMapper;

    public ItemRequest toItemRequest(ItemRequestDtoChange dto, User requestor) {
        Objects.requireNonNull(dto, "ДТО (ItemRequestDtoChange) не должен быть null");

        return ItemRequest.builder()
                .description(dto.getDescription())
                .created(dto.getCreated())
                .requestor(requestor)
                .build();
    }

    public ItemRequestDtoResponse toItemRequestDtoResponse(ItemRequest entity) {
        Objects.requireNonNull(entity, "Запрос вещи (ItemRequest) не должен быть null");

        List<ItemForRequestDto> itemForRequestDtos = entity.getItems().stream()
                .map(itemMapper::toItemForRequestDto)
                .toList();

        return ItemRequestDtoResponse.builder()
                .id(entity.getId())
                .description(entity.getDescription())
                .created(entity.getCreated())
                .items(itemForRequestDtos)
                .build();
    }
}