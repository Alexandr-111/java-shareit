package ru.practicum.shareit.user;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    boolean userNotExists(long id);

    User create(User user);

    User update(User user);

    List<User> getAll();

    Optional<User> getUserById(long id);

    void deleteUser(long id);

    boolean findEmail(String email);

    void  resetStorage();
}