CREATE TABLE assignments (
    id SERIAL PRIMARY KEY,
    lesson_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    due_date TIMESTAMP,
    max_score INT DEFAULT 100,

    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,

    -- Уникальность названия в рамках урока
    UNIQUE (lesson_id, title),

    -- Проверка максимального балла
    CONSTRAINT check_max_score CHECK (max_score > 0 AND max_score <= 1000)
);

-- Индексы для быстрого поиска
CREATE INDEX idx_assignments_lesson ON assignments(lesson_id);
CREATE INDEX idx_assignments_due_date ON assignments(due_date);