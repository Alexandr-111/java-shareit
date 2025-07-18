package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.user.dto.UserDtoChange;
import ru.practicum.shareit.user.dto.UserDtoResponse;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserStorage;
import ru.practicum.shareit.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserStorage userStorage;

    private UserDtoChange validUserDtoChange;

    @BeforeEach
    void setUp() {
        userStorage.resetStorage();
        validUserDtoChange = new UserDtoChange("Иван Иванов", "ivan@example.com");
    }

    @Test
    void getUserById_ValidId_ReturnsOkWithUserDto() throws Exception {
        UserDtoResponse createdUser = userService.create(validUserDtoChange);

        mockMvc.perform(get("/users/" + createdUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", is(createdUser.getId().intValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", is(createdUser.getName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email", is(createdUser.getEmail())));
    }

    @Test
    void getUserById_InvalidId_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/users/9999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void createUser_ValidInput_ReturnsCreatedWithUserDto() throws Exception {
        String userDtoJson = objectMapper.writeValueAsString(validUserDtoChange);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userDtoJson))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", is("Иван Иванов")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email", is("ivan@example.com")));

        // Проверка, что пользователь действительно создан в хранилище
        User createdUser = userStorage.getUserById(1).orElseThrow(() -> new DataNotFoundException("Пользователь не найден."));

        assertNotNull(createdUser);
        assertEquals(1, createdUser.getId());
        assertEquals(validUserDtoChange.getName(), createdUser.getName());
        assertEquals(validUserDtoChange.getEmail(), createdUser.getEmail());
    }

    @Test
    void createUser_InvalidInput_ReturnsBadRequest() throws Exception {
        UserDtoChange invalidUserDto = new UserDtoChange("", "invalid-email");
        String userDtoJson = objectMapper.writeValueAsString(invalidUserDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userDtoJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void removeUser_ValidId_ReturnsOkWithSuccessMessage() throws Exception {
        User user = User.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build();
        User createdUser = userStorage.create(user);
        Long id = createdUser.getId();

        mockMvc.perform(delete("/users/" + 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name")
                        .value("Выполнено"))
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$.description").value("Пользователь c id " + id + " удален"));

        // Проверяем, что пользователь действительно удален из хранилища
        assertTrue((userStorage).getAll().isEmpty());
    }

    @Test
    void removeUser_InvalidId_ReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/users/9999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}