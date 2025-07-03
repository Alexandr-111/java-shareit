package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDtoChange;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.user.User;

import java.util.Objects;

@Slf4j
@Component
public class ItemMapper {
    public ItemDtoResponse toItemDtoResponse(Item item) {
        Objects.requireNonNull(item, "Вещь (Item) не должна быть null");
        return new ItemDtoResponse(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest() != null ? item.getRequest() : null);
    }

    public Item toItem(User owner, ItemDtoChange itemDtoChange) {
        Objects.requireNonNull(itemDtoChange, "ДТО (ItemDtoChange) не должен быть null");
        Objects.requireNonNull(owner, "Владелец (User) не должен быть null");
        return new Item(
                itemDtoChange.getName(),
                itemDtoChange.getDescription(),
                itemDtoChange.getAvailable(),
                owner,
                itemDtoChange.getRequest()
        );
    }
}