package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.UserDtoChange;
import ru.practicum.shareit.user.dto.UserDtoResponse;

import java.util.List;
import java.util.Map;

@Service
public class UserClient extends BaseClient {
    private static final String API_PREFIX = "/users";

    @Autowired
    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public UserDtoResponse create(UserDtoChange userDtoChange) {
        ResponseEntity<Object> response = post("", userDtoChange);
        return objectMapper.convertValue(response.getBody(), UserDtoResponse.class);
    }

    public UserDtoResponse update(Long userId, UserDtoChange userDtoChange) {

        String path = "/" + userId;
        ResponseEntity<Object> response = patch(path, userId, userDtoChange);
        return objectMapper.convertValue(response.getBody(), UserDtoResponse.class);
    }

    public ResponseEntity<List<UserDtoResponse>> getAll(Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return getList("", parameters, UserDtoResponse.class);
    }

    public ResponseEntity<UserDtoResponse> getUserById(Long id) {
        return getObject("/" + id, id, UserDtoResponse.class);
    }

    public void deleteUser(Long id) {
        String path = "/" + id;
        delete(path);
    }
}