-- Farm Management System - database setup
--
-- Run from the project root on a machine that has MySQL installed:
--     mysql -u root -p < schema.sql
--
-- The script is idempotent: tables are only created if missing and expense
-- costs are always reset to the reference values compiled into the
-- application, so it is safe to run more than once.

CREATE DATABASE IF NOT EXISTS farm_management_system
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE farm_management_system;

-- users is the only table the application never creates itself (Login and
-- Register both assume it exists), so this file has to supply it.
CREATE TABLE IF NOT EXISTS users (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    dob      DATE         NOT NULL,
    gender   ENUM('male', 'female') NOT NULL,
    mobile   VARCHAR(20)  NOT NULL,
    email    VARCHAR(255) NOT NULL,
    district VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    UNIQUE KEY uq_users_email  (email),
    UNIQUE KEY uq_users_mobile (mobile)
) ENGINE = InnoDB;

-- Mirrors the DDL that CropPlanPage.seedExpenses() runs at runtime.
CREATE TABLE IF NOT EXISTS expenses (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    crop_name     VARCHAR(100) NOT NULL,
    expense_type  VARCHAR(100) NOT NULL,
    cost_per_acre DOUBLE       NOT NULL
) ENGINE = InnoDB;

-- Mirrors the DDL that CropPlanPage.storeInDb() runs at runtime.
CREATE TABLE IF NOT EXISTS crop_plans (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(100) NOT NULL,
    crop       VARCHAR(50)  NOT NULL,
    acres      DOUBLE       NOT NULL,
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB;

-- Reference expense costs (UGX per acre), copied from CropPlanPage.EXPENSE_SEED.
-- The code values are the source of truth: rows that already exist are
-- corrected to them and rows that are missing are added.

CREATE TEMPORARY TABLE expense_seed (
    crop_name     VARCHAR(100) NOT NULL,
    expense_type  VARCHAR(100) NOT NULL,
    cost_per_acre DOUBLE       NOT NULL,
    KEY idx_seed (crop_name, expense_type)
);

INSERT INTO expense_seed (crop_name, expense_type, cost_per_acre) VALUES
    ('Wheat',     'Seed',      5000.0),
    ('Wheat',     'Fertilizer', 12000.0),
    ('Wheat',     'Labor',     800000.0),
    ('Wheat',     'Equipment',  40000.0),
    ('Rice',      'Seed',       6000.0),
    ('Rice',      'Fertilizer', 150000.0),
    ('Rice',      'Labor',     100000.0),
    ('Rice',      'Equipment', 500000.0),
    ('Soybean',   'Seed',       4000.0),
    ('Soybean',   'Fertilizer', 90000.0),
    ('Soybean',   'Labor',      70000.0),
    ('Soybean',   'Equipment', 350000.0),
    ('Sugarcane', 'Seed',        200.0),
    ('Sugarcane', 'Fertilizer',  2500.0),
    ('Sugarcane', 'Labor',      18000.0),
    ('Sugarcane', 'Equipment', 900000.0);

UPDATE expenses e
    JOIN expense_seed s
      ON s.crop_name = e.crop_name AND s.expense_type = e.expense_type
SET e.cost_per_acre = s.cost_per_acre;

INSERT INTO expenses (crop_name, expense_type, cost_per_acre)
SELECT s.crop_name, s.expense_type, s.cost_per_acre
FROM expense_seed s
LEFT JOIN expenses e
       ON e.crop_name = s.crop_name AND e.expense_type = s.expense_type
WHERE e.id IS NULL;

DROP TEMPORARY TABLE expense_seed;
