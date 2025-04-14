package ru.yandex.practicum.filmorate.dao.genre;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class GenreDao {

    private final JdbcTemplate jdbcTemplate;

    public GenreDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Получение всех жанров
    public List<Genre> getAllGenres() {
        String sql = "SELECT * FROM genres";
        return jdbcTemplate.query(sql, this::mapRowToGenre);
    }

    // Получение жанра по ID
    public Optional<Genre> getGenreById(Long id) {
        String sql = "SELECT * FROM genres WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, this::mapRowToGenre, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    // Метод для маппинга строки ResultSet в объект Genre
    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        Genre genre = new Genre();
        genre.setId(rs.getLong("id"));
        genre.setName(rs.getString("name"));
        return genre;
    }
}