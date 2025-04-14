package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dao.genre.GenreDao;
import ru.yandex.practicum.filmorate.model.Genre;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(GenreDao.class)
class GenreDaoTests {

    @Autowired
    private GenreDao genreDao;

    @Test
    public void testGetAllGenres() {
        List<Genre> genres = genreDao.getAllGenres();
        assertThat(genres).isNotEmpty();
        assertThat(genres).extracting(Genre::getName).contains("Комедия", "Драма");
    }

    @Test
    public void testGetGenreById() {
        Optional<Genre> genre = genreDao.getGenreById(1L);
        assertThat(genre).isPresent();
        assertThat(genre.get().getName()).isEqualTo("Комедия");
    }

    @Test
    public void testGetNonExistentGenre() {
        Optional<Genre> genre = genreDao.getGenreById(999L);
        assertThat(genre).isEmpty();
    }
}