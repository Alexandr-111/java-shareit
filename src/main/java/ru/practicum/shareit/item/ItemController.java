package ru.practicum.shareit.item;

import jakarta.validation.constraints.Positive;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.shareit.item.dto.ItemDtoChange;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoShort;
import ru.practicum.shareit.validate.OnCreate;
import ru.practicum.shareit.validate.OnUpdate;

import java.util.List;

@Slf4j
@Controller
@Validated
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemDtoResponse> createItem(@Positive
                                                      @RequestHeader("X-Sharer-User-Id") Long userId,
                                                      @Validated({OnCreate.class, Default.class})
                                                      @RequestBody ItemDtoChange itemDtoChange) {
        log.debug("ItemController. Создание вещи пользователем с ID {}. Получен объект {}",
                userId, itemDtoChange);
        ItemDtoResponse readyDto = itemService.create(userId, itemDtoChange);
        return ResponseEntity.status(HttpStatus.CREATED).body(readyDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDtoResponse> updateItem(@Positive(message = "ID должен быть положительным")
                                                      @RequestHeader("X-Sharer-User-Id") Long userId,
                                                      @Positive(message = "ID должен быть положительным")
                                                      @PathVariable Long itemId,
                                                      @Validated({OnUpdate.class, Default.class})
                                                      @RequestBody ItemDtoChange itemDtoChange) {
        log.debug("ItemController. Обновление вещи с ID {}. Пользователем с ID {}. Получен объект {}",
                itemId, userId, itemDtoChange);
        ItemDtoResponse readyDto = itemService.update(userId, itemId, itemDtoChange);
        return ResponseEntity.status(HttpStatus.OK).body(readyDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDtoResponse> foundItem(@Positive(message = "ID должен быть положительным")
                                                     @RequestHeader("X-Sharer-User-Id") Long userId,
                                                     @Positive(message = "ID должен быть положительным")
                                                     @PathVariable Long itemId) {
        log.debug("ItemController. Получение вещи с ID {}. Пользователем с ID {}", itemId, userId);
        ItemDtoResponse readyDto = itemService.getItemById(userId, itemId);
        return ResponseEntity.status(HttpStatus.OK).body(readyDto);
    }

    // Просмотр владельцем списка всех его вещей, с указанием названия и описания для каждой из них
    @GetMapping
    public ResponseEntity<List<ItemDtoShort>> getItemsByOwner(@Positive(message = "ID должен быть положительным")
                                                              @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.debug("ItemController. Получение всех его вещей, пользователем с ID {}", userId);
        List<ItemDtoShort> itemDtos = itemService.getItemsByOwner(userId);
        return ResponseEntity.status(HttpStatus.OK).body(itemDtos);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDtoResponse>> searchItems(@Positive(message = "ID должен быть положительным")
                                                             @RequestHeader("X-Sharer-User-Id") Long userId,
                                                             @RequestParam String text) {
        log.debug("ItemController. Поиск вещи по запросу - {}, пользователем с ID {}", text, userId);
        List<ItemDtoResponse> itemDtos = itemService.searchItems(userId, text);
        return ResponseEntity.status(HttpStatus.OK).body(itemDtos);
    }
}