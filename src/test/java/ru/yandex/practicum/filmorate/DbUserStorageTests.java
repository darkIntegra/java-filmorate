package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.dao.user.DbUserStorage;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test") // Активируем профиль "test"
public class DbUserStorageTests {

    @Autowired
    private DbUserStorage dbUserStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Очистка таблиц перед каждым тестом
        clearTables();
    }

    private void clearTables() {
        // Отключаем проверку внешних ключей
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");

        // Очищаем таблицы
        jdbcTemplate.execute("TRUNCATE TABLE friendships");
        jdbcTemplate.execute("TRUNCATE TABLE likes");
        jdbcTemplate.execute("TRUNCATE TABLE users");

        // Включаем проверку внешних ключей
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }


    @Test
    public void testCreateUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testLogin");
        user.setName("Test Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Long userId = dbUserStorage.createUser(user);
        assertThat(userId).isNotNull();

        User createdUser = dbUserStorage.getUserById(userId);
        assertThat(createdUser.getEmail()).isEqualTo("test@example.com");
        assertThat(createdUser.getLogin()).isEqualTo("testLogin");
        assertThat(createdUser.getName()).isEqualTo("Test Name");
        assertThat(createdUser.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
    }

    @Test
    public void testUpdateUser() {
        User user = new User();
        user.setEmail("old@example.com");
        user.setLogin("oldLogin");
        user.setName("Old Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Long userId = dbUserStorage.createUser(user);

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setEmail("new@example.com");
        updatedUser.setLogin("newLogin");
        updatedUser.setName("New Name");
        updatedUser.setBirthday(LocalDate.of(1995, 1, 1));

        dbUserStorage.updateUser(updatedUser);

        User fetchedUser = dbUserStorage.getUserById(userId);
        assertThat(fetchedUser.getEmail()).isEqualTo("new@example.com");
        assertThat(fetchedUser.getLogin()).isEqualTo("newLogin");
        assertThat(fetchedUser.getName()).isEqualTo("New Name");
        assertThat(fetchedUser.getBirthday()).isEqualTo(LocalDate.of(1995, 1, 1));
    }

    @Test
    public void testAddAndRemoveFriend() {
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1Login");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2Login");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1995, 1, 1));

        Long userId1 = dbUserStorage.createUser(user1);
        Long userId2 = dbUserStorage.createUser(user2);

        dbUserStorage.addFriend(userId1, userId2);

        List<User> friendsOfUser1 = dbUserStorage.getFriends(userId1);
        assertThat(friendsOfUser1).hasSize(1);
        assertThat(friendsOfUser1.get(0).getId()).isEqualTo(userId2);

        dbUserStorage.removeFriend(userId1, userId2);

        friendsOfUser1 = dbUserStorage.getFriends(userId1);
        assertThat(friendsOfUser1).isEmpty();
    }

    @Test
    public void testGetCommonFriends() {
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1Login");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2Login");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1995, 1, 1));

        User user3 = new User();
        user3.setEmail("user3@example.com");
        user3.setLogin("user3Login");
        user3.setName("User Three");
        user3.setBirthday(LocalDate.of(2000, 1, 1));

        Long userId1 = dbUserStorage.createUser(user1);
        Long userId2 = dbUserStorage.createUser(user2);
        Long userId3 = dbUserStorage.createUser(user3);

        dbUserStorage.addFriend(userId1, userId2);
        dbUserStorage.addFriend(userId1, userId3);
        dbUserStorage.addFriend(userId2, userId3);

        List<User> commonFriends = dbUserStorage.getCommonFriends(userId1, userId2);
        assertThat(commonFriends).hasSize(1);
        assertThat(commonFriends.get(0).getId()).isEqualTo(userId3);
    }
}