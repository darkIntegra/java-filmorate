package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final Map<Long, Set<Long>> likes = new HashMap<>();

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

    /**
     * Добавляет лайк фильму от пользователя.
     * Логика работы:
     * 1. Используется Map<Long, Set<Long>> likes, где ключ — это ID фильма,
     * а значение — множество ID пользователей, поставивших лайк.
     * 2. Метод getOrDefault возвращает множество лайков для фильма с filmId.
     * Если такого фильма нет в Map, возвращается пустое множество.
     * 3. В множество добавляется ID пользователя (userId), что означает,
     * что пользователь поставил лайк фильму.
     */
    public void addLike(Long filmId, Long userId) {
        // Проверяем существование фильма
        filmStorage.getFilmById(filmId); // Выбросит исключение, если фильм не найден

        // Проверяем существование пользователя
        userStorage.getUserById(userId); // Выбросит исключение, если пользователь не найден

        // Добавляем лайк
        likes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
    }

    public void removeLike(Long filmId, Long userId) {
        // Проверяем существование фильма
        filmStorage.getFilmById(filmId); // Выбросит исключение, если фильм не найден

        // Проверяем существование пользователя
        userStorage.getUserById(userId); // Выбросит исключение, если пользователь не найден

        // Удаляем лайк
        likes.getOrDefault(filmId, Collections.emptySet()).remove(userId);
    }

    /**
     * Возвращает список наиболее популярных фильмов на основе количества лайков.
     * Логика работы:
     * 1. Получаем все фильмы из хранилища через filmStorage.getAllFilms().
     * 2. Сортируем фильмы по количеству лайков (в порядке убывания):
     * - Для каждого фильма получаем множество лайков через likes.getOrDefault(f.getId(), Collections.emptySet()).
     * - Размер множества лайков определяет популярность фильма.
     * 3. Ограничиваем результат до указанного количества фильмов (count).
     * 4. Преобразуем Stream в List и возвращаем результат.
     */
    public List<Film> getMostPopularFilms(int count) {
        return filmStorage.getAllFilms().stream()
                .sorted(Comparator.comparingInt(f -> -likes.getOrDefault(f.getId(),
                        Collections.emptySet()).size()))
                .limit(count)
                .collect(Collectors.toList());
    }
}
