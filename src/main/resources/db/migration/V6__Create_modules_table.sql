CREATE TABLE modules (
    id SERIAL PRIMARY KEY,
    course_id INTEGER NOT NULL,
    title VARCHAR(200) NOT NULL,
    order_index INTEGER NOT NULL,
    description TEXT,

    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,

    -- Уникальность порядка в рамках курса (опционально)
    UNIQUE (course_id, order_index),

    -- Уникальность названия в рамках курса (опционально)
    UNIQUE (course_id, title)
);

-- Индексы для быстрого поиска
CREATE INDEX idx_modules_course ON modules(course_id);
CREATE INDEX idx_modules_order ON modules(order_index);