CREATE TABLE submissions (
    id SERIAL PRIMARY KEY,
    assignment_id INTEGER NOT NULL,
    student_id INTEGER NOT NULL,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    content TEXT,
    score INTEGER,
    feedback TEXT,
    status VARCHAR(20) DEFAULT 'SUBMITTED',

    FOREIGN KEY (assignment_id) REFERENCES assignments(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,

    -- Проверка оценки
    CONSTRAINT check_score
        CHECK (score IS NULL OR (score >= 0)),

    -- Проверка статуса
    CONSTRAINT check_status
        CHECK (status IN ('SUBMITTED', 'UNDER_REVIEW', 'GRADED', 'NEEDS_REVISION', 'LATE', 'REJECTED'))
);

-- Индексы для быстрого поиска
CREATE INDEX idx_submissions_assignment ON submissions(assignment_id);
CREATE INDEX idx_submissions_student ON submissions(student_id);
CREATE INDEX idx_submissions_status ON submissions(status);
CREATE INDEX idx_submissions_score ON submissions(score);
CREATE INDEX idx_submissions_submitted ON submissions(submitted_at);