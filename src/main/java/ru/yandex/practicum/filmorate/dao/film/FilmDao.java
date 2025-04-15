package ru.yandex.practicum.filmorate.dao.film;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.sql.*;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class FilmDao {
    private static final Logger log = LoggerFactory.getLogger(FilmDao.class);

    public final JdbcTemplate jdbcTemplate;

    public FilmDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long createFilm(Film film) {
        // Проверка обязательных полей
        if (film.getName() == null || film.getName().isEmpty()) {
            throw new IllegalArgumentException("Название фильма не может быть пустым.");
        }
        if (film.getDescription() == null) {
            throw new IllegalArgumentException("Описание фильма не может быть null.");
        }
        if (film.getReleaseDate() == null) {
            throw new IllegalArgumentException("Дата релиза фильма не может быть null.");
        }
        if (film.getDuration() <= 0) {
            throw new IllegalArgumentException("Продолжительность фильма должна быть положительной.");
        }

        // Проверка существования рейтинга (MPA)
        if (film.getMpa() != null && !mpaExists(film.getMpa().getId())) {
            log.warn("Рейтинг с ID {} не существует.", film.getMpa().getId());
            throw new IllegalArgumentException("Рейтинг с ID " + film.getMpa().getId() + " не существует.");
        }

        // Получение идентификаторов жанров
        List<Long> genreIds = film.getGenres() != null
                ? film.getGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toList())
                : Collections.emptyList();

        log.debug("Genre IDs: {}", genreIds);

        // Проверка существования жанров
        if (!genreIds.isEmpty()) {
            for (Long genreId : genreIds) {
                if (!genreExists(genreId)) {
                    log.warn("Жанр с ID {} не существует.", genreId);
                    throw new IllegalArgumentException("Жанр с ID " + genreId + " не существует.");
                }
            }
        }

        // SQL-запрос для создания фильма
        String sql = "INSERT INTO films (name, description, release_date, duration, rating_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setLong(4, film.getDuration());
            ps.setObject(5, film.getMpa() != null ? film.getMpa().getId() : null, Types.BIGINT); // Поддержка nullable rating_id
            return ps;
        }, keyHolder);

        // Проверяем, что ключ был сгенерирован
        if (keyHolder.getKey() == null) {
            throw new RuntimeException("Не удалось создать фильм: сгенерированный ID отсутствует.");
        }

        Long filmId = keyHolder.getKey().longValue();

        // Добавляем жанры фильма
        if (!genreIds.isEmpty()) {
            addGenresToFilm(filmId, genreIds);
        }

        return filmId;
    }

    public Film updateFilm(Film film) {
        if (!filmExists(film.getId())) {
            log.warn("Попытка обновить несуществующий фильм с ID: {}", film.getId());
            throw new IllegalArgumentException("Фильм с ID " + film.getId() + " не существует.");
        }

        // SQL-запрос для обновления фильма
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getRatingId(),
                film.getId());
        // Удаляем старые жанры фильма
        deleteGenresByFilmId(film.getId());
        // Добавляем новые жанры фильма
        List<Long> genreIds = film.getGenreIds();
        if (genreIds != null && !genreIds.isEmpty()) {
            addGenresToFilm(film.getId(), genreIds);
        }
        return getFilmById(film.getId());
    }

    public Film getFilmById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID фильма не может быть null.");
        }

        // Основной запрос для получения фильма
        String sqlFilm = "SELECT * FROM films WHERE id = ?";
        log.debug("Выполняется запрос на получение фильма с ID: {}", id);

        try {
            // Получаем основные данные фильма
            List<Film> films = jdbcTemplate.query(sqlFilm, this::mapRowToFilm, id);
            if (films.isEmpty()) {
                log.warn("Фильм с ID {} не найден", id);
                throw new IllegalArgumentException("Фильм с ID " + id + " не существует.");
            }
            Film film = films.get(0);

            // Получаем данные рейтинга (MPA)
            Optional<Rating> mpa = getRatingByFilmId(film.getId());
            mpa.ifPresentOrElse(
                    film::setMpa,
                    () -> film.setMpa(null) // Устанавливаем null, если рейтинг отсутствует
            );

            // Получаем данные жанров
            List<Genre> genres = getGenresByFilmId(id);
            film.setGenres(genres); // Устанавливаем жанры

            return film;
        } catch (Exception e) {
            log.error("Ошибка при получении фильма с ID {}", id, e);
            throw new RuntimeException("Не удалось получить фильм с ID " + id, e);
        }
    }

    public List<Film> getAllFilms() {
        String sql = "SELECT * FROM films";
        log.debug("Выполняется запрос на получение всех фильмов");
        try {
            return jdbcTemplate.query(sql, this::mapRowToFilm);
        } catch (DataAccessException e) {
            log.error("Ошибка при получении списка фильмов", e);
            throw new RuntimeException("Не удалось получить список фильмов", e);
        }
    }

    public List<Film> getMostPopularFilms(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Количество популярных фильмов должно быть больше 0.");
        }

        String sql = "SELECT f.* FROM films f " +
                "LEFT JOIN likes l ON f.id = l.film_id " +
                "GROUP BY f.id " +
                "ORDER BY COUNT(l.user_id) DESC " +
                "LIMIT ?";

        log.debug("Выполняется запрос на получение {} самых популярных фильмов", count);
        try {
            return jdbcTemplate.query(sql, this::mapRowToFilm, count);
        } catch (DataAccessException e) {
            log.error("Ошибка при получении популярных фильмов", e);
            throw new RuntimeException("Не удалось получить популярные фильмы", e);
        }
    }

    public void removeFilm(Long filmId) {
        if (!filmExists(filmId)) {
            log.warn("Попытка удалить несуществующий фильм с ID: {}", filmId);
            throw new FilmNotFoundException("Фильм с ID " + filmId + " не существует.");
        }

        String sql = "DELETE FROM films WHERE id = ?";
        try {
            int rowsAffected = jdbcTemplate.update(sql, filmId);
            if (rowsAffected == 0) {
                log.warn("Фильм с ID {} не найден", filmId);
                throw new IllegalArgumentException("Фильм с ID " + filmId + " не существует.");
            }
            log.debug("Фильм удален: filmId={}", filmId);
        } catch (DataAccessException e) {
            log.error("Ошибка при удалении фильма с ID {}", filmId, e);
            throw new RuntimeException("Не удалось удалить фильм", e);
        }
    }

    public void addLike(Long filmId, Long userId) {
        if (!filmExists(filmId)) {
            log.error("Фильм с ID {} не существует.", filmId);
            throw new IllegalArgumentException("Фильм с ID " + filmId + " не существует.");
        }

        if (!userExists(userId)) {
            log.error("Пользователь с ID {} не существует.", userId);
            throw new IllegalArgumentException("Пользователь с ID " + userId + " не существует.");
        }

        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        try {
            jdbcTemplate.update(sql, filmId, userId);
            log.debug("Лайк добавлен: filmId={}, userId={}", filmId, userId);
        } catch (DataAccessException e) {
            log.error("Ошибка при добавлении лайка: filmId={}, userId={}", filmId, userId, e);
            throw new RuntimeException("Не удалось добавить лайк", e);
        }
    }


    public void removeLike(Long filmId, Long userId) {
        if (!filmExists(filmId)) {
            log.error("Фильм с ID {} не существует.", filmId);
            throw new IllegalArgumentException("Фильм с ID " + filmId + " не существует.");
        }

        if (!userExists(userId)) {
            log.error("Пользователь с ID {} не существует.", userId);
            throw new IllegalArgumentException("Пользователь с ID " + userId + " не существует.");
        }

        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        try {
            int rowsAffected = jdbcTemplate.update(sql, filmId, userId);
            if (rowsAffected == 0) {
                log.warn("Лайк не найден: filmId={}, userId={}", filmId, userId);
                throw new IllegalArgumentException("Лайк для фильма с ID " + filmId + " и пользователя с ID " + userId + " не найден.");
            }
            log.debug("Лайк удален: filmId={}, userId={}", filmId, userId);
        } catch (DataAccessException e) {
            log.error("Ошибка при удалении лайка: filmId={}, userId={}", filmId, userId, e);
            throw new RuntimeException("Не удалось удалить лайк", e);
        }
    }

    // Добавление жанров фильма
    public void addGenresToFilm(Long filmId, List<Long> genreIds) {
        String checkSql = "SELECT COUNT(*) FROM film_genres WHERE film_id = ? AND genre_id = ?";
        String insertSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

        for (Long genreId : genreIds) {
            // Проверяем, существует ли запись с такими film_id и genre_id
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, filmId, genreId);
            if (count == null || count == 0) {
                // Если запись не существует, добавляем её
                jdbcTemplate.update(insertSql, filmId, genreId);
            }
        }
    }

    // Удаление жанров фильма
    public void deleteGenresByFilmId(Long filmId) {
        String sql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
    }

    // Получение жанров фильма
    public List<Genre> getGenresByFilmId(Long filmId) {
        String sql = "SELECT g.id, g.name " +
                "FROM genres g " +
                "JOIN film_genres fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id = ?";
        try {
            return jdbcTemplate.query(sql, this::mapRowToGenre, filmId);
        } catch (EmptyResultDataAccessException e) {
            // Возвращаем пустой список, если жанры не найдены
            return List.of();
        }
    }

    public Film getFullFilmById(Long id) {
        // Получение основных данных фильма
        String sqlFilm = "SELECT * FROM films WHERE id = ?";
        Film film = jdbcTemplate.queryForObject(sqlFilm, this::mapRowToFilm, id);

        if (film == null) {
            throw new IllegalArgumentException("Фильм с ID " + id + " не существует.");
        }

        // Получение данных рейтинга (MPA)
        String sqlRating = "SELECT r.id, r.name FROM ratings r WHERE r.id = ?";
        Optional<Rating> mpa = jdbcTemplate.query(sqlRating, this::mapRowToRating, film.getRatingId())
                .stream()
                .findFirst();

        // Установка MPA в объект фильма
        mpa.ifPresent(film::setMpa);

        // Получение данных жанров
        String sqlGenres = "SELECT g.id, g.name FROM genres g JOIN film_genres fg ON g.id = fg.genre_id WHERE fg.film_id = ?";
        List<Genre> genres = jdbcTemplate.query(sqlGenres, this::mapRowToGenre, id);

        // Установка жанров в объект фильма
        film.setGenres(genres);

        return film;
    }

    public Optional<Rating> getRatingByFilmId(Long filmId) {
        String sql = "SELECT r.id, r.name FROM ratings r WHERE r.id = (SELECT rating_id FROM films WHERE id = ?)";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, this::mapRowToRating, filmId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getLong("duration"));
        film.setRatingId(rs.getLong("rating_id"));
        return film;
    }

    private Rating mapRowToRating(ResultSet rs, int rowNum) throws SQLException {
        Rating rating = new Rating();
        rating.setId(rs.getLong("id"));
        rating.setName(rs.getString("name"));
        return rating;
    }

    // Метод для маппинга строки ResultSet в объект Genre
    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        Genre genre = new Genre();
        genre.setId(rs.getLong("id"));
        genre.setName(rs.getString("name"));
        return genre;
    }

    private boolean userExists(Long userId) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, userId);
        return count != null && count > 0;
    }

    private boolean filmExists(Long filmId) {
        String sql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, filmId);
        return count != null && count > 0;
    }

    private boolean mpaExists(Long ratingId) {
        String sql = "SELECT COUNT(*) FROM ratings WHERE id = ? AND name IS NOT NULL";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, ratingId);
        return count != null && count > 0;
    }

    private boolean genreExists(Long genreId) {
        String sql = "SELECT COUNT(*) FROM genres WHERE id = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, genreId);
        log.debug("Проверка существования жанра с ID {}: count = {}", genreId, count);
        return count != null && count > 0;
    }
}