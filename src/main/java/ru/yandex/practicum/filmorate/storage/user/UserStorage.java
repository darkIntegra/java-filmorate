package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Set;

public interface UserStorage {

    Long createUser(User user);

    void updateUser(User user);

    User getUserById(Long id);

    List<User> getAllUsers();

    void addFriend(Long userId, Long friendId);

    void removeFriend(Long userId, Long friendId);

    Set<Long> getFriends(Long userId);
}