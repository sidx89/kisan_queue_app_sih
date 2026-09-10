import { Request, Response, NextFunction } from 'express';
import { pool } from '../config/db';

let isMaintenanceCached = false;
let lastCheckTime = 0;

export async function checkMaintenanceStatus(): Promise<boolean> {
  const now = Date.now();
  if (now - lastCheckTime < 5000) {
    return isMaintenanceCached;
  }

  try {
    const [rows]: any = await pool.execute(
      `SELECT value_string FROM system_settings WHERE key_name = 'maintenance_mode' LIMIT 1`
    );
    isMaintenanceCached = rows[0]?.value_string === 'true';
    lastCheckTime = now;
  } catch {
    // If DB is unreachable, preserve previous cached state
  }
  return isMaintenanceCached;
}

export function setMaintenanceCache(enabled: boolean) {
  isMaintenanceCached = enabled;
  lastCheckTime = Date.now();
}

export async function maintenanceMiddleware(req: Request, res: Response, next: NextFunction) {
  // Allow health check and admin API endpoints regardless of maintenance
  if (
    req.path.startsWith('/health') ||
    req.path.startsWith('/api/admin') ||
    req.path.startsWith('/api/auth') ||
    req.path.startsWith('/api/errors/report')
  ) {
    return next();
  }

  const inMaintenance = await checkMaintenanceStatus();
  if (inMaintenance) {
    // Check if user is staff/admin
    const userRole = req.user?.role;
    if (userRole === 'SUPER_ADMIN' || userRole === 'ADMIN' || userRole === 'CENTRE_MANAGER' || userRole === 'OPERATOR') {
      return next();
    }

    return res.status(503).json({
      success: false,
      maintenance: true,
      message: 'System maintenance is currently in progress. Please try again later.',
      requestId: req.requestId,
    });
  }

  next();
}
