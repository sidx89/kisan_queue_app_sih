import { Request, Response, NextFunction } from 'express';
import { getSystemMetrics } from '../services/monitoring.service';

export async function getMetrics(req: Request, res: Response, next: NextFunction) {
  try {
    const metrics = await getSystemMetrics();
    return res.json({
      success: true,
      metrics,
      requestId: req.requestId,
    });
  } catch (err) {
    next(err);
  }
}
