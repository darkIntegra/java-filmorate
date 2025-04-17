package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {

    Long createUser(User user);

    void updateUser(User user);

    User getUserById(Long id);

    List<User> getAllUsers();

    void addFriend(Long userId, Long friendId);

    void deleteUser(Long userId);

    void removeFriend(Long userId, Long friendId);

    List<User> getFriends(Long userId);

    List<User> getCommonFriends(Long userId, Long otherUserId);
}