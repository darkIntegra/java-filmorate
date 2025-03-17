package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.groups.Default;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validation.OnCreate;
import ru.yandex.practicum.filmorate.validation.OnUpdate;

import java.util.*;

@RestController
@Validated
@Slf4j
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> getAllFilms() {
        log.info("Запрошены все фильмы");
        return films.values();
    }

    @PostMapping
    public Film addFilm(@RequestBody @Validated({OnCreate.class, Default.class}) Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Добавлен новый фильм: {}", film.getName());
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody @Validated({OnUpdate.class, Default.class}) Film newFilm) {
        if (!films.containsKey(newFilm.getId())) {
            throw new ValidationException("Фильм с ID " + newFilm.getId() + " не найден.");
        }

        Film oldFilm = films.get(newFilm.getId());

        // Обновляем только те поля, которые были переданы и прошли валидацию
        if (newFilm.getName() != null) {
            oldFilm.setName(newFilm.getName());
        }
        if (newFilm.getDescription() != null) {
            oldFilm.setDescription(newFilm.getDescription());
        }
        if (newFilm.getReleaseDate() != null) {
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
        }
        if (newFilm.getDuration() != null) {
            oldFilm.setDuration(newFilm.getDuration());
        }

        log.info("Обновлен фильм: {}", newFilm.getName());
        return oldFilm;
    }

    // Метод для генерации следующего идентификатора
    private long getNextId() {
        return films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(1);
    }
}