package ru.yandex.practicum.filmorate.dao.user;

import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

@Repository
public class UserDao {
    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long createUser(User user) {
        validateUser(user, false);

        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
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
            log.error("Ошибка при создании пользователя: {}", user, e);
            throw new RuntimeException("Не удалось создать пользователя", e);
        }
    }

    public void updateUser(User user) {
        validateUser(user, true);

        if (!userExists(user.getId())) {
            throw new UserNotFoundException("Пользователь с ID " + user.getId() + " не найден.");
        }

        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        try {
            jdbcTemplate.update(sql,
                    user.getEmail(),
                    user.getLogin(),
                    user.getName(),
                    user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null,
                    user.getId());
        } catch (DataAccessException e) {
            log.error("Ошибка при обновлении пользователя с ID {}", user.getId(), e);
            throw new RuntimeException("Не удалось обновить пользователя", e);
        }
    }

    public User getUserById(Long id) {
        if (id == null) {
            throw new ValidationException("ID пользователя не может быть null.");
        }

        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, this::mapRowToUser, id);
        } catch (EmptyResultDataAccessException e) {
            throw new UserNotFoundException("Пользователь с ID " + id + " не найден.");
        }
    }

    public List<User> getAllUsers() {
        try {
            return jdbcTemplate.query("SELECT * FROM users", this::mapRowToUser);
        } catch (DataAccessException e) {
            log.error("Ошибка при получении списка пользователей", e);
            throw new RuntimeException("Не удалось получить список пользователей", e);
        }
    }

    public void deleteUser(Long userId) {
        if (!userExists(userId)) {
            throw new UserNotFoundException("Пользователь с ID " + userId + " не найден.");
        }

        String sql = "DELETE FROM users WHERE id = ?";
        try {
            jdbcTemplate.update(sql, userId);
        } catch (DataAccessException e) {
            log.error("Ошибка при удалении пользователя с ID {}", userId, e);
            throw new RuntimeException("Не удалось удалить пользователя", e);
        }
    }

    public void addFriend(Long userId, Long friendId) {
        if (!userExists(userId)) {
            throw new UserNotFoundException("Пользователь с ID " + userId + " не найден.");
        }
        if (!userExists(friendId)) {
            throw new UserNotFoundException("Пользователь с ID " + friendId + " не найден.");
        }
        if (friendshipExists(userId, friendId)) {
            log.warn("Дружба между userId={} и friendId={} уже существует.", userId, friendId);
            throw new IllegalArgumentException("Дружба между userId=" + userId + " и friendId=" + friendId + " уже существует.");
        }

        String sql = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'CONFIRMED')";
        try {
            jdbcTemplate.update(sql, userId, friendId);
            log.debug("Дружба добавлена: userId={}, friendId={}", userId, friendId);
        } catch (DataAccessException e) {
            log.error("Ошибка при добавлении дружбы: userId={}, friendId={}", userId, friendId, e);
            throw new RuntimeException("Не удалось добавить дружбу", e);
        }
    }

    public void removeFriend(Long userId, Long friendId) {
        if (!userExists(userId)) {
            throw new UserNotFoundException("Пользователь с ID " + userId + " не найден.");
        }
        if (!userExists(friendId)) {
            throw new UserNotFoundException("Пользователь с ID " + friendId + " не найден.");
        }

        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        try {
            int result = jdbcTemplate.update(sql, userId, friendId);
            if (result == 0) {
                log.warn("Дружба между userId={} и friendId={} не существует. Запись не удалена.", userId, friendId);
                // Не выбрасываем исключение, чтобы избежать статуса 404
            } else {
                log.debug("Дружба успешно удалена: userId={}, friendId={}", userId, friendId);
            }
        } catch (DataAccessException e) {
            log.error("Ошибка при удалении дружбы: userId={}, friendId={}", userId, friendId, e);
            throw new RuntimeException("Не удалось удалить дружбу", e);
        }
    }

    public List<User> getFriends(Long userId) {
        if (!userExists(userId)) {
            throw new UserNotFoundException("Пользователь с ID " + userId + " не найден.");
        }

        String sql = "SELECT u.* FROM users u JOIN friendships f ON u.id = f.friend_id WHERE f.user_id = ? " +
                "AND f.status = 'CONFIRMED'";
        try {
            return jdbcTemplate.query(sql, this::mapRowToUser, userId);
        } catch (DataAccessException e) {
            log.error("Ошибка при получении списка друзей для userId={}", userId, e);
            throw new RuntimeException("Не удалось получить список друзей", e);
        }
    }

    boolean userExists(Long id) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    private boolean friendshipExists(Long userId, Long friendId) {
        String sql = "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, friendId);
        return count != null && count > 0;
    }

    private void validateUser(User user, boolean isUpdate) {
        if (isUpdate && user.getId() == null) {
            throw new ValidationException("ID пользователя обязателен при обновлении.");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ValidationException("Email не может быть пустым.");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            throw new ValidationException("Login не может быть пустым.");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем.");
        }
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();

        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));

        Date sqlDate = rs.getDate("birthday");
        if (sqlDate != null) {
            user.setBirthday(sqlDate.toLocalDate());
        }

        user.setFriendships(new HashSet<>());

        return user;
    }
}