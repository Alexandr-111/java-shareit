package ru.practicum.shareit.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.practicum.shareit.request.dto.ItemRequestDtoChange;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;
import ru.practicum.shareit.validate.OnCreate;

import java.net.URI;
import java.util.List;

import static ru.practicum.shareit.booking.BookingGatewayController.USER_ID;

@Slf4j
@Validated
@Controller
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestGatewayController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<ItemRequestDtoResponse> createRequest(
            @Positive @RequestHeader(USER_ID) Long userId,
            @Validated({OnCreate.class, Default.class}) @RequestBody ItemRequestDtoChange itemRequestDtoChange) {
        log.debug("ItemRequestGatewayController. Создание запроса на вещь - пользователем {}. Получен объект {}",
                userId, itemRequestDtoChange);
        ItemRequestDtoResponse readyDto = itemRequestClient.create(userId, itemRequestDtoChange);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(readyDto.getId())
                .toUri();
        return ResponseEntity.created(location).body(readyDto);
    }

    // Метод позволяет получить список своих запросов вместе с данными об ответах на них
    @GetMapping
    public ResponseEntity<List<ItemRequestDtoResponse>> getRequestsByOwner(
            @Positive(message = "ID должен быть положительным") @RequestHeader(USER_ID) Long userId) {
        log.debug("ItemRequestGatewayController. Получение его запросов, пользователем с ID {}", userId);
        return itemRequestClient.getRequestsByOwner(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ItemRequestDtoResponse>> getAllRequests(
            @Positive(message = "ID должен быть положительным") @RequestHeader(USER_ID) Long userId,
            @PositiveOrZero @RequestParam(name = "from", required = false, defaultValue = "0") Integer from,
            @Max(value = 100, message = "Размер страницы не может превышать 100")
            @Positive
            @RequestParam(name = "size", required = false, defaultValue = "10") Integer size) {
        log.debug("ItemRequestGatewayController. Начато получение списка всех запросов на вещи");
        return itemRequestClient.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ItemRequestDtoResponse> foundRequest(
            @Positive(message = "ID должен быть положительным") @RequestHeader(USER_ID) Long userId,
            @Positive(message = "ID должен быть положительным") @PathVariable Long requestId) {
        log.debug("ItemRequestGatewayController. Получение запроса с ID {}. Пользователем с ID {}",
                requestId, userId);
        return itemRequestClient.getRequestById(userId, requestId);
    }
}