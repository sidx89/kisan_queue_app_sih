import { pool } from '../config/db';
import { logger } from '../utils/logger';
import { emitToUser, emitToAdmin } from './socket.service';

export interface PushNotificationPayload {
  userId: number;
  title: string;
  message: string;
  type: string;
  data?: Record<string, any>;
}

export async function sendPushNotification(payload: PushNotificationPayload): Promise<boolean> {
  try {
    // 1. Save notification to database
    await pool.execute(
      `INSERT INTO notifications (user_id, title, message, type, is_read)
       VALUES (?, ?, ?, ?, 0)`,
      [payload.userId, payload.title, payload.message, payload.type]
    );

    // 2. Fetch user's FCM token if available
    const [rows]: any = await pool.execute(
      `SELECT fcm_token FROM users WHERE id = ?`,
      [payload.userId]
    );
    const fcmToken = rows[0]?.fcm_token;

    // 3. Emit real-time socket event to user
    emitToUser(payload.userId, 'notification:new', {
      title: payload.title,
      message: payload.message,
      type: payload.type,
      data: payload.data,
      created_at: new Date().toISOString(),
    });

    emitToAdmin('notification:sent', {
      userId: payload.userId,
      title: payload.title,
      type: payload.type,
    });

    logger.info(`FCM PUSH [${payload.type}] to User #${payload.userId}: "${payload.title}" (Token: ${fcmToken ? 'Registered' : 'None'})`);
    return true;
  } catch (err) {
    logger.error('Failed to send push notification', err, payload);
    return false;
  }
}

export async function registerFcmToken(userId: number, token: string): Promise<void> {
  await pool.execute(
    `UPDATE users SET fcm_token = ? WHERE id = ?`,
    [token, userId]
  );
  logger.info(`Registered FCM token for user #${userId}`);
}
