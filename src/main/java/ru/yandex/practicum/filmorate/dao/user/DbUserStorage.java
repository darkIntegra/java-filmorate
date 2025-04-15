package ru.yandex.practicum.filmorate.dao.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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
        return userDao.getUserById(id);
    }

    @Override
    public List<User> getAllUsers() {
        return userDao.getAllUsers();
    }

    @Override
    public void deleteUser(Long userId) {
        userDao.deleteUser(userId);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        if (!userDao.userExists(userId) || !userDao.userExists(friendId)) {
            throw new IllegalArgumentException("Один из пользователей не существует.");
        }
        userDao.addFriend(userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        if (!userDao.userExists(userId) || !userDao.userExists(friendId)) {
            throw new IllegalArgumentException("Один из пользователей не существует.");
        }
        userDao.removeFriend(userId, friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        if (!userDao.userExists(userId)) {
            throw new IllegalArgumentException("Пользователь с ID " + userId + " не существует.");
        }
        Set<Long> friendIds = userDao.getFriends(userId);
        return friendIds.stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        Set<Long> userFriends = userDao.getFriends(userId);
        Set<Long> otherUserFriends = userDao.getFriends(otherUserId);
        return userFriends.stream()
                .filter(otherUserFriends::contains)
                .map(this::getUserById)
                .collect(Collectors.toList());
    }
}