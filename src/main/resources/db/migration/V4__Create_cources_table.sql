-- Таблица учебных курсов
CREATE TABLE courses (
    id SERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    category_id INTEGER NOT NULL,
    teacher_id INTEGER NOT NULL,
    duration VARCHAR(50),  -- например: "8 недель", "36 часов"
    start_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Внешние ключи
    CONSTRAINT fk_course_category
        FOREIGN KEY (category_id)
        REFERENCES categories(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_course_teacher
        FOREIGN KEY (teacher_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    -- Проверки
    CONSTRAINT chk_course_duration CHECK (duration IS NULL OR length(duration) > 0),
    CONSTRAINT chk_start_date_future CHECK (start_date IS NULL OR start_date >= CURRENT_DATE)
);

-- Индексы
CREATE INDEX idx_courses_title ON courses(title);
CREATE INDEX idx_courses_category_id ON courses(category_id);
CREATE INDEX idx_courses_teacher_id ON courses(teacher_id);
CREATE INDEX idx_courses_start_date ON courses(start_date);

-- Комментарии
COMMENT ON TABLE courses IS 'Учебные курсы платформы';
COMMENT ON COLUMN courses.title IS 'Название курса';
COMMENT ON COLUMN courses.description IS 'Подробное описание курса';
COMMENT ON COLUMN courses.category_id IS 'Категория курса';
COMMENT ON COLUMN courses.teacher_id IS 'Преподаватель курса';
COMMENT ON COLUMN courses.duration IS 'Продолжительность курса';
COMMENT ON COLUMN courses.start_date IS 'Дата начала курса';