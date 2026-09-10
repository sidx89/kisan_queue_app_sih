import { Request, Response, NextFunction } from 'express';
import { pool } from '../config/db';
import { logAudit } from '../services/audit.service';
import { emitToAdmin } from '../services/socket.service';
import { sendPushNotification } from '../services/fcm.service';

export async function getPayments(req: Request, res: Response, next: NextFunction) {
  try {
    const { status, search } = req.query;

    let sql = `
      SELECT pay.*,
             pr.receipt_number, pr.procurement_amount, pr.quantity_received,
             cr.name as crop_name,
             p.full_name as farmer_name, p.farmer_code, p.phone as farmer_phone, p.bank_account_masked,
             c.name as centre_name
      FROM payments pay
      JOIN procurement_records pr ON pay.procurement_id = pr.id
      JOIN crops cr ON pr.crop_id = cr.id
      JOIN procurement_centres c ON pr.centre_id = c.id
      JOIN users u ON pay.farmer_id = u.id
      JOIN profiles p ON u.id = p.user_id
      WHERE 1=1
    `;
    const params: any[] = [];

    if (status) {
      sql += ` AND pay.status = ?`;
      params.push(status);
    }
    if (search) {
      sql += ` AND (pay.transaction_reference LIKE ? OR pr.receipt_number LIKE ? OR p.full_name LIKE ?)`;
      const term = `%${search}%`;
      params.push(term, term, term);
    }

    sql += ` ORDER BY pay.id DESC LIMIT 100`;

    const [payments]: any = await pool.execute(sql, params);
    return res.json({ success: true, payments, requestId: req.requestId });
  } catch (err) {
    next(err);
  }
}

export async function updatePaymentStatus(req: Request, res: Response, next: NextFunction) {
  try {
    const paymentId = parseInt(req.params.id as string, 10);
    const { status, paymentMethod } = req.body;
    const actor = req.user ? `${req.user.role} #${req.user.id}` : 'Admin';

    const [rows]: any = await pool.execute(
      `SELECT pay.*, p.full_name as farmer_name FROM payments pay
       JOIN profiles p ON pay.farmer_id = p.user_id
       WHERE pay.id = ? LIMIT 1`,
      [paymentId]
    );

    if (rows.length === 0) {
      return res.status(404).json({ success: false, message: 'Payment record not found' });
    }

    const currentPay = rows[0];

    await pool.execute(
      `UPDATE payments
       SET status = ?,
           payment_method = COALESCE(?, payment_method),
           completed_at = CASE WHEN ? = 'COMPLETED' THEN NOW() ELSE completed_at END
       WHERE id = ?`,
      [status, paymentMethod, status, paymentId]
    );

    // Financial Audit (Requirement 37)
    await logAudit({
      actor,
      role: req.user?.role || 'ADMIN',
      action: `PAYMENT_STATUS_${status}`,
      entity: 'PAYMENT',
      entityId: paymentId,
      metadata: {
        previousStatus: currentPay.status,
        newStatus: status,
        amount: currentPay.amount,
        farmer: currentPay.farmer_name,
        txnRef: currentPay.transaction_reference,
      },
      ipAddress: req.ip,
    });

    if (status === 'COMPLETED') {
      await sendPushNotification({
        userId: currentPay.farmer_id,
        title: 'Payment Credited',
        message: `Amount of Rs. ${Number(currentPay.amount).toLocaleString('en-IN')} has been successfully transferred via DBT. Ref: ${currentPay.transaction_reference}`,
        type: 'PAYMENT_SUCCESS',
        data: { paymentId, amount: currentPay.amount },
      });
    }

    emitToAdmin('stats:refresh', { trigger: 'payment_status_update' });

    return res.json({ success: true, message: `Payment marked as ${status}.` });
  } catch (err) {
    next(err);
  }
}
