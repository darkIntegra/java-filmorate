//package ru.yandex.practicum.filmorate.storage.user;
//
//import org.springframework.stereotype.Component;
//import ru.yandex.practicum.filmorate.model.User;
//
//import java.util.*;
//
//@Component("inMemoryUserStorage")
//public class InMemoryUserStorage implements UserStorage {
//
//    private final Map<Long, User> users = new HashMap<>();
//    private final Map<Long, Set<Long>> friends = new HashMap<>();
//    private long nextId = 1;
//
//    @Override
//    public Long createUser(User user) {
//        user.setId(nextId++);
//        users.put(user.getId(), user);
//        return user.getId();
//    }
//
//    @Override
//    public void updateUser(User user) {
//        if (!users.containsKey(user.getId())) {
//            throw new IllegalArgumentException("Пользователь с ID " + user.getId() + " не найден.");
//        }
//        users.put(user.getId(), user);
//    }
//
//    @Override
//    public User getUserById(Long id) {
//        User user = users.get(id);
//        if (user == null) {
//            throw new IllegalArgumentException("Пользователь с ID " + id + " не найден.");
//        }
//        return user;
//    }
//
//    @Override
//    public List<User> getAllUsers() {
//        return new ArrayList<>(users.values());
//    }
//
//    @Override
//    public void addFriend(Long userId, Long friendId) {
//        friends.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
//        friends.computeIfAbsent(friendId, k -> new HashSet<>()).add(userId);
//    }
//
//    @Override
//    public void removeFriend(Long userId, Long friendId) {
//        friends.getOrDefault(userId, Collections.emptySet()).remove(friendId);
//        friends.getOrDefault(friendId, Collections.emptySet()).remove(userId);
//    }
//
//    @Override
//    public Set<Long> getFriends(Long userId) {
//        return friends.getOrDefault(userId, Collections.emptySet());
//    }
//}