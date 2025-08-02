package ru.practicum.shareit.user;

import jakarta.validation.constraints.Positive;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.practicum.shareit.exception.Response;
import ru.practicum.shareit.user.dto.UserDtoChange;
import ru.practicum.shareit.user.dto.UserDtoResponse;
import ru.practicum.shareit.validate.OnCreate;
import ru.practicum.shareit.validate.OnUpdate;

import java.net.URI;
import java.util.List;

@Slf4j
@Controller
@Validated
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserGatewayController {
    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<UserDtoResponse> createUser(@Validated({OnCreate.class, Default.class})
                                                      @RequestBody UserDtoChange userDtoChange) {
        log.debug("UserGatewayController. Создание пользователя из DTO. Получен объект {}", userDtoChange);
        UserDtoResponse readyDto = userClient.create(userDtoChange);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(readyDto.getId())
                .toUri();
        return ResponseEntity.created(location).body(readyDto);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserDtoResponse> updateUser(@Positive(message = "ID должен быть положительным числом")
                                                      @PathVariable Long userId,
                                                      @Validated({OnUpdate.class, Default.class})
                                                      @RequestBody UserDtoChange userDtoChange) {
        log.debug("UserGatewayController. Обновление пользователя с ID {}. Получен объект {}", userId, userDtoChange);
        UserDtoResponse readyDto = userClient.update(userId, userDtoChange);
        return ResponseEntity.ok(readyDto);
    }

    @GetMapping
    public ResponseEntity<List<UserDtoResponse>> getAllUsers() {
        log.debug("UserGatewayController. Начато получение списка всех пользователей");
        return userClient.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDtoResponse> getUser(
            @PathVariable @Positive(message = "Id должен быть больше 0") Long id) {
        log.debug("UserGatewayController. Начато получение пользователя по ID. Получен id {}", id);
        return userClient.getUserById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response> removeUser(
            @PathVariable @Positive(message = "Id должен быть больше 0") Long id) {
        log.debug("UserGatewayController. Начато удаление пользователя по ID. Получен id {}", id);
        userClient.deleteUser(id);
        Response response = new Response("Выполнено", "Пользователь c id " + id + " удален");
        return ResponseEntity.ok(response);
    }
}