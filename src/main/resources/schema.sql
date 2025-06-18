-- Создание таблицы для логов импорта
CREATE TABLE IF NOT EXISTS import_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL COMMENT 'Тип импорта (Товары, Категории, Остатки)',
    status VARCHAR(20) NOT NULL COMMENT 'Статус импорта (В процессе, Успешно, Ошибка)',
    total_items INT COMMENT 'Общее количество элементов для импорта',
    processed_items INT DEFAULT 0 COMMENT 'Количество обработанных элементов',
    failed_items INT DEFAULT 0 COMMENT 'Количество элементов с ошибками',
    source_file VARCHAR(255) COMMENT 'Путь к исходному файлу',
    error_message VARCHAR(1000) COMMENT 'Сообщение об ошибке',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата и время создания записи',
    updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT 'Дата и время последнего обновления',
    completed_at DATETIME COMMENT 'Дата и время завершения импорта',
    execution_time_ms BIGINT COMMENT 'Время выполнения в миллисекундах',
    import_profile VARCHAR(100) COMMENT 'Профиль импорта',
    user_id BIGINT COMMENT 'ID пользователя, выполнившего импорт',
    session_id VARCHAR(100) COMMENT 'ID сессии импорта',
    INDEX idx_type_status (type, status),
    INDEX idx_created_at (created_at),
    INDEX idx_completed_at (completed_at),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Логи импорта данных';

-- Создание представления для статистики импорта
CREATE OR REPLACE VIEW import_statistics AS
SELECT
    type,
    status,
    COUNT(*) as total_imports,
    AVG(total_items) as avg_total_items,
    AVG(processed_items) as avg_processed_items,
    AVG(failed_items) as avg_failed_items,
    AVG(execution_time_ms) as avg_execution_time,
    MIN(created_at) as first_import,
    MAX(created_at) as last_import
FROM import_log
GROUP BY type, status;

-- Создание представления для последних импортов
CREATE OR REPLACE VIEW recent_imports AS
SELECT
    id,
    type,
    status,
    total_items,
    processed_items,
    failed_items,
    created_at,
    completed_at,
    execution_time_ms,
    error_message
FROM import_log
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
ORDER BY created_at DESC;