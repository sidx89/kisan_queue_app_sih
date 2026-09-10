import { pool } from '../config/db';
import { logger, sanitize } from '../utils/logger';
import { emitToAdmin } from './socket.service';

export interface AuditEntry {
  actor: string;
  role: string;
  action: string;
  entity: string;
  entityId?: string | number;
  metadata?: Record<string, any>;
  ipAddress?: string;
}

export async function logAudit(entry: AuditEntry): Promise<void> {
  try {
    const safeMeta = entry.metadata ? JSON.stringify(sanitize(entry.metadata)) : null;
    const [result]: any = await pool.execute(
      `INSERT INTO audit_logs (actor, role, action, entity, entity_id, metadata, ip_address)
       VALUES (?, ?, ?, ?, ?, ?, ?)`,
      [
        entry.actor,
        entry.role,
        entry.action,
        entry.entity,
        entry.entityId ? String(entry.entityId) : null,
        safeMeta,
        entry.ipAddress || '127.0.0.1',
      ]
    );

    const auditRecord = {
      id: result.insertId,
      actor: entry.actor,
      role: entry.role,
      action: entry.action,
      entity: entry.entity,
      entity_id: entry.entityId,
      metadata: entry.metadata,
      ip_address: entry.ipAddress,
      timestamp: new Date().toISOString(),
    };

    emitToAdmin('audit:new', auditRecord);
    logger.info(`AUDIT: [${entry.role}] ${entry.actor} -> ${entry.action} on ${entry.entity}:${entry.entityId || ''}`);
  } catch (err) {
    logger.error('Failed to write audit log entry', err, entry);
  }
}
