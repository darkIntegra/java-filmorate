package ru.yandex.practicum.filmorate.dao.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component("dbUserStorage")
public class DbUserStorage implements UserStorage {

    private final UserDao userDao;

    @Autowired
    public DbUserStorage(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public Long createUser(User user) {
        return userDao.createUser(user);
    }

    @Override
    public void updateUser(User user) {
        userDao.updateUser(user);
    }

    @Override
    public User getUserById(Long id) {
        return userDao.getUserById(id); // уже кидает UserNotFoundException
    }

    @Override
    public List<User> getAllUsers() {
        return userDao.getAllUsers();
    }

    @Override
    public void deleteUser(Long userId) {
        userDao.deleteUser(userId); // внутри уже проверка на существование
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        userDao.addFriend(userId, friendId); // внутренняя валидация есть
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        userDao.removeFriend(userId, friendId); // с проверкой на наличие дружбы
    }

    @Override
    public List<User> getFriends(Long userId) {
        if (!userDao.userExists(userId)) {
            throw new UserNotFoundException("Пользователь с ID " + userId + " не существует.");
        }
        return userDao.getFriends(userId);
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        if (!userDao.userExists(userId)) {
            throw new UserNotFoundException("Пользователь с ID " + userId + " не существует.");
        }
        if (!userDao.userExists(otherUserId)) {
            throw new UserNotFoundException("Пользователь с ID " + otherUserId + " не существует.");
        }

        Set<Long> userFriends = userDao.getFriends(userId).stream()
                .map(User::getId)
                .collect(Collectors.toSet());
        Set<Long> otherUserFriends = userDao.getFriends(otherUserId).stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        return userFriends.stream()
                .filter(otherUserFriends::contains)
                .map(userDao::getUserById)
                .collect(Collectors.toList());
    }
}