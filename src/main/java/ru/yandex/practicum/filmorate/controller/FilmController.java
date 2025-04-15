package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.validation.OnCreate;
import ru.yandex.practicum.filmorate.validation.OnUpdate;

import java.util.Collection;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    // Получение всех фильмов
    @GetMapping
    public Collection<Film> getAllFilms() {
        log.info("Запрошены все фильмы");
        return filmService.getAllFilms();
    }

    // Получение фильма по ID
    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        log.info("Запрошен фильм с ID: {}", id);
        return filmService.getFilmById(id);
    }

    // Добавление нового фильма
    @PostMapping
    public Film addFilm(@RequestBody @Validated(OnCreate.class)@Valid Film film) {
        log.info("Добавлен новый фильм: {}", film.getName());
        return filmService.addFilm(film);
    }

    // Обновление фильма
    @PutMapping
    public Film updateFilm(@RequestBody @Validated(OnUpdate.class)@Valid Film film) {
        log.info("Обновлен фильм с ID: {}", film.getId());
        return filmService.updateFilm(film);
    }

    // Пользователь ставит лайк фильму
    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Пользователь с ID {} поставил лайк фильму с ID {}", userId, id);
        filmService.addLike(id, userId);
    }

    // Пользователь удаляет лайк
    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Пользователь с ID {} удалил лайк у фильма с ID {}", userId, id);
        filmService.removeLike(id, userId);
    }

    // Получение списка популярных фильмов
    @GetMapping("/popular")
    public List<Film> getMostPopularFilms(@RequestParam(defaultValue = "10") int count) {
        log.info("Запрошено {} самых популярных фильмов", count);
        return filmService.getMostPopularFilms(count);
    }
}