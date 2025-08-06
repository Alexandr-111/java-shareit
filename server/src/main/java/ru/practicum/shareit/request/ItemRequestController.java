package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.practicum.shareit.PageResponse;
import ru.practicum.shareit.request.dto.ItemRequestDtoChange;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;

import java.net.URI;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    public static final String USER_ID = "X-Sharer-User-Id";
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ResponseEntity<ItemRequestDtoResponse> createRequest(
            @RequestHeader(USER_ID) Long userId,
            @RequestBody ItemRequestDtoChange itemRequestDtoChange) {
        log.debug("ItemRequestController. Создание запроса на вещь - пользователем {}. Получен объект {}",
                userId, itemRequestDtoChange);
        ItemRequestDtoResponse readyDto = itemRequestService.create(itemRequestDtoChange, userId);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(readyDto.getId())
                .toUri();
        return ResponseEntity.created(location).body(readyDto);
    }

    // Метод позволяет получить список своих запросов вместе с данными об ответах на них
    @GetMapping
    public ResponseEntity<List<ItemRequestDtoResponse>> getRequestsByOwner(@RequestHeader(USER_ID) Long userId) {
        log.debug("ItemRequestController. Получение его запросов, пользователем с ID {}", userId);
        List<ItemRequestDtoResponse> itemRequestDtos = itemRequestService.getRequestsByOwner(userId);
        return ResponseEntity.ok(itemRequestDtos);
    }

    @GetMapping("/all")
    public ResponseEntity<PageResponse<ItemRequestDtoResponse>> getAllRequests(
            @RequestParam(name = "from", required = false, defaultValue = "0") Integer from,
            @RequestParam(name = "size", required = false, defaultValue = "10") Integer size) {
        log.debug("ItemRequestController. Начато получение списка всех запросов");
        Page<ItemRequestDtoResponse> page = itemRequestService.getAllRequests(from, size);

        PageResponse<ItemRequestDtoResponse> response = new PageResponse<>();
        response.setContent(page.getContent());
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ItemRequestDtoResponse> foundRequest(@RequestHeader(USER_ID) Long userId,
                                                               @PathVariable Long requestId) {
        log.debug("ItemRequestController. Получение запроса с ID {}. Пользователем с ID {}", requestId, userId);
        ItemRequestDtoResponse dtoResponse = itemRequestService.getRequestById(requestId);
        return ResponseEntity.ok(dtoResponse);
    }
}