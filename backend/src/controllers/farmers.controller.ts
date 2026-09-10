import { Request, Response, NextFunction } from 'express';
import { pool } from '../config/db';
import { logAudit } from '../services/audit.service';

function maskPhone(phone: string): string {
  if (!phone || phone.length < 8) return phone;
  return phone.substring(0, phone.length - 5) + '*****';
}

export async function getFarmers(req: Request, res: Response, next: NextFunction) {
  try {
    const { search, status, district } = req.query;

    let sql = `
      SELECT u.id, u.email, u.status, u.created_at,
             p.farmer_code, p.full_name, p.phone, p.village, p.district, p.state, p.land_size_acres,
             (SELECT COUNT(*) FROM bookings WHERE farmer_id = u.id) AS total_bookings,
             (SELECT COALESCE(SUM(procurement_amount), 0) FROM procurement_records WHERE farmer_id = u.id) AS total_procured_amount
      FROM users u
      JOIN profiles p ON u.id = p.user_id
      WHERE u.role = 'FARMER'
    `;
    const params: any[] = [];

    if (search) {
      sql += ` AND (p.full_name LIKE ? OR p.farmer_code LIKE ? OR p.phone LIKE ? OR p.village LIKE ?)`;
      const term = `%${search}%`;
      params.push(term, term, term, term);
    }

    if (status) {
      sql += ` AND u.status = ?`;
      params.push(status);
    }

    if (district) {
      sql += ` AND p.district = ?`;
      params.push(district);
    }

    sql += ` ORDER BY u.id DESC`;

    const [rows]: any = await pool.execute(sql, params);

    const farmers = rows.map((f: any) => ({
      ...f,
      masked_phone: maskPhone(f.phone),
    }));

    return res.json({ success: true, farmers, requestId: req.requestId });
  } catch (err) {
    next(err);
  }
}

export async function getFarmerById(req: Request, res: Response, next: NextFunction) {
  try {
    const id = parseInt(req.params.id as string, 10);
    const [rows]: any = await pool.execute(
      `SELECT u.id, u.email, u.status, u.created_at,
              p.farmer_code, p.full_name, p.phone, p.village, p.district, p.state, p.land_size_acres, p.bank_account_masked
       FROM users u
       JOIN profiles p ON u.id = p.user_id
       WHERE u.id = ? AND u.role = 'FARMER' LIMIT 1`,
      [id]
    );

    if (rows.length === 0) {
      return res.status(404).json({ success: false, message: 'Farmer not found', requestId: req.requestId });
    }

    const [bookings]: any = await pool.execute(
      `SELECT b.*, c.name as centre_name, cr.name as crop_name
       FROM bookings b
       JOIN procurement_centres c ON b.centre_id = c.id
       JOIN crops cr ON b.crop_id = cr.id
       WHERE b.farmer_id = ?
       ORDER BY b.id DESC LIMIT 20`,
      [id]
    );

    const [procurements]: any = await pool.execute(
      `SELECT pr.*, cr.name as crop_name, c.name as centre_name
       FROM procurement_records pr
       JOIN crops cr ON pr.crop_id = cr.id
       JOIN procurement_centres c ON pr.centre_id = c.id
       WHERE pr.farmer_id = ?
       ORDER BY pr.id DESC LIMIT 20`,
      [id]
    );

    return res.json({
      success: true,
      farmer: {
        ...rows[0],
        masked_phone: maskPhone(rows[0].phone),
      },
      bookings,
      procurements,
      requestId: req.requestId,
    });
  } catch (err) {
    next(err);
  }
}

export async function toggleFarmerStatus(req: Request, res: Response, next: NextFunction) {
  try {
    const id = parseInt(req.params.id as string, 10);
    const { status } = req.body;

    if (!['ACTIVE', 'DISABLED'].includes(status)) {
      return res.status(400).json({ success: false, message: 'Invalid status. Must be ACTIVE or DISABLED.' });
    }

    await pool.execute(`UPDATE users SET status = ? WHERE id = ? AND role = 'FARMER'`, [status, id]);

    await logAudit({
      actor: req.user ? `${req.user.role} #${req.user.id}` : 'Admin',
      role: req.user?.role || 'ADMIN',
      action: status === 'ACTIVE' ? 'FARMER_ENABLED' : 'FARMER_DISABLED',
      entity: 'USER',
      entityId: id,
      metadata: { target_id: id, new_status: status },
      ipAddress: req.ip,
    });

    return res.json({ success: true, message: `Farmer account ${status.toLowerCase()} successfully.` });
  } catch (err) {
    next(err);
  }
}
