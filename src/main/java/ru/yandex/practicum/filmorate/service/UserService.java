package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Service
public class UserService {
    private final UserStorage userStorage;
    private final Map<Long, Set<Long>> friends = new HashMap<>();

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

    /**
     * Добавляет двух пользователей в друзья друг другу.
     * Логика работы:
     * 1. Используется Map<Long, Set<Long>> friends, где ключ — это ID пользователя,
     * а значение — множество ID его друзей.
     * 2. Метод computeIfAbsent проверяет, есть ли уже запись для userId в Map.
     * Если записи нет, создаётся новое пустое множество.
     * 3. В множество друзей пользователя с userId добавляется friendId.
     * 4. Аналогично обновляется множество друзей пользователя с friendId.
     * 5. Таким образом, дружба становится двусторонней.
     */
    public void addFriend(Long userId, Long friendId) {
        // Проверяем существование обоих пользователей
        userStorage.getUserById(userId); // Выбросит исключение, если пользователь не найден
        userStorage.getUserById(friendId); // Выбросит исключение, если пользователь не найден

        friends.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
        friends.computeIfAbsent(friendId, k -> new HashSet<>()).add(userId);
    }

    /**
     * Удаляет двух пользователей из списка друзей друг друга.
     * Логика работы:
     * 1. Используется Map<Long, Set<Long>> friends, где ключ — это ID пользователя,
     * а значение — множество ID его друзей.
     * 2. Метод getOrDefault возвращает множество друзей для userId.
     * Если записи нет, возвращается пустое множество.
     * 3. Из множества друзей пользователя с userId удаляется friendId.
     * 4. Аналогично обновляется множество друзей пользователя с friendId.
     * 5. Таким образом, дружба удаляется с обеих сторон.
     */
    public void removeFriend(Long userId, Long friendId) {
        // Проверяем существование обоих пользователей
        userStorage.getUserById(userId); // Выбросит исключение, если пользователь не найден
        userStorage.getUserById(friendId);

        friends.getOrDefault(userId, Collections.emptySet()).remove(friendId);
        friends.getOrDefault(friendId, Collections.emptySet()).remove(userId);
    }

    public List<User> getFriends(Long userId) {
        // Проверяем существование пользователя
        userStorage.getUserById(userId); // Выбросит исключение, если пользователь не найден

        // Возвращаем список друзей
        return friends.getOrDefault(userId, Collections.emptySet()).stream()
                .map(userStorage::getUserById)
                .toList();
    }

    /**
     * Возвращает список общих друзей двух пользователей.
     * Логика работы:
     * 1. Используется Map<Long, Set<Long>> friends, где ключ — это ID пользователя,
     * а значение — множество ID его друзей.
     * 2. Для каждого пользователя (userId и otherUserId) получаем множество его друзей
     * через friends.getOrDefault(id, Collections.emptySet()).
     * 3. Находим пересечение множеств друзей двух пользователей с помощью метода filter.
     * 4. Для каждого общего друга (ID) получаем объект User через userStorage.getUserById.
     * 5. Преобразуем Stream в List и возвращаем результат.
     */
    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        // Проверяем существование обоих пользователей
        userStorage.getUserById(userId); // Выбросит исключение, если пользователь не найден
        userStorage.getUserById(otherUserId);

        // Находим пересечение множеств друзей
        Set<Long> userFriends = friends.getOrDefault(userId, Collections.emptySet());
        Set<Long> otherUserFriends = friends.getOrDefault(otherUserId, Collections.emptySet());

        return userFriends.stream()
                .filter(otherUserFriends::contains)
                .map(userStorage::getUserById)
                .toList();
    }
}