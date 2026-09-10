import { Request, Response, NextFunction } from 'express';
import { v4 as uuidv4 } from 'uuid';
import { pool } from '../config/db';
import { logger } from '../utils/logger';
import { emitToAdmin } from '../services/socket.service';
import { recordSystemError } from '../services/monitoring.service';

function generateErrorId(): string {
  const now = new Date();
  const yyyy = now.getFullYear();
  const mm = String(now.getMonth() + 1).padStart(2, '0');
  const dd = String(now.getDate()).padStart(2, '0');
  const rand = Math.floor(10000 + Math.random() * 90000);
  return `ERR-${yyyy}${mm}${dd}-${rand}`;
}

export async function centralErrorHandler(
  err: any,
  req: Request,
  res: Response,
  next: NextFunction
) {
  recordSystemError();
  const errorId = generateErrorId();
  const requestId = req.requestId || 'REQ-UNKNOWN';
  const statusCode = err.status || err.statusCode || 500;
  const message = err.message || 'Internal server error occurred';

  // 1. Log detailed stack trace locally
  logger.error(`[${errorId}] [${requestId}] ${req.method} ${req.originalUrl}: ${message}`, err, {
    user: req.user,
    ip: req.ip,
  });

  // 2. Persist safe diagnostic info to database
  try {
    const userRef = req.user ? `${req.user.role} #${req.user.id}` : 'Anonymous';
    const role = req.user?.role || 'PUBLIC';
    const severity = statusCode >= 500 ? 'CRITICAL' : 'ERROR';

    await pool.execute(
      `INSERT INTO error_logs
       (error_id, request_id, source, severity, message, error_code, stack_trace, endpoint, http_method, http_status, user_reference, role, ip_metadata)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
      [
        errorId,
        requestId,
        'Backend',
        severity,
        message,
        err.code || `HTTP_${statusCode}`,
        err.stack || String(err),
        req.originalUrl || req.url,
        req.method,
        statusCode,
        userRef,
        role,
        req.ip || '127.0.0.1',
      ]
    );

    // 3. Emit real-time notification to admin control panel
    emitToAdmin('system:error', {
      error_id: errorId,
      request_id: requestId,
      source: 'Backend',
      severity,
      message,
      endpoint: req.originalUrl || req.url,
      http_status: statusCode,
      user_reference: userRef,
      timestamp: new Date().toISOString(),
    });
  } catch (dbErr) {
    logger.error('Failed to log error to database', dbErr);
  }

  // 4. Return clean, sanitized response without stack traces
  return res.status(statusCode).json({
    success: false,
    message: statusCode === 500 ? 'Something went wrong. Please try again.' : message,
    errorId,
    requestId,
  });
}
