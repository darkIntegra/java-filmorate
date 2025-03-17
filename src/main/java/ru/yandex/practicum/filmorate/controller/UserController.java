package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.groups.Default;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.OnCreate;
import ru.yandex.practicum.filmorate.validation.OnUpdate;

import java.util.*;

@RestController
@Validated
@Slf4j
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getAllUsers() {
        log.info("Запрошены все пользователи");
        return users.values();
    }

    @PostMapping
    public User createUser(@RequestBody @Validated({OnCreate.class, Default.class}) User user) {
        user.setId(getNextId());
        // Если имя не указано, используем логин
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        log.info("Добавлен новый пользователь: {}", user.getLogin());
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody @Validated({OnUpdate.class, Default.class}) User newUser) {
        if (!users.containsKey(newUser.getId())) {
            throw new ValidationException("Пользователь с ID " + newUser.getId() + " не найден.");
        }
        User oldUser = users.get(newUser.getId());

        // Обновляем только те поля, которые были переданы и прошли валидацию
        if (newUser.getEmail() != null) {
            oldUser.setEmail(newUser.getEmail());
        }
        if (newUser.getLogin() != null) {
            oldUser.setLogin(newUser.getLogin());
        }
        if (newUser.getBirthday() != null) {
            oldUser.setBirthday(newUser.getBirthday());
        }

        // Если имя пустое, используем логин
        oldUser.setName((newUser.getName() == null || newUser.getName().isBlank()) ? newUser.getLogin() :
                newUser.getName());

        log.info("Обновлен пользователь: {}", oldUser.getLogin());
        return oldUser;
    }

    private long getNextId() {
        return users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0) + 1;
    }
}