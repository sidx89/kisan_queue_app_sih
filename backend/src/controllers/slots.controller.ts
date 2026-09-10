import { Request, Response, NextFunction } from 'express';
import { pool } from '../config/db';
import { logAudit } from '../services/audit.service';

const DEFAULT_TIME_SLOTS = [
  ['09:00', '09:30'],
  ['09:30', '10:00'],
  ['10:00', '10:30'],
  ['10:30', '11:00'],
  ['11:00', '11:30'],
  ['11:30', '12:00'],
  ['12:00', '12:30'],
  ['12:30', '13:00'],
  ['14:00', '14:30'],
  ['14:30', '15:00'],
  ['15:00', '15:30'],
  ['15:30', '16:00'],
  ['16:00', '16:30'],
  ['16:30', '17:00'],
];

export async function getSlots(req: Request, res: Response, next: NextFunction) {
  try {
    const centreId = parseInt(req.query.centreId as string, 10);
    const date = (req.query.date as string) || new Date().toISOString().split('T')[0];

    if (!centreId) {
      return res.status(400).json({ success: false, message: 'centreId query param is required' });
    }

    // 1. Fetch existing slots
    let [slots]: any = await pool.execute(
      `SELECT * FROM slots WHERE centre_id = ? AND slot_date = ? ORDER BY start_time ASC`,
      [centreId, date]
    );

    // 2. If no slots generated for that day yet, auto-populate default slots
    if (slots.length === 0) {
      for (const [start, end] of DEFAULT_TIME_SLOTS) {
        await pool.execute(
          `INSERT IGNORE INTO slots (centre_id, slot_date, start_time, end_time, capacity, booked_count, status)
           VALUES (?, ?, ?, ?, 10, 0, 'AVAILABLE')`,
          [centreId, date, start, end]
        );
      }

      const [fresh]: any = await pool.execute(
        `SELECT * FROM slots WHERE centre_id = ? AND slot_date = ? ORDER BY start_time ASC`,
        [centreId, date]
      );
      slots = fresh;
    }

    return res.json({ success: true, centreId, date, slots, requestId: req.requestId });
  } catch (err) {
    next(err);
  }
}

export async function updateSlot(req: Request, res: Response, next: NextFunction) {
  try {
    const slotId = parseInt(req.params.id as string, 10);
    const { capacity, status } = req.body;

    await pool.execute(
      `UPDATE slots
       SET capacity = COALESCE(?, capacity),
           status = COALESCE(?, status)
       WHERE id = ?`,
      [capacity, status, slotId]
    );

    await logAudit({
      actor: req.user ? `${req.user.role} #${req.user.id}` : 'Admin',
      role: req.user?.role || 'ADMIN',
      action: 'SLOT_UPDATED',
      entity: 'SLOT',
      entityId: slotId,
      metadata: { capacity, status },
    });

    return res.json({ success: true, message: 'Slot configuration updated successfully.' });
  } catch (err) {
    next(err);
  }
}
