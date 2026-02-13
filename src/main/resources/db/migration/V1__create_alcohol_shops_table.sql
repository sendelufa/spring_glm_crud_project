-- Alcohol Shops Table
-- Domain entity: AlcoholShop
-- Clean Architecture - Domain Layer Persistence

CREATE TABLE alcohol_shops (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(500) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    phone_number VARCHAR(20),
    working_hours VARCHAR(50),
    shop_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- Performance indexes for common query patterns
CREATE INDEX idx_alcohol_shops_name ON alcohol_shops(name);
CREATE INDEX idx_alcohol_shops_type ON alcohol_shops(shop_type);
CREATE INDEX idx_alcohol_shops_location ON alcohol_shops(latitude, longitude);

-- Add table comment for documentation
COMMENT ON TABLE alcohol_shops IS 'Stores information about alcohol retail shops';
COMMENT ON COLUMN alcohol_shops.id IS 'Unique identifier (UUID)';
COMMENT ON COLUMN alcohol_shops.name IS 'Shop name (max 100 characters)';
COMMENT ON COLUMN alcohol_shops.address IS 'Full shop address';
COMMENT ON COLUMN alcohol_shops.latitude IS 'Geographic latitude coordinate';
COMMENT ON COLUMN alcohol_shops.longitude IS 'Geographic longitude coordinate';
COMMENT ON COLUMN alcohol_shops.phone_number IS 'Contact phone number';
COMMENT ON COLUMN alcohol_shops.working_hours IS 'Operating hours';
COMMENT ON COLUMN alcohol_shops.shop_type IS 'Shop classification: RETAIL, SUPERMARKET, DUTY_FREE, SPECIALIZED';
COMMENT ON COLUMN alcohol_shops.created_at IS 'Record creation timestamp';
