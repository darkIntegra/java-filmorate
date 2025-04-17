package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.genre.GenreDao;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Service
public class GenreService {

    private final GenreDao genreDao;

    public GenreService(GenreDao genreDao) {
        this.genreDao = genreDao;
    }

    // Получение всех жанров
    public List<Genre> getAllGenres() {
        return genreDao.getAllGenres();
    }

    // Получение жанра по ID
    public Genre getGenreById(Long id) {
        return genreDao.getGenreById(id)
                .orElseThrow(() -> new IllegalArgumentException("Жанр с ID " + id + " не найден"));
    }
}