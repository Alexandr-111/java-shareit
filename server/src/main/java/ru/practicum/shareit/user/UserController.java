package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.practicum.shareit.PageResponse;
import ru.practicum.shareit.exception.Response;
import ru.practicum.shareit.user.dto.UserDtoChange;
import ru.practicum.shareit.user.dto.UserDtoResponse;

import java.net.URI;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDtoResponse> createUser(@RequestBody UserDtoChange userDtoChange) {
        log.debug("UserController. Создание пользователя из DTO. Получен объект {}", userDtoChange);
        UserDtoResponse readyDto = userService.create(userDtoChange);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(readyDto.getId())
                .toUri();
        return ResponseEntity.created(location).body(readyDto);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserDtoResponse> updateUser(@PathVariable Long userId,
                                                      @RequestBody UserDtoChange userDtoChange) {
        log.debug("UserController. Обновление пользователя с ID {}. Получен объект {}", userId, userDtoChange);
        UserDtoResponse readyDto = userService.update(userId, userDtoChange);
        return ResponseEntity.ok(readyDto);
    }

    @GetMapping
    public ResponseEntity<PageResponse<UserDtoResponse>> getAllUsers(
            @RequestParam(name = "from", required = false, defaultValue = "0") Integer from,
            @RequestParam(name = "size", required = false, defaultValue = "10") Integer size) {
        log.debug("UserController. Начато получение списка всех пользователей");
        Page<UserDtoResponse> page = userService.getAll(from, size);

        PageResponse<UserDtoResponse> response = new PageResponse<>();
        response.setContent(page.getContent());
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDtoResponse> getUser(@PathVariable Long id) {
        log.debug("UserController. Начато получение пользователя по ID. Получен id {}", id);
        UserDtoResponse userDto = userService.getUserById(id);
        log.info("UserController. Получен один пользователь с ID {} и данными: {}", id, userDto);
        return ResponseEntity.ok(userDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response> removeUser(@PathVariable Long id) {
        log.debug("UserController. Начато удаление пользователя по ID. Получен id {}", id);
        userService.deleteUser(id);
        Response response = new Response("Выполнено",
                "Пользователь c id " + id + " удален");
        return ResponseEntity.ok(response);
    }
}