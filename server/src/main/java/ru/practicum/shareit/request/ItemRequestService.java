package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDtoChange;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDtoResponse create(ItemRequestDtoChange itemRequestDtoChange, Long userId);

    List<ItemRequestDtoResponse> getRequestsByOwner(Long userId);

    List<ItemRequestDtoResponse> getAllRequests();

    ItemRequestDtoResponse getRequestById(Long requestId);
}