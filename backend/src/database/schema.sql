-- Smart Farmer Procurement System Database Schema
-- Target: MariaDB 10.4 / MySQL 8.0 on XAMPP

CREATE DATABASE IF NOT EXISTS `smart_procurement` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `smart_procurement`;

-- Disable foreign key checks for clean recreation if needed
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `notifications`;
DROP TABLE IF EXISTS `system_settings`;
DROP TABLE IF EXISTS `system_alerts`;
DROP TABLE IF EXISTS `audit_logs`;
DROP TABLE IF EXISTS `error_logs`;
DROP TABLE IF EXISTS `payments`;
DROP TABLE IF EXISTS `procurement_records`;
DROP TABLE IF EXISTS `bookings`;
DROP TABLE IF EXISTS `slots`;
DROP TABLE IF EXISTS `crops`;
DROP TABLE IF EXISTS `counters`;
DROP TABLE IF EXISTS `procurement_centres`;
DROP TABLE IF EXISTS `profiles`;
DROP TABLE IF EXISTS `users`;

SET FOREIGN_KEY_CHECKS = 1;

-- 1. Users Table (Authentication & Roles)
CREATE TABLE `users` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `email` VARCHAR(100) UNIQUE NOT NULL,
  `password_hash` VARCHAR(255) NOT NULL,
  `role` ENUM('SUPER_ADMIN', 'ADMIN', 'CENTRE_MANAGER', 'OPERATOR', 'FARMER') NOT NULL,
  `status` ENUM('ACTIVE', 'DISABLED') DEFAULT 'ACTIVE',
  `fcm_token` VARCHAR(255) NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_role_status` (`role`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Profiles Table (Farmer & Staff Profiles)
CREATE TABLE `profiles` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `farmer_code` VARCHAR(50) UNIQUE NULL,
  `full_name` VARCHAR(100) NOT NULL,
  `phone` VARCHAR(20) NOT NULL,
  `village` VARCHAR(100) NOT NULL,
  `district` VARCHAR(100) NOT NULL,
  `state` VARCHAR(100) NOT NULL DEFAULT 'Gujarat',
  `land_size_acres` DECIMAL(6,2) DEFAULT 0.00,
  `bank_account_masked` VARCHAR(30) NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
  INDEX `idx_phone` (`phone`),
  INDEX `idx_district` (`district`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Procurement Centres Table
CREATE TABLE `procurement_centres` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `code` VARCHAR(50) UNIQUE NOT NULL,
  `name` VARCHAR(150) NOT NULL,
  `address` VARCHAR(255) NOT NULL,
  `village` VARCHAR(100) NOT NULL,
  `district` VARCHAR(100) NOT NULL,
  `state` VARCHAR(100) NOT NULL DEFAULT 'Gujarat',
  `daily_capacity` INT NOT NULL DEFAULT 150,
  `open_time` VARCHAR(10) NOT NULL DEFAULT '09:00',
  `close_time` VARCHAR(10) NOT NULL DEFAULT '17:00',
  `status` ENUM('OPEN', 'BUSY', 'CLOSED') DEFAULT 'OPEN',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Counters Table (Counters inside centres)
CREATE TABLE `counters` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `centre_id` INT NOT NULL,
  `counter_number` INT NOT NULL,
  `operator_id` INT NULL,
  `status` ENUM('ACTIVE', 'CALLING', 'IDLE', 'CLOSED') DEFAULT 'IDLE',
  `current_token` INT NULL,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`centre_id`) REFERENCES `procurement_centres`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`operator_id`) REFERENCES `users`(`id`) ON DELETE SET NULL,
  UNIQUE KEY `uk_centre_counter` (`centre_id`, `counter_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Crops Table (Supported commodities & MSP)
CREATE TABLE `crops` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL,
  `category` VARCHAR(50) NOT NULL,
  `minimum_support_price` DECIMAL(10,2) NOT NULL,
  `unit` VARCHAR(20) DEFAULT 'Quintal'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Slots Table (Daily booking slots per centre)
CREATE TABLE `slots` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `centre_id` INT NOT NULL,
  `slot_date` DATE NOT NULL,
  `start_time` VARCHAR(10) NOT NULL,
  `end_time` VARCHAR(10) NOT NULL,
  `capacity` INT NOT NULL DEFAULT 10,
  `booked_count` INT NOT NULL DEFAULT 0,
  `status` ENUM('AVAILABLE', 'ALMOST_FULL', 'FULL', 'CLOSED') DEFAULT 'AVAILABLE',
  FOREIGN KEY (`centre_id`) REFERENCES `procurement_centres`(`id`) ON DELETE CASCADE,
  UNIQUE KEY `uk_centre_slot` (`centre_id`, `slot_date`, `start_time`),
  INDEX `idx_date` (`slot_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Bookings Table (Farmer reservations and live queue state)
CREATE TABLE `bookings` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `booking_ref` VARCHAR(50) UNIQUE NOT NULL,
  `token_number` INT NOT NULL,
  `farmer_id` INT NOT NULL,
  `centre_id` INT NOT NULL,
  `slot_id` INT NOT NULL,
  `crop_id` INT NOT NULL,
  `expected_quantity` DECIMAL(10,2) NOT NULL,
  `booking_date` DATE NOT NULL,
  `status` ENUM('WAITING', 'CALLED', 'ARRIVED', 'PROCESSING', 'WEIGHMENT', 'QUALITY_CHECK', 'COMPLETED', 'NO_SHOW', 'CANCELLED', 'REJECTED') DEFAULT 'WAITING',
  `counter_id` INT NULL,
  `queue_position` INT DEFAULT 0,
  `estimated_wait_minutes` INT DEFAULT 15,
  `checked_in_at` DATETIME NULL,
  `called_at` DATETIME NULL,
  `processing_started_at` DATETIME NULL,
  `completed_at` DATETIME NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`farmer_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`centre_id`) REFERENCES `procurement_centres`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`slot_id`) REFERENCES `slots`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`crop_id`) REFERENCES `crops`(`id`),
  FOREIGN KEY (`counter_id`) REFERENCES `counters`(`id`) ON DELETE SET NULL,
  INDEX `idx_queue_status` (`centre_id`, `status`),
  INDEX `idx_farmer_booking` (`farmer_id`, `booking_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Procurement Records Table (Weighment & Quality verification)
CREATE TABLE `procurement_records` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `booking_id` INT NOT NULL UNIQUE,
  `farmer_id` INT NOT NULL,
  `centre_id` INT NOT NULL,
  `crop_id` INT NOT NULL,
  `quantity_received` DECIMAL(10,2) NOT NULL,
  `quality_grade` ENUM('A', 'B', 'C', 'REJECTED') NOT NULL,
  `rate_per_unit` DECIMAL(10,2) NOT NULL,
  `procurement_amount` DECIMAL(12,2) NOT NULL,
  `moisture_percentage` DECIMAL(5,2) DEFAULT 0.00,
  `foreign_matter_percentage` DECIMAL(5,2) DEFAULT 0.00,
  `remarks` TEXT NULL,
  `receipt_number` VARCHAR(50) UNIQUE NOT NULL,
  `processed_by` INT NOT NULL,
  `processed_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`booking_id`) REFERENCES `bookings`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`farmer_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`centre_id`) REFERENCES `procurement_centres`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`crop_id`) REFERENCES `crops`(`id`),
  FOREIGN KEY (`processed_by`) REFERENCES `users`(`id`),
  INDEX `idx_processed_at` (`processed_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Payments Table (Financial disbursement tracking)
CREATE TABLE `payments` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `procurement_id` INT NOT NULL UNIQUE,
  `farmer_id` INT NOT NULL,
  `amount` DECIMAL(12,2) NOT NULL,
  `payment_method` ENUM('BANK_TRANSFER', 'UPI', 'DEMO_CASH') NOT NULL DEFAULT 'BANK_TRANSFER',
  `transaction_reference` VARCHAR(100) UNIQUE NOT NULL,
  `status` ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED') DEFAULT 'PENDING',
  `initiated_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `completed_at` DATETIME NULL,
  `failure_reason` TEXT NULL,
  FOREIGN KEY (`procurement_id`) REFERENCES `procurement_records`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`farmer_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
  INDEX `idx_payment_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Error Logs Table (Full diagnostic error reporting & grouping)
CREATE TABLE `error_logs` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `error_id` VARCHAR(64) UNIQUE NOT NULL,
  `request_id` VARCHAR(64) NULL,
  `source` VARCHAR(32) NOT NULL DEFAULT 'Backend',
  `severity` ENUM('INFO', 'WARNING', 'ERROR', 'CRITICAL') DEFAULT 'ERROR',
  `message` TEXT NOT NULL,
  `error_code` VARCHAR(64) NULL,
  `stack_trace` MEDIUMTEXT NULL,
  `endpoint` VARCHAR(255) NULL,
  `http_method` VARCHAR(10) NULL,
  `http_status` INT NULL,
  `user_reference` VARCHAR(64) NULL,
  `role` VARCHAR(32) NULL,
  `device_reference` VARCHAR(255) NULL,
  `app_version` VARCHAR(32) NULL,
  `network_type` VARCHAR(32) NULL,
  `screen_name` VARCHAR(64) NULL,
  `ip_metadata` VARCHAR(64) NULL,
  `timestamp` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `resolved` TINYINT(1) DEFAULT 0,
  `resolved_by` VARCHAR(64) NULL,
  `resolved_at` DATETIME NULL,
  `notes` TEXT NULL,
  INDEX `idx_err_code_endpoint` (`error_code`, `endpoint`),
  INDEX `idx_severity` (`severity`),
  INDEX `idx_resolved` (`resolved`),
  INDEX `idx_timestamp` (`timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Audit Logs Table (Immutable activity audit trail)
CREATE TABLE `audit_logs` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `actor` VARCHAR(64) NOT NULL,
  `role` VARCHAR(32) NOT NULL,
  `action` VARCHAR(64) NOT NULL,
  `entity` VARCHAR(64) NOT NULL,
  `entity_id` VARCHAR(64) NULL,
  `metadata` LONGTEXT NULL,
  `ip_address` VARCHAR(64) NULL,
  `timestamp` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_actor` (`actor`),
  INDEX `idx_action` (`action`),
  INDEX `idx_timestamp` (`timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. System Alerts Table
CREATE TABLE `system_alerts` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `alert_type` VARCHAR(64) NOT NULL,
  `title` VARCHAR(150) NOT NULL,
  `description` TEXT NOT NULL,
  `severity` ENUM('INFO', 'WARNING', 'ERROR', 'CRITICAL') DEFAULT 'WARNING',
  `status` ENUM('ACTIVE', 'ACKNOWLEDGED', 'RESOLVED') DEFAULT 'ACTIVE',
  `timestamp` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. System Settings Table
CREATE TABLE `system_settings` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `key_name` VARCHAR(64) UNIQUE NOT NULL,
  `value_string` TEXT NOT NULL,
  `description` VARCHAR(255) NULL,
  `updated_by` VARCHAR(64) NULL,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. Notifications Table (Farmer & Staff app notifications)
CREATE TABLE `notifications` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `title` VARCHAR(150) NOT NULL,
  `message` TEXT NOT NULL,
  `type` VARCHAR(50) DEFAULT 'GENERAL',
  `is_read` TINYINT(1) DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
  INDEX `idx_user_read` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
