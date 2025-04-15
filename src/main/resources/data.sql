-- Добавление рейтингов MPAA
INSERT INTO ratings (id, name)
SELECT 1, 'G' WHERE NOT EXISTS (SELECT 1 FROM ratings WHERE id = 1);

INSERT INTO ratings (id, name)
SELECT 2, 'PG' WHERE NOT EXISTS (SELECT 1 FROM ratings WHERE id = 2);

INSERT INTO ratings (id, name)
SELECT 3, 'PG-13' WHERE NOT EXISTS (SELECT 1 FROM ratings WHERE id = 3);

INSERT INTO ratings (id, name)
SELECT 4, 'R' WHERE NOT EXISTS (SELECT 1 FROM ratings WHERE id = 4);

INSERT INTO ratings (id, name)
SELECT 5, 'NC-17' WHERE NOT EXISTS (SELECT 1 FROM ratings WHERE id = 5);

-- Добавление жанров
INSERT INTO genres (id, name)
SELECT 1, 'Комедия' WHERE NOT EXISTS (SELECT 1 FROM genres WHERE id = 1);

INSERT INTO genres (id, name)
SELECT 2, 'Драма' WHERE NOT EXISTS (SELECT 1 FROM genres WHERE id = 2);

INSERT INTO genres (id, name)
SELECT 3, 'Мультфильм' WHERE NOT EXISTS (SELECT 1 FROM genres WHERE id = 3);

INSERT INTO genres (id, name)
SELECT 4, 'Триллер' WHERE NOT EXISTS (SELECT 1 FROM genres WHERE id = 4);

INSERT INTO genres (id, name)
SELECT 5, 'Документальный' WHERE NOT EXISTS (SELECT 1 FROM genres WHERE id = 5);

INSERT INTO genres (id, name)
SELECT 6, 'Боевик' WHERE NOT EXISTS (SELECT 1 FROM genres WHERE id = 6);

-- Добавление пользователей
INSERT INTO users (email, login, name, birthday)
VALUES
    ('user1@example.com', 'user1', 'User One', '1990-01-01'),
    ('user2@example.com', 'user2', 'User Two', '1985-05-15'),
    ('user3@example.com', 'user3', 'User Three', '1980-10-20');

-- Добавление фильмов
INSERT INTO films (name, description, release_date, duration, rating_id)
VALUES
    ('Film One', 'Description of Film One', '2020-01-01', 120, 1),
    ('Film Two', 'Description of Film Two', '2019-06-15', 90, 2);

-- Привязка жанров к фильмам
INSERT INTO film_genres (film_id, genre_id)
VALUES
    (1, 1), -- Film One -> Комедия
    (1, 2), -- Film One -> Драма
    (2, 3); -- Film Two -> Мультфильм

-- Добавление дружбы между пользователями
INSERT INTO friendships (user_id, friend_id, status)
VALUES
    (1, 2, 'CONFIRMED'), -- User One и User Two являются друзьями
    (1, 3, 'UNCONFIRMED'); -- User One отправил запрос User Three

-- Добавление лайков
INSERT INTO likes (film_id, user_id)
VALUES
    (1, 1), -- User One поставил лайк Film One
    (1, 2), -- User Two поставил лайк Film One
    (2, 1); -- User One поставил лайк Film Two