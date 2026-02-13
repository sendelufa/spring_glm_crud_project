-- Migration: Make shop_type column nullable
-- This migration allows alcohol shops to be created without specifying a shop type

-- Alter the shop_type column to allow NULL values
ALTER TABLE alcohol_shops ALTER COLUMN shop_type DROP NOT NULL;

-- Add comment to document the change
COMMENT ON COLUMN alcohol_shops.shop_type IS 'Shop classification: RETAIL, SUPERMARKET, DUTY_FREE, SPECIALIZED (optional field)';
