package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Friendship {
    private Long userId;
    private Long friendId;
    private String status; // Статус дружбы ("UNCONFIRMED" или "CONFIRMED")

    // Константы для статусов
    public static final String STATUS_UNCONFIRMED = "UNCONFIRMED";
    public static final String STATUS_CONFIRMED = "CONFIRMED";
}
