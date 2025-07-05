package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDtoChange;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.user.User;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class ItemMapper {
    private final ItemRequestMapper itemRequestMapper;

    public ItemDtoResponse toItemDtoResponse(Item item) {
        Objects.requireNonNull(item, "Вещь (Item) не должна быть null");

        ItemDtoResponse.ItemDtoResponseBuilder builder = ItemDtoResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable());

        if (item.getRequest() != null) {
            builder.requestDto(itemRequestMapper.toItemRequestDtoChange(item.getRequest()));
        }
        return builder.build();
    }

    public Item toItem(User owner, ItemDtoChange itemDtoChange) {
        Objects.requireNonNull(itemDtoChange, "ДТО (ItemDtoChange) не должен быть null");
        Objects.requireNonNull(owner, "Владелец (User) не должен быть null");

        Item.ItemBuilder builder = Item.builder()
                .name(itemDtoChange.getName())
                .description(itemDtoChange.getDescription())
                .available(itemDtoChange.getAvailable())
                .owner(owner);

        if (itemDtoChange.getRequestDto() != null) {
            builder.request(itemRequestMapper.toItemRequest(itemDtoChange.getRequestDto()));
        }
        return builder.build();
    }
}