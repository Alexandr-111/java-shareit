package ru.practicum.shareit.item;

import jakarta.validation.constraints.Positive;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.practicum.shareit.comment.dto.CommentDtoChange;
import ru.practicum.shareit.comment.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoChange;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoShort;
import ru.practicum.shareit.item.dto.ItemDtoWithDetails;
import ru.practicum.shareit.validate.OnCreate;
import ru.practicum.shareit.validate.OnUpdate;

import java.net.URI;
import java.util.List;

import static ru.practicum.shareit.booking.BookingGatewayController.USER_ID;

@Slf4j
@Controller
@Validated
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemGatewayController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<ItemDtoResponse> createItem(
            @Positive @RequestHeader(USER_ID) Long userId,
            @Validated({OnCreate.class, Default.class}) @RequestBody ItemDtoChange itemDtoChange) {
        log.debug("ItemGatewayController. Создание вещи пользователем с ID {}. Получен объект {}",
                userId, itemDtoChange);
        ItemDtoResponse readyDto = itemClient.create(userId, itemDtoChange);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(readyDto.getId())
                .toUri();
        return ResponseEntity.created(location).body(readyDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDtoResponse> updateItem(
            @Positive(message = "ID должен быть положительным") @RequestHeader(USER_ID) Long userId,
            @Positive(message = "ID должен быть положительным") @PathVariable Long itemId,
            @Validated({OnUpdate.class, Default.class}) @RequestBody ItemDtoChange itemDtoChange) {
        log.debug("ItemGatewayController. Обновление вещи с ID {}. Пользователем с ID {}. Получен объект {}",
                itemId, userId, itemDtoChange);
        ItemDtoResponse readyDto = itemClient.update(userId, itemId, itemDtoChange);
        return ResponseEntity.ok(readyDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDtoWithDetails> foundItem(
            @Positive(message = "ID должен быть положительным") @RequestHeader(USER_ID) Long userId,
            @Positive(message = "ID должен быть положительным") @PathVariable Long itemId) {
        log.debug("ItemGatewayController. Получение вещи с ID {}. Пользователем с ID {}", itemId, userId);
        return itemClient.getItemById(userId, itemId);
    }

    // Просмотр владельцем списка всех его вещей, с указанием названия и описания для каждой из них
    @GetMapping
    public ResponseEntity<List<ItemDtoShort>> getItemsByOwner(
            @Positive(message = "ID должен быть положительным") @RequestHeader(USER_ID) Long userId) {
        log.debug("ItemGatewayController. Получение всех его вещей, пользователем с ID {}", userId);
        return itemClient.getItemsByOwner(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDtoResponse>> searchItems(
            @Positive(message = "ID должен быть положительным") @RequestHeader(USER_ID) Long userId,
            @RequestParam String text) {
        log.debug("ItemGatewayController. Поиск вещи по запросу {}, пользователем с ID {}", text, userId);
        return itemClient.searchItems(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentDtoResponse> createComment(
            @Positive @RequestHeader(USER_ID) Long userId,
            @Positive(message = "ID должен быть положительным") @PathVariable Long itemId,
            @Validated({OnCreate.class, Default.class}) @RequestBody CommentDtoChange commentDtoChange) {
        log.debug("ItemGatewayController. Создание отзыва на вещь с ID {}, пользователем с ID {}. Получен объект {}",
                itemId, userId, commentDtoChange);
        CommentDtoResponse readyDto = itemClient.createComment(itemId, userId, commentDtoChange);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(readyDto.getId())
                .toUri();
        return ResponseEntity.created(location).body(readyDto);
    }
}