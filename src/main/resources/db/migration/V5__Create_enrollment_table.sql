CREATE TABLE enrollments (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    course_id INTEGER NOT NULL,
    enroll_date DATE NOT NULL DEFAULT CURRENT_DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'COMPLETED', 'CANCELLED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_enrollment_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_enrollment_course
        FOREIGN KEY (course_id)
        REFERENCES courses(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_enrollment_user_course
        UNIQUE (user_id, course_id)
);

-- Индексы для оптимизации запросов
CREATE INDEX idx_enrollments_user_id ON enrollments(user_id);
CREATE INDEX idx_enrollments_course_id ON enrollments(course_id);
CREATE INDEX idx_enrollments_status ON enrollments(status);
CREATE INDEX idx_enrollments_user_course ON enrollments(user_id, course_id);