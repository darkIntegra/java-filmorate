package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Data
@Slf4j
public class User {
    private Long id;

    @NotBlank(message = "Email не может быть пустым.")
    @Email(message = "некорректный формат email.")
    private String email;

    @NotBlank(message = "Поле с логином не должно быть пустым.")
    @Pattern(regexp = "^\\S+$", message = "Логин не должен содержать пробелы.")
    private String login;

    private String name;

    @NotNull(message = "Дата рождения не может быть null")
    @Past(message = "Дата рождения не может быть в будущем.")
    private LocalDate birthday;
}