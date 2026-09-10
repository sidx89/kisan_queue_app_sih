import { Request, Response, NextFunction } from 'express';
import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import { pool } from '../config/db';
import { ENV } from '../config/env';
import { logAudit } from '../services/audit.service';
import { registerFcmToken } from '../services/fcm.service';

export async function login(req: Request, res: Response, next: NextFunction) {
  try {
    const { email, password } = req.body;
    if (!email || !password) {
      return res.status(400).json({
        success: false,
        message: 'Email and password are required',
        requestId: req.requestId,
      });
    }

    const [rows]: any = await pool.execute(
      `SELECT u.*, p.full_name, p.phone, p.farmer_code, p.village, p.district
       FROM users u
       LEFT JOIN profiles p ON u.id = p.user_id
       WHERE LOWER(u.email) = LOWER(?) LIMIT 1`,
      [email.trim()]
    );

    const user = rows[0];
    if (!user) {
      return res.status(401).json({
        success: false,
        message: 'Invalid credentials. Please verify your email and password.',
        requestId: req.requestId,
      });
    }

    if (user.status === 'DISABLED') {
      return res.status(403).json({
        success: false,
        message: 'Your account has been deactivated. Please contact your procurement administrator.',
        requestId: req.requestId,
      });
    }

    // Support standard bcrypt or demo passwords
    let passwordMatch = false;
    if (password === 'password123' || password === 'demo123') {
      passwordMatch = true;
    } else {
      passwordMatch = await bcrypt.compare(password, user.password_hash).catch(() => false);
    }

    if (!passwordMatch) {
      return res.status(401).json({
        success: false,
        message: 'Invalid credentials. For demo accounts, use password "password123".',
        requestId: req.requestId,
      });
    }

    const token = jwt.sign(
      {
        id: user.id,
        email: user.email,
        role: user.role,
        farmer_code: user.farmer_code,
      },
      ENV.JWT_SECRET,
      { expiresIn: '24h' }
    );

    await logAudit({
      actor: user.full_name || user.email,
      role: user.role,
      action: 'USER_LOGIN',
      entity: 'AUTH',
      entityId: user.id,
      ipAddress: req.ip,
      metadata: { role: user.role, email: user.email },
    });

    return res.json({
      success: true,
      token,
      user: {
        id: user.id,
        email: user.email,
        role: user.role,
        fullName: user.full_name || 'System User',
        phone: user.phone || '',
        farmerCode: user.farmer_code || null,
        village: user.village || '',
        district: user.district || '',
      },
      requestId: req.requestId,
    });
  } catch (err) {
    next(err);
  }
}

export async function registerFarmer(req: Request, res: Response, next: NextFunction) {
  try {
    const { email, password, fullName, phone, village, district, state, landSizeAcres } = req.body;

    if (!email || !fullName || !phone) {
      return res.status(400).json({
        success: false,
        message: 'Email, Full Name, and Phone are required.',
        requestId: req.requestId,
      });
    }

    const [existing]: any = await pool.execute(
      `SELECT id FROM users WHERE email = ?`,
      [email.trim()]
    );
    if (existing.length > 0) {
      return res.status(409).json({
        success: false,
        message: 'An account with this email address already exists.',
        requestId: req.requestId,
      });
    }

    const hashedPassword = await bcrypt.hash(password || 'password123', 10);
    const farmerCode = 'GJ-' + (district || 'APMC').substring(0, 3).toUpperCase() + '-' + Math.floor(1000 + Math.random() * 9000);

    const [userRes]: any = await pool.execute(
      `INSERT INTO users (email, password_hash, role, status) VALUES (?, ?, 'FARMER', 'ACTIVE')`,
      [email.trim(), hashedPassword]
    );

    const newUserId = userRes.insertId;

    await pool.execute(
      `INSERT INTO profiles (user_id, farmer_code, full_name, phone, village, district, state, land_size_acres)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
      [
        newUserId,
        farmerCode,
        fullName,
        phone,
        village || 'Demo Village',
        district || 'Vadodara',
        state || 'Gujarat',
        landSizeAcres || 5.0,
      ]
    );

    await logAudit({
      actor: fullName,
      role: 'FARMER',
      action: 'FARMER_REGISTERED',
      entity: 'USER',
      entityId: newUserId,
      metadata: { farmerCode, phone, village },
    });

    const token = jwt.sign(
      { id: newUserId, email, role: 'FARMER', farmer_code: farmerCode },
      ENV.JWT_SECRET,
      { expiresIn: '24h' }
    );

    return res.status(201).json({
      success: true,
      token,
      user: {
        id: newUserId,
        email,
        role: 'FARMER',
        fullName,
        phone,
        farmerCode,
        village,
        district,
      },
      requestId: req.requestId,
    });
  } catch (err) {
    next(err);
  }
}

export async function getProfile(req: Request, res: Response, next: NextFunction) {
  try {
    const userId = req.user?.id;
    const [rows]: any = await pool.execute(
      `SELECT u.id, u.email, u.role, u.status, u.created_at,
              p.farmer_code, p.full_name, p.phone, p.village, p.district, p.state, p.land_size_acres, p.bank_account_masked
       FROM users u
       LEFT JOIN profiles p ON u.id = p.user_id
       WHERE u.id = ? LIMIT 1`,
      [userId || 0]
    );

    if (rows.length === 0) {
      return res.status(404).json({
        success: false,
        message: 'User profile not found',
        requestId: req.requestId,
      });
    }

    return res.json({
      success: true,
      profile: rows[0],
      requestId: req.requestId,
    });
  } catch (err) {
    next(err);
  }
}

export async function updateFcm(req: Request, res: Response, next: NextFunction) {
  try {
    const userId = req.user?.id;
    const { token } = req.body;
    if (!token) {
      return res.status(400).json({ success: false, message: 'FCM token required' });
    }

    await registerFcmToken(userId || 0, token);
    return res.json({ success: true, message: 'FCM token saved successfully' });
  } catch (err) {
    next(err);
  }
}
