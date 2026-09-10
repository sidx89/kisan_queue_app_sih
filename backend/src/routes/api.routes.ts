import { Router } from 'express';
import * as HealthCtrl from '../controllers/health.controller';
import * as AuthCtrl from '../controllers/auth.controller';
import * as DashboardCtrl from '../controllers/dashboard.controller';
import * as FarmersCtrl from '../controllers/farmers.controller';
import * as CentresCtrl from '../controllers/centres.controller';
import * as SlotsCtrl from '../controllers/slots.controller';
import * as BookingsCtrl from '../controllers/bookings.controller';
import * as QueueCtrl from '../controllers/queue.controller';
import * as ProcurementCtrl from '../controllers/procurement.controller';
import * as PaymentsCtrl from '../controllers/payments.controller';
import * as ErrorsCtrl from '../controllers/errors.controller';
import * as AuditCtrl from '../controllers/audit.controller';
import * as AlertsCtrl from '../controllers/alerts.controller';
import * as SystemCtrl from '../controllers/system.controller';
import * as SettingsCtrl from '../controllers/settings.controller';
import { authenticate, requireRole } from '../middleware/auth';

export const router = Router();

// Health Check Endpoints (Public)
router.get('/health', HealthCtrl.getOverallHealth);
router.get('/health/database', HealthCtrl.getDbHealth);
router.get('/health/socket', HealthCtrl.getSocketHealth);
router.get('/health/notifications', HealthCtrl.getNotificationHealth);

// Public System Config — Android app reads this via USB to get current tunnel URL
router.get('/api/system/public-config', SystemCtrl.getPublicConfig);
router.get('/api/system/connection-test', SystemCtrl.getConnectionTest);

// Client Error Reporting Endpoint (Requirement 5, 6, 22, 23)
router.post('/api/errors/report', ErrorsCtrl.reportClientError);

// Authentication Endpoints
router.post('/api/auth/login', AuthCtrl.login);
router.post('/api/auth/register', AuthCtrl.registerFarmer);
router.get('/api/auth/profile', authenticate, AuthCtrl.getProfile);
router.post('/api/auth/fcm-token', authenticate, AuthCtrl.updateFcm);

// Public / Farmer Directory Endpoints
router.get('/api/centres', CentresCtrl.getCentres);
router.get('/api/crops', CentresCtrl.getCrops);
router.get('/api/slots', SlotsCtrl.getSlots);
router.get('/api/centres/:id/slots', SlotsCtrl.getSlots);
router.get('/api/queue/:centreId', QueueCtrl.getLiveQueue);

// Farmer Specific Endpoints
router.post('/api/bookings', authenticate, BookingsCtrl.createBooking);
router.get('/api/bookings', authenticate, BookingsCtrl.getFarmerBookings);
router.get('/api/bookings/my', authenticate, BookingsCtrl.getFarmerBookings);
router.post('/api/queue/check-in', authenticate, BookingsCtrl.qrCheckIn);

// Operator Workflow Endpoints
const operatorRoles = ['OPERATOR', 'CENTRE_MANAGER', 'ADMIN', 'SUPER_ADMIN'];
router.post('/api/operator/queue/call-next', authenticate, requireRole(operatorRoles), QueueCtrl.callNext);
router.patch('/api/operator/queue/:bookingId/status', authenticate, requireRole(operatorRoles), QueueCtrl.updateQueueStatus);
router.post('/api/operator/procurement', authenticate, requireRole(operatorRoles), ProcurementCtrl.createProcurementRecord);
router.post('/api/operator/queue/emergency', authenticate, requireRole(operatorRoles), QueueCtrl.emergencyQueueControl);

// Admin Control Panel Endpoints
const adminRoles = ['ADMIN', 'SUPER_ADMIN', 'CENTRE_MANAGER'];
router.get('/api/admin/dashboard', authenticate, requireRole(adminRoles), DashboardCtrl.getDashboardStats);
router.get('/api/admin/system/metrics', authenticate, requireRole(adminRoles), SystemCtrl.getMetrics);
router.get('/api/admin/farmers', authenticate, requireRole(adminRoles), FarmersCtrl.getFarmers);
router.get('/api/admin/farmers/:id', authenticate, requireRole(adminRoles), FarmersCtrl.getFarmerById);
router.patch('/api/admin/farmers/:id/status', authenticate, requireRole(['ADMIN', 'SUPER_ADMIN']), FarmersCtrl.toggleFarmerStatus);

router.get('/api/admin/centres', authenticate, requireRole(adminRoles), CentresCtrl.getCentres);
router.post('/api/admin/centres', authenticate, requireRole(['ADMIN', 'SUPER_ADMIN']), CentresCtrl.createCentre);
router.patch('/api/admin/centres/:id', authenticate, requireRole(['ADMIN', 'SUPER_ADMIN']), CentresCtrl.updateCentre);

router.get('/api/admin/counters', authenticate, requireRole(adminRoles), CentresCtrl.getCounters);
router.post('/api/admin/counters', authenticate, requireRole(adminRoles), CentresCtrl.addCounter);
router.patch('/api/admin/counters/:id', authenticate, requireRole(adminRoles), CentresCtrl.assignOperatorToCounter);

router.patch('/api/admin/slots/:id', authenticate, requireRole(adminRoles), SlotsCtrl.updateSlot);
router.get('/api/admin/bookings', authenticate, requireRole(adminRoles), BookingsCtrl.getAllBookings);
router.get('/api/admin/procurement', authenticate, requireRole(adminRoles), ProcurementCtrl.getProcurementRecords);

router.get('/api/admin/payments', authenticate, requireRole(adminRoles), PaymentsCtrl.getPayments);
router.patch('/api/admin/payments/:id', authenticate, requireRole(['ADMIN', 'SUPER_ADMIN']), PaymentsCtrl.updatePaymentStatus);

router.get('/api/admin/errors', authenticate, requireRole(adminRoles), ErrorsCtrl.getGroupedErrors);
router.get('/api/admin/errors/raw', authenticate, requireRole(adminRoles), ErrorsCtrl.getRawErrors);
router.patch('/api/admin/errors/:id/resolve', authenticate, requireRole(adminRoles), ErrorsCtrl.resolveError);

router.get('/api/admin/audit-logs', authenticate, requireRole(adminRoles), AuditCtrl.getAuditLogs);
router.get('/api/admin/alerts', authenticate, requireRole(adminRoles), AlertsCtrl.getAlerts);
router.post('/api/admin/alerts', authenticate, requireRole(adminRoles), AlertsCtrl.createAlert);
router.patch('/api/admin/alerts/:id', authenticate, requireRole(adminRoles), AlertsCtrl.updateAlertStatus);

router.get('/api/admin/settings', authenticate, requireRole(adminRoles), SettingsCtrl.getSettings);
router.post('/api/admin/settings', authenticate, requireRole(['ADMIN', 'SUPER_ADMIN']), SettingsCtrl.updateSetting);
