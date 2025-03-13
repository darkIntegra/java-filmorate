package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody User user) {
        validateUser(user);  // Добавляем валидацию
        return userService.create(user);
    }

    @PutMapping("/{userId}")
    public User updateUser(@PathVariable Long userId, @RequestBody User updatedUser) {
        validateUser(updatedUser);  // Добавляем валидацию
        return userService.update(userId, updatedUser);
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    private void validateUser(User user) {
        // Электронная почта не может быть пустой и должна содержать символ '@'
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ '@'");
        }

        // Логин не может быть пустым и содержать пробелы
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }

        // Имя для отображения может быть пустым — в таком случае будет использован логин
        // Этот пункт не требует явной валидации, так как допустимо пустое значение имени.

        // Дата рождения не может быть в будущем
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}