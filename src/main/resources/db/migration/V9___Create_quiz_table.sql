CREATE TABLE quizzes (
    id SERIAL PRIMARY KEY,
    module_id INTEGER UNIQUE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    time_limit_minutes INTEGER,
    passing_score INTEGER DEFAULT 70,
    max_attempts INTEGER DEFAULT 3,
    is_published BOOLEAN DEFAULT FALSE,

    -- Либо module_id, либо course_id (XOR constraint через CHECK)
    FOREIGN KEY (module_id) REFERENCES modules(id) ON DELETE CASCADE,

    -- Проверка passing_score
    CONSTRAINT check_passing_score
        CHECK (passing_score IS NULL OR (passing_score >= 0 AND passing_score <= 100)),

    -- Проверка max_attempts
    CONSTRAINT check_max_attempts
        CHECK (max_attempts IS NULL OR max_attempts > 0)
);

-- Индексы
CREATE INDEX idx_quizzes_module ON quizzes(module_id);
CREATE INDEX idx_quizzes_published ON quizzes(is_published);