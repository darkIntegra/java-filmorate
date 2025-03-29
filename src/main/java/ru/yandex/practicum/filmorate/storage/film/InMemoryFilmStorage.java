package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    // можно было разными способами likes сделать, взял этот чтобы проще на БД было перейти потом
    private final Map<Long, Set<Long>> likes = new HashMap<>();
    private long nextId = 1;

    @Override
    public Collection<Film> getAllFilms() {
        return films.values();
    }

    @Override
    public Film addFilm(Film film) {
        film.setId(nextId++);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new IllegalArgumentException("Фильм с ID " + film.getId() + " не найден.");
        }
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film getFilmById(Long id) {
        Film film = films.get(id);
        if (film == null) {
            throw new IllegalArgumentException("Фильм с ID " + id + " не найден.");
        }
        return film;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        likes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        likes.getOrDefault(filmId, Collections.emptySet()).remove(userId);
    }

    @Override
    public Set<Long> getLikes(Long filmId) {
        return likes.getOrDefault(filmId, Collections.emptySet());
    }
}
