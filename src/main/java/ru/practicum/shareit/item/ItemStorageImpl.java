package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ItemStorageImpl implements ItemStorage {
    private final Map<Long, Item> storageItems = new HashMap<>();
    private long id = 0;

    public boolean itemNotExists(long id) {
        return !(storageItems.containsKey(id));
    }

    @Override
    public Item create(Item item) {
        long itemId = ++id;
        item.setId(itemId);
        storageItems.put(itemId, item);
        return item;
    }

    @Override
    public Item update(Item item) {
        Long id = item.getId();
        storageItems.put(id, item);
        return item;
    }

    @Override
    public Optional<Item> getItemById(long itemId) {
        return Optional.ofNullable(storageItems.get(itemId));
    }

    @Override
    public List<Item> getItemsByOwner(Long userId) {
        return storageItems.values().stream()
                .filter(element -> userId.equals(element.getOwner().getId()))
                .toList();
    }

    @Override
    public List<Item> getAllItems() {
        return storageItems.values().stream()
                .toList();
    }

    @Override
    public void resetStorage() {
        storageItems.clear();
        id = 0;
    }
}