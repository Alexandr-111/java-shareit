package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserDtoChange {
    private String name;
    private String email;
}