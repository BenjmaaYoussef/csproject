CREATE DATABASE IF NOT EXISTS workout_tracker;
USE workout_tracker;

CREATE TABLE IF NOT EXISTS workout_sessions (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    session_date VARCHAR(20) NOT NULL,
    notes        TEXT
);

CREATE TABLE IF NOT EXISTS session_exercises (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    session_id   INT NOT NULL,
    name         VARCHAR(100) NOT NULL,
    exercise_type VARCHAR(20) NOT NULL,
    sets         INT DEFAULT 0,
    reps         INT DEFAULT 0,
    weight_kg    DOUBLE DEFAULT 0,
    duration_min INT DEFAULT 0,
    FOREIGN KEY (session_id) REFERENCES workout_sessions(id) ON DELETE CASCADE
);
