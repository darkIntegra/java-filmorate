package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.rating.RatingDao;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.List;

@Service
public class RatingService {

    private final RatingDao ratingDao;

    public RatingService(RatingDao ratingDao) {
        this.ratingDao = ratingDao;
    }

    // Получение всех рейтингов
    public List<Rating> getAllRatings() {
        return ratingDao.getAllRatings();
    }

    // Получение рейтинга по ID
    public Rating getRatingById(Long id) {
        return ratingDao.getRatingById(id)
                .orElseThrow(() -> new IllegalArgumentException("Рейтинг с ID " + id + " не найден"));
    }
}