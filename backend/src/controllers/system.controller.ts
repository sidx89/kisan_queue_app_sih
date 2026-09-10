import { Request, Response, NextFunction } from 'express';
import { getSystemMetrics } from '../services/monitoring.service';
import { getSocketClientCount } from '../services/socket.service';
import { testDbConnection } from '../config/db';
import fs from 'fs';
import path from 'path';

// Path to runtime files written by the launcher
const RUNTIME_DIR = path.join(process.cwd(), '..', 'runtime');
const TUNNEL_URL_FILE = path.join(RUNTIME_DIR, 'tunnel-url.txt');
const TUNNEL_STATUS_FILE = path.join(RUNTIME_DIR, 'tunnel-status.json');

function readTunnelUrl(): string | null {
  try {
    if (fs.existsSync(TUNNEL_URL_FILE)) {
      const url = fs.readFileSync(TUNNEL_URL_FILE, 'utf8').trim();
      if (url && url.startsWith('https://')) return url;
    }
  } catch (_) {}
  return null;
}

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

/**
 * GET /api/system/public-config
 * Public endpoint — no auth required.
 * Returns the current tunnel URL and environment config.
 * Android app uses this via USB to bootstrap tunnel URL.
 */
export async function getPublicConfig(req: Request, res: Response) {
  const tunnelUrl = readTunnelUrl();
  let tunnelStatus: any = null;
  try {
    if (fs.existsSync(TUNNEL_STATUS_FILE)) {
      tunnelStatus = JSON.parse(fs.readFileSync(TUNNEL_STATUS_FILE, 'utf8'));
    }
  } catch (_) {}

  return res.json({
    success: true,
    apiBaseUrl: tunnelUrl || null,
    socketUrl: tunnelUrl || null,
    environment: tunnelUrl ? 'tunnel' : 'local',
    tunnelRunning: !!tunnelUrl,
    tunnelStartedAt: tunnelStatus?.startedAt || null,
    localUrl: 'http://127.0.0.1:5000',
    timestamp: new Date().toISOString(),
  });
}

/**
 * GET /api/system/connection-test
 * Public endpoint — returns live server/DB/socket health.
 */
export async function getConnectionTest(req: Request, res: Response) {
  const dbResult = await testDbConnection();
  const socketClients = getSocketClientCount();

  return res.json({
    success: true,
    server: 'online',
    database: dbResult.connected ? 'connected' : 'disconnected',
    dbLatencyMs: dbResult.latencyMs ?? null,
    socket: 'running',
    socketClients,
    environment: process.env.NODE_ENV || 'development',
    timestamp: new Date().toISOString(),
  });
}
