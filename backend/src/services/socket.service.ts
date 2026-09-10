import { Server as SocketIOServer, Socket } from 'socket.io';
import { Server as HttpServer } from 'http';
import { logger } from '../utils/logger';

let io: SocketIOServer | null = null;
const onlineFarmers = new Set<string>();

export function initSocket(server: HttpServer, allowedOrigins: string[]): SocketIOServer {
  io = new SocketIOServer(server, {
    cors: {
      origin: '*',
      methods: ['GET', 'POST'],
    },
  });

  io.on('connection', (socket: Socket) => {
    logger.info(`Socket connected: ${socket.id}`);

    socket.on('join:admin', () => {
      socket.join('admin_channel');
      logger.info(`Socket ${socket.id} joined admin_channel`);
    });

    socket.on('join:centre', (centreId: number | string) => {
      socket.join(`centre_${centreId}`);
      logger.info(`Socket ${socket.id} joined centre_${centreId}`);
    });

    socket.on('join:farmer', (farmerId: number | string) => {
      socket.join(`user_${farmerId}`);
      onlineFarmers.add(String(farmerId));
      emitToAdmin('stats:farmer_online', { onlineCount: onlineFarmers.size });
    });

    socket.on('disconnect', () => {
      logger.info(`Socket disconnected: ${socket.id}`);
      // Remove farmer if present
      for (const room of socket.rooms) {
        if (room.startsWith('user_')) {
          const fid = room.replace('user_', '');
          onlineFarmers.delete(fid);
          emitToAdmin('stats:farmer_online', { onlineCount: onlineFarmers.size });
        }
      }
    });
  });

  return io;
}

export function getSocketClientCount(): number {
  if (!io) return 0;
  return io.engine.clientsCount || 0;
}

export function getOnlineFarmersCount(): number {
  return onlineFarmers.size;
}

export function emitToAdmin(event: string, data: any) {
  if (io) {
    io.to('admin_channel').emit(event, data);
    io.emit(event, data); // also broadcast globally for simple clients
  }
}

export function emitToCentre(centreId: number | string, event: string, data: any) {
  if (io) {
    io.to(`centre_${centreId}`).emit(event, data);
    emitToAdmin(event, { centreId, ...data });
  }
}

export function emitToUser(userId: number | string, event: string, data: any) {
  if (io) {
    io.to(`user_${userId}`).emit(event, data);
  }
}
