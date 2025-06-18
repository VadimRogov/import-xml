# Import-xml Import Service

Сервис для импорта данных из API import-xml в OpenCart.

## Требования

- Linux (Ubuntu/Debian)
- Java 17
- Maven
- Docker и Docker Compose
- MySQL (уже установлена, база данных: kirilwgb_oc1)

## Установка

1. Установите необходимые компоненты:
```bash
sudo chmod +x install.sh
sudo ./install.sh
```

2. Настройте переменные окружения:
```bash
# - Учетные данные для существующей базы данных MySQL (kirilwgb_oc1)
# - Учетные данные для API Project111
```

## Запуск

### Локальный запуск (без Docker)

1. Соберите проект:
```bash
./mvnw clean package
```

2. Запустите приложение:
```bash
java -jar target/import-xml-1.0.0.jar
```

### Запуск в Docker

1. Соберите и запустите контейнер:
```bash
docker-compose up -d
```

2. Проверьте логи:
```bash
docker-compose logs -f app
```

## Конфигурация базы данных

Приложение использует существующую базу данных MySQL:
- Имя базы данных: `kirilwgb_oc1`
- Хост: `localhost`
- Порт: `3306`

Учетные данные для подключения к базе данных указываются в переменных окружения:
- `DB_USERNAME` - имя пользователя MySQL
- `DB_PASSWORD` - пароль пользователя MySQL

## Конфигурация API

Основные настройки находятся в файле `application.yml`:

- `import-xml.api.base-url`: Базовый URL API Project111
- `import-xml.api.username`: Логин для API
- `import-xml.api.password`: Пароль для API
- `import-xml.import.directory`: Директория для импорта файлов
- `import-xml.sync.cron`: Расписание синхронизации (по умолчанию каждые 4 часа)

## API Endpoints

- `GET /api/products` - Получить список продуктов
- `GET /api/categories` - Получить список категорий
- `POST /api/sync` - Запустить синхронизацию вручную

## Логи

Логи приложения находятся в директории `logs/application.log`

## Мониторинг

Доступны следующие эндпоинты для мониторинга:
- `GET /actuator/health` - Проверка состояния приложения
- `GET /actuator/metrics` - Метрики приложения
- `GET /actuator/info` - Информация о приложении 