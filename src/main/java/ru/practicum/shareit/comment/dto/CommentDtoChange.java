package ru.practicum.shareit.comment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.validate.OnCreate;

@Data
@Builder
public class CommentDtoChange {
    @NotBlank(groups = {OnCreate.class}, message = "Отзыв не должен быть пустым")
    private String text;
}