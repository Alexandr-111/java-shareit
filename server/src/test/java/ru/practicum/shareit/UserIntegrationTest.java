package ru.practicum.shareit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDtoChange;
import ru.practicum.shareit.user.dto.UserDtoResponse;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void initDatabase() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("test-schema.sql")
            );
        }
    }

    @Test
    void fullUserLifecycleTest() throws Exception {
        // Создание пользователя с валидным email
        UserDtoChange createDto = new UserDtoChange();
        createDto.setEmail("valid-email@example.com");
        createDto.setName("Valid User");

        MvcResult createResult = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("valid-email@example.com"))
                .andReturn();

        String responseJson = createResult.getResponse().getContentAsString();
        Long userId = objectMapper.readTree(responseJson).get("id").asLong();

        // Получение пользователя по ID
        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value("valid-email@example.com"));

        // Обновление пользователя
        UserDtoChange updateDto = new UserDtoChange();
        updateDto.setEmail("updated-email@example.com");
        updateDto.setName("Updated Name");

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated-email@example.com"));

        // Проверка обновления
        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated-email@example.com"));


        // Удаление пользователя
        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("Выполнено"))
                .andExpect(jsonPath("$.description")
                        .value("Пользователь c id " + userId + " удален"));

        // Проверка удаления
        assertThrows(DataNotFoundException.class, () -> userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("Пользователь не найден")));
    }

    @Test
    void shouldPreventDuplicateEmailOnCreate() throws Exception {
        // Первый пользователь - успешное создание
        UserDtoChange user1 = new UserDtoChange();
        user1.setEmail("unique-email@example.com");
        user1.setName("First User");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isCreated());

        // Второй пользователь с тем же email - должен вернуть конфликт
        UserDtoChange user2 = new UserDtoChange();
        user2.setEmail("unique-email@example.com");
        user2.setName("Second User");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Нарушение уникальности"))
                .andExpect(jsonPath("$.description")
                        .value("Такой email уже зарегистрирован, необходимо использовать другой."));
    }

    @Test
    void shouldHandleInvalidEmailFormat() throws Exception {
        // Пользователь с невалидным email
        UserDtoChange invalidUser = new UserDtoChange();
        invalidUser.setEmail("invalid-email");
        invalidUser.setName("Invalid User");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturnAllUsersWithPagination() throws Exception {
        // Создаем двух пользователей с валидными email
        UserDtoChange user1 = new UserDtoChange();
        user1.setEmail("first-user@example.com");
        user1.setName("First User");

        UserDtoChange user2 = new UserDtoChange();
        user2.setEmail("second-user@example.com");
        user2.setName("Second User");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isCreated());

        // Получаем всех пользователей с пагинацией
        MvcResult result = mockMvc.perform(get("/users")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.totalPages").exists())
                .andReturn();

        String response = result.getResponse().getContentAsString();

        // Используем TypeReference для правильного парсинга вложенной структуры
        PageResponse<UserDtoResponse> pageResponse = objectMapper.readValue(response,
                new TypeReference<PageResponse<UserDtoResponse>>() {
                });

        // Проверяем общее количество элементов
        assertEquals(2, pageResponse.getTotalElements());

        // Проверяем количество элементов на странице
        assertEquals(2, pageResponse.getContent().size());
    }
}