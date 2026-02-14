-- Fix admin user password with a working BCrypt hash
-- New password: admin (simpler than admin123 for testing)
-- BCrypt hash generated with strength 10
UPDATE users
SET password = '$2a$10$CS91CHzgfgQnL7Mg9WuJm.jDtWvLg8V7eZ7.ny8Tj0ENqIQ1Tl6ry',
    updated_at = CURRENT_TIMESTAMP
WHERE username = 'admin';

-- Verify update
SELECT username, LENGTH(password) as pwd_length
FROM users
WHERE username = 'admin';
