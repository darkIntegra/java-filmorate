package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Component("inMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private final Map<Long, Set<Long>> likes = new HashMap<>();
    private long nextId = 1;

    @Override
    public Long createFilm(Film film) {
        film.setId(nextId++);
        films.put(film.getId(), film);
        return film.getId();
    }

    @Override
    public void updateFilm(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new IllegalArgumentException("Фильм с ID " + film.getId() + " не найден.");
        }
        films.put(film.getId(), film);
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
    public List<Film> getAllFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public void removeFilm(Long filmId) {
        if (!films.containsKey(filmId)) {
            throw new IllegalArgumentException("Фильм с ID " + filmId + " не найден.");
        }
        // Удаляем фильм из основного хранилища
        films.remove(filmId);
        // Удаляем все лайки, связанные с этим фильмом
        likes.remove(filmId);
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
    public List<Film> getMostPopularFilms(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Количество популярных фильмов должно быть больше 0.");
        }
        // Сортируем фильмы по количеству лайков в порядке убывания
        return films.values().stream()
                .sorted((f1, f2) -> {
                    long likes1 = likes.getOrDefault(f1.getId(), Collections.emptySet()).size();
                    long likes2 = likes.getOrDefault(f2.getId(), Collections.emptySet()).size();
                    return Long.compare(likes2, likes1); // Обратный порядок для убывания
                })
                .limit(count) // Ограничиваем количество фильмов
                .collect(Collectors.toList());
    }
}