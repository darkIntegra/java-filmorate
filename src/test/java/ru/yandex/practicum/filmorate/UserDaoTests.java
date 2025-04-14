package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dao.user.UserDao;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDao.class})
class UserDaoTests {

    private final UserDao userDao;

    @Test
    public void testCreateUser() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("test_login");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        // Act
        Long generatedId = userDao.createUser(user);

        // Assert
        assertThat(generatedId).isNotNull().isPositive();

        User createdUser = userDao.getUserById(generatedId);
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getEmail()).isEqualTo("test@example.com");
        assertThat(createdUser.getLogin()).isEqualTo("test_login");
        assertThat(createdUser.getName()).isEqualTo("Test User");
        assertThat(createdUser.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
    }

    @Test
    public void testUpdateUser() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("test_login");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Long userId = userDao.createUser(user);

        // Act
        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setEmail("updated@example.com");
        updatedUser.setLogin("updated_login");
        updatedUser.setName("Updated User");
        updatedUser.setBirthday(LocalDate.of(1995, 5, 5));

        userDao.updateUser(updatedUser);

        // Assert
        User fetchedUser = userDao.getUserById(userId);
        assertThat(fetchedUser).isNotNull();
        assertThat(fetchedUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(fetchedUser.getLogin()).isEqualTo("updated_login");
        assertThat(fetchedUser.getName()).isEqualTo("Updated User");
        assertThat(fetchedUser.getBirthday()).isEqualTo(LocalDate.of(1995, 5, 5));
    }

    @Test
    public void testGetUserById() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("test_login");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Long userId = userDao.createUser(user);

        // Act
        User fetchedUser = userDao.getUserById(userId);

        // Assert
        assertThat(fetchedUser).isNotNull();
        assertThat(fetchedUser.getId()).isEqualTo(userId);
        assertThat(fetchedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(fetchedUser.getLogin()).isEqualTo("test_login");
        assertThat(fetchedUser.getName()).isEqualTo("Test User");
        assertThat(fetchedUser.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
    }

    @Test
    public void testGetAllUsers() {
        // Arrange
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

        userDao.createUser(user1);
        userDao.createUser(user2);

        // Act
        List<User> users = userDao.getAllUsers();

        // Assert
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getEmail).containsExactlyInAnyOrder("user1@example.com",
                "user2@example.com");
    }

    @Test
    public void testAddAndRemoveFriend() {
        // Arrange
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
        userDao.addFriend(userId1, userId2);

        // Assert
        Set<Long> friendsOfUser1 = userDao.getFriends(userId1);
        Set<Long> friendsOfUser2 = userDao.getFriends(userId2);

        assertThat(friendsOfUser1).containsExactly(userId2);
        assertThat(friendsOfUser2).containsExactly(userId1);

        // Remove friend
        userDao.removeFriend(userId1, userId2);

        friendsOfUser1 = userDao.getFriends(userId1);
        friendsOfUser2 = userDao.getFriends(userId2);

        assertThat(friendsOfUser1).isEmpty();
        assertThat(friendsOfUser2).isEmpty();
    }
}