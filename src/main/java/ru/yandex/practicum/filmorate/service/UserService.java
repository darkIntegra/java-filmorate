package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public User getUserById(Long id) {
        return userStorage.getUserById(id);
    }

    // Добавляет двух пользователей в друзья друг другу.
    public void addFriend(Long userId, Long friendId) {
        // Проверяем существование обоих пользователей
        userStorage.getUserById(userId); // Выбросит исключение, если пользователь не найден
        userStorage.getUserById(friendId);
        userStorage.addFriend(userId, friendId);
    }

    //Удаляет двух пользователей из списка друзей друг друга.
    public void removeFriend(Long userId, Long friendId) {
        // Проверяем существование обоих пользователей
        userStorage.getUserById(userId); // Выбросит исключение, если пользователь не найден
        userStorage.getUserById(friendId);
        userStorage.removeFriend(userId, friendId);
    }

    // Возвращает список друзей пользователя
    public List<User> getFriends(Long userId) {
        // Проверяем существование пользователя
        userStorage.getUserById(userId); // Выбросит исключение, если пользователь не найден

        return userStorage.getFriends(userId).stream()
                .map(userStorage::getUserById)
                .toList();
    }

    // Возвращает список общих друзей двух пользователей.
    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        // Проверяем существование обоих пользователей
        userStorage.getUserById(userId); // Выбросит исключение, если пользователь не найден
        userStorage.getUserById(otherUserId);

        // Находим пересечение множеств друзей
        Set<Long> userFriends = userStorage.getFriends(userId);
        Set<Long> otherUserFriends = userStorage.getFriends(otherUserId);

        return userFriends.stream()
                .filter(otherUserFriends::contains)
                .map(userStorage::getUserById)
                .toList();
    }
}