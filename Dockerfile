# Используем официальный образ Java 17
FROM docker.io/library/eclipse-temurin:17-jdk-alpine

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем файлы проекта
COPY . .

# Скачиваем зависимости и собираем jar внутри контейнера
RUN ./mvnw dependency:go-offline
RUN ./mvnw package -DskipTests

# Создаем директорию для импорта
RUN mkdir -p /root/import-xml/import

# Открываем порт
EXPOSE 8080

# Запускаем приложение
CMD ["java", "-jar", "target/import-xml-0.0.1-SNAPSHOT.jar"]