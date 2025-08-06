package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.ItemRequestServiceImpl;
import ru.practicum.shareit.request.dto.ItemRequestDtoChange;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private ItemRequestMapper itemRequestMapper;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    User user = User.builder().id(1L).name("Test User").email("test@example.com").build();
    private final ItemRequestDtoChange requestDto = new ItemRequestDtoChange("Test description",
            null, null);
    private final ItemRequest request = new ItemRequest(1L, "Test description", user,
            LocalDateTime.now(), Collections.<Item>emptyList());
    private final ItemRequestDtoResponse responseDto = new ItemRequestDtoResponse(1L,
            "Test description", LocalDateTime.now(), Collections.<ItemForRequestDto>emptyList());

    @Test
    void create_shouldCreateRequestWhenUserExists() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRequestMapper.toItemRequest(any(ItemRequestDtoChange.class), any(User.class))).thenReturn(request);
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(request);
        when(itemRequestMapper.toItemRequestDtoResponse(any(ItemRequest.class))).thenReturn(responseDto);

        ItemRequestDtoResponse result = itemRequestService.create(requestDto, user.getId());

        assertNotNull(result);
        assertEquals(responseDto.getId(), result.getId());
        assertEquals(responseDto.getDescription(), result.getDescription());

        verify(userRepository).findById(user.getId());
        verify(itemRequestMapper).toItemRequest(requestDto, user);
        verify(itemRequestRepository).save(request);
        verify(itemRequestMapper).toItemRequestDtoResponse(request);
    }

    @Test
    void create_shouldThrowWhenUserNotFound() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> itemRequestService.create(requestDto, userId)
        );

        assertEquals("Пользователь с id " + userId + " не найден", exception.getMessage());
        verify(userRepository).findById(userId);
        verifyNoInteractions(itemRequestMapper, itemRequestRepository);
    }

    @Test
    void getRequestsByOwner_shouldReturnRequestsWhenUserExists() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(itemRequestRepository.findByRequestorIdWithItems(anyLong())).thenReturn(List.of(request));
        when(itemRequestMapper.toItemRequestDtoResponse(any(ItemRequest.class))).thenReturn(responseDto);

        List<ItemRequestDtoResponse> result = itemRequestService.getRequestsByOwner(user.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(responseDto.getId(), result.getFirst().getId());

        verify(userRepository).existsById(user.getId());
        verify(itemRequestRepository).findByRequestorIdWithItems(user.getId());
        verify(itemRequestMapper).toItemRequestDtoResponse(request);
    }

    @Test
    void getRequestsByOwner_shouldThrowWhenUserNotFound() {
        Long userId = 999L;
        when(userRepository.existsById(userId)).thenReturn(false);

        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> itemRequestService.getRequestsByOwner(userId)
        );
        assertEquals("Пользователь с id " + userId + " не найден", exception.getMessage());
        verify(userRepository).existsById(userId);
        verifyNoInteractions(itemRequestRepository, itemRequestMapper);
    }

    @Test
    void getAllRequests_shouldReturnPageOfRequests() {
        // Подготовка параметров пагинации
        int from = 0;
        int size = 10;

        // Создаем объект пагинации
        Pageable pageable = PageRequest.of(from / size, size,
                Sort.by(Sort.Direction.DESC, "created"));

        // Создаем страницу с одним запросом
        Page<ItemRequest> requestPage = new PageImpl<>(
                List.of(request),
                pageable,
                1
        );

        // Мокирование
        when(itemRequestRepository.findAllByOrderByCreatedDesc(pageable))
                .thenReturn(requestPage);
        when(itemRequestMapper.toItemRequestDtoResponse(any(ItemRequest.class)))
                .thenReturn(responseDto);

        // Вызов тестируемого метода
        Page<ItemRequestDtoResponse> result = itemRequestService.getAllRequests(from, size);

        // Проверки
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getContent().size());
        assertEquals(responseDto.getId(), result.getContent().getFirst().getId());

        // Проверка взаимодействий
        verify(itemRequestRepository).findAllByOrderByCreatedDesc(pageable);
        verify(itemRequestMapper).toItemRequestDtoResponse(request);
    }

    @Test
    void getRequestById_shouldReturnRequestWhenExists() {
        Long requestId = 1L;
        when(itemRequestRepository.findByIdWithItems(anyLong())).thenReturn(Optional.of(request));
        when(itemRequestMapper.toItemRequestDtoResponse(any(ItemRequest.class))).thenReturn(responseDto);

        ItemRequestDtoResponse result = itemRequestService.getRequestById(requestId);

        assertNotNull(result);
        assertEquals(responseDto.getId(), result.getId());
        assertEquals(responseDto.getDescription(), result.getDescription());

        verify(itemRequestRepository).findByIdWithItems(requestId);
        verify(itemRequestMapper).toItemRequestDtoResponse(request);
    }

    @Test
    void getRequestById_shouldThrowWhenRequestNotFound() {
        Long requestId = 999L;
        when(itemRequestRepository.findByIdWithItems(requestId)).thenReturn(Optional.empty());

        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> itemRequestService.getRequestById(requestId)
        );

        assertEquals("Запрос с id " + requestId + " не найден", exception.getMessage());
        verify(itemRequestRepository).findByIdWithItems(requestId);
        verify(itemRequestMapper, never()).toItemRequestDtoResponse(any());
    }

    @Test
    void getRequestById_shouldIncludeItemsWhenAvailable() {
        // Подготовка данных с предметами
        ItemRequest requestWithItems = new ItemRequest(1L, "Test description",
                user, LocalDateTime.now(), List.of(new Item()));
        ItemRequestDtoResponse responseWithItems = new ItemRequestDtoResponse(1L,
                "Test description", LocalDateTime.now(), List.of(new ItemForRequestDto()));

        when(itemRequestRepository.findByIdWithItems(anyLong())).thenReturn(Optional.of(requestWithItems));
        when(itemRequestMapper.toItemRequestDtoResponse(requestWithItems)).thenReturn(responseWithItems);

        ItemRequestDtoResponse result = itemRequestService.getRequestById(1L);

        assertNotNull(result);
        assertFalse(result.getItems().isEmpty());

        verify(itemRequestRepository).findByIdWithItems(1L);
        verify(itemRequestMapper).toItemRequestDtoResponse(requestWithItems);
    }
}