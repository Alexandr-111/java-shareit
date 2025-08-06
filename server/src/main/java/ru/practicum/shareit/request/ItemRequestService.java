package ru.practicum.shareit.request;

import org.springframework.data.domain.Page;
import ru.practicum.shareit.request.dto.ItemRequestDtoChange;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDtoResponse create(ItemRequestDtoChange itemRequestDtoChange, Long userId);

    List<ItemRequestDtoResponse> getRequestsByOwner(Long userId);


    ItemRequestDtoResponse getRequestById(Long requestId);

    Page<ItemRequestDtoResponse> getAllRequests(int from, int size);
}