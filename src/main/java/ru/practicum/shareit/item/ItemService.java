package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDtoChange;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoShort;

import java.util.List;

public interface ItemService {
    ItemDtoResponse create(Long userId, ItemDtoChange itemDtoChange);

    ItemDtoResponse update(Long userId, Long itemId, ItemDtoChange itemDtoChange);

    ItemDtoResponse getItemById(Long userId, Long itemId);

    List<ItemDtoShort> getItemsByOwner(Long userId);

    List<ItemDtoResponse> searchItems(Long userId, String text) ;
}