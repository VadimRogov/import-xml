# Импорт 10 продуктов из XML через API

## Описание

Добавлен эндпоинт для парсинга и сохранения первых 10 продуктов из XML-файла в базу данных.

**URL:**
```
POST /api/v1/products/import-10
```

**Параметры запроса:**
- `file` — XML-файл с продуктами (multipart/form-data)

## Пример curl-запроса

```sh
curl -F "file=@products.xml" http://localhost:8080/api/v1/products/import-10
```

- Замените `products.xml` на путь к вашему XML-файлу.
- Если приложение работает на другом порту или хосте — измените URL.

## Ответы
- **200 OK** — Продукты успешно импортированы:
  > First 10 products imported successfully
- **500 Internal Server Error** — Ошибка при обработке файла:
  > Error importing first 10 products: <текст ошибки>

## Примечания
- Импортируются только первые 10 продуктов из файла, остальные игнорируются.
- Структура XML должна соответствовать формату `<product>...</product>` (см. DATA_STRUCTURE.md). 