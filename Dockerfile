# Базовый образ
FROM openjdk:17-jdk-slim

# Рабочая директория внутри контейнера
WORKDIR /app

# Копируем JAR-файл из target в контейнер
COPY target/filmorate.jar /app/filmorate.jar

# Команда для запуска приложения
ENTRYPOINT ["java", "-jar", "filmorate.jar"]