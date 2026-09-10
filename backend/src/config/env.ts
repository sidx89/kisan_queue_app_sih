import dotenv from 'dotenv';
import path from 'path';

dotenv.config({ path: path.resolve(__dirname, '../../.env') });

export const ENV = {
  PORT: parseInt(process.env.PORT || '5000', 10),
  NODE_ENV: process.env.NODE_ENV || 'development',
  DB: {
    HOST: process.env.DB_HOST || 'localhost',
    PORT: parseInt(process.env.DB_PORT || '3306', 10),
    USER: process.env.DB_USER || 'root',
    PASSWORD: process.env.DB_PASSWORD || '',
    NAME: process.env.DB_NAME || 'smart_procurement',
  },
  JWT_SECRET: process.env.JWT_SECRET || 'kisan_smart_procure_jwt_super_secret_key_2026_secure',
  JWT_EXPIRES_IN: process.env.JWT_EXPIRES_IN || '24h',
  UPLOAD_DIR: path.resolve(__dirname, '../../uploads'),
  CORS_ORIGINS: (process.env.CORS_ORIGINS || 'http://localhost:3000,http://127.0.0.1:3000').split(','),
  FCM_SERVER_KEY: process.env.FCM_SERVER_KEY || 'mock_demo_key',
};
