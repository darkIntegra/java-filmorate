package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    Long createFilm(Film film);

    void updateFilm(Film film);

    Film getFilmById(Long id);

    List<Film> getAllFilms();

    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);

    List<Film> getMostPopularFilms(int count);
}