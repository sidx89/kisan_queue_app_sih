import { Request, Response, NextFunction } from 'express';
import { pool } from '../config/db';
import { logAudit } from '../services/audit.service';
import { emitToCentre, emitToAdmin } from '../services/socket.service';
import { sendPushNotification } from '../services/fcm.service';

export async function createBooking(req: Request, res: Response, next: NextFunction) {
  const conn = await pool.getConnection();
  try {
    const farmerId = req.user?.id;
    const { centreId, slotId, cropId, expectedQuantity, estimated_quantity_kg, estimatedQuantityKg, notes, bookingDate } = req.body;
    const qty = expectedQuantity || estimated_quantity_kg || estimatedQuantityKg;

    if (!centreId || !slotId || !cropId || !qty) {
      return res.status(400).json({
        success: false,
        message: 'centreId, slotId, cropId, and quantity are required',
      });
    }

    await conn.beginTransaction();

    // 1. Lock and check slot availability
    const [slots]: any = await conn.execute(
      `SELECT * FROM slots WHERE id = ? FOR UPDATE`,
      [slotId]
    );
    if (slots.length === 0) {
      await conn.rollback();
      return res.status(404).json({ success: false, message: 'Slot not found' });
    }

    const slot = slots[0];
    if (slot.booked_count >= slot.capacity) {
      await conn.rollback();
      return res.status(400).json({ success: false, message: 'Selected slot is completely full. Please choose another.' });
    }

    const slotDateStr = slot.slot_date instanceof Date
      ? slot.slot_date.toISOString().split('T')[0]
      : String(slot.slot_date).split('T')[0];

    // 2. Generate next sequential token for this centre & date
    const [tokenRows]: any = await conn.execute(
      `SELECT COALESCE(MAX(token_number), 100) + 1 AS next_token
       FROM bookings WHERE centre_id = ? AND booking_date = ? FOR UPDATE`,
      [centreId, slotDateStr]
    );
    const tokenNumber = tokenRows[0].next_token;
    const bookingRef = `BK-${slotDateStr.replace(/-/g, '')}-${tokenNumber}`;

    // 3. Insert booking
    const [insertRes]: any = await conn.execute(
      `INSERT INTO bookings
       (booking_ref, token_number, farmer_id, centre_id, slot_id, crop_id, expected_quantity, booking_date, status, queue_position, estimated_wait_minutes)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'WAITING', ?, ?)`,
      [bookingRef, tokenNumber, farmerId, centreId, slotId, cropId, qty, slotDateStr, 1, 15]
    );

    const bookingId = insertRes.insertId;

    // 4. Update slot booked count
    const newBookedCount = slot.booked_count + 1;
    const newStatus = newBookedCount >= slot.capacity ? 'FULL' : newBookedCount >= slot.capacity * 0.7 ? 'ALMOST_FULL' : 'AVAILABLE';
    await conn.execute(
      `UPDATE slots SET booked_count = ?, status = ? WHERE id = ?`,
      [newBookedCount, newStatus, slotId]
    );

    await conn.commit();

    // 5. Audit log
    await logAudit({
      actor: req.user?.email || `Farmer #${farmerId}`,
      role: 'FARMER',
      action: 'BOOKED_SLOT',
      entity: 'BOOKING',
      entityId: bookingId,
      metadata: { bookingRef, tokenNumber, centreId, cropId, quantity: qty },
      ipAddress: req.ip,
    });

    // 6. Push notification to farmer
    await sendPushNotification({
      userId: farmerId!,
      title: 'Booking Confirmed!',
      message: `Your slot on ${slotDateStr} (${slot.start_time}) is confirmed. Token Number: ${tokenNumber}.`,
      type: 'BOOKING_CONFIRMED',
      data: { bookingId, tokenNumber, bookingRef },
    });

    // 7. Socket.IO live broadcasts
    emitToCentre(centreId, 'booking:new', {
      bookingId,
      tokenNumber,
      bookingRef,
      farmerId,
      slotTime: slot.start_time,
      status: 'WAITING',
    });

    emitToAdmin('stats:refresh', { trigger: 'new_booking' });

    const bookingObj = {
      id: bookingId,
      bookingRef,
      bookingToken: bookingRef,
      booking_token: bookingRef,
      tokenNumber: String(tokenNumber),
      token_number: String(tokenNumber),
      bookingDate: slotDateStr,
      slot_date: slotDateStr,
      slotTime: `${slot.start_time} - ${slot.end_time}`,
      slot_time: `${slot.start_time} - ${slot.end_time}`,
      status: 'WAITING',
      queuePosition: 1,
      estimatedWaitMinutes: 15,
    };

    return res.status(201).json({
      success: true,
      booking: bookingObj,
      data: bookingObj,
      requestId: req.requestId,
    });
  } catch (err) {
    await conn.rollback();
    next(err);
  } finally {
    conn.release();
  }
}

export async function getFarmerBookings(req: Request, res: Response, next: NextFunction) {
  try {
    const farmerId = req.user?.id;
    const [bookings]: any = await pool.execute(
      `SELECT b.*, c.name as centre_name, c.address as centre_address,
              cr.name as crop_name, cr.minimum_support_price, s.start_time, s.end_time,
              ct.counter_number
       FROM bookings b
       JOIN procurement_centres c ON b.centre_id = c.id
       JOIN crops cr ON b.crop_id = cr.id
       JOIN slots s ON b.slot_id = s.id
       LEFT JOIN counters ct ON b.counter_id = ct.id
       WHERE b.farmer_id = ?
       ORDER BY b.id DESC`,
      [farmerId || 0]
    );

    const formatted = bookings.map((b: any) => ({
      ...b,
      booking_token: b.booking_ref || `BK-${b.id}`,
      bookingToken: b.booking_ref || `BK-${b.id}`,
      slot_date: b.booking_date instanceof Date ? b.booking_date.toISOString().split('T')[0] : String(b.booking_date || ''),
      slotDate: b.booking_date instanceof Date ? b.booking_date.toISOString().split('T')[0] : String(b.booking_date || ''),
      slot_time: b.start_time && b.end_time ? `${b.start_time} - ${b.end_time}` : (b.start_time || '09:00 - 11:00'),
      slotTime: b.start_time && b.end_time ? `${b.start_time} - ${b.end_time}` : (b.start_time || '09:00 - 11:00'),
      estimated_quantity_kg: b.estimated_quantity || b.estimated_quantity_kg || 100,
      estimatedQuantityKg: b.estimated_quantity || b.estimated_quantity_kg || 100,
    }));

    return res.json({ success: true, bookings: formatted, data: formatted, requestId: req.requestId });
  } catch (err) {
    next(err);
  }
}

export async function getAllBookings(req: Request, res: Response, next: NextFunction) {
  try {
    const { centreId, date, status, search } = req.query;

    let sql = `
      SELECT b.*, c.name as centre_name, cr.name as crop_name, cr.unit,
             p.full_name as farmer_name, p.farmer_code, p.phone as farmer_phone,
             s.start_time, s.end_time, ct.counter_number
      FROM bookings b
      JOIN procurement_centres c ON b.centre_id = c.id
      JOIN crops cr ON b.crop_id = cr.id
      JOIN users u ON b.farmer_id = u.id
      JOIN profiles p ON u.id = p.user_id
      JOIN slots s ON b.slot_id = s.id
      LEFT JOIN counters ct ON b.counter_id = ct.id
      WHERE 1=1
    `;
    const params: any[] = [];

    if (centreId) {
      sql += ` AND b.centre_id = ?`;
      params.push(centreId);
    }
    if (date) {
      sql += ` AND b.booking_date = ?`;
      params.push(date);
    }
    if (status) {
      sql += ` AND b.status = ?`;
      params.push(status);
    }
    if (search) {
      sql += ` AND (b.booking_ref LIKE ? OR p.full_name LIKE ? OR p.farmer_code LIKE ?)`;
      const term = `%${search}%`;
      params.push(term, term, term);
    }

    sql += ` ORDER BY b.booking_date DESC, b.token_number ASC LIMIT 100`;

    const [bookings]: any = await pool.execute(sql, params);
    return res.json({ success: true, bookings, requestId: req.requestId });
  } catch (err) {
    next(err);
  }
}

export async function qrCheckIn(req: Request, res: Response, next: NextFunction) {
  try {
    const { tokenNumber, bookingRef, centreId } = req.body;
    const actor = req.user ? `${req.user.role} #${req.user.id}` : 'QR Scanner';

    const [rows]: any = await pool.execute(
      `SELECT b.*, p.full_name FROM bookings b
       JOIN profiles p ON b.farmer_id = p.user_id
       WHERE (b.token_number = ? OR b.booking_ref = ?) AND b.booking_date = CURDATE() LIMIT 1`,
      [tokenNumber || 0, bookingRef || '']
    );

    if (rows.length === 0) {
      return res.status(404).json({ success: false, message: 'Valid today booking not found for token/QR' });
    }

    const booking = rows[0];

    await pool.execute(
      `UPDATE bookings
       SET status = 'ARRIVED', checked_in_at = COALESCE(checked_in_at, NOW())
       WHERE id = ?`,
      [booking.id]
    );

    await logAudit({
      actor,
      role: req.user?.role || 'OPERATOR',
      action: 'SCANNED_QR',
      entity: 'BOOKING',
      entityId: booking.id,
      metadata: { tokenNumber: booking.token_number, farmer: booking.full_name },
      ipAddress: req.ip,
    });

    emitToCentre(booking.centre_id, 'queue:update', {
      action: 'arrived',
      bookingId: booking.id,
      tokenNumber: booking.token_number,
    });

    return res.json({
      success: true,
      message: `Token ${booking.token_number} (${booking.full_name}) marked as ARRIVED.`,
      booking,
    });
  } catch (err) {
    next(err);
  }
}
