package ru.yandex.practicum.filmorate.dao.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;

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
    public void addFriend(Long userId, Long friendId) {
        // Логика добавления друзей в базу данных
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        // Логика удаления друзей из базы данных
    }

    @Override
    public Set<Long> getFriends(Long userId) {
        // Логика получения списка друзей из базы данных
        return null;
    }
}