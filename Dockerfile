# Используем официальный образ Java 17
FROM eclipse-temurin:17-jdk-alpine

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем pom.xml и mvnw
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Устанавливаем зависимости
RUN ./mvnw dependency:go-offline

# Копируем исходный код
COPY src ./src

# Собираем приложение
RUN ./mvnw package -DskipTests

# Копируем собранный jar-файл
COPY target/import-xml-1.0.0.jar target/

# Создаем директорию для импорта
RUN mkdir -p /root/import-xml/import

# Открываем порт
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "target/import-xml-1.0.0.jar"]