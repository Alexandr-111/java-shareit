package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.BadInputException;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.item.dto.ItemDtoChange;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoShort;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserStorage;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;
    private final ItemMapper itemMapper;

    @Override
    public ItemDtoResponse create(Long ownerId, ItemDtoChange itemDtoChange) {
        User owner = userStorage.getUserById(ownerId)
                .orElseThrow(() -> new DataNotFoundException("Пользователь с id " + ownerId + " не найден"));
        Item item = itemMapper.toItem(owner, itemDtoChange);
        Item createdItem = itemStorage.create(item);
        return itemMapper.toItemDtoResponse(createdItem);
    }

    @Override
    public ItemDtoResponse update(Long userId, Long itemId, ItemDtoChange itemDtoChange) {
        if (userStorage.userNotExists(userId)) {
            throw new DataNotFoundException("Пользователь с id " + userId + " не найден");
        }

        if (itemDtoChange.getName() == null
                && itemDtoChange.getDescription() == null
                && itemDtoChange.getAvailable() == null) {
            throw new BadInputException("Должно быть задано хотя бы одно поле для обновления вещи");
        }
        Item existingItem = itemStorage.getItemById(itemId)
                .orElseThrow(() -> new DataNotFoundException("Вещь с id " + itemId + " не найдена"));
        Long ownerId = existingItem.getOwner().getId();

        if (!userId.equals(ownerId)) {
            throw new BadInputException("Обновить вещь может только владелец вещи");
        }
        if (itemDtoChange.getName() != null) {
            existingItem.setName(itemDtoChange.getName());
        }
        if (itemDtoChange.getDescription() != null) {
            existingItem.setDescription(itemDtoChange.getDescription());
        }
        if (itemDtoChange.getAvailable() != null) {
            existingItem.setAvailable(itemDtoChange.getAvailable());
        }
        Item updateItem = itemStorage.update(existingItem);
        return itemMapper.toItemDtoResponse(updateItem);
    }

    @Override
    public ItemDtoResponse getItemById(Long userId, Long itemId) {
        if (userStorage.userNotExists(userId)) {
            throw new DataNotFoundException("Пользователь с id " + userId + " не найден");
        }
        Item itemFound = itemStorage.getItemById(itemId)
                .orElseThrow(() -> new DataNotFoundException("Вещь с id " + itemId + " не найдена"));
        return itemMapper.toItemDtoResponse(itemFound);
    }

    @Override
    public List<ItemDtoShort> getItemsByOwner(Long userId) {
        if (userStorage.userNotExists(userId)) {
            throw new DataNotFoundException("Пользователь с id " + userId + " не найден");
        }
        List<Item> items = itemStorage.getItemsByOwner(userId);
        return items.stream()
                .map(element -> ItemDtoShort.builder()
                        .name(element.getName())
                        .description(element.getDescription())
                        .build())
                .toList();
    }

    @Override
    public List<ItemDtoResponse> searchItems(Long userId, String text) {
        if (userStorage.userNotExists(userId)) {
            throw new DataNotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        String search = text.toLowerCase();
        List<Item> items = itemStorage.getAllItems();
        log.debug("Получено {} вещей из ItemStorage", items.size());

        // Отбираем только вещи доступные для аренды
        // и содержащие текст в названии или описании (без учета регистра)
        return items.stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item -> (item.getName() != null && item.getName().toLowerCase().contains(search))
                        || (item.getDescription() != null && item.getDescription().toLowerCase().contains(search)))
                .map(itemMapper::toItemDtoResponse)
                .toList();
    }
}