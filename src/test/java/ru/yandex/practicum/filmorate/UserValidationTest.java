package ru.yandex.practicum.filmorate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.OnCreate;
import ru.yandex.practicum.filmorate.validation.OnUpdate;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserValidationTest {

    private static Validator validator;

    @BeforeAll
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidUserOnCreate() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("user123");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user, OnCreate.class);
        assertTrue(violations.isEmpty(), "Ошибок валидации быть не должно");
    }

    @Test
    public void testInvalidEmailOnCreate() {
        User user = new User();
        user.setEmail("invalid-email"); // Некорректный email
        user.setLogin("user123");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user, OnCreate.class);
        assertFalse(violations.isEmpty(), "Должна быть ошибка валидации для некорректного email");
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("некорректный формат email"));
    }

    @Test
    public void testBlankLoginOnCreate() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin(""); // Пустой логин
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user, OnCreate.class);
        assertFalse(violations.isEmpty(), "Должна быть ошибка валидации для пустого логина");
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("Логин не должен быть пустым и не должен " +
                "содержать пробелы."));
    }

    @Test
    public void testFutureBirthdayOnCreate() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("user123");
        user.setBirthday(LocalDate.now().plusDays(1)); // Дата рождения в будущем

        Set<ConstraintViolation<User>> violations = validator.validate(user, OnCreate.class);
        assertFalse(violations.isEmpty(), "Должна быть ошибка валидации для даты рождения в будущем");
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("Дата рождения не может быть в будущем"));
    }

    @Test
    public void testNullFieldsOnUpdate() {
        User user = new User();
        user.setId(1L); // ID обязателен при обновлении

        Set<ConstraintViolation<User>> violations = validator.validate(user, OnUpdate.class);
        assertTrue(violations.isEmpty(), "Ошибок валидации быть не должно, так как поля опциональны");
    }
}