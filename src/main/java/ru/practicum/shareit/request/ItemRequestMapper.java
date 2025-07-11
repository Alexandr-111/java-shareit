package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.request.dto.ItemRequestDtoChange;
import ru.practicum.shareit.user.UserMapper;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ItemRequestMapper {
    private final UserMapper userMapper;

    // DTO в сущность
    public ItemRequest toItemRequest(ItemRequestDtoChange dto) {
        Objects.requireNonNull(dto, "ДТО (ItemRequestDtoChange) не должен быть null");

        return ItemRequest.builder()
                .description(dto.getDescription())
                .requestor(userMapper.toUser(dto.getRequestorDto()))
                .created(dto.getCreated())
                .build();
    }

    // Сущность в DTO
    public ItemRequestDtoChange toItemRequestDtoChange(ItemRequest entity) {
        Objects.requireNonNull(entity, "Запрос вещи (ItemRequest) не должен быть null");

        return ItemRequestDtoChange.builder()
                .description(entity.getDescription())
                .requestorDto(userMapper.toUserDtoChange(entity.getRequestor()))
                .created(entity.getCreated())
                .build();
    }
}