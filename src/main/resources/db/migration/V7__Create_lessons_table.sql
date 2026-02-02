CREATE TABLE lessons (
    id SERIAL PRIMARY KEY,
    module_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    video_url VARCHAR(500),

    FOREIGN KEY (module_id) REFERENCES modules(id) ON DELETE CASCADE,

    -- Уникальность названия в рамках модуля (опционально)
    UNIQUE (module_id, title)
);

-- Индексы для быстрого поиска
CREATE INDEX idx_lessons_module ON lessons(module_id);