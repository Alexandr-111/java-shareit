package ru.practicum.shareit.user;

import jakarta.validation.constraints.Positive;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
import ru.practicum.shareit.exception.Response;
import ru.practicum.shareit.user.dto.UserDtoChange;
import ru.practicum.shareit.user.dto.UserDtoResponse;
import ru.practicum.shareit.validate.OnCreate;
import ru.practicum.shareit.validate.OnUpdate;

import java.util.List;

@Slf4j
@Controller
@Validated
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDtoResponse> createUser(@Validated({OnCreate.class, Default.class})
                                           @RequestBody UserDtoChange userDtoChange) {
        log.debug("UserController. Создание пользователя из DTO. Получен объект {}", userDtoChange);
        UserDtoResponse readyDto = userService.create(userDtoChange);
        return ResponseEntity.status(HttpStatus.CREATED).body(readyDto);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserDtoResponse> updateUser(@Positive(message = "ID должен быть положительным числом")
                                           @PathVariable Long userId,
                                           @Validated({OnUpdate.class, Default.class})
                                           @RequestBody UserDtoChange userDtoChange) {
        log.debug("UserController. Обновление пользователя с ID {}. Получен объект {}", userId, userDtoChange);
        UserDtoResponse readyDto = userService.update(userId, userDtoChange);
        return ResponseEntity.status(HttpStatus.OK).body(readyDto);
    }

    @GetMapping
    public ResponseEntity<List<UserDtoResponse>> getAllUsers() {
        log.debug("UserController. Начато получение списка всех пользователей");
        List<UserDtoResponse> result = userService.getAll();
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDtoResponse> getUser(@PathVariable @Positive(message = "Id должен быть больше 0") Long id) {
        log.debug("UserController. Начато получение пользователя по ID. Получен id {}", id);
        UserDtoResponse userDto = userService.getUserById(id);
        return ResponseEntity.status(HttpStatus.OK).body(userDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response> removeUser(@PathVariable @Positive(message = "Id должен быть больше 0") Long id) {
        log.debug("UserController. Начато удаление пользователя по ID. Получен id {}", id);
        userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new Response("Выполнено", "Пользователь c id " + id + " удален"));
    }
}