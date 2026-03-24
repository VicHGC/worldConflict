-- Script to add google_id column and update user table
USE worldConflict_db;

-- Add google_id column if not exists
ALTER TABLE users ADD COLUMN google_id VARCHAR(255) UNIQUE;

-- Verify the column was added
SHOW COLUMNS FROM users LIKE 'google_id';

-- Show all users
SELECT id, username, email, display_name, google_id FROM users;