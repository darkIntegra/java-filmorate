package ru.yandex.practicum.filmorate.dao.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

@Repository
public class UserDao {
    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    public final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long createUser(User user) {
        // Валидация входных данных
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email не может быть пустым.");
        }
        if (user.getLogin() == null || user.getLogin().isEmpty()) {
            throw new IllegalArgumentException("Login не может быть пустым.");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Дата рождения не может быть в будущем.");
        }
        // SQL-запрос
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        // Логирование
        log.debug("Создание пользователя: email={}, login={}", user.getEmail(), user.getLogin());
        // Выполнение запроса и получение сгенерированного ID
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, user.getEmail());
                ps.setString(2, user.getLogin());
                ps.setString(3, user.getName());
                ps.setDate(4, user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null);
                return ps;
            }, keyHolder);

            if (keyHolder.getKey() == null) {
                throw new RuntimeException("Не удалось получить сгенерированный ID пользователя.");
            }
            return keyHolder.getKey().longValue();
        } catch (DataAccessException e) {
            log.error("Ошибка при создании пользователя: email={}, login={}", user.getEmail(), user.getLogin(), e);
            throw new RuntimeException("Не удалось создать пользователя", e);
        }
    }

    public void updateUser(User user) {
        // Валидация входных данных
        if (user.getId() == null) {
            throw new IllegalArgumentException("ID пользователя не может быть null.");
        }
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email не может быть пустым.");
        }
        if (user.getLogin() == null || user.getLogin().isEmpty()) {
            throw new IllegalArgumentException("Login не может быть пустым.");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Дата рождения не может быть в будущем.");
        }
        // Проверка существования пользователя
        if (!userExists(user.getId())) {
            log.warn("Попытка обновить несуществующего пользователя с ID: {}", user.getId());
            throw new IllegalArgumentException("Пользователь с ID " + user.getId() + " не существует.");
        }
        // SQL-запрос
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        // Логирование
        log.debug("Обновление пользователя с ID: {}", user.getId());
        // Выполнение запроса
        try {
            int rowsAffected = jdbcTemplate.update(sql,
                    user.getEmail(),
                    user.getLogin(),
                    user.getName(),
                    user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null,
                    user.getId());
            if (rowsAffected == 0) {
                log.warn("Пользователь с ID {} не найден", user.getId());
                throw new IllegalArgumentException("Пользователь с ID " + user.getId() + " не существует.");
            }
        } catch (DataAccessException e) {
            log.error("Ошибка при обновлении пользователя с ID {}", user.getId(), e);
            throw new RuntimeException("Не удалось обновить пользователя", e);
        }
    }

    public User getUserById(Long id) {
        // Проверка входных данных
        if (id == null) {
            throw new IllegalArgumentException("ID пользователя не может быть null.");
        }
        // SQL-запрос
        String sql = "SELECT * FROM users WHERE id = ?";
        log.debug("Выполняется запрос на получение пользователя с ID: {}", id);
        try {
            return jdbcTemplate.queryForObject(sql, this::mapRowToUser, id);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Пользователь с ID {} не найден", id);
            throw new RuntimeException("User with id " + id + " not found");
        }
    }

    public List<User> getAllUsers() {
        String sql = "SELECT * FROM users";
        log.debug("Выполняется запрос на получение всех пользователей");

        try {
            return jdbcTemplate.query(sql, this::mapRowToUser);
        } catch (DataAccessException e) {
            log.error("Ошибка при получении списка пользователей", e);
            throw new RuntimeException("Не удалось получить список пользователей", e);
        }
    }

    public void addFriend(Long userId, Long friendId) {
        // Проверка существования пользователей
        if (!userExists(userId)) {
            throw new IllegalArgumentException("Пользователь с ID " + userId + " не существует.");
        }
        if (!userExists(friendId)) {
            throw new IllegalArgumentException("Пользователь с ID " + friendId + " не существует.");
        }
        // SQL-запросы для добавления дружбы
        String sql = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'CONFIRMED')";
        try {
            // Добавляем дружбу от userId к friendId
            jdbcTemplate.update(sql, userId, friendId);
            log.debug("Добавлена дружба: userId={}, friendId={}", userId, friendId);
            // Добавляем дружбу от friendId к userId
            jdbcTemplate.update(sql, friendId, userId);
            log.debug("Добавлена дружба: userId={}, friendId={}", friendId, userId);
        } catch (DataAccessException e) {
            log.error("Ошибка при добавлении дружбы: userId={}, friendId={}", userId, friendId, e);
            throw new RuntimeException("Не удалось добавить дружбу", e);
        }
    }

    public void removeFriend(Long userId, Long friendId) {
        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        try {
            // Удаляем дружбу от userId к friendId
            jdbcTemplate.update(sql, userId, friendId);
            log.debug("Удалена дружба: userId={}, friendId={}", userId, friendId);
            // Удаляем дружбу от friendId к userId
            jdbcTemplate.update(sql, friendId, userId);
            log.debug("Удалена дружба: userId={}, friendId={}", friendId, userId);
        } catch (DataAccessException e) {
            log.error("Ошибка при удалении дружбы: userId={}, friendId={}", userId, friendId, e);
            throw new RuntimeException("Не удалось удалить дружбу", e);
        }
    }

    public List<User> getFriends(Long userId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships f ON u.id = f.friend_id " +
                "WHERE f.user_id = ? AND f.status = 'CONFIRMED'";
        return jdbcTemplate.query(sql, this::mapRowToUser, userId);
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        return user;
    }

    private boolean userExists(Long id) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }
}