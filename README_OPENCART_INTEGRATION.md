# Интеграция Spring Boot сервиса с OpenCart через REST API

## 1. Требования к OpenCart

1. OpenCart должен быть установлен и доступен по HTTP(S).
2. На стороне OpenCart должен быть установлен и настроен REST API-модуль, поддерживающий:
   - POST-запросы на `/api/products` (для товаров)
   - POST-запросы на `/api/categories` (для категорий)
   - Приём данных в формате JSON
   - Проверку API-ключа через заголовок `X-API-Key`

**Примеры модулей:**
- [opencart-restapi (GitHub)](https://github.com/burk2/opencart-restapi)
- [oc-rest-api (OpenCart Marketplace)](https://www.opencart.com/index.php?route=marketplace/extension/info&extension_id=20892)

> ⚠️ Если у вас нет REST API для OpenCart — установите один из модулей или доработайте свой.

---

## 2. Настройка Spring Boot сервиса

### 2.1. Конфигурация `application.yml`

```yaml
opencart:
  base-url: http://ВАШ_OPENCART_СЕРВЕР
  api-key: ВАШ_API_КЛЮЧ
  username: admin
  password: admin
  sync:
    batch-size: 100
    cron: "0 0 */4 * * *"  # каждые 4 часа
```

- `base-url` — адрес вашего OpenCart (например, http://localhost или https://shop.example.com)
- `api-key` — API-ключ, который должен совпадать с ключом на стороне OpenCart REST API
- `cron` — расписание автоматической отправки данных

### 2.2. Проверка сервисов
- `OpenCartService` — отвечает за отправку товаров и категорий
- `SyncService` — отвечает за автоматическую синхронизацию по расписанию

---

## 3. Структура отправляемых данных

### 3.1. Пример запроса для товара (`/api/products`)
```json
{
  "product_id": 12345,
  "model": "ART-001",
  "sku": "ART-001",
  "quantity": 10,
  "price": 199.99,
  "status": true,
  "image": "/images/product1.jpg",
  "descriptions": [
    {
      "languageId": 1,
      "name": "Название товара",
      "description": "Описание товара",
      "metaTitle": "Meta Title",
      "metaDescription": "Meta Description"
    }
  ],
  "categoryIds": [100, 101]
}
```

### 3.2. Пример запроса для категории (`/api/categories`)
```json
{
  "category_id": 100,
  "parent_id": 0,
  "name": "Категория",
  "description": "Описание категории",
  "status": true,
  "sortOrder": 1,
  "image": "/images/cat1.jpg",
  "descriptions": [
    {
      "languageId": 1,
      "name": "Категория",
      "description": "Описание категории",
      "metaTitle": "Meta Title",
      "metaDescription": "Meta Description"
    }
  ]
}
```

---

## 4. Проверка работы интеграции

1. Убедитесь, что OpenCart REST API доступен по адресу, указанному в `base-url`.
2. Проверьте, что API-ключ совпадает с ключом на стороне OpenCart.
3. Запустите Spring Boot сервис.
4. Дождитесь автоматической синхронизации (по cron) или вызовите вручную (например, через POST `/api/sync` если реализовано).
5. Проверьте, что товары и категории появились/обновились в OpenCart.
6. Проверяйте логи приложения (`logs/application.log`) на наличие ошибок.

---

## 5. Рекомендации по доработке OpenCart (если требуется)

- Если у вас нет REST API — установите модуль (см. выше).
- Проверьте, что эндпоинты `/api/products` и `/api/categories` принимают POST-запросы с JSON и API-ключом.
- Если структура данных отличается — доработайте маппинг в `SyncService`/`OpenCartService` или на стороне OpenCart.
- Для тестирования можно использовать Postman/cURL:

```bash
curl -X POST \
  http://ВАШ_OPENCART_СЕРВЕР/api/products \
  -H 'X-API-Key: ВАШ_API_КЛЮЧ' \
  -H 'Content-Type: application/json' \
  -d '{...}'
```

---

## 6. Возможные проблемы и их решение

- **Ошибка 401/403:** Проверьте API-ключ и права доступа.
- **Ошибка 404:** Проверьте, что эндпоинты существуют на стороне OpenCart.
- **Ошибка 500:** Проверьте логи OpenCart и корректность структуры JSON.
- **Нет данных в OpenCart:** Проверьте, что синхронизация прошла успешно, и нет ошибок в логах Spring Boot.

---

## 7. Дополнительно
- Для ручного запуска синхронизации реализуйте REST-эндпоинт (например, POST `/api/sync`), который вызывает `SyncService.syncData()`.
- Для мониторинга используйте actuator-эндпоинты (`/actuator/health`, `/actuator/metrics`).
- Для отладки включите подробное логирование.

---

## 8. Настройка OpenCart REST API (пошагово)

1. **Установите REST API модуль для OpenCart**
   - Например, [opencart-restapi (GitHub)](https://github.com/burk2/opencart-restapi) или аналогичный.
   - Следуйте инструкции модуля для установки и активации.
2. **Сгенерируйте API-ключ** в настройках модуля и скопируйте его.
3. **Проверьте, что появились эндпоинты:**
   - `POST /api/products` — для товаров
   - `POST /api/categories` — для категорий
4. **Проверьте, что OpenCart принимает POST-запросы с заголовком `X-API-Key` и телом в формате JSON.**
5. **Пропишите API-ключ и адрес OpenCart в `application.yml` вашего Spring Boot сервиса.**

---

## 9. Ручной запуск синхронизации

- Теперь вы можете вручную запустить синхронизацию с OpenCart через POST-запрос:

```
POST http://localhost:8080/api/sync
```

- В ответ получите сообщение об успехе или ошибке.
- Это удобно для тестирования и отладки.

---

## 10. Как OpenCart получает данные

- Ваш сервис отправляет данные по HTTP POST на:
  - `http://ВАШ_OPENCART_СЕРВЕР/api/products` (товары)
  - `http://ВАШ_OPENCART_СЕРВЕР/api/categories` (категории)
- В заголовке: `X-API-Key: ВАШ_API_КЛЮЧ`
- В теле: JSON с данными (см. примеры выше)
- OpenCart REST API принимает эти данные и добавляет/обновляет товары и категории в своей базе.

---

## 11. Что нужно сделать в OpenCart для работы автоматической интеграции

1. **Установите REST API модуль для OpenCart**
   - Пример: [opencart-restapi (GitHub)](https://github.com/burk2/opencart-restapi) или аналогичный модуль из OpenCart Marketplace.
   - Следуйте инструкции по установке и активации модуля.

2. **Сгенерируйте API-ключ**
   - В настройках REST API модуля создайте новый ключ (API Key).
   - Скопируйте этот ключ — он понадобится для настройки Spring Boot сервиса.

3. **Проверьте наличие эндпоинтов**
   - После установки модуля должны быть доступны:
     - `POST /api/products` — для загрузки/обновления товаров
     - `POST /api/categories` — для загрузки/обновления категорий

4. **Проверьте права доступа**
   - Убедитесь, что у пользователя/ключа есть права на добавление и обновление товаров и категорий через API.

5. **Проверьте работу API вручную**
   - Используйте Postman или cURL для теста:
   ```bash
   curl -X POST http://ВАШ_OPENCART_СЕРВЕР/api/products \
     -H 'X-API-Key: ВАШ_API_КЛЮЧ' \
     -H 'Content-Type: application/json' \
     -d '{"product_id": 12345, ...}'
   ```
   - Если OpenCart возвращает успешный ответ — всё готово к интеграции.

6. **Пропишите API-ключ и адрес OpenCart в `application.yml` вашего Spring Boot сервиса**
   ```yaml
   opencart:
     base-url: http://ВАШ_OPENCART_СЕРВЕР
     api-key: ВАШ_API_КЛЮЧ
   ```

7. **Перезапустите Spring Boot сервис**
   - После этого интеграция будет работать автоматически: сервис сам отправит все данные в OpenCart по расписанию или по ручному запросу.

---

**Если возникнут вопросы по установке или настройке REST API для OpenCart — напишите, помогу подобрать и настроить модуль!**

**Если возникнут вопросы по настройке или интеграции — обращайтесь!** 