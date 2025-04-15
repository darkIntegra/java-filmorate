package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.dao.film.DbFilmStorage;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test") // Активируем профиль "test"
public class DbFilmStorageTests {

    @Autowired
    private DbFilmStorage dbFilmStorage;

    @BeforeEach
    void setUp() {
        // Очистка всех фильмов перед каждым тестом
        dbFilmStorage.getAllFilms().forEach(film -> dbFilmStorage.removeFilm(film.getId()));
    }

    @Test
    public void testCreateFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2023, 1, 1));
        film.setDuration(120L);

        Long filmId = dbFilmStorage.createFilm(film);
        assertThat(filmId).isNotNull();

        Film createdFilm = dbFilmStorage.getFilmById(filmId);
        assertThat(createdFilm).isNotNull();
        assertThat(createdFilm.getName()).isEqualTo("Test Film");
        assertThat(createdFilm.getDescription()).isEqualTo("Test Description");
        assertThat(createdFilm.getReleaseDate()).isEqualTo(LocalDate.of(2023, 1, 1));
        assertThat(createdFilm.getDuration()).isEqualTo(120L);
    }

    @Test
    public void testUpdateFilm() {
        Film film = new Film();
        film.setName("Old Name");
        film.setDescription("Old Description");
        film.setReleaseDate(LocalDate.of(2023, 1, 1));
        film.setDuration(120L);

        Long filmId = dbFilmStorage.createFilm(film);

        Film updatedFilm = new Film();
        updatedFilm.setId(filmId);
        updatedFilm.setName("New Name");
        updatedFilm.setDescription("New Description");
        updatedFilm.setReleaseDate(LocalDate.of(2024, 1, 1));
        updatedFilm.setDuration(150L);

        dbFilmStorage.updateFilm(updatedFilm);

        Film fetchedFilm = dbFilmStorage.getFilmById(filmId);
        assertThat(fetchedFilm).isNotNull();
        assertThat(fetchedFilm.getName()).isEqualTo("New Name");
        assertThat(fetchedFilm.getDescription()).isEqualTo("New Description");
        assertThat(fetchedFilm.getReleaseDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(fetchedFilm.getDuration()).isEqualTo(150L);
    }

    @Test
    public void testGetAllFilms() {
        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Description 1");
        film1.setReleaseDate(LocalDate.of(2023, 1, 1));
        film1.setDuration(120L);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2023, 2, 1));
        film2.setDuration(110L);

        dbFilmStorage.createFilm(film1);
        dbFilmStorage.createFilm(film2);

        List<Film> films = dbFilmStorage.getAllFilms();
        assertThat(films).hasSize(2);
        assertThat(films.stream().map(Film::getName)).containsExactlyInAnyOrder("Film 1", "Film 2");
    }

    @Test
    public void testRemoveFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2023, 1, 1));
        film.setDuration(120L);

        Long filmId = dbFilmStorage.createFilm(film);
        dbFilmStorage.removeFilm(filmId);

        List<Film> films = dbFilmStorage.getAllFilms();
        assertThat(films).isEmpty();
    }
}