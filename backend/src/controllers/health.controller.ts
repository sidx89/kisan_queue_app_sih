import { Request, Response } from 'express';
import { testDbConnection } from '../config/db';
import { getSocketClientCount } from '../services/socket.service';

export async function getOverallHealth(req: Request, res: Response) {
  const dbHealth = await testDbConnection();
  const socketClients = getSocketClientCount();

  const isHealthy = dbHealth.connected;

  return res.status(isHealthy ? 200 : 503).json({
    success: true,
    status: isHealthy ? 'healthy' : 'degraded',
    services: {
      database: dbHealth.connected ? 'connected' : 'disconnected',
      socket: 'running',
      notifications: 'available',
    },
    uptime: Math.floor(process.uptime()),
    timestamp: new Date().toISOString(),
  });
}

export async function getDbHealth(req: Request, res: Response) {
  const dbHealth = await testDbConnection();
  return res.status(dbHealth.connected ? 200 : 503).json({
    success: dbHealth.connected,
    service: 'database',
    status: dbHealth.connected ? 'connected' : 'disconnected',
    latencyMs: dbHealth.latencyMs,
    lastConnected: dbHealth.lastConnected,
    error: dbHealth.error,
  });
}

export async function getSocketHealth(req: Request, res: Response) {
  const count = getSocketClientCount();
  return res.json({
    success: true,
    service: 'socket.io',
    status: 'running',
    connectedClients: count,
  });
}

export async function getNotificationHealth(req: Request, res: Response) {
  return res.json({
    success: true,
    service: 'notifications',
    status: 'available',
    channels: ['FCM', 'InApp', 'Socket.IO'],
  });
}
