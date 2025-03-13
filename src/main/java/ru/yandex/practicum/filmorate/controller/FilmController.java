package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film addFilm(@RequestBody Film film) {
        validateFilm(film);  // Добавляем валидацию
        return filmService.add(film);
    }

    @PutMapping("/{filmId}")
    public Film updateFilm(@PathVariable Long filmId, @RequestBody Film updatedFilm) {
        validateFilm(updatedFilm);  // Добавляем валидацию
        return filmService.update(filmId, updatedFilm);
    }

    @GetMapping
    public List<Film> getAllFilms() {
        return filmService.findAll();
    }

    private void validateFilm(Film film) {
        // Название не может быть пустым
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым");
        }

        // Максимальная длина описания — 200 символов
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }

        // Дата релиза — не раньше 28 декабря 1895 года
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        // Продолжительность фильма должна быть положительным числом
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}