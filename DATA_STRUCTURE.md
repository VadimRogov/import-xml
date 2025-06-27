# Структура данных и связи между таблицами

## Таблица `products`
- `id` BIGINT PRIMARY KEY AUTO_INCREMENT
- `product_id` VARCHAR(255) UNIQUE NOT NULL — внешний идентификатор товара
- `name` TEXT — наименование товара
- `description` TEXT — описание
- `brand` TEXT
- `code` TEXT
- `content` TEXT
- `group_id` TEXT
- `image` TEXT
- `status_id` INT
- `status_name` TEXT
- `barcode` TEXT
- `weight` INT
- `volume` INT
- `ondemand` BOOLEAN
- `moq` TEXT
- `days` TEXT
- `demandtype` TEXT
- `multiplicity` INT
- `product_size` TEXT
- `matherial` TEXT
- `alert` TEXT
- `small_image` TEXT
- `super_big_image` TEXT
- `price` DECIMAL(10,2)
- `quantity` INT
- ... (дополнительные поля)

## Таблица `categories`
- `id` BIGINT PRIMARY KEY AUTO_INCREMENT
- `category_id` VARCHAR(255) UNIQUE NOT NULL
- `name` TEXT
- `description` TEXT
- `parent_id` VARCHAR(255) — ссылка на родительскую категорию
- `uri` TEXT
- `image` TEXT
- `page_id` VARCHAR(255) UNIQUE
- ...

## Таблица `complects`
- `id` BIGINT PRIMARY KEY AUTO_INCREMENT
- `complect_id` VARCHAR(255) UNIQUE NOT NULL
- `name` TEXT
- `description` TEXT
- `product_id` VARCHAR(255) — основной товар-комплект
- ...

## Таблица `complect_parts`
- `id` BIGINT PRIMARY KEY AUTO_INCREMENT
- `complect_id` VARCHAR(255) — внешний ключ на complects
- `product_id` VARCHAR(255) — товар-часть комплекта
- `code` TEXT
- `name` TEXT
- ...

## Таблица `filters`
- `id` BIGINT PRIMARY KEY AUTO_INCREMENT
- `filter_id` VARCHAR(255) UNIQUE NOT NULL
- `filter_type_id` VARCHAR(255)
- `name` TEXT
- ...

## Таблица `filter_types`
- `id` BIGINT PRIMARY KEY AUTO_INCREMENT
- `filter_type_id` VARCHAR(255) UNIQUE NOT NULL
- `name` TEXT

## Таблица `product_filters` (связь многие-ко-многим)
- `product_id` VARCHAR(255) — внешний ключ на products
- `filter_id` VARCHAR(255) — внешний ключ на filters

## Таблица `category_products` (связь многие-ко-многим)
- `category_id` VARCHAR(255) — внешний ключ на categories
- `product_id` VARCHAR(255) — внешний ключ на products

## Таблица `stock`
- `id` BIGINT PRIMARY KEY AUTO_INCREMENT
- `product_id` VARCHAR(255) — внешний ключ на products
- `quantity` INT — остаток на складе
- ...

---

## Связи между таблицами
- **products <-> categories**: многие-ко-многим через `category_products`
- **products <-> filters**: многие-ко-многим через `product_filters`
- **products <-> complects**: один-ко-многим (product_id в complects)
- **complects <-> complect_parts**: один-ко-многим
- **products <-> complect_parts**: один-ко-многим (product_id в complect_parts)
- **categories**: иерархия через `parent_id`
- **products <-> stock**: один-ко-многим (product_id в stock)
- **filters <-> filter_types**: многие-ко-одному (filter_type_id)

---

## Пример схемы связей (SQL)

```sql
-- Пример создания связей
CREATE TABLE products (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id VARCHAR(255) UNIQUE NOT NULL,
  name TEXT,
  ...
);

CREATE TABLE categories (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  category_id VARCHAR(255) UNIQUE NOT NULL,
  name TEXT,
  parent_id VARCHAR(255),
  ...
);

CREATE TABLE category_products (
  category_id VARCHAR(255),
  product_id VARCHAR(255),
  PRIMARY KEY (category_id, product_id),
  FOREIGN KEY (category_id) REFERENCES categories(category_id),
  FOREIGN KEY (product_id) REFERENCES products(product_id)
);

CREATE TABLE filters (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  filter_id VARCHAR(255) UNIQUE NOT NULL,
  filter_type_id VARCHAR(255),
  name TEXT,
  ...
);

CREATE TABLE product_filters (
  product_id VARCHAR(255),
  filter_id VARCHAR(255),
  PRIMARY KEY (product_id, filter_id),
  FOREIGN KEY (product_id) REFERENCES products(product_id),
  FOREIGN KEY (filter_id) REFERENCES filters(filter_id)
);

CREATE TABLE complects (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  complect_id VARCHAR(255) UNIQUE NOT NULL,
  product_id VARCHAR(255),
  ...
);

CREATE TABLE complect_parts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  complect_id VARCHAR(255),
  product_id VARCHAR(255),
  ...
);

CREATE TABLE stock (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id VARCHAR(255),
  quantity INT,
  ...
);
```

---

## Краткое текстовое описание
- **products** — товары
- **categories** — категории (с иерархией)
- **complects** — комплекты (наборы товаров)
- **filters** и **filter_types** — фильтры и их типы
- **product_filters** — связь товаров и фильтров
- **category_products** — связь товаров и категорий
- **stock** — остатки товаров на складе

---

Если нужно добавить примеры данных или расширить описание — сообщите! 

---

## Примеры структуры и связей в XML-файлах

### Пример товара (products) в XML
```xml
<product>
  <product_id>218767</product_id>
  <name>Кружка Cheer Up</name>
  <description>...</description>
  <brand>...</brand>
  <code>4665-60</code>
  <content>...</content>
  <group_id>...</group_id>
  <image>...</image>
  <status_id>1</status_id>
  <status_name>В наличии</status_name>
  <barcode>1234567890</barcode>
  <weight>350</weight>
  <volume>400</volume>
  <ondemand>false</ondemand>
  <moq>1</moq>
  <days>2</days>
  <demandtype>...</demandtype>
  <multiplicity>1</multiplicity>
  <product_size>XL</product_size>
  <matherial>Керамика</matherial>
  <alert>...</alert>
  <small_image>...</small_image>
  <super_big_image>...</super_big_image>
  <price>199.99</price>
  <quantity>100</quantity>
  <!-- ... -->
</product>
```

### Пример категории (categories) в XML
```xml
<category>
  <category_id>1104129</category_id>
  <name>Корпоративная одежда с логотипом</name>
  <description>...</description>
  <parent_id>1</parent_id>
  <uri>odejda</uri>
  <image>...</image>
  <page_id>1104129</page_id>
  <!-- ... -->
</category>
```

### Пример связи категория — товар (category_products) в XML (файл tree)
```xml
<page>
  <page_id>1104129</page_id>
  <name>Корпоративная одежда с логотипом</name>
  <product>
    <page>1104129</page>
    <product>218767</product>
  </product>
  <!-- ... -->
</page>
```

### Пример комплекта (complects) и его частей (complect_parts) в XML
```xml
<complect>
  <id>271</id>
  <product_id>35877</product_id> <!-- основной товар-комплект -->
  <tocomplect>false</tocomplect>
  <parts>
    <part>
      <id>673</id>
      <product_id>43937</product_id> <!-- товар-часть комплекта -->
      <code>4665-60</code>
      <name>Кружка Cheer Up без ложки, белая</name>
      <small_image>...</small_image>
      <super_big_image>...</super_big_image>
      <print>
        <name>H1</name>
        <description>деколь (4 цвета)</description>
      </print>
    </part>
    <!-- ... -->
  </parts>
</complect>
```

### Пример фильтров и их типов (filters, filter_types) в XML
```xml
<filtertypes>
  <filtertype>
    <filtertypeid>1</filtertypeid>
    <filtertypename>Размер одежды</filtertypename>
    <filters>
      <filter>
        <filterid>998</filterid>
        <filtername>S/M</filtername>
      </filter>
      <!-- ... -->
    </filters>
  </filtertype>
  <!-- ... -->
</filtertypes>
```

### Пример связи товар — фильтр (product_filters) в XML
(Обычно реализуется отдельным тегом или атрибутом в товаре, например:)
```xml
<product>
  ...
  <filters>
    <filter>998</filter>
    <filter>1001</filter>
  </filters>
</product>
```

---

**Как парсить связи:**
- Для связей многие-ко-многим (категория-товар, товар-фильтр) ищите соответствующие вложенные теги или отдельные секции, где указывается ID обеих сущностей.
- Для иерархий (категории) используйте поле `parent_id`.
- Для комплектов используйте вложенные теги `<parts>` и `<part>`.

Если нужны примеры кода для парсинга — дайте знать! 