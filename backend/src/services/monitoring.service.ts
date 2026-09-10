import os from 'os';
import { testDbConnection } from '../config/db';
import { getSocketClientCount } from './socket.service';

interface MetricSnapshot {
  uptimeSeconds: number;
  cpuPercent: number;
  memory: {
    totalGb: number;
    freeGb: number;
    usedGb: number;
    processMb: number;
  };
  api: {
    totalRequests: number;
    totalErrors: number;
    averageResponseTimeMs: number;
  };
  database: {
    connected: boolean;
    latencyMs: number;
    lastConnected: string | null;
  };
  socket: {
    connectedClients: number;
    status: 'ONLINE' | 'OFFLINE';
  };
  notifications: {
    status: 'ONLINE' | 'DEGRADED';
  };
}

let totalRequestsCount = 0;
let totalErrorsCount = 0;
const latencyBuffer: number[] = [];
const MAX_LATENCY_BUFFER = 100;

export function recordApiRequest(statusCode: number, latencyMs: number) {
  totalRequestsCount++;
  if (statusCode >= 400) {
    totalErrorsCount++;
  }
  latencyBuffer.push(latencyMs);
  if (latencyBuffer.length > MAX_LATENCY_BUFFER) {
    latencyBuffer.shift();
  }
}

export function recordSystemError() {
  totalErrorsCount++;
}

function calculateCpuUsage(): number {
  const cpus = os.cpus();
  let totalIdle = 0;
  let totalTick = 0;

  for (const cpu of cpus) {
    for (const type in cpu.times) {
      totalTick += (cpu.times as any)[type];
    }
    totalIdle += cpu.times.idle;
  }

  const idleFraction = totalIdle / (totalTick || 1);
  const usage = Math.round((1 - idleFraction) * 100);
  return Math.max(2, Math.min(100, usage));
}

export async function getSystemMetrics(): Promise<MetricSnapshot> {
  const dbHealth = await testDbConnection();
  const memTotal = os.totalmem();
  const memFree = os.freemem();
  const memUsed = memTotal - memFree;
  const procMem = process.memoryUsage().heapUsed;

  const avgLatency =
    latencyBuffer.length > 0
      ? Math.round(latencyBuffer.reduce((a, b) => a + b, 0) / latencyBuffer.length)
      : 25;

  const socketCount = getSocketClientCount();

  return {
    uptimeSeconds: Math.floor(process.uptime()),
    cpuPercent: calculateCpuUsage(),
    memory: {
      totalGb: parseFloat((memTotal / (1024 * 1024 * 1024)).toFixed(1)),
      freeGb: parseFloat((memFree / (1024 * 1024 * 1024)).toFixed(1)),
      usedGb: parseFloat((memUsed / (1024 * 1024 * 1024)).toFixed(1)),
      processMb: Math.round(procMem / (1024 * 1024)),
    },
    api: {
      totalRequests: totalRequestsCount,
      totalErrors: totalErrorsCount,
      averageResponseTimeMs: avgLatency,
    },
    database: {
      connected: dbHealth.connected,
      latencyMs: dbHealth.latencyMs,
      lastConnected: dbHealth.lastConnected,
    },
    socket: {
      connectedClients: socketCount,
      status: 'ONLINE',
    },
    notifications: {
      status: 'ONLINE',
    },
  };
}
