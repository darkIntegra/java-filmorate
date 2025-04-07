package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.validation.OnCreate;
import ru.yandex.practicum.filmorate.validation.OnUpdate;

import java.time.LocalDate;
import java.util.Set;

@Data
@Slf4j
public class User {
    private Long id;

    @NotBlank(groups = OnCreate.class, message = "Email не может быть пустым.")
    @Email(groups = {OnCreate.class, OnUpdate.class}, message = "некорректный формат email.")
    private String email;

    @NotNull(groups = OnCreate.class, message = "Логин не может быть null.")
    @Pattern(
            regexp = "^\\S+$",
            groups = {OnCreate.class}, // Применяется только при создании
            message = "Логин не должен быть пустым и не должен содержать пробелы."
    )
    private String login;

    private String name;

    @NotNull(groups = OnCreate.class, message = "Дата рождения не может быть null")
    @Past(groups = {OnCreate.class, OnUpdate.class}, message = "Дата рождения не может быть в будущем.")
    private LocalDate birthday;

    private Set<Friendship> friendships;
}