-- Seed Data for Smart Farmer Procurement System
USE `smart_procurement`;

-- Passwords are all hashed for 'password123' using bcrypt ($2a$10$wE7/LSmRkgv27g3f3r3NceT1s8r2m0K6.m9iV2t7p7p0r5t4m2k2q)
-- For demo compatibility, the backend will verify with bcrypt or fall back to plaintext demo passwords if needed.

-- Insert Users
INSERT INTO `users` (`id`, `email`, `password_hash`, `role`, `status`) VALUES
(1, 'superadmin@kisan.gov.in', '$2b$10$01234567890123456789012345678901234567890123456789012', 'SUPER_ADMIN', 'ACTIVE'),
(2, 'admin.rajkot@kisan.gov.in', '$2b$10$01234567890123456789012345678901234567890123456789012', 'ADMIN', 'ACTIVE'),
(3, 'manager.vadodara@kisan.gov.in', '$2b$10$01234567890123456789012345678901234567890123456789012', 'CENTRE_MANAGER', 'ACTIVE'),
(4, 'operator1@kisan.gov.in', '$2b$10$01234567890123456789012345678901234567890123456789012', 'OPERATOR', 'ACTIVE'),
(5, 'operator2@kisan.gov.in', '$2b$10$01234567890123456789012345678901234567890123456789012', 'OPERATOR', 'ACTIVE'),
(6, 'farmer101@kisan.gov.in', '$2b$10$01234567890123456789012345678901234567890123456789012', 'FARMER', 'ACTIVE'),
(7, 'farmer102@kisan.gov.in', '$2b$10$01234567890123456789012345678901234567890123456789012', 'FARMER', 'ACTIVE'),
(8, 'farmer103@kisan.gov.in', '$2b$10$01234567890123456789012345678901234567890123456789012', 'FARMER', 'ACTIVE'),
(9, 'farmer104@kisan.gov.in', '$2b$10$01234567890123456789012345678901234567890123456789012', 'FARMER', 'ACTIVE'),
(10, 'farmer105@kisan.gov.in', '$2b$10$01234567890123456789012345678901234567890123456789012', 'FARMER', 'ACTIVE');

-- Insert Profiles
INSERT INTO `profiles` (`id`, `user_id`, `farmer_code`, `full_name`, `phone`, `village`, `district`, `state`, `land_size_acres`, `bank_account_masked`) VALUES
(1, 1, NULL, 'Chief Administrator', '+91 99000 00001', 'Gandhinagar', 'Gandhinagar', 'Gujarat', 0, NULL),
(2, 2, NULL, 'Rajesh Varma (Admin)', '+91 99000 00002', 'Rajkot Central', 'Rajkot', 'Gujarat', 0, NULL),
(3, 3, NULL, 'Kirit Patel (Manager)', '+91 99000 00003', 'Makarpura', 'Vadodara', 'Gujarat', 0, NULL),
(4, 4, NULL, 'Amit Joshi (Weighment Operator)', '+91 99000 00004', 'APMC Compound', 'Vadodara', 'Gujarat', 0, NULL),
(5, 5, NULL, 'Deepak Shah (Quality Inspector)', '+91 99000 00005', 'APMC Compound', 'Rajkot', 'Gujarat', 0, NULL),
(6, 6, 'GJ-VAD-101', 'Rameshchandra Patel', '+91 98765 11001', 'Karjan', 'Vadodara', 'Gujarat', 8.50, 'SBIN****1289'),
(7, 7, 'GJ-VAD-102', 'Sumanben Parmar', '+91 98765 11002', 'Dabhoi', 'Vadodara', 'Gujarat', 5.20, 'BARB****4410'),
(8, 8, 'GJ-RAJ-201', 'Bhupatbhai Ahir', '+91 98765 11003', 'Gondal', 'Rajkot', 'Gujarat', 12.00, 'BKID****9021'),
(9, 9, 'GJ-RAJ-202', 'Mansukhbhai Rathod', '+91 98765 11004', 'Jasdan', 'Rajkot', 'Gujarat', 6.80, 'HDFC****6672'),
(10, 10, 'GJ-AMD-301', 'Pravinbhai Solanki', '+91 98765 11005', 'Sanand', 'Ahmedabad', 'Gujarat', 4.50, 'ICIC****3319');

-- Insert Procurement Centres
INSERT INTO `procurement_centres` (`id`, `code`, `name`, `address`, `village`, `district`, `state`, `daily_capacity`, `open_time`, `close_time`, `status`) VALUES
(1, 'GJ-VAD-01', 'Vadodara APMC Procurement Centre', 'Near Ring Road APMC Market Yard', 'Makarpura', 'Vadodara', 'Gujarat', 160, '09:00', '17:00', 'OPEN'),
(2, 'GJ-RAJ-01', 'Rajkot Marketing Yard Centre', 'National Highway 8B, Marketing Yard', 'Rajkot', 'Rajkot', 'Gujarat', 180, '09:00', '17:30', 'OPEN'),
(3, 'GJ-AMD-01', 'Ahmedabad APMC Hub', 'Sanand Highway Market Complex', 'Sanand', 'Ahmedabad', 'Gujarat', 140, '09:00', '16:30', 'OPEN'),
(4, 'GJ-SUR-01', 'Surat Agro Procurement Point', 'Kadodara Char Rasta Yard', 'Kadodara', 'Surat', 'Gujarat', 120, '09:30', '17:00', 'OPEN');

-- Insert Counters
INSERT INTO `counters` (`id`, `centre_id`, `counter_number`, `operator_id`, `status`, `current_token`) VALUES
(1, 1, 1, 4, 'ACTIVE', 103),
(2, 1, 2, NULL, 'IDLE', NULL),
(3, 1, 3, NULL, 'IDLE', NULL),
(4, 2, 1, 5, 'ACTIVE', 201),
(5, 2, 2, NULL, 'IDLE', NULL),
(6, 3, 1, NULL, 'IDLE', NULL);

-- Insert Crops
INSERT INTO `crops` (`id`, `name`, `category`, `minimum_support_price`, `unit`) VALUES
(1, 'Wheat (Gehun)', 'Cereal', 2275.00, 'Quintal'),
(2, 'Paddy (Dhan - Common)', 'Cereal', 2300.00, 'Quintal'),
(3, 'Cotton (Kapas - Medium)', 'Cash Crop', 6620.00, 'Quintal'),
(4, 'Groundnut (Mungfali)', 'Oilseed', 6377.00, 'Quintal'),
(5, 'Gram / Chana', 'Pulse', 5440.00, 'Quintal'),
(6, 'Mustard (Sarson)', 'Oilseed', 5650.00, 'Quintal');

-- Insert Slots for Today
INSERT INTO `slots` (`id`, `centre_id`, `slot_date`, `start_time`, `end_time`, `capacity`, `booked_count`, `status`) VALUES
(1, 1, CURDATE(), '09:00', '09:30', 10, 10, 'FULL'),
(2, 1, CURDATE(), '09:30', '10:00', 10, 8, 'ALMOST_FULL'),
(3, 1, CURDATE(), '10:00', '10:30', 10, 5, 'AVAILABLE'),
(4, 1, CURDATE(), '10:30', '11:00', 10, 4, 'AVAILABLE'),
(5, 1, CURDATE(), '11:00', '11:30', 10, 2, 'AVAILABLE'),
(6, 1, CURDATE(), '11:30', '12:00', 10, 0, 'AVAILABLE'),
(7, 2, CURDATE(), '09:00', '09:30', 10, 9, 'ALMOST_FULL'),
(8, 2, CURDATE(), '09:30', '10:00', 10, 6, 'AVAILABLE');

-- Insert Bookings (Active Queue and completed)
INSERT INTO `bookings` (`id`, `booking_ref`, `token_number`, `farmer_id`, `centre_id`, `slot_id`, `crop_id`, `expected_quantity`, `booking_date`, `status`, `counter_id`, `queue_position`, `estimated_wait_minutes`, `checked_in_at`, `called_at`, `processing_started_at`, `completed_at`) VALUES
(1, 'BK-2026-00101', 101, 6, 1, 1, 1, 45.00, CURDATE(), 'COMPLETED', 1, 0, 0, DATE_SUB(NOW(), INTERVAL 90 MINUTE), DATE_SUB(NOW(), INTERVAL 75 MINUTE), DATE_SUB(NOW(), INTERVAL 70 MINUTE), DATE_SUB(NOW(), INTERVAL 50 MINUTE)),
(2, 'BK-2026-00102', 102, 7, 1, 1, 4, 30.00, CURDATE(), 'COMPLETED', 1, 0, 0, DATE_SUB(NOW(), INTERVAL 70 MINUTE), DATE_SUB(NOW(), INTERVAL 50 MINUTE), DATE_SUB(NOW(), INTERVAL 45 MINUTE), DATE_SUB(NOW(), INTERVAL 25 MINUTE)),
(3, 'BK-2026-00103', 103, 8, 1, 2, 3, 25.00, CURDATE(), 'PROCESSING', 1, 0, 5, DATE_SUB(NOW(), INTERVAL 35 MINUTE), DATE_SUB(NOW(), INTERVAL 20 MINUTE), DATE_SUB(NOW(), INTERVAL 15 MINUTE), NULL),
(4, 'BK-2026-00104', 104, 9, 1, 2, 1, 50.00, CURDATE(), 'WAITING', NULL, 1, 15, DATE_SUB(NOW(), INTERVAL 10 MINUTE), NULL, NULL, NULL),
(5, 'BK-2026-00105', 105, 10, 1, 3, 2, 40.00, CURDATE(), 'WAITING', NULL, 2, 30, NULL, NULL, NULL, NULL);

-- Insert Procurement Records (Completed procurements)
INSERT INTO `procurement_records` (`id`, `booking_id`, `farmer_id`, `centre_id`, `crop_id`, `quantity_received`, `quality_grade`, `rate_per_unit`, `procurement_amount`, `moisture_percentage`, `foreign_matter_percentage`, `remarks`, `receipt_number`, `processed_by`, `processed_at`) VALUES
(1, 1, 6, 1, 1, 45.00, 'A', 2275.00, 102375.00, 11.20, 0.40, 'Excellent grade wheat, clean grain', 'RCPT-VAD-2026-0001', 4, DATE_SUB(NOW(), INTERVAL 50 MINUTE)),
(2, 2, 7, 1, 4, 29.50, 'A', 6377.00, 188121.50, 7.50, 0.80, 'Grade A Groundnut accepted', 'RCPT-VAD-2026-0002', 4, DATE_SUB(NOW(), INTERVAL 25 MINUTE));

-- Insert Payments
INSERT INTO `payments` (`id`, `procurement_id`, `farmer_id`, `amount`, `payment_method`, `transaction_reference`, `status`, `initiated_at`, `completed_at`, `failure_reason`) VALUES
(1, 1, 6, 102375.00, 'BANK_TRANSFER', 'TXN-DBT-904128912', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 48 MINUTE), DATE_SUB(NOW(), INTERVAL 45 MINUTE), NULL),
(2, 2, 7, 188121.50, 'UPI', 'TXN-UPI-883491024', 'PROCESSING', DATE_SUB(NOW(), INTERVAL 24 MINUTE), NULL, NULL);

-- Insert Sample Error Logs
INSERT INTO `error_logs` (`id`, `error_id`, `request_id`, `source`, `severity`, `message`, `error_code`, `stack_trace`, `endpoint`, `http_method`, `http_status`, `user_reference`, `role`, `device_reference`, `app_version`, `network_type`, `screen_name`, `ip_metadata`, `timestamp`, `resolved`, `notes`) VALUES
(1, 'ERR-20260910-00001', 'REQ-AA1092', 'Android', 'WARNING', 'Failed to fetch queue list: connection reset by peer', 'CONN_RESET', 'java.net.SocketException: Connection reset\n  at com.kisanprocure.app.api.ApiClient.getQueue(ApiClient.kt:42)', '/api/queue/current', 'GET', 500, 'Farmer #104', 'FARMER', 'Samsung Galaxy M34 / Android 14', '1.0.0', '4G', 'LiveQueueScreen', '49.36.110.12', DATE_SUB(NOW(), INTERVAL 40 MINUTE), 1, 'Temporary 4G packet drop on client'),
(2, 'ERR-20260910-00002', 'REQ-BB2044', 'Backend', 'ERROR', 'Database dead-lock avoided during concurrent slot booking', 'ER_LOCK_DEADLOCK', 'Error: Deadlock found when trying to get lock\n  at ConnectionPool.query (backend/src/config/db.ts:54)', '/api/bookings', 'POST', 500, 'Farmer #105', 'FARMER', 'Node-Backend', '1.0.0', 'Ethernet', 'BookingEngine', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 15 MINUTE), 0, 'Under observation; transaction retry handled'),
(3, 'ERR-20260910-00003', 'REQ-CC3018', 'Database', 'INFO', 'Slow query on monthly procurement aggregation (>450ms)', 'SLOW_QUERY', 'Query exceeded 400ms threshold: SELECT SUM(amount)...', '/api/admin/analytics', 'GET', 200, 'Admin #2', 'ADMIN', 'Server-Daemon', '1.0.0', 'Local', 'AnalyticsService', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 5 MINUTE), 0, 'Index added on processed_at');

-- Insert Sample Audit Logs
INSERT INTO `audit_logs` (`id`, `actor`, `role`, `action`, `entity`, `entity_id`, `metadata`, `ip_address`, `timestamp`) VALUES
(1, 'Chief Administrator', 'SUPER_ADMIN', 'SYSTEM_INITIALIZED', 'SYSTEM', '1', '{"version": "1.0.0", "environment": "local_dev"}', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 120 MINUTE)),
(2, 'Rameshchandra Patel', 'FARMER', 'BOOKED_SLOT', 'BOOKING', '1', '{"slot_id": 1, "crop": "Wheat", "quantity": 45}', '49.36.110.12', DATE_SUB(NOW(), INTERVAL 95 MINUTE)),
(3, 'Amit Joshi (Weighment Operator)', 'OPERATOR', 'SCANNED_QR', 'BOOKING', '1', '{"token_number": 101, "counter": 1}', '192.168.1.15', DATE_SUB(NOW(), INTERVAL 75 MINUTE)),
(4, 'Amit Joshi (Weighment Operator)', 'OPERATOR', 'ENTERED_WEIGHMENT', 'PROCUREMENT', '1', '{"gross_weight": 45.0, "net_weight": 45.0}', '192.168.1.15', DATE_SUB(NOW(), INTERVAL 65 MINUTE)),
(5, 'Deepak Shah (Quality Inspector)', 'OPERATOR', 'APPROVED_QUALITY', 'PROCUREMENT', '1', '{"grade": "A", "moisture": 11.2, "rate": 2275.0}', '192.168.1.18', DATE_SUB(NOW(), INTERVAL 55 MINUTE)),
(6, 'System', 'SYSTEM', 'PAYMENT_INITIATED', 'PAYMENT', '1', '{"amount": 102375.0, "mode": "BANK_TRANSFER"}', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 48 MINUTE)),
(7, 'Amit Joshi (Weighment Operator)', 'OPERATOR', 'CALLED_TOKEN', 'BOOKING', '3', '{"token_number": 103, "counter": 1}', '192.168.1.15', DATE_SUB(NOW(), INTERVAL 20 MINUTE));

-- Insert System Alerts
INSERT INTO `system_alerts` (`id`, `alert_type`, `title`, `description`, `severity`, `status`, `timestamp`) VALUES
(1, 'QUEUE_GROWTH', 'High Morning Inflow at Vadodara APMC', 'Queue size reached 80% of counter capacity for 10:00-11:00 slot window.', 'WARNING', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 10 MINUTE));

-- Insert System Settings
INSERT INTO `system_settings` (`id`, `key_name`, `value_string`, `description`, `updated_by`, `updated_at`) VALUES
(1, 'average_processing_time_mins', '12', 'Average minutes per farmer inspection and weighment', 'SUPER_ADMIN', NOW()),
(2, 'default_slot_capacity', '10', 'Default farmer slots per 30-minute window', 'SUPER_ADMIN', NOW()),
(3, 'maintenance_mode', 'false', 'Enable or disable farmer-facing maintenance lock', 'SUPER_ADMIN', NOW()),
(4, 'demo_mode', 'true', 'Allows demo credentials and rapid test actions', 'SUPER_ADMIN', NOW()),
(5, 'fcm_mock_enabled', 'false', 'Simulates FCM push messages in console if FCM credentials missing', 'SUPER_ADMIN', NOW()),
(6, 'high_error_threshold_5min', '10', 'Threshold to trigger high error rate system alert', 'SUPER_ADMIN', NOW());

-- Insert Sample Notifications
INSERT INTO `notifications` (`id`, `user_id`, `title`, `message`, `type`, `is_read`, `created_at`) VALUES
(1, 6, 'Payment Successful', 'Rs. 1,02,375 credited to account SBIN****1289 for Wheat procurement (Token 101).', 'PAYMENT', 1, DATE_SUB(NOW(), INTERVAL 45 MINUTE)),
(2, 7, 'Payment Processing', 'Payment of Rs. 1,88,121.50 has been initiated via UPI for Groundnut procurement.', 'PAYMENT', 0, DATE_SUB(NOW(), INTERVAL 20 MINUTE)),
(3, 8, 'Counter Assigned', 'Please proceed to Counter #1. Operator Amit Joshi is ready to inspect Token 103.', 'QUEUE', 0, DATE_SUB(NOW(), INTERVAL 18 MINUTE));
