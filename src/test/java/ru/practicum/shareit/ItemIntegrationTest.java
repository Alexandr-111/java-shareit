package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemStorage;
import ru.practicum.shareit.item.dto.ItemDtoChange;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoShort;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserStorage;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ItemIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserStorage userStorage;

    @Autowired
    private ItemStorage itemStorage;

    @BeforeEach
    void clearStorage() {
        itemStorage.resetStorage();
        userStorage.resetStorage();
    }

    @Test
    void createItem_shouldCreateItemAndReturnDto() {
        // Подготовка
        User owner = new User(null, "user@mail.com", "User Name");
        User savedUser = userStorage.create(owner);
        ItemDtoChange requestDto = TestDataFactory.validCreateDto();

        // Формирование запроса
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Sharer-User-Id", savedUser.getId().toString());
        HttpEntity<ItemDtoChange> request = new HttpEntity<>(requestDto, headers);

        // Выполнение запроса
        ResponseEntity<ItemDtoResponse> response = restTemplate.exchange(
                "http://localhost:" + port + "/items",
                HttpMethod.POST,
                request,
                ItemDtoResponse.class
        );

        // Проверки
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        ItemDtoResponse responseBody = response.getBody();
        assertNotNull(responseBody);
        assertNotNull(responseBody.getId());
        assertEquals(requestDto.getName(), responseBody.getName());
        assertEquals(requestDto.getDescription(), responseBody.getDescription());
        assertEquals(requestDto.getAvailable(), responseBody.getAvailable());

        // Проверка сохранения в хранилище
        Item storedItem = itemStorage.getItemById(responseBody.getId()).orElseThrow();
        assertEquals(requestDto.getName(), storedItem.getName());
        assertEquals(savedUser.getId(), storedItem.getOwner().getId());
    }

    @Test
    void createItem_withInvalidUser_shouldReturnNotFound() {
        // Подготовка DTO
        ItemDtoChange requestDto = TestDataFactory.validCreateDto();

        // Формирование запроса с несуществующим ID
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Sharer-User-Id", "9999");
        HttpEntity<ItemDtoChange> request = new HttpEntity<>(requestDto, headers);

        // Выполнение запроса
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/items",
                HttpMethod.POST,
                request,
                String.class
        );

        // Проверки
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Пользователь с id 9999 не найден"));
    }

    @Test
    void getItemsByOwner_shouldReturnOwnerItems() {
        // Подготовка
        User owner = new User(null, "owner@mail.com", "Owner");
        User savedOwner = userStorage.create(owner);
        Item item1 = itemStorage.create(new Item("Дрель", "Мощная дрель",
                true, savedOwner, null));
        Item item2 = itemStorage.create(new Item("Отвертка", "Крестовая",
                true, savedOwner, null));

        // Формирование запроса
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Sharer-User-Id", savedOwner.getId().toString());
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Выполнение запроса
        ResponseEntity<ItemDtoShort[]> response = restTemplate.exchange(
                "http://localhost:" + port + "/items",
                HttpMethod.GET,
                request,
                ItemDtoShort[].class
        );

        // Проверки
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<ItemDtoShort> items = Arrays.asList(response.getBody());
        assertEquals(2, items.size());

        // Проверяем первую вещь
        ItemDtoShort firstItem = items.getFirst();
        assertEquals(item1.getName(), firstItem.getName());
        assertEquals(item1.getDescription(), firstItem.getDescription());

        // Проверяем вторую вещь
        ItemDtoShort secondItem = items.get(1);
        assertEquals(item2.getName(), secondItem.getName());
        assertEquals(item2.getDescription(), secondItem.getDescription());
    }

    @Test
    void getItemsByOwner_withInvalidUser_shouldReturnNotFound() {
        // Формирование запроса с несуществующим ID
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Sharer-User-Id", "9999");
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Выполнение запроса
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/items",
                HttpMethod.GET,
                request,
                String.class
        );

        // Проверки
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Пользователь с id 9999 не найден"));
    }
}