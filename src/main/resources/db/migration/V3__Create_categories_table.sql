-- Таблица категорий курсов
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Индексы
CREATE INDEX idx_categories_name ON categories(name);

-- Комментарии
COMMENT ON TABLE categories IS 'Категории учебных курсов';
COMMENT ON COLUMN categories.name IS 'Название категории (уникальное)';