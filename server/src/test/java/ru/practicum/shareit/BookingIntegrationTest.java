package ru.practicum.shareit;

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
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private BookingRepository bookingRepository;

    private User owner;
    private User booker;
    private Item item;
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String ID_USER = "X-Sharer-User-Id";

    @BeforeEach
    void initDatabase() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("test-schema.sql")
            );
        }

        // Создание тестовых данных
        User user1 = User.builder().id(null).name("Owner").email("owner@example.com").build();
        owner = userRepository.save(user1);
        User user2 = User.builder().id(null).name("Booker").email("booker@example.com").build();
        booker = userRepository.save(user2);
        item = itemRepository.save(Item.builder()
                .name("Item")
                .description("Description")
                .available(true)
                .owner(owner)
                .build());
    }

    @Test
    void shouldCreateGetUpdateBooking() throws Exception {
        // Создание бронирования
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(2);

        String bookingJson = "{\"start\":\"" + start.format(formatter) +
                "\",\"end\":\"" + end.format(formatter) +
                "\",\"itemId\":" + item.getId() + "}";

        MvcResult createResult = mockMvc.perform(post("/bookings")
                        .header(ID_USER, booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andReturn();

        String responseJson = createResult.getResponse().getContentAsString();
        Long bookingId = objectMapper.readTree(responseJson).get("id").asLong();

        // Получение бронирования по ID
        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(ID_USER, booker.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value("WAITING"));

        // Подтверждение бронирования владельцем
        mockMvc.perform(patch("/bookings/{bookingId}?approved=true", bookingId)
                        .header(ID_USER, owner.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        // Проверка обновления статуса
        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(ID_USER, booker.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void shouldValidateBookingDates() throws Exception {
        // Начало бронирования позже окончания
        LocalDateTime validStart = LocalDateTime.now().plusDays(2);
        LocalDateTime beforeStart = validStart.minusDays(1);

        String invalidOrderJson = "{\"start\":\"" + validStart.format(formatter) +
                "\",\"end\":\"" + beforeStart.format(formatter) +
                "\",\"itemId\":" + item.getId() + "}";

        mockMvc.perform(post("/bookings")
                        .header(ID_USER, booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidOrderJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description")
                        .value("Дата начала бронирования не может быть позже даты окончания"));
    }

    @Test
    void shouldHandleBookingCreationErrors() throws Exception {
        // Недоступная вещь
        item.setAvailable(false);
        itemRepository.save(item);

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(2);

        String bookingJson = "{\"start\":\"" + start.format(formatter) +
                "\",\"end\":\"" + end.format(formatter) +
                "\",\"itemId\":" + item.getId() + "}";

        mockMvc.perform(post("/bookings")
                        .header(ID_USER, booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description")
                        .value("Вещь с id " + item.getId() + " недоступна для бронирования"));

        // Несуществующая вещь
        String invalidItemJson = "{\"start\":\"" + start.format(formatter) +
                "\",\"end\":\"" + end.format(formatter) +
                "\",\"itemId\":9999}";

        mockMvc.perform(post("/bookings")
                        .header(ID_USER, booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidItemJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.description").value("Вещь с id 9999 не найдена"));

        // Несуществующий пользователь
        mockMvc.perform(post("/bookings")
                        .header(ID_USER, 9999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.description")
                        .value("Пользователь с id 9999 не найден"));
    }

    @Test
    void shouldHandleApprovalErrors() throws Exception {
        // Создание бронирования
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(2);

        String bookingJson = "{\"start\":\"" + start.format(formatter) +
                "\",\"end\":\"" + end.format(formatter) +
                "\",\"itemId\":" + item.getId() + "}";

        MvcResult createResult = mockMvc.perform(post("/bookings")
                        .header(ID_USER, booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson))
                .andExpect(status().isCreated())
                .andReturn();

        String responseJson = createResult.getResponse().getContentAsString();
        Long bookingId = objectMapper.readTree(responseJson).get("id").asLong();

        // Попытка подтверждения не владельцем
        mockMvc.perform(patch("/bookings/{bookingId}?approved=true", bookingId)
                        .header(ID_USER, booker.getId()))
                .andExpect(status().isBadRequest());

        // Попытка повторного подтверждения
        mockMvc.perform(patch("/bookings/{bookingId}?approved=true", bookingId)
                        .header(ID_USER, owner.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/bookings/{bookingId}?approved=false", bookingId)
                        .header(ID_USER, owner.getId()))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldGetBookingsByUser() throws Exception {
        // Создание бронирований с разными статусами
        createBooking(booker, item, Status.WAITING, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        createBooking(booker, item, Status.APPROVED, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4));
        createBooking(booker, item, Status.REJECTED, LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(6));

        // Получение всех бронирований
        MvcResult allResult = mockMvc.perform(get("/bookings")
                        .header(ID_USER, booker.getId()))
                .andExpect(status().isOk())
                .andReturn();

        List<?> allBookings = objectMapper.readValue(
                allResult.getResponse().getContentAsString(), List.class);
        assertEquals(3, allBookings.size());

        // Фильтрация по статусу
        mockMvc.perform(get("/bookings?state=WAITING")
                        .header(ID_USER, booker.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("WAITING"));
    }

    @Test
    void shouldGetBookingsForOwner() throws Exception {
        // Создание бронирований
        createBooking(booker, item, Status.APPROVED, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        createBooking(booker, item, Status.REJECTED, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4));

        // Получение бронирований владельца
        MvcResult result = mockMvc.perform(get("/bookings/owner")
                        .header(ID_USER, owner.getId()))
                .andExpect(status().isOk())
                .andReturn();

        List<?> ownerBookings = objectMapper.readValue(
                result.getResponse().getContentAsString(), List.class);
        assertEquals(2, ownerBookings.size());

        mockMvc.perform(get("/bookings/owner?state=FUTURE")
                        .header(ID_USER, owner.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldHandleInvalidState() throws Exception {
        mockMvc.perform(get("/bookings?state=INVALID")
                        .header(ID_USER, booker.getId()))
                .andExpect(status().isNotFound());
    }

    private void createBooking(User booker, Item item, Status status,
                               LocalDateTime start, LocalDateTime end) {
        Booking booking = Booking.builder()
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(status)
                .build();
        bookingRepository.save(booking);
    }
}