import { Request, Response, NextFunction } from 'express';
import { pool } from '../config/db';
import { logAudit } from '../services/audit.service';
import { setMaintenanceCache } from '../middleware/maintenance';
import { emitToAdmin } from '../services/socket.service';

export async function getSettings(req: Request, res: Response, next: NextFunction) {
  try {
    const [rows]: any = await pool.execute(`SELECT * FROM system_settings ORDER BY id ASC`);
    const settingsMap: Record<string, any> = {};
    for (const r of rows) {
      settingsMap[r.key_name] = r.value_string;
    }
    return res.json({ success: true, settings: settingsMap, raw: rows, requestId: req.requestId });
  } catch (err) {
    next(err);
  }
}

export async function updateSetting(req: Request, res: Response, next: NextFunction) {
  try {
    const { keyName, valueString } = req.body;
    const actor = req.user ? `${req.user.role} #${req.user.id}` : 'Admin';

    if (!keyName || valueString === undefined) {
      return res.status(400).json({ success: false, message: 'keyName and valueString are required' });
    }

    await pool.execute(
      `INSERT INTO system_settings (key_name, value_string, updated_by, updated_at)
       VALUES (?, ?, ?, NOW())
       ON DUPLICATE KEY UPDATE value_string = VALUES(value_string), updated_by = VALUES(updated_by), updated_at = NOW()`,
      [keyName, String(valueString), actor]
    );

    if (keyName === 'maintenance_mode') {
      const enabled = String(valueString) === 'true';
      setMaintenanceCache(enabled);
      emitToAdmin('system:maintenance', { enabled });
    }

    await logAudit({
      actor,
      role: req.user?.role || 'ADMIN',
      action: 'SETTING_UPDATED',
      entity: 'SETTING',
      entityId: keyName,
      metadata: { keyName, valueString },
      ipAddress: req.ip,
    });

    return res.json({ success: true, message: `Setting '${keyName}' updated to '${valueString}'.` });
  } catch (err) {
    next(err);
  }
}
