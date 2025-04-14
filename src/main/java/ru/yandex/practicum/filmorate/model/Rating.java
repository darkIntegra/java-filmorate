package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Rating {
    private Long id; // Уникальный идентификатор
    private String name; // Название рейтинга (например, "G", "PG")
}