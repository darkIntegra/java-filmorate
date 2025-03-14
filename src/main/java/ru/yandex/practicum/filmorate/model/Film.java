package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Data
@Slf4j
public class Film {
    private Long id;

    @NotBlank(message = "Имя не должно быть пустым")
    private String name;

    @Size(max = 200, message = "Размер не должен превышать 200 символов")
    private String description;

    @NotNull(message = "Дата релиза не может быть null")
    private LocalDate releaseDate;

    @NotNull(message = "Продолжительность фильма не может быть null")
    @PositiveOrZero(message = "Продолжительность фильма должна быть положительным числом")
    private Long duration;

    @AssertTrue(message = "Дата релиза не может быть раньше 28 декабря 1895 года")
    public boolean isReleaseFateValid() {
        return !releaseDate.isBefore(LocalDate.of(1895, 12, 28));
    }

}