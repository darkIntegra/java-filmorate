package ru.yandex.practicum.filmorate.dao.rating;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Rating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class RatingDao {

    private final JdbcTemplate jdbcTemplate;

    public RatingDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Получение всех рейтингов
    public List<Rating> getAllRatings() {
        String sql = "SELECT * FROM ratings";
        return jdbcTemplate.query(sql, this::mapRowToRating);
    }

    // Получение рейтинга по ID
    public Optional<Rating> getRatingById(Long id) {
        String sql = "SELECT * FROM ratings WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, this::mapRowToRating, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    // Метод для маппинга строки ResultSet в объект Rating
    private Rating mapRowToRating(ResultSet rs, int rowNum) throws SQLException {
        Rating rating = new Rating();
        rating.setId(rs.getLong("id"));
        rating.setName(rs.getString("name"));
        return rating;
    }
}