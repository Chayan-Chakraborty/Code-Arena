DROP TABLE IF EXISTS submissions;
DROP TABLE IF EXISTS test_cases;
DROP TABLE IF EXISTS problems;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(80) NOT NULL UNIQUE,
    email VARCHAR(160) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER'
);

CREATE TABLE problems (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    difficulty VARCHAR(20),
    topic VARCHAR(60),
    constraints TEXT,
    sample_input TEXT,
    sample_output TEXT,
    starter_code TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'APPROVED',
    created_by BIGINT
);

CREATE TABLE test_cases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    problem_id BIGINT NOT NULL,
    input TEXT,
    expected_output TEXT,
    is_sample BOOLEAN DEFAULT FALSE
);

CREATE TABLE submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    problem_id BIGINT,
    code TEXT,
    status VARCHAR(40),
    execution_time DOUBLE,
    created_at TIMESTAMP
);
