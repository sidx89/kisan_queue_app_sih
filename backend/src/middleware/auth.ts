import { Request, Response, NextFunction } from 'express';
import jwt from 'jsonwebtoken';
import { ENV } from '../config/env';

export interface TokenPayload {
  id: number;
  email: string;
  role: string;
  farmer_code?: string;
}

export function authenticate(req: Request, res: Response, next: NextFunction) {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({
      success: false,
      message: 'Authorization required. Please provide a valid Bearer token.',
      requestId: req.requestId,
    });
  }

  const token = authHeader.split(' ')[1];
  try {
    const decoded = jwt.verify(token, ENV.JWT_SECRET) as TokenPayload;
    req.user = decoded;
    next();
  } catch (err: any) {
    return res.status(401).json({
      success: false,
      message: 'Session expired or invalid token. Please log in again.',
      requestId: req.requestId,
    });
  }
}

export function requireRole(allowedRoles: string[]) {
  return (req: Request, res: Response, next: NextFunction) => {
    if (!req.user) {
      return res.status(401).json({
        success: false,
        message: 'Authentication required',
        requestId: req.requestId,
      });
    }

    if (!allowedRoles.includes(req.user.role)) {
      return res.status(403).json({
        success: false,
        message: `Forbidden: role '${req.user.role}' is not authorized to access this resource.`,
        requestId: req.requestId,
      });
    }

    next();
  };
}
