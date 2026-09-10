import { Request, Response, NextFunction } from 'express';
import { pool } from '../config/db';
import { logAudit } from '../services/audit.service';
import { emitToAdmin } from '../services/socket.service';

export async function getAlerts(req: Request, res: Response, next: NextFunction) {
  try {
    const [alerts]: any = await pool.execute(
      `SELECT * FROM system_alerts ORDER BY timestamp DESC LIMIT 50`
    );
    return res.json({ success: true, alerts, requestId: req.requestId });
  } catch (err) {
    next(err);
  }
}

export async function createAlert(req: Request, res: Response, next: NextFunction) {
  try {
    const { alertType, title, description, severity } = req.body;

    const [resInsert]: any = await pool.execute(
      `INSERT INTO system_alerts (alert_type, title, description, severity, status)
       VALUES (?, ?, ?, ?, 'ACTIVE')`,
      [alertType || 'SYSTEM_ALERT', title, description, severity || 'WARNING']
    );

    const alert = {
      id: resInsert.insertId,
      alert_type: alertType,
      title,
      description,
      severity,
      status: 'ACTIVE',
      timestamp: new Date().toISOString(),
    };

    emitToAdmin('system:alert', alert);

    return res.status(201).json({ success: true, alert });
  } catch (err) {
    next(err);
  }
}

export async function updateAlertStatus(req: Request, res: Response, next: NextFunction) {
  try {
    const alertId = parseInt(req.params.id as string, 10);
    const { status } = req.body;

    await pool.execute(
      `UPDATE system_alerts SET status = ? WHERE id = ?`,
      [status, alertId]
    );

    await logAudit({
      actor: req.user ? `${req.user.role} #${req.user.id}` : 'Admin',
      role: req.user?.role || 'ADMIN',
      action: `ALERT_${status}`,
      entity: 'ALERT',
      entityId: alertId,
      ipAddress: req.ip,
    });

    return res.json({ success: true, message: `Alert status updated to ${status}.` });
  } catch (err) {
    next(err);
  }
}
