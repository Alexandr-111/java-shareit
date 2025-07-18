package ru.practicum.shareit.item;

import java.util.List;
import java.util.Optional;

public interface ItemStorage {

    Item create(Item item);

    Item update(Item item);

    Optional<Item> getItemById(long itemId);

    List<Item> getItemsByOwner(Long userId);

    List<Item> getAllItems();

    void  resetStorage();
}