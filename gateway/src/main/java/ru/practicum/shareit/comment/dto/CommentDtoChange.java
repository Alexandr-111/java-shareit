package ru.practicum.shareit.comment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.validate.OnCreate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDtoChange {
    @NotBlank(groups = {OnCreate.class}, message = "Отзыв не должен быть пустым")
    private String text;
}