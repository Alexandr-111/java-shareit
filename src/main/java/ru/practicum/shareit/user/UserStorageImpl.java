package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class UserStorageImpl implements UserStorage {

    private final Map<Long, User> storageUsers = new HashMap<>();
    private long id = 0;

    public boolean userNotExists(long id) {
        return !(storageUsers.containsKey(id));
    }

    @Override
    public User create(User user) {
        long userId = ++id;
        user.setId(userId);
        storageUsers.put(userId, user);
        return user;
    }

    @Override
    public User update(User user) {
        Long id = user.getId();
        storageUsers.put(id, user);
        return user;
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(storageUsers.values());
    }

    @Override
    public Optional<User> getUserById(long id) {
        return Optional.ofNullable(storageUsers.get(id));
    }

    @Override
    public void deleteUser(long id) {
        storageUsers.remove(id);
    }

    @Override
    public boolean findEmail(String email) {
        List<User> users = storageUsers.values().stream()
                .filter(user -> email.equals(user.getEmail()))
                .toList();
        return !users.isEmpty();
    }

    @Override
    public void resetStorage(){
        storageUsers.clear();
        id = 0;
    }
}