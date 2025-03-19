package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.validation.OnCreate;
import ru.yandex.practicum.filmorate.validation.OnUpdate;

import java.time.LocalDate;

@Data
@Slf4j
public class Film {
    @NotNull(groups = OnUpdate.class, message = "ID не может быть null при обновлении")
    private Long id;

    @NotBlank(groups = {OnCreate.class}, message = "Имя не должно быть пустым")
    private String name;

    @Size(max = 200, groups = {OnCreate.class, OnUpdate.class}, message = "Размер не должен превышать 200 символов")
    private String description;

    @NotNull(groups = OnCreate.class, message = "Дата релиза не может быть null")
    private LocalDate releaseDate;

    @NotNull(groups = OnCreate.class, message = "Продолжительность фильма не может быть null")
    @Positive(groups = {OnCreate.class, OnUpdate.class}, message = "Продолжительность фильма должна быть положительной")
    private Long duration;

    @AssertTrue(groups = {OnCreate.class, OnUpdate.class}, message = "Дата релиза не может быть раньше 28 декабря " +
            "1895 года")
    public boolean isReleaseFateValid() {
        return releaseDate == null || !releaseDate.isBefore(LocalDate.of(1895, 12, 28));
    }

}