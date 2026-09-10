import fs from 'fs';
import path from 'path';

const LOGS_DIR = path.resolve(__dirname, '../../logs');

if (!fs.existsSync(LOGS_DIR)) {
  fs.mkdirSync(LOGS_DIR, { recursive: true });
}

const APP_LOG_PATH = path.join(LOGS_DIR, 'app.log');
const ERROR_LOG_PATH = path.join(LOGS_DIR, 'error.log');
const ACCESS_LOG_PATH = path.join(LOGS_DIR, 'access.log');

const SENSITIVE_KEYS = [
  'password',
  'password_hash',
  'token',
  'jwt',
  'otp',
  'aadhaar',
  'card',
  'secret',
  'authorization',
  'bearer',
];

export function sanitize(data: any): any {
  if (!data || typeof data !== 'object') return data;
  if (Array.isArray(data)) return data.map(sanitize);

  const clean: Record<string, any> = {};
  for (const [key, value] of Object.entries(data)) {
    const isSensitive = SENSITIVE_KEYS.some((k) => key.toLowerCase().includes(k));
    if (isSensitive) {
      clean[key] = '[REDACTED]';
    } else if (typeof value === 'object' && value !== null) {
      clean[key] = sanitize(value);
    } else {
      clean[key] = value;
    }
  }
  return clean;
}

function appendLog(filePath: string, line: string) {
  try {
    fs.appendFileSync(filePath, line + '\n', 'utf8');
  } catch (err) {
    console.error('Failed to write log line:', err);
  }
}

export const logger = {
  info: (message: string, meta?: any) => {
    const timestamp = new Date().toISOString();
    const safeMeta = meta ? JSON.stringify(sanitize(meta)) : '';
    const line = `[${timestamp}] [INFO] ${message} ${safeMeta}`;
    console.log(line);
    appendLog(APP_LOG_PATH, line);
  },

  warn: (message: string, meta?: any) => {
    const timestamp = new Date().toISOString();
    const safeMeta = meta ? JSON.stringify(sanitize(meta)) : '';
    const line = `[${timestamp}] [WARN] ${message} ${safeMeta}`;
    console.warn(line);
    appendLog(APP_LOG_PATH, line);
  },

  error: (message: string, err?: any, meta?: any) => {
    const timestamp = new Date().toISOString();
    const stack = err?.stack || (typeof err === 'string' ? err : '');
    const safeMeta = meta ? JSON.stringify(sanitize(meta)) : '';
    const line = `[${timestamp}] [ERROR] ${message} ${stack} ${safeMeta}`;
    console.error(line);
    appendLog(ERROR_LOG_PATH, line);
    appendLog(APP_LOG_PATH, line);
  },

  access: (method: string, url: string, status: number, durationMs: number, user?: string, reqId?: string) => {
    const timestamp = new Date().toISOString();
    const line = `[${timestamp}] [ACCESS] [${reqId || 'N/A'}] ${method} ${url} ${status} ${durationMs}ms user=${user || 'anonymous'}`;
    console.log(line);
    appendLog(ACCESS_LOG_PATH, line);
  },
};
