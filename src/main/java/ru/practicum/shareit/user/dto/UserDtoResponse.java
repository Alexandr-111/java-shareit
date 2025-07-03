package ru.practicum.shareit.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDtoResponse {
    private Long id;
    private String name;
    private String email;

    public UserDtoResponse(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
}