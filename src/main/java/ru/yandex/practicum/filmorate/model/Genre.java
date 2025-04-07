package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Genre {
    private Long id;
    private String name;
}
// Это не требуется по 12ТЗ, но надо бы не забыть добавить методы в FilmStorage для работы с жанрами, например
// addGenreToFilm, getGenresByFilmId
