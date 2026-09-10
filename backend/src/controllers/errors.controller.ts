import { Request, Response, NextFunction } from 'express';
import { pool } from '../config/db';
import { logger, sanitize } from '../utils/logger';
import { emitToAdmin } from '../services/socket.service';
import { recordSystemError } from '../services/monitoring.service';
import { logAudit } from '../services/audit.service';

export async function reportClientError(req: Request, res: Response, next: NextFunction) {
  try {
    recordSystemError();

    const {
      errorId,
      screen,
      message,
      errorCode,
      httpStatus,
      endpoint,
      httpMethod,
      appVersion,
      deviceInfo,
      networkType,
      userReference,
      stackTrace,
    } = req.body;

    const safeErrorId = errorId || `ERR-CLIENT-${Date.now().toString().slice(-6)}`;
    const requestId = req.requestId || 'REQ-UNKNOWN';

    // Strictly sanitize diagnostic information
    const cleanMessage = String(message || 'Unknown mobile client error').substring(0, 500);
    const safeStack = stackTrace ? String(stackTrace).substring(0, 5000) : null;

    logger.error(`[CLIENT ERROR] [${safeErrorId}] Screen: ${screen} - ${cleanMessage}`, null, {
      screen,
      endpoint,
      httpStatus,
      userReference,
      deviceInfo,
    });

    await pool.execute(
      `INSERT INTO error_logs
       (error_id, request_id, source, severity, message, error_code, stack_trace, endpoint, http_method, http_status, user_reference, role, device_reference, app_version, network_type, screen_name, ip_metadata)
       VALUES (?, ?, 'Android', 'ERROR', ?, ?, ?, ?, ?, ?, ?, 'FARMER', ?, ?, ?, ?, ?)`,
      [
        safeErrorId,
        requestId,
        cleanMessage,
        errorCode || 'CLIENT_EXCEPTION',
        safeStack,
        endpoint || '/api',
        httpMethod || 'POST',
        httpStatus || 500,
        userReference || 'Farmer #Demo',
        deviceInfo || 'Android Device',
        appVersion || '1.0.0',
        networkType || '4G/WiFi',
        screen || 'UnknownScreen',
        req.ip || '127.0.0.1',
      ]
    );

    emitToAdmin('system:error', {
      error_id: safeErrorId,
      request_id: requestId,
      source: 'Android',
      severity: 'ERROR',
      message: cleanMessage,
      endpoint: endpoint || screen,
      http_status: httpStatus || 500,
      user_reference: userReference || 'Farmer',
      screen_name: screen,
      timestamp: new Date().toISOString(),
    });

    return res.status(200).json({
      success: true,
      message: 'Diagnostic report received securely.',
      errorId: safeErrorId,
    });
  } catch (err) {
    next(err);
  }
}

export async function getGroupedErrors(req: Request, res: Response, next: NextFunction) {
  try {
    const { severity, source, resolved } = req.query;

    let filterSql = '';
    const params: any[] = [];

    if (severity) {
      filterSql += ` AND severity = ?`;
      params.push(severity);
    }
    if (source) {
      filterSql += ` AND source = ?`;
      params.push(source);
    }
    if (resolved !== undefined && resolved !== '') {
      filterSql += ` AND resolved = ?`;
      params.push(resolved === 'true' || resolved === '1' ? 1 : 0);
    }

    const sql = `
      SELECT
        MD5(CONCAT(COALESCE(error_code, 'NONE'), ':', COALESCE(endpoint, 'NONE'), ':', LEFT(message, 60))) AS group_hash,
        MIN(error_id) AS sample_error_id,
        source,
        severity,
        message,
        error_code,
        endpoint,
        http_method,
        http_status,
        COUNT(*) AS occurrence_count,
        COUNT(DISTINCT user_reference) AS affected_users,
        MIN(timestamp) AS first_seen,
        MAX(timestamp) AS last_seen,
        MAX(resolved) AS resolved,
        MAX(stack_trace) AS sample_stack_trace,
        MAX(notes) AS notes
      FROM error_logs
      WHERE 1=1 ${filterSql}
      GROUP BY group_hash, source, severity, message, error_code, endpoint, http_method, http_status
      ORDER BY last_seen DESC LIMIT 100
    `;

    const [groups]: any = await pool.execute(sql, params);
    return res.json({ success: true, groups, requestId: req.requestId });
  } catch (err) {
    next(err);
  }
}

export async function getRawErrors(req: Request, res: Response, next: NextFunction) {
  try {
    const { search, severity, source, resolved } = req.query;

    let sql = `SELECT * FROM error_logs WHERE 1=1`;
    const params: any[] = [];

    if (severity) {
      sql += ` AND severity = ?`;
      params.push(severity);
    }
    if (source) {
      sql += ` AND source = ?`;
      params.push(source);
    }
    if (resolved !== undefined && resolved !== '') {
      sql += ` AND resolved = ?`;
      params.push(resolved === 'true' || resolved === '1' ? 1 : 0);
    }
    if (search) {
      sql += ` AND (error_id LIKE ? OR request_id LIKE ? OR message LIKE ? OR endpoint LIKE ?)`;
      const term = `%${search}%`;
      params.push(term, term, term, term);
    }

    sql += ` ORDER BY timestamp DESC LIMIT 100`;

    const [errors]: any = await pool.execute(sql, params);
    return res.json({ success: true, errors, requestId: req.requestId });
  } catch (err) {
    next(err);
  }
}

export async function resolveError(req: Request, res: Response, next: NextFunction) {
  try {
    const errorId = String(req.params.id);
    const { notes } = req.body;
    const resolver = req.user ? `${req.user.role} #${req.user.id}` : 'Admin';

    await pool.execute(
      `UPDATE error_logs
       SET resolved = 1, resolved_by = ?, resolved_at = NOW(), notes = ?
       WHERE error_id = ? OR id = ?`,
      [resolver, notes || 'Marked as resolved by administrator', errorId, errorId]
    );

    await logAudit({
      actor: resolver,
      role: req.user?.role || 'ADMIN',
      action: 'ERROR_RESOLVED',
      entity: 'ERROR_LOG',
      entityId: errorId,
      metadata: { errorId, notes },
      ipAddress: req.ip,
    });

    emitToAdmin('stats:refresh', { trigger: 'error_resolved' });

    return res.json({ success: true, message: `Error ${errorId} marked as resolved.` });
  } catch (err) {
    next(err);
  }
}
