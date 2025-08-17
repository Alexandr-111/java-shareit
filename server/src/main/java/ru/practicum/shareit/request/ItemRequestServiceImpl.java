package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDtoChange;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRequestMapper itemRequestMapper;

    @Override
    @Transactional
    public ItemRequestDtoResponse create(ItemRequestDtoChange itemRequestDtoChange, Long userId) {
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("Пользователь с id " + userId + " не найден"));
        ItemRequest request = itemRequestMapper.toItemRequest(itemRequestDtoChange, requestor);
        ItemRequest createdRequest = itemRequestRepository.save(request);
        return itemRequestMapper.toItemRequestDtoResponse(createdRequest);
    }

    @Override
    public List<ItemRequestDtoResponse> getRequestsByOwner(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new DataNotFoundException("Пользователь с id " + userId + " не найден");
        }
        List<ItemRequest> itemRequests = itemRequestRepository.findByRequestorIdWithItems(userId);
        return itemRequests.stream()
                .map(itemRequestMapper::toItemRequestDtoResponse)
                .toList();
    }

    @Override
    public Page<ItemRequestDtoResponse> getAllRequests(int from, int size) {
        log.debug("Вызван метод ItemRequestService.getAllRequests()");
        int pageNumber = from / size;
        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by(Sort.Direction.DESC, "created"));
        Page<ItemRequest> itemRequestPage = itemRequestRepository.findAllByOrderByCreatedDesc(pageable);
        return itemRequestPage.map(itemRequestMapper::toItemRequestDtoResponse);
    }

    @Override
    public ItemRequestDtoResponse getRequestById(Long requestId) {
        ItemRequest itemRequest = itemRequestRepository.findByIdWithItems(requestId)
                .orElseThrow(() -> new DataNotFoundException("Запрос с id " + requestId + " не найден"));
        return itemRequestMapper.toItemRequestDtoResponse(itemRequest);
    }
}