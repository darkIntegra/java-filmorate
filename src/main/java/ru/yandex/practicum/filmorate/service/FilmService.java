package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;

@Service
public class FilmService {

    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(@Qualifier("dbFilmStorage") FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Long addFilm(Film film) {
        filmStorage.createFilm(film);
        return film.getId();
    }

    public Film updateFilm(Film film) {
        filmStorage.updateFilm(film);
        return film;
    }

    public Film getFilmById(Long id) {
        return filmStorage.getFilmById(id);
    }

    public void addLike(Long filmId, Long userId) {
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getMostPopularFilms(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Количество популярных фильмов должно быть больше 0.");
        }
        return filmStorage.getMostPopularFilms(count);
    }
}