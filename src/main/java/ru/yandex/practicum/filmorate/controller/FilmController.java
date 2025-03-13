package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film addFilm(@RequestBody Film film) {
        validateFilm(film); // Валидация
        long id = getNextId();
        film.setId(id);
        films.put(id, film);
        log.info("Добавлен новый фильм: {}", film.getName());
        return film;
    }

    @PutMapping("/{filmId}")
    public Film updateFilm(@PathVariable Long filmId, @RequestBody Film updatedFilm) {
        validateFilm(updatedFilm); // Валидация
        if (!films.containsKey(filmId)) {
            throw new ValidationException("Фильм с ID " + filmId + " не найден.");
        }
        updatedFilm.setId(filmId);
        films.put(filmId, updatedFilm);
        log.info("Обновлен фильм: {}", updatedFilm.getName());
        return updatedFilm;
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.info("Запрошены все фильмы");
        return new ArrayList<>(films.values());
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }

    // Метод для генерации следующего идентификатора
    private long getNextId() {
        return films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0) + 1;
    }
}