package ru.yandex.practicum.filmorate.dao.film;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.*;
import java.util.List;

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
        // SQL-запрос для создания фильма
        String sql = "INSERT INTO films (name, description, release_date, duration, rating_id) VALUES (?, ?, ?, ?, ?)";
        // Используем KeyHolder для получения сгенерированного ключа
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setLong(4, film.getDuration());
            ps.setObject(5, film.getRatingId(), Types.BIGINT); // Поддержка nullable rating_id
            return ps;
        }, keyHolder);
        // Проверяем, что ключ был сгенерирован
        if (keyHolder.getKey() == null) {
            throw new RuntimeException("Не удалось создать фильм: сгенерированный ID отсутствует.");
        }

        Long filmId = keyHolder.getKey().longValue();
        // Добавляем жанры фильма
        List<Long> genreIds = film.getGenreIds();
        if (genreIds != null && !genreIds.isEmpty()) {
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

        String sql = "SELECT * FROM films WHERE id = ?";
        log.debug("Выполняется запрос на получение фильма с ID: {}", id);

        try {
            List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, id);
            if (films.isEmpty()) {
                log.warn("Фильм с ID {} не найден", id);
                throw new IllegalArgumentException("Фильм с ID " + id + " не существует.");
            }
            return films.get(0);
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
            throw new IllegalArgumentException("Фильм с ID " + filmId + " не существует.");
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
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        for (Long genreId : genreIds) {
            jdbcTemplate.update(sql, filmId, genreId);
        }
    }

    // Удаление жанров фильма
    public void deleteGenresByFilmId(Long filmId) {
        String sql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
    }

    // Получение жанров фильма
    public List<Long> getGenresByFilmId(Long filmId) {
        String sql = "SELECT genre_id FROM film_genres WHERE film_id = ?";
        return jdbcTemplate.queryForList(sql, Long.class, filmId);
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

    private boolean userExists(Long userId) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null && count > 0;
    }

    private boolean filmExists(Long filmId) {
        String sql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, filmId);
        return count != null && count > 0;
    }
}