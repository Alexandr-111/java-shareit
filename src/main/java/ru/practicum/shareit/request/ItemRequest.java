package ru.practicum.shareit.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.validate.OnCreate;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ItemRequest {
    Long id;
    @NotBlank(groups = {OnCreate.class}, message = "Описание запроса обязательно")
    String description;
    User requestor; // пользователь, создавший запрос;
    LocalDateTime created; //дата и время создания запроса

@Builder
    public ItemRequest(String description, User requestor, LocalDateTime created) {
        this.description = description;
        this.requestor = requestor;
        this.created = created;
    }
}