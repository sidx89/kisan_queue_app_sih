import http from 'http';
import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import path from 'path';
import { ENV } from './config/env';
import { testDbConnection } from './config/db';
import { logger } from './utils/logger';
import { traceMiddleware } from './middleware/trace';
import { maintenanceMiddleware } from './middleware/maintenance';
import { centralErrorHandler } from './middleware/errorHandler';
import { initSocket } from './services/socket.service';
import { router } from './routes/api.routes';

const app = express();
const server = http.createServer(app);

// Initialize Socket.IO
initSocket(server, ENV.CORS_ORIGINS);

// Standard Security & Body Parsers
app.use(helmet({ contentSecurityPolicy: false }));
app.use(
  cors({
    origin: '*', // Allow all origins for mobile APK & tunnel support
    methods: ['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'OPTIONS'],
    allowedHeaders: ['Content-Type', 'Authorization', 'X-Request-Id'],
  })
);

app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

// Static file storage for uploads
app.use('/uploads', express.static(ENV.UPLOAD_DIR));

// Trace & Performance Middleware
app.use(traceMiddleware);

// Maintenance Mode Interceptor
app.use(maintenanceMiddleware);

// Main API Routes
app.use(router);

// 404 Handler
app.use((req, res) => {
  res.status(404).json({
    success: false,
    message: `Endpoint '${req.method} ${req.originalUrl}' not found.`,
    requestId: req.requestId,
  });
});

// Central Error Handler
app.use(centralErrorHandler);

// Server Startup
async function startServer() {
  logger.info('========================================');
  logger.info(' SMART FARMER PROCUREMENT BACKEND');
  logger.info('========================================');

  // Test DB connection
  const dbHealth = await testDbConnection();
  if (dbHealth.connected) {
    logger.info(`[OK] MySQL Database connected on ${ENV.DB.HOST}:${ENV.DB.PORT}/${ENV.DB.NAME} (${dbHealth.latencyMs}ms)`);
  } else {
    logger.error(`[FAIL] Could not connect to MySQL: ${dbHealth.error}`);
    logger.warn('Check that XAMPP MySQL is running on port ' + ENV.DB.PORT);
  }

  server.listen(ENV.PORT, '0.0.0.0', () => {
    logger.info(`[OK] HTTP Server listening on http://0.0.0.0:${ENV.PORT}`);
    logger.info(`[OK] Socket.IO real-time engine running`);
    logger.info(`[OK] Health check: http://localhost:${ENV.PORT}/health`);
    logger.info('========================================');
  });
}

// Global Exception Safety (Requirement 29, 30)
process.on('uncaughtException', (err) => {
  logger.error('CRITICAL UNCAUGHT EXCEPTION:', err);
});

process.on('unhandledRejection', (reason, promise) => {
  logger.error('UNHANDLED PROMISE REJECTION:', reason as any);
});

startServer().catch((err) => {
  logger.error('FATAL ERROR DURING SERVER STARTUP:', err);
  process.exit(1);
});
