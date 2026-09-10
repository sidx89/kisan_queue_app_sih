import { Request, Response, NextFunction } from 'express';
import { pool } from '../config/db';
import { logAudit } from '../services/audit.service';
import { emitToCentre, emitToAdmin, emitToUser } from '../services/socket.service';
import { sendPushNotification } from '../services/fcm.service';

const VALID_TRANSITIONS: Record<string, string[]> = {
  WAITING: ['CALLED', 'ARRIVED', 'CANCELLED', 'NO_SHOW'],
  CALLED: ['ARRIVED', 'PROCESSING', 'NO_SHOW'],
  ARRIVED: ['CALLED', 'PROCESSING', 'NO_SHOW'],
  PROCESSING: ['WEIGHMENT', 'QUALITY_CHECK', 'COMPLETED', 'REJECTED'],
  WEIGHMENT: ['QUALITY_CHECK', 'COMPLETED', 'REJECTED'],
  QUALITY_CHECK: ['COMPLETED', 'REJECTED'],
};

export async function getLiveQueue(req: Request, res: Response, next: NextFunction) {
  try {
    const centreId = parseInt(req.params.centreId as string, 10);
    const date = (req.query.date as string) || new Date().toISOString().split('T')[0];

    const [bookings]: any = await pool.execute(
      `SELECT b.*, p.full_name as farmer_name, p.farmer_code, p.phone as farmer_phone,
              cr.name as crop_name, cr.minimum_support_price, s.start_time, s.end_time,
              ct.counter_number
       FROM bookings b
       JOIN users u ON b.farmer_id = u.id
       JOIN profiles p ON u.id = p.user_id
       JOIN crops cr ON b.crop_id = cr.id
       JOIN slots s ON b.slot_id = s.id
       LEFT JOIN counters ct ON b.counter_id = ct.id
       WHERE b.centre_id = ? AND b.booking_date = ?
       ORDER BY
         CASE
           WHEN b.status IN ('PROCESSING', 'WEIGHMENT', 'QUALITY_CHECK') THEN 1
           WHEN b.status = 'CALLED' THEN 2
           WHEN b.status = 'ARRIVED' THEN 3
           WHEN b.status = 'WAITING' THEN 4
           ELSE 5
         END ASC,
         b.token_number ASC`,
      [centreId, date]
    );

    // Calculate dynamic queue position and estimated wait
    let waitingIndex = 1;
    const avgMins = 12;

    const queue = bookings.map((b: any) => {
      let waitEst = 0;
      let pos = 0;
      if (b.status === 'WAITING' || b.status === 'ARRIVED') {
        pos = waitingIndex++;
        waitEst = pos * avgMins;
      }
      return {
        ...b,
        queue_position: pos,
        estimated_wait_minutes: waitEst,
      };
    });

    const [counters]: any = await pool.execute(
      `SELECT ct.*, p.full_name as operator_name
       FROM counters ct
       LEFT JOIN users u ON ct.operator_id = u.id
       LEFT JOIN profiles p ON u.id = p.user_id
       WHERE ct.centre_id = ?`,
      [centreId]
    );

    return res.json({ success: true, centreId, date, queue, counters, requestId: req.requestId });
  } catch (err) {
    next(err);
  }
}

export async function callNext(req: Request, res: Response, next: NextFunction) {
  const conn = await pool.getConnection();
  try {
    const { centreId, counterId } = req.body;
    const operatorId = req.user?.id;
    const operatorName = req.user?.email || 'Operator';

    if (!centreId || !counterId) {
      return res.status(400).json({ success: false, message: 'centreId and counterId are required' });
    }

    await conn.beginTransaction();

    // 1. Fetch next waiting farmer with row lock
    const [nextRows]: any = await conn.execute(
      `SELECT b.*, p.full_name, u.id as user_id FROM bookings b
       JOIN users u ON b.farmer_id = u.id
       JOIN profiles p ON u.id = p.user_id
       WHERE b.centre_id = ? AND b.booking_date = CURDATE()
         AND b.status IN ('WAITING', 'ARRIVED')
       ORDER BY b.token_number ASC
       LIMIT 1 FOR UPDATE`,
      [centreId]
    );

    if (nextRows.length === 0) {
      await conn.rollback();
      return res.status(404).json({ success: false, message: 'No more waiting farmers in today queue.' });
    }

    const booking = nextRows[0];

    // 2. Update booking status to CALLED with counter
    await conn.execute(
      `UPDATE bookings
       SET status = 'CALLED', counter_id = ?, called_at = NOW()
       WHERE id = ?`,
      [counterId, booking.id]
    );

    // 3. Update counter status
    await conn.execute(
      `UPDATE counters
       SET status = 'CALLING', current_token = ?
       WHERE id = ?`,
      [booking.token_number, counterId]
    );

    await conn.commit();

    // 4. Audit
    await logAudit({
      actor: operatorName,
      role: req.user?.role || 'OPERATOR',
      action: 'CALLED_TOKEN',
      entity: 'BOOKING',
      entityId: booking.id,
      metadata: { tokenNumber: booking.token_number, counterId, farmer: booking.full_name },
      ipAddress: req.ip,
    });

    // 5. Notify farmer via push and socket
    await sendPushNotification({
      userId: booking.user_id,
      title: `Your Turn! Token #${booking.token_number}`,
      message: `Please proceed immediately to Counter #${counterId}. Operator is ready for your crop verification.`,
      type: 'YOUR_TURN',
      data: { tokenNumber: booking.token_number, counterId },
    });

    // 6. Broadcast to live queue
    emitToCentre(centreId, 'queue:update', {
      action: 'called',
      bookingId: booking.id,
      tokenNumber: booking.token_number,
      counterId,
      status: 'CALLED',
    });

    emitToAdmin('stats:refresh', { trigger: 'token_called' });

    return res.json({
      success: true,
      message: `Token #${booking.token_number} (${booking.full_name}) called to Counter #${counterId}.`,
      calledBooking: booking,
    });
  } catch (err) {
    await conn.rollback();
    next(err);
  } finally {
    conn.release();
  }
}

export async function updateQueueStatus(req: Request, res: Response, next: NextFunction) {
  const conn = await pool.getConnection();
  try {
    const bookingId = parseInt(req.params.bookingId as string, 10);
    const { status, counterId } = req.body;
    const actor = req.user ? `${req.user.role} #${req.user.id}` : 'Staff';

    await conn.beginTransaction();

    const [rows]: any = await conn.execute(
      `SELECT b.*, p.full_name, u.id as user_id FROM bookings b
       JOIN users u ON b.farmer_id = u.id
       JOIN profiles p ON u.id = p.user_id
       WHERE b.id = ? FOR UPDATE`,
      [bookingId]
    );

    if (rows.length === 0) {
      await conn.rollback();
      return res.status(404).json({ success: false, message: 'Booking not found' });
    }

    const currentBooking = rows[0];
    const allowed = VALID_TRANSITIONS[currentBooking.status] || [];

    if (!allowed.includes(status)) {
      await conn.rollback();
      return res.status(400).json({
        success: false,
        message: `Invalid queue transition from '${currentBooking.status}' to '${status}'. Allowed: ${allowed.join(', ')}`,
      });
    }

    // Set timestamps
    let processingSql = '';
    if (status === 'PROCESSING') {
      processingSql = `, processing_started_at = COALESCE(processing_started_at, NOW())`;
    } else if (status === 'COMPLETED') {
      processingSql = `, completed_at = NOW()`;
    }

    await conn.execute(
      `UPDATE bookings
       SET status = ?, counter_id = COALESCE(?, counter_id) ${processingSql}
       WHERE id = ?`,
      [status, counterId || null, bookingId]
    );

    // If counter is set to COMPLETED or REJECTED, free up the counter
    if ((status === 'COMPLETED' || status === 'REJECTED' || status === 'NO_SHOW') && currentBooking.counter_id) {
      await conn.execute(
        `UPDATE counters SET status = 'IDLE', current_token = NULL WHERE id = ?`,
        [currentBooking.counter_id]
      );
    }

    await conn.commit();

    await logAudit({
      actor,
      role: req.user?.role || 'OPERATOR',
      action: `QUEUE_${status}`,
      entity: 'BOOKING',
      entityId: bookingId,
      metadata: { previousStatus: currentBooking.status, newStatus: status, tokenNumber: currentBooking.token_number },
      ipAddress: req.ip,
    });

    emitToCentre(currentBooking.centre_id, 'queue:update', {
      action: 'status_changed',
      bookingId,
      tokenNumber: currentBooking.token_number,
      status,
    });

    emitToAdmin('stats:refresh', { trigger: 'status_update' });

    return res.json({ success: true, message: `Status updated to ${status}.` });
  } catch (err) {
    await conn.rollback();
    next(err);
  } finally {
    conn.release();
  }
}

export async function emergencyQueueControl(req: Request, res: Response, next: NextFunction) {
  try {
    const centreId = parseInt(req.params.centreId as string, 10);
    const { action } = req.body; // 'PAUSE', 'RESUME'
    const actor = req.user ? `${req.user.role} #${req.user.id}` : 'Admin';

    const newCentreStatus = action === 'PAUSE' ? 'BUSY' : 'OPEN';
    await pool.execute(`UPDATE procurement_centres SET status = ? WHERE id = ?`, [newCentreStatus, centreId]);

    await logAudit({
      actor,
      role: req.user?.role || 'ADMIN',
      action: `EMERGENCY_QUEUE_${action}`,
      entity: 'CENTRE',
      entityId: centreId,
      metadata: { action, newCentreStatus },
      ipAddress: req.ip,
    });

    emitToCentre(centreId, 'queue:emergency', { action, centreStatus: newCentreStatus });

    return res.json({ success: true, message: `Queue action ${action} executed for centre #${centreId}.` });
  } catch (err) {
    next(err);
  }
}
