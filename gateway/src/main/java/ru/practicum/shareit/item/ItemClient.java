package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.comment.dto.CommentDtoChange;
import ru.practicum.shareit.comment.dto.CommentDtoResponse;
import ru.practicum.shareit.exception.ApiOperationException;
import ru.practicum.shareit.item.dto.ItemDtoChange;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoShort;
import ru.practicum.shareit.item.dto.ItemDtoWithDetails;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ItemDtoResponse create(Long userId, ItemDtoChange itemDtoChange) {
        ResponseEntity<Object> response = post("", userId, itemDtoChange);

        if (response.getStatusCode().is2xxSuccessful()) {
            return objectMapper.convertValue(response.getBody(), ItemDtoResponse.class);
        } else {
            if (response.getBody() instanceof byte[]) {
                String errorBody = new String((byte[]) response.getBody(), StandardCharsets.UTF_8);
                throw new ApiOperationException("Ошибка сервера :" + errorBody, response.getStatusCode());
            } else {
                throw new ApiOperationException("Ошибка сервера", response.getStatusCode());
            }
        }
    }

    public ItemDtoResponse update(Long userId, Long itemId, ItemDtoChange itemDtoChange) {

        String path = "/" + itemId;
        ResponseEntity<Object> response = patch(path, userId, itemDtoChange);
        return objectMapper.convertValue(response.getBody(), ItemDtoResponse.class);
    }

    public ResponseEntity<ItemDtoWithDetails> getItemById(Long userId, Long itemId) {
        return getObject("/" + itemId, userId, ItemDtoWithDetails.class);
    }

    public ResponseEntity<List<ItemDtoShort>> getItemsByOwner(Long userId) {
        return getList("", userId, ItemDtoShort.class);
    }

    public ResponseEntity<List<ItemDtoResponse>> searchItems(Long userId, String text) {
        Map<String, Object> queryParams = Map.of("text", text);
        return getList("/search", userId, queryParams, ItemDtoResponse.class);
    }

    public CommentDtoResponse createComment(Long itemId, Long userId, CommentDtoChange commentDtoChange) {
        ResponseEntity<Object> response = post("/" + itemId + "/comment", userId, commentDtoChange);
        return objectMapper.convertValue(response.getBody(), CommentDtoResponse.class);
    }
}