import { Request, Response, NextFunction } from 'express';
import { pool } from '../config/db';

export async function getAuditLogs(req: Request, res: Response, next: NextFunction) {
  try {
    const { role, action, entity, search, startDate, endDate } = req.query;

    let sql = `SELECT * FROM audit_logs WHERE 1=1`;
    const params: any[] = [];

    if (role) {
      sql += ` AND role = ?`;
      params.push(role);
    }
    if (action) {
      sql += ` AND action = ?`;
      params.push(action);
    }
    if (entity) {
      sql += ` AND entity = ?`;
      params.push(entity);
    }
    if (search) {
      sql += ` AND (actor LIKE ? OR metadata LIKE ? OR action LIKE ?)`;
      const term = `%${search}%`;
      params.push(term, term, term);
    }
    if (startDate) {
      sql += ` AND timestamp >= ?`;
      params.push(startDate);
    }
    if (endDate) {
      sql += ` AND timestamp <= ?`;
      params.push(endDate);
    }

    sql += ` ORDER BY timestamp DESC LIMIT 150`;

    const [logs]: any = await pool.execute(sql, params);

    const parsedLogs = logs.map((l: any) => {
      let meta = null;
      try {
        meta = l.metadata ? JSON.parse(l.metadata) : null;
      } catch {
        meta = l.metadata;
      }
      return { ...l, metadata: meta };
    });

    return res.json({ success: true, logs: parsedLogs, requestId: req.requestId });
  } catch (err) {
    next(err);
  }
}
