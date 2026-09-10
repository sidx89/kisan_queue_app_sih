import { Request, Response, NextFunction } from 'express';
import { pool } from '../config/db';
import { logAudit } from '../services/audit.service';

export async function getCentres(req: Request, res: Response, next: NextFunction) {
  try {
    const [centres]: any = await pool.execute(
      `SELECT c.*,
              COUNT(DISTINCT ct.id) as counter_count,
              COUNT(DISTINCT CASE WHEN ct.status = 'ACTIVE' THEN ct.id END) as active_counters,
              (SELECT COUNT(*) FROM bookings WHERE centre_id = c.id AND booking_date = CURDATE() AND status = 'WAITING') as current_queue,
              (SELECT COALESCE(AVG(TIMESTAMPDIFF(MINUTE, checked_in_at, processing_started_at)), 25)
               FROM bookings WHERE centre_id = c.id AND booking_date = CURDATE() AND completed_at IS NOT NULL) as avg_wait_mins
       FROM procurement_centres c
       LEFT JOIN counters ct ON c.id = ct.centre_id
       GROUP BY c.id
       ORDER BY c.id ASC`
    );

    return res.json({ success: true, centres, data: centres, requestId: req.requestId });
  } catch (err) {
    next(err);
  }
}

export async function createCentre(req: Request, res: Response, next: NextFunction) {
  try {
    const { code, name, address, village, district, state, dailyCapacity, openTime, closeTime } = req.body;

    if (!code || !name || !address || !district) {
      return res.status(400).json({ success: false, message: 'Code, Name, Address, and District are required' });
    }

    const [resInsert]: any = await pool.execute(
      `INSERT INTO procurement_centres (code, name, address, village, district, state, daily_capacity, open_time, close_time, status)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'OPEN')`,
      [
        code.trim().toUpperCase(),
        name.trim(),
        address.trim(),
        village || 'Central',
        district.trim(),
        state || 'Gujarat',
        dailyCapacity || 150,
        openTime || '09:00',
        closeTime || '17:00',
      ]
    );

    const centreId = resInsert.insertId;

    // Create 2 default counters for new centre
    await pool.execute(
      `INSERT INTO counters (centre_id, counter_number, status) VALUES (?, 1, 'IDLE'), (?, 2, 'IDLE')`,
      [centreId, centreId]
    );

    await logAudit({
      actor: req.user ? `${req.user.role} #${req.user.id}` : 'Admin',
      role: req.user?.role || 'ADMIN',
      action: 'CENTRE_CREATED',
      entity: 'CENTRE',
      entityId: centreId,
      metadata: { code, name, district, dailyCapacity },
    });

    return res.status(201).json({ success: true, centreId, message: 'Centre created successfully.' });
  } catch (err) {
    next(err);
  }
}

export async function updateCentre(req: Request, res: Response, next: NextFunction) {
  try {
    const id = parseInt(req.params.id as string, 10);
    const { name, address, dailyCapacity, openTime, closeTime, status } = req.body;

    await pool.execute(
      `UPDATE procurement_centres
       SET name = COALESCE(?, name),
           address = COALESCE(?, address),
           daily_capacity = COALESCE(?, daily_capacity),
           open_time = COALESCE(?, open_time),
           close_time = COALESCE(?, close_time),
           status = COALESCE(?, status)
       WHERE id = ?`,
      [name, address, dailyCapacity, openTime, closeTime, status, id]
    );

    await logAudit({
      actor: req.user ? `${req.user.role} #${req.user.id}` : 'Admin',
      role: req.user?.role || 'ADMIN',
      action: 'CENTRE_UPDATED',
      entity: 'CENTRE',
      entityId: id,
      metadata: { name, dailyCapacity, status },
    });

    return res.json({ success: true, message: 'Centre updated successfully.' });
  } catch (err) {
    next(err);
  }
}

export async function getCounters(req: Request, res: Response, next: NextFunction) {
  try {
    const { centreId } = req.query;
    let sql = `
      SELECT ct.*, c.name as centre_name, u.email as operator_email, p.full_name as operator_name
      FROM counters ct
      JOIN procurement_centres c ON ct.centre_id = c.id
      LEFT JOIN users u ON ct.operator_id = u.id
      LEFT JOIN profiles p ON u.id = p.user_id
    `;
    const params: any[] = [];
    if (centreId) {
      sql += ` WHERE ct.centre_id = ?`;
      params.push(centreId);
    }
    sql += ` ORDER BY ct.centre_id, ct.counter_number`;

    const [counters]: any = await pool.execute(sql, params);
    return res.json({ success: true, counters, requestId: req.requestId });
  } catch (err) {
    next(err);
  }
}

export async function addCounter(req: Request, res: Response, next: NextFunction) {
  try {
    const { centreId } = req.body;
    const [maxRows]: any = await pool.execute(
      `SELECT COALESCE(MAX(counter_number), 0) + 1 AS next_counter FROM counters WHERE centre_id = ?`,
      [centreId]
    );
    const nextNum = maxRows[0].next_counter;

    await pool.execute(
      `INSERT INTO counters (centre_id, counter_number, status) VALUES (?, ?, 'IDLE')`,
      [centreId, nextNum]
    );

    await logAudit({
      actor: req.user ? `${req.user.role} #${req.user.id}` : 'Admin',
      role: req.user?.role || 'ADMIN',
      action: 'COUNTER_ADDED',
      entity: 'CENTRE',
      entityId: centreId,
      metadata: { counter_number: nextNum },
    });

    return res.json({ success: true, message: `Counter ${nextNum} added successfully.` });
  } catch (err) {
    next(err);
  }
}

export async function assignOperatorToCounter(req: Request, res: Response, next: NextFunction) {
  try {
    const counterId = parseInt(req.params.id as string, 10);
    const { operatorId, status } = req.body;

    await pool.execute(
      `UPDATE counters SET operator_id = ?, status = COALESCE(?, 'ACTIVE') WHERE id = ?`,
      [operatorId || null, status, counterId]
    );

    await logAudit({
      actor: req.user ? `${req.user.role} #${req.user.id}` : 'Admin',
      role: req.user?.role || 'ADMIN',
      action: 'OPERATOR_ASSIGNED_COUNTER',
      entity: 'COUNTER',
      entityId: counterId,
      metadata: { operator_id: operatorId, status },
    });

    return res.json({ success: true, message: 'Operator counter assignment updated.' });
  } catch (err) {
    next(err);
  }
}

export async function getCrops(req: Request, res: Response, next: NextFunction) {
  try {
    const [crops]: any = await pool.execute(`SELECT * FROM crops ORDER BY name ASC`);
    return res.json({ success: true, crops, data: crops, requestId: req.requestId });
  } catch (err) {
    next(err);
  }
}
