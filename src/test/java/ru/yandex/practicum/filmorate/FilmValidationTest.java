package ru.yandex.practicum.filmorate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validation.OnCreate;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FilmValidationTest {

    private static Validator validator;

    @BeforeAll
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidFilmOnCreate() {
        Film film = new Film();
        film.setName("Film Name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100L);

        Set<ConstraintViolation<Film>> violations = validator.validate(film, OnCreate.class);
        assertTrue(violations.isEmpty(), "Ошибок валидации быть не должно");
    }

    @Test
    public void testInvalidReleaseDateOnCreate() {
        Film film = new Film();
        film.setName("Film Name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(1895, 12, 27)); // Дата раньше 28 декабря 1895 года
        film.setDuration(100L);

        Set<ConstraintViolation<Film>> violations = validator.validate(film, OnCreate.class);
        assertFalse(violations.isEmpty(), "Должна быть ошибка валидации для даты релиза");
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("Дата релиза не может быть раньше"));
    }

    @Test
    public void testNegativeDurationOnCreate() {
        Film film = new Film();
        film.setName("Film Name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(-10L); // Отрицательная продолжительность

        Set<ConstraintViolation<Film>> violations = validator.validate(film, OnCreate.class);
        assertFalse(violations.isEmpty(), "Должна быть ошибка валидации для отрицательной продолжительности");
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("Продолжительность фильма должна быть положительной"));
    }
}