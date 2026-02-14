-- src/main/resources/db/migration/V5__alter_users_password_column.sql
-- Increase password column size to accommodate BCrypt hashes (up to 255 characters)
ALTER TABLE users ALTER COLUMN password TYPE VARCHAR(255);
