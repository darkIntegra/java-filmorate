package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film getFilmById(Long id) {
        return filmStorage.getFilmById(id);
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    // Добавляет лайк фильму от пользователя.
    public void addLike(Long filmId, Long userId) {
        // Проверяем существование фильма и пользователя
        filmStorage.getFilmById(filmId); // Выбросит исключение, если фильм не найден
        userStorage.getUserById(userId); // Выбросит исключение, если пользователь не найден
        filmStorage.addLike(filmId, userId);
    }

    // Удаляет лайк у фильма.
    public void removeLike(Long filmId, Long userId) {
        // Проверяем существование фильма и пользователя
        filmStorage.getFilmById(filmId); // Выбросит исключение, если фильм не найден
        userStorage.getUserById(userId); // Выбросит исключение, если пользователь не найден
        filmStorage.removeLike(filmId, userId);
    }

    // Возвращает список наиболее популярных фильмов на основе количества лайков.
    public List<Film> getMostPopularFilms(int count) {
        return filmStorage.getAllFilms().stream()
                .sorted(Comparator.comparingInt(f -> -filmStorage.getLikes(f.getId()).size()))
                .limit(count)
                .toList();
    }
}
