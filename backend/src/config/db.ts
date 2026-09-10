import mysql, { Pool, PoolOptions, RowDataPacket, ResultSetHeader } from 'mysql2/promise';
import { ENV } from './env';

const poolOptions: PoolOptions = {
  host: ENV.DB.HOST,
  port: ENV.DB.PORT,
  user: ENV.DB.USER,
  password: ENV.DB.PASSWORD,
  database: ENV.DB.NAME,
  waitForConnections: true,
  connectionLimit: 20,
  queueLimit: 0,
  enableKeepAlive: true,
  keepAliveInitialDelay: 10000,
};

export const pool: Pool = mysql.createPool(poolOptions);

export interface DbHealth {
  connected: boolean;
  latencyMs: number;
  lastConnected: string | null;
  error?: string;
}

let lastConnectedTime: string | null = null;

export async function testDbConnection(): Promise<DbHealth> {
  const start = Date.now();
  try {
    const conn = await pool.getConnection();
    await conn.ping();
    conn.release();
    const latency = Date.now() - start;
    lastConnectedTime = new Date().toISOString();
    return {
      connected: true,
      latencyMs: latency,
      lastConnected: lastConnectedTime,
    };
  } catch (err: any) {
    return {
      connected: false,
      latencyMs: Date.now() - start,
      lastConnected: lastConnectedTime,
      error: err.message,
    };
  }
}

export async function executeQuery<T extends RowDataPacket[][] | RowDataPacket[] | ResultSetHeader>(
  sql: string,
  params?: any[]
): Promise<T> {
  const [rows] = await pool.execute<T>(sql, params);
  return rows;
}
