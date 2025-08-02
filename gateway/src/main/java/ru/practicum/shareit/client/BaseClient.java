package ru.practicum.shareit.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.shareit.exception.CustomApiException;
import ru.practicum.shareit.exception.NetworkException;
import ru.practicum.shareit.exception.ServerResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ru.practicum.shareit.booking.BookingGatewayController.USER_ID;

@Slf4j
@Service
public class BaseClient {
    protected final RestTemplate rest;
    protected final ObjectMapper objectMapper = new ObjectMapper();

    {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public BaseClient(RestTemplate rest) {
        this.rest = rest;
    }

    protected <T> List<T> convertToList(Object rawBody, Class<T> elementType) {
        try {
            JsonNode jsonNode = objectMapper.valueToTree(rawBody);

            if (jsonNode.isArray()) {
                List<T> result = new ArrayList<>();
                for (JsonNode node : jsonNode) {
                    result.add(objectMapper.treeToValue(node, elementType));
                }
                return result;
            }
            throw new IllegalArgumentException("Объект не является JSON-массивом");
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка конвертации JSON", e);
        }
    }

    protected <T> ResponseEntity<List<T>> getList(String path, Class<T> responseType) {
        return getList(path, null, null, responseType);
    }

    protected <T> ResponseEntity<List<T>> getList(String path, Long userId, Class<T> responseType) {
        return getList(path, userId, null, responseType);
    }

    protected <T> ResponseEntity<List<T>> getList(String path, Long userId, Map<String, Object> parameters,
                                                  Class<T> responseType) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(path);
        if (parameters != null && !parameters.isEmpty()) {
            parameters.forEach(builder::queryParam);
        }
        String fullPath = builder.toUriString();

        ResponseEntity<Object> rawResponse = makeAndSendRequest(
                HttpMethod.GET,
                fullPath,
                userId,
                null,
                null
        );
        if (rawResponse.getStatusCode().is2xxSuccessful() && rawResponse.getBody() != null) {
            try {
                List<T> result = convertToList(rawResponse.getBody(), responseType);
                return ResponseEntity.ok(result);
            } catch (RuntimeException e) {
                throw new CustomApiException("Ошибка преобразования списка", HttpStatus.INTERNAL_SERVER_ERROR, e);
            }
        }
        return ResponseEntity.status(rawResponse.getStatusCode()).body(null);
    }

    protected <T> ResponseEntity<T> getObject(String path, Long userId, Class<T> responseType) {
        return getObject(path, userId, null, responseType);
    }

    protected <T> ResponseEntity<T> getObject(String path, Long userId,
                                              @Nullable Map<String, Object> parameters, Class<T> responseType) {

        log.info("Отправление GET запроса: path: {}, userId: {}, params: {}", path, userId, parameters);
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(path);

        if (parameters != null && !parameters.isEmpty()) {
            parameters.forEach(builder::queryParam);
        }
        String fullPath = builder.toUriString();
        ResponseEntity<Object> rawResponse = makeAndSendRequest(HttpMethod.GET, fullPath, userId,
                null, null);
        log.info("Полученый ответ: status={}, body={}", rawResponse.getStatusCode(), rawResponse.getBody());

        if (rawResponse.getStatusCode().is2xxSuccessful() && rawResponse.getBody() != null) {
            try {
                String jsonBody = objectMapper.writeValueAsString(rawResponse.getBody());
                T result = objectMapper.readValue(jsonBody, responseType);
                return ResponseEntity.ok(result);
            } catch (JsonProcessingException | IllegalArgumentException e) {
                throw new CustomApiException("Ошибка преобразования ответа", HttpStatus.INTERNAL_SERVER_ERROR, e);
            }
        }
        return ResponseEntity.status(rawResponse.getStatusCode()).body(null);
    }

    protected <T> ResponseEntity<Object> post(String path, T body) {
        return post(path, null, body);
    }

    protected <T> ResponseEntity<Object> post(String path, Long userId, T body) {
        return makeAndSendRequest(HttpMethod.POST, path, userId, null, body);
    }

    protected <T> ResponseEntity<Object> put(String path, long userId,
                                             @Nullable Map<String, Object> parameters, T body) {
        return makeAndSendRequest(HttpMethod.PUT, path, userId, parameters, body);
    }

    protected <T> ResponseEntity<Object> patch(String path, T body) {
        return patch(path, null, null, body);
    }

    protected <T> ResponseEntity<Object> patch(String path, long userId,
                                               @Nullable Map<String, Object> parameters) {
        return patch(path, userId, parameters, null);
    }

    protected <T> ResponseEntity<Object> patch(String path, long userId, T body) {
        return patch(path, userId, null, body);
    }

    protected <T> ResponseEntity<Object> patch(String path, Long userId,
                                               @Nullable Map<String, Object> parameters, T body) {

        String fullPath = path;
        if (parameters != null && !parameters.isEmpty()) {
            fullPath = buildPathWithParams(path, parameters);
        }
        return makeAndSendRequest(HttpMethod.PATCH, fullPath, userId, null, body);
    }

    private String buildPathWithParams(String path, @Nullable Map<String, Object> parameters) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(path);
        if (parameters != null) {
            parameters.forEach((key, value) -> {
                if (value != null) {
                    builder.queryParam(key, value);
                }
            });
        }
        return builder.toUriString();
    }

    protected void delete(String path) {
        makeAndSendRequest(HttpMethod.DELETE, path, null, null, null);
    }

    private HttpHeaders defaultHeaders(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (userId != null) {
            headers.set(USER_ID, String.valueOf(userId));
        }
        return headers;
    }

    private <T> ResponseEntity<Object> makeAndSendRequest(HttpMethod method,
                                                          String path,
                                                          Long userId,
                                                          @Nullable Map<String, Object> parameters,
                                                          @Nullable T body) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body, defaultHeaders(userId));
        try {
            if (parameters != null) {
                return rest.exchange(path, method, requestEntity, Object.class, parameters);
            }
            return rest.exchange(path, method, requestEntity, Object.class);
        } catch (HttpStatusCodeException e) {
            throw new ServerResponseException(e.getResponseBodyAsString(), e.getStatusCode());
        } catch (ResourceAccessException e) {
            throw new NetworkException("Ошибка подключения: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Внутренняя ошибка шлюза", e);
        }
    }
}