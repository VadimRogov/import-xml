# Примеры curl-запросов для всех API-эндпоинтов

---

## Продукты (ProductController)

### Получить все продукты
```
GET /api/v1/products
```
**Параметры:**
- categoryId (опционально)
- search (опционально)
- active (опционально, true/false)
- page, size (пагинация)

**Пример:**
```sh
curl "http://localhost:8080/api/v1/products?page=0&size=20"
```

### Получить продукт по productId
```
GET /api/v1/products/{productId}
```
**Пример:**
```sh
curl "http://localhost:8080/api/v1/products/12345"
```

### Получить продукты по категории
```
GET /api/v1/products/category/{categoryId}
```
**Пример:**
```sh
curl "http://localhost:8080/api/v1/products/category/1104129?page=0&size=20"
```

### Поиск продуктов
```
GET /api/v1/products/search?query=Текст
```
**Пример:**
```sh
curl "http://localhost:8080/api/v1/products/search?query=Кружка&page=0&size=20"
```

### Загрузить XML с продуктами
```
POST /api/v1/products/upload-xml
```
**Параметры:** file (multipart/form-data)

**Пример:**
```sh
curl -F "file=@products.xml" http://localhost:8080/api/v1/products/upload-xml
```

### Импортировать только 10 продуктов из XML
```
POST /api/v1/products/import-10
```
**Параметры:** file (multipart/form-data)

**Пример:**
```sh
curl -F "file=@products.xml" http://localhost:8080/api/v1/products/import-10
```

---

## Категории (CategoryController)

### Получить все категории
```
GET /api/v1/categories
```
**Параметры:**
- parentId (опционально)
- active (опционально)
- page, size (пагинация)

**Пример:**
```sh
curl "http://localhost:8080/api/v1/categories?page=0&size=20"
```

### Получить категорию по categoryId
```
GET /api/v1/categories/{categoryId}
```
**Пример:**
```sh
curl "http://localhost:8080/api/v1/categories/1104129"
```

### Получить дерево категорий
```
GET /api/v1/categories/tree
```
**Параметры:** active (опционально)

**Пример:**
```sh
curl "http://localhost:8080/api/v1/categories/tree"
```

### Получить категорию с продуктами
```
GET /api/v1/categories/{categoryId}/products
```
**Пример:**
```sh
curl "http://localhost:8080/api/v1/categories/1104129/products?page=0&size=20"
```

### Загрузить XML с категориями
```
POST /api/v1/categories/upload-xml
```
**Параметры:**
- file (multipart/form-data)
- type (tree или catalogue)

**Пример:**
```sh
curl -F "file=@categories.xml" -F "type=tree" http://localhost:8080/api/v1/categories/upload-xml
```

---

## Комплекты (ComplectController)

### Загрузить XML с комплектами
```
POST /api/v1/complects/upload-xml
```
**Параметры:** file (multipart/form-data)

**Пример:**
```sh
curl -F "file=@complects.xml" http://localhost:8080/api/v1/complects/upload-xml
```

---

## Фильтры (FilterController)

### Загрузить XML с фильтрами
```
POST /api/v1/filters/upload-xml
```
**Параметры:** file (multipart/form-data)

**Пример:**
```sh
curl -F "file=@filters.xml" http://localhost:8080/api/v1/filters/upload-xml
```

---

## Остатки (StockController)

### Загрузить XML с остатками
```
POST /api/v1/stock/upload-xml
```
**Параметры:** file (multipart/form-data)

**Пример:**
```sh
curl -F "file=@stock.xml" http://localhost:8080/api/v1/stock/upload-xml
```

---

## Каталог (CatalogueController)

### Загрузить XML с каталогом (категории + продукты)
```
POST /api/v1/catalogue/upload-xml
```
**Параметры:** file (multipart/form-data)

**Пример:**
```sh
curl -F "file=@catalogue.xml" http://localhost:8080/api/v1/catalogue/upload-xml
```

---

## Синхронизация с OpenCart (SyncController)

### Запустить синхронизацию
```
POST /api/sync
```
**Пример:**
```sh
curl -X POST http://localhost:8080/api/sync
``` 