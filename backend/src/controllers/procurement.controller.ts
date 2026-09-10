import { Request, Response, NextFunction } from 'express';
import { pool } from '../config/db';
import { logAudit } from '../services/audit.service';
import { emitToCentre, emitToAdmin } from '../services/socket.service';
import { sendPushNotification } from '../services/fcm.service';

export async function createProcurementRecord(req: Request, res: Response, next: NextFunction) {
  const conn = await pool.getConnection();
  try {
    const operatorId = req.user?.id || 4;
    const operatorName = req.user?.email || 'Operator';
    const {
      bookingId,
      quantityReceived,
      qualityGrade,
      moisturePercentage,
      foreignMatterPercentage,
      remarks,
    } = req.body;

    if (!bookingId || !quantityReceived || !qualityGrade) {
      return res.status(400).json({
        success: false,
        message: 'bookingId, quantityReceived, and qualityGrade are required',
      });
    }

    await conn.beginTransaction();

    // 1. Fetch booking and crop details
    const [bookings]: any = await conn.execute(
      `SELECT b.*, cr.name as crop_name, cr.minimum_support_price, p.full_name as farmer_name
       FROM bookings b
       JOIN crops cr ON b.crop_id = cr.id
       JOIN profiles p ON b.farmer_id = p.user_id
       WHERE b.id = ? FOR UPDATE`,
      [bookingId]
    );

    if (bookings.length === 0) {
      await conn.rollback();
      return res.status(404).json({ success: false, message: 'Booking not found' });
    }

    const booking = bookings[0];
    const mspRate = parseFloat(booking.minimum_support_price);
    const quantity = parseFloat(quantityReceived);
    const totalAmount = qualityGrade === 'REJECTED' ? 0 : parseFloat((quantity * mspRate).toFixed(2));

    const yyyy = new Date().getFullYear();
    const randReceipt = Math.floor(1000 + Math.random() * 9000);
    const receiptNumber = `RCPT-${booking.centre_id}-${yyyy}-${randReceipt}`;

    // 2. Insert procurement record
    const [procRes]: any = await conn.execute(
      `INSERT INTO procurement_records
       (booking_id, farmer_id, centre_id, crop_id, quantity_received, quality_grade, rate_per_unit, procurement_amount, moisture_percentage, foreign_matter_percentage, remarks, receipt_number, processed_by)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
      [
        bookingId,
        booking.farmer_id,
        booking.centre_id,
        booking.crop_id,
        quantity,
        qualityGrade,
        mspRate,
        totalAmount,
        moisturePercentage || 0,
        foreignMatterPercentage || 0,
        remarks || '',
        receiptNumber,
        operatorId,
      ]
    );

    const procurementId = procRes.insertId;

    // 3. Update booking to COMPLETED or REJECTED
    const finalBookingStatus = qualityGrade === 'REJECTED' ? 'REJECTED' : 'COMPLETED';
    await conn.execute(
      `UPDATE bookings SET status = ?, completed_at = NOW() WHERE id = ?`,
      [finalBookingStatus, bookingId]
    );

    // 4. Create automatic payment record if accepted
    if (qualityGrade !== 'REJECTED' && totalAmount > 0) {
      const txnRef = `TXN-DBT-${Date.now().toString().slice(-8)}${Math.floor(100 + Math.random() * 900)}`;
      await conn.execute(
        `INSERT INTO payments
         (procurement_id, farmer_id, amount, payment_method, transaction_reference, status)
         VALUES (?, ?, ?, 'BANK_TRANSFER', ?, 'PROCESSING')`,
        [procurementId, booking.farmer_id, totalAmount, txnRef]
      );
    }

    // 5. Free up counter if operator had one assigned
    if (booking.counter_id) {
      await conn.execute(
        `UPDATE counters SET status = 'IDLE', current_token = NULL WHERE id = ?`,
        [booking.counter_id]
      );
    }

    await conn.commit();

    // 6. Audit logging (Requirement 17 & 37)
    await logAudit({
      actor: operatorName,
      role: req.user?.role || 'OPERATOR',
      action: 'COMPLETED_PROCUREMENT',
      entity: 'PROCUREMENT',
      entityId: procurementId,
      metadata: {
        bookingId,
        farmer: booking.farmer_name,
        quantity,
        grade: qualityGrade,
        rate: mspRate,
        amount: totalAmount,
        receiptNumber,
      },
      ipAddress: req.ip,
    });

    // 7. Push notification to Farmer
    await sendPushNotification({
      userId: booking.farmer_id,
      title: 'Procurement Slip Generated',
      message: `Your produce (${quantity} Quintals ${booking.crop_name}) graded '${qualityGrade}'. Total Amount: Rs. ${totalAmount.toLocaleString('en-IN')}. Receipt #${receiptNumber}`,
      type: 'PROCUREMENT_COMPLETED',
      data: { procurementId, receiptNumber, totalAmount },
    });

    // 8. Real-time updates
    emitToCentre(booking.centre_id, 'queue:update', {
      action: 'completed',
      bookingId,
      tokenNumber: booking.token_number,
      status: finalBookingStatus,
    });

    emitToAdmin('procurement:new', {
      procurementId,
      amount: totalAmount,
      crop: booking.crop_name,
      grade: qualityGrade,
    });

    emitToAdmin('stats:refresh', { trigger: 'procurement_completed' });

    return res.status(201).json({
      success: true,
      message: 'Procurement recorded successfully and digital receipt generated.',
      procurement: {
        id: procurementId,
        receiptNumber,
        crop: booking.crop_name,
        quantity,
        grade: qualityGrade,
        rate: mspRate,
        totalAmount,
      },
      requestId: req.requestId,
    });
  } catch (err) {
    await conn.rollback();
    next(err);
  } finally {
    conn.release();
  }
}

export async function getProcurementRecords(req: Request, res: Response, next: NextFunction) {
  try {
    const { centreId, date, search } = req.query;

    let sql = `
      SELECT pr.*, cr.name as crop_name, cr.unit,
             c.name as centre_name,
             p.full_name as farmer_name, p.farmer_code, p.phone as farmer_phone,
             pay.status as payment_status, pay.transaction_reference
      FROM procurement_records pr
      JOIN crops cr ON pr.crop_id = cr.id
      JOIN procurement_centres c ON pr.centre_id = c.id
      JOIN users u ON pr.farmer_id = u.id
      JOIN profiles p ON u.id = p.user_id
      LEFT JOIN payments pay ON pr.id = pay.procurement_id
      WHERE 1=1
    `;
    const params: any[] = [];

    if (centreId) {
      sql += ` AND pr.centre_id = ?`;
      params.push(centreId);
    }
    if (date) {
      sql += ` AND DATE(pr.processed_at) = ?`;
      params.push(date);
    }
    if (search) {
      sql += ` AND (pr.receipt_number LIKE ? OR p.full_name LIKE ? OR p.farmer_code LIKE ?)`;
      const term = `%${search}%`;
      params.push(term, term, term);
    }

    sql += ` ORDER BY pr.id DESC LIMIT 100`;

    const [records]: any = await pool.execute(sql, params);
    return res.json({ success: true, records, requestId: req.requestId });
  } catch (err) {
    next(err);
  }
}
