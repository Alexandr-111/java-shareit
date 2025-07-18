package ru.practicum.shareit.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.comment.dto.CommentDtoChange;
import ru.practicum.shareit.comment.dto.CommentDtoResponse;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentMapper {

    public CommentDtoResponse toCommentDtoResponse(User user, Comment comment) {
        Objects.requireNonNull(user, "Пользователь (User) не должен быть null");
        Objects.requireNonNull(comment, "Отзыв (Comment) не должен быть null");

        return CommentDtoResponse.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(user.getName())
                .created(comment.getCreated())
                .build();
    }

    public Comment toComment(Item item, User user, CommentDtoChange commentDtoChange) {
        Objects.requireNonNull(commentDtoChange, "ДТО (CommentDtoChange) не должен быть null");
        return Comment.builder()
                .text(commentDtoChange.getText())
                .item(item)
                .author(user)
                .created(LocalDateTime.now())
                .build();
    }
}