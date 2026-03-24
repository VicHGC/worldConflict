-- Script to create users and comments tables
-- Run this in your MySQL database

USE worldConflict_db;

-- Create users table if not exists
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    display_name VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create comments table if not exists
CREATE TABLE IF NOT EXISTS comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content VARCHAR(1000) NOT NULL,
    news_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    user_name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert test user if not exists
INSERT INTO users (username, email, password, display_name) 
SELECT 'testuser', 'test@worldconflict.com', 'password123', 'Test User'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'testuser');

-- Verify tables
SHOW TABLES;
SELECT 'Users table:' as '';
SELECT * FROM users;
SELECT 'Comments table:' as '';
SELECT * FROM comments;