package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.ItemRequestDtoChange;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;

import java.util.List;

@Service
public class ItemRequestClient extends BaseClient {
    private static final String API_PREFIX = "/requests";

    @Autowired
    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ItemRequestDtoResponse create(Long userId, ItemRequestDtoChange itemRequestDtoChange) {
        ResponseEntity<Object> response = post("", userId, itemRequestDtoChange);
        return objectMapper.convertValue(response.getBody(), ItemRequestDtoResponse.class);
    }

    public ResponseEntity<List<ItemRequestDtoResponse>> getRequestsByOwner(Long userId) {
        return getList("", userId, ItemRequestDtoResponse.class);
    }

    public ResponseEntity<List<ItemRequestDtoResponse>> getAllRequests(Long userId) {
        return getList("/all", userId, ItemRequestDtoResponse.class);
    }

    public ResponseEntity<ItemRequestDtoResponse> getRequestById(Long userId, Long requestId) {
        return getObject("/" + requestId, userId, ItemRequestDtoResponse.class);
    }
}