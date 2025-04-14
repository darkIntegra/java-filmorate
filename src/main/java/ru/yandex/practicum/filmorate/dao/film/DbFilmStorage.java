package ru.yandex.practicum.filmorate.dao.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;

@Component("dbFilmStorage")
public class DbFilmStorage implements FilmStorage {

    public final FilmDao filmDao;

    @Autowired
    public DbFilmStorage(FilmDao filmDao) {
        this.filmDao = filmDao;
    }

    @Override
    public Long createFilm(Film film) {
        return filmDao.createFilm(film);
    }

    @Override
    public void updateFilm(Film film) {
        filmDao.updateFilm(film);
    }

    @Override
    public Film getFilmById(Long id) {
        return filmDao.getFilmById(id);
    }

    @Override
    public List<Film> getAllFilms() {
        return filmDao.getAllFilms();
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        filmDao.addLike(filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        filmDao.removeLike(filmId, userId);
    }

    @Override
    public List<Film> getMostPopularFilms(int count) {
        return filmDao.getMostPopularFilms(count);
    }
}