package ru.practicum.shareit.booking;

import lombok.Data;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Data
public class Booking {
    Long id; // уникальный идентификатор бронирования;
    LocalDateTime start;// дата и время начала бронирования;
    LocalDateTime end; //дата и время конца бронирования;
    Item item; // вещь, которую пользователь бронирует;
    User booker; // пользователь, который осуществляет бронирование;
    Status status;
}