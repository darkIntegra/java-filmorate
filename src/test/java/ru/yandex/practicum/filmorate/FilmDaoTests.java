package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dao.film.FilmDao;
import ru.yandex.practicum.filmorate.dao.user.UserDao;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDao.class, UserDao.class})
public class FilmDaoTests {

    private final FilmDao filmDao;
    private final UserDao userDao;

    @Test
    public void testCreateFilm() {
        // Arrange
        Film film = new Film();
        film.setName("Inception");
        film.setDescription("A mind-bending thriller.");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148L);
        film.setRatingId(1L);

        // Act
        Long generatedId = filmDao.createFilm(film);

        // Assert
        assertThat(generatedId).isNotNull().isPositive();

        Film createdFilm = filmDao.getFilmById(generatedId);
        assertThat(createdFilm).isNotNull();
        assertThat(createdFilm.getName()).isEqualTo("Inception");
        assertThat(createdFilm.getDescription()).isEqualTo("A mind-bending thriller.");
        assertThat(createdFilm.getReleaseDate()).isEqualTo(LocalDate.of(2010, 7, 16));
        assertThat(createdFilm.getDuration()).isEqualTo(148);
        assertThat(createdFilm.getRatingId()).isEqualTo(1L);
    }

    @Test
    public void testUpdateFilm() {
        // Arrange
        Film film = new Film();
        film.setName("Inception");
        film.setDescription("A mind-bending thriller.");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148L);
        film.setRatingId(1L);

        Long filmId = filmDao.createFilm(film);

        // Act
        Film updatedFilm = new Film();
        updatedFilm.setId(filmId);
        updatedFilm.setName("Updated Inception");
        updatedFilm.setDescription("An updated mind-bending thriller.");
        updatedFilm.setReleaseDate(LocalDate.of(2011, 8, 17));
        updatedFilm.setDuration(150L);
        updatedFilm.setRatingId(2L);

        filmDao.updateFilm(updatedFilm);

        // Assert
        Film fetchedFilm = filmDao.getFilmById(filmId);
        assertThat(fetchedFilm).isNotNull();
        assertThat(fetchedFilm.getName()).isEqualTo("Updated Inception");
        assertThat(fetchedFilm.getDescription()).isEqualTo("An updated mind-bending thriller.");
        assertThat(fetchedFilm.getReleaseDate()).isEqualTo(LocalDate.of(2011, 8, 17));
        assertThat(fetchedFilm.getDuration()).isEqualTo(150);
        assertThat(fetchedFilm.getRatingId()).isEqualTo(2L);
    }

    @Test
    public void testGetFilmById() {
        // Arrange
        Film film = new Film();
        film.setName("Inception");
        film.setDescription("A mind-bending thriller.");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148L);
        film.setRatingId(1L);

        Long filmId = filmDao.createFilm(film);

        // Act
        Film fetchedFilm = filmDao.getFilmById(filmId);

        // Assert
        assertThat(fetchedFilm).isNotNull();
        assertThat(fetchedFilm.getId()).isEqualTo(filmId);
        assertThat(fetchedFilm.getName()).isEqualTo("Inception");
        assertThat(fetchedFilm.getDescription()).isEqualTo("A mind-bending thriller.");
        assertThat(fetchedFilm.getReleaseDate()).isEqualTo(LocalDate.of(2010, 7, 16));
        assertThat(fetchedFilm.getDuration()).isEqualTo(148);
        assertThat(fetchedFilm.getRatingId()).isEqualTo(1L);
    }

    @Test
    public void testGetAllFilms() {
        // Arrange
        Film film1 = new Film();
        film1.setName("Inception");
        film1.setDescription("A mind-bending thriller.");
        film1.setReleaseDate(LocalDate.of(2010, 7, 16));
        film1.setDuration(148L);
        film1.setRatingId(1L);

        Film film2 = new Film();
        film2.setName("Interstellar");
        film2.setDescription("A space exploration story.");
        film2.setReleaseDate(LocalDate.of(2014, 11, 7));
        film2.setDuration(169L);
        film2.setRatingId(2L);

        filmDao.createFilm(film1);
        filmDao.createFilm(film2);

        // Act
        List<Film> films = filmDao.getAllFilms();

        // Assert
        assertThat(films).hasSize(2);
        assertThat(films).extracting(Film::getName).containsExactlyInAnyOrder("Inception", "Interstellar");
    }

    @Test
    public void testGetMostPopularFilms() {
        // Arrange
        Film film1 = new Film();
        film1.setName("Inception");
        film1.setDescription("A mind-bending thriller.");
        film1.setReleaseDate(LocalDate.of(2010, 7, 16));
        film1.setDuration(148L);
        film1.setRatingId(1L);

        Film film2 = new Film();
        film2.setName("Interstellar");
        film2.setDescription("A space exploration story.");
        film2.setReleaseDate(LocalDate.of(2014, 11, 7));
        film2.setDuration(169L);
        film2.setRatingId(2L);

        Long filmId1 = filmDao.createFilm(film1);
        Long filmId2 = filmDao.createFilm(film2);

        // Создаем пользователей
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1_login");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2_login");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1995, 5, 5));

        Long userId1 = userDao.createUser(user1);
        Long userId2 = userDao.createUser(user2);

        // Add likes to films
        filmDao.addLike(filmId1, userId1);
        filmDao.addLike(filmId1, userId2);
        filmDao.addLike(filmId2, userId1);

        // Act
        List<Film> popularFilms = filmDao.getMostPopularFilms(1);

        // Assert
        assertThat(popularFilms).hasSize(1);
        assertThat(popularFilms.get(0).getName()).isEqualTo("Inception");
    }

    @Test
    public void testAddAndRemoveLike() {
        // Arrange
        Film film = new Film();
        film.setName("Inception");
        film.setDescription("A mind-bending thriller.");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148L);
        film.setRatingId(1L);

        Long filmId = filmDao.createFilm(film);

        // Создаем пользователей
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1_login");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2_login");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1995, 5, 5));

        Long userId1 = userDao.createUser(user1);
        Long userId2 = userDao.createUser(user2);

        // Act
        filmDao.addLike(filmId, userId1);
        filmDao.addLike(filmId, userId2);

        // Assert
        List<Film> popularFilms = filmDao.getMostPopularFilms(1);
        assertThat(popularFilms).hasSize(1);
        assertThat(popularFilms.get(0).getName()).isEqualTo("Inception");

        // Remove like
        filmDao.removeLike(filmId, userId1);

        popularFilms = filmDao.getMostPopularFilms(1);
        assertThat(popularFilms).hasSize(1);
        assertThat(popularFilms.get(0).getName()).isEqualTo("Inception");
    }
}