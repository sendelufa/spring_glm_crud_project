-- src/main/resources/db/migration/V4__seed_admin_user.sql
-- Password: password123 (simple test password)
-- BCrypt hash generated with work factor 10
INSERT INTO users (
    id,
    username,
    password,
    role,
    created_at,
    updated_at
) VALUES (
    '123e4567-e89b-12d3-a456-426614174000',
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhW',
    'ADMIN',
    '2025-02-14 00:00:00',
    '2025-02-14 00:00:00'
);
