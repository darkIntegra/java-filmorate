package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody User user) {
        validateUser(user); // Валидация
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin()); // Если имя пустое, используем логин
        }
        long id = getNextId();
        user.setId(id);
        users.put(id, user);
        log.info("Добавлен новый пользователь: {}", user.getLogin());
        return user;
    }

    @PutMapping("/{userId}")
    public User updateUser(@PathVariable Long userId, @RequestBody User updatedUser) {
        validateUser(updatedUser); // Валидация
        if (!users.containsKey(userId)) {
            throw new ValidationException("Пользователь с ID " + userId + " не найден.");
        }
        updatedUser.setId(userId);
        if (updatedUser.getName() == null || updatedUser.getName().isBlank()) {
            updatedUser.setName(updatedUser.getLogin()); // Если имя пустое, используем логин
        }
        users.put(userId, updatedUser);
        log.info("Обновлен пользователь: {}", updatedUser.getLogin());
        return updatedUser;
    }

    @GetMapping
    public List<User> getAllUsers() {
        log.info("Запрошены все пользователи");
        return new ArrayList<>(users.values());
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ '@'");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    // Метод для генерации следующего идентификатора
    private long getNextId() {
        return users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0) + 1;
    }
}