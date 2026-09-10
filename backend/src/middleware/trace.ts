import { Request, Response, NextFunction } from 'express';
import { v4 as uuidv4 } from 'uuid';
import { logger } from '../utils/logger';
import { recordApiRequest } from '../services/monitoring.service';

declare global {
  namespace Express {
    interface Request {
      requestId?: string;
      user?: {
        id: number;
        email: string;
        role: string;
        farmer_code?: string;
      };
    }
  }
}

export function traceMiddleware(req: Request, res: Response, next: NextFunction) {
  const reqId = 'REQ-' + uuidv4().replace(/-/g, '').substring(0, 8).toUpperCase();
  req.requestId = reqId;
  res.setHeader('X-Request-Id', reqId);

  const startTime = Date.now();

  res.on('finish', () => {
    const duration = Date.now() - startTime;
    recordApiRequest(res.statusCode, duration);
    const userRef = req.user ? `${req.user.role}:${req.user.id}` : undefined;
    logger.access(req.method, req.originalUrl || req.url, res.statusCode, duration, userRef, reqId);
  });

  next();
}
