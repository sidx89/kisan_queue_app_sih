# 🌾 KisanProcure: Smart Farmer Procurement & Live Queue Management System

A production-grade, end-to-end Smart Agriculture Procurement & Virtual Queue Management platform engineered for Government Procurement Centers, APMC Mandis, Operators, and Farmers.

---

## 🏗 System Architecture

```text
┌─────────────────────────────────────────────────────────────┐
│                       KISANPROCURE                          │
└─────────────────────────────────────────────────────────────┘
                               │
       ┌───────────────────────┼───────────────────────┐
       ▼                       ▼                       ▼
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│ Android App  │       │  Admin Web   │       │   Operator   │
│(Jetpack M3)  │       │(React+Vite)  │       │   Station    │
└──────┬───────┘       └──────┬───────┘       └──────┬───────┘
       │                      │                      │
       │    REST + Socket.IO  │                      │
       └───────────────┬──────┴──────────────────────┘
                       ▼
       ┌──────────────────────────────┐
       │   Node.js / Express Server   │
       │  (TypeScript, Socket.IO)     │
       └──────────────┬───────────────┘
                      ▼
       ┌──────────────────────────────┐
       │   MySQL (smart_procurement)  │
       │   14 Relational Tables       │
       └──────────────────────────────┘
```

---

## 🚀 Components

### 1. Android Mobile Application (`android/`)
- **Modern Tech Stack**: Kotlin, Jetpack Compose, Material 3, MVVM Architecture, StateFlow, Coroutines.
- **Networking**: Retrofit 2 + OkHttp 4 with automatic request trace headers (`REQ-XXXXXXXX`).
- **Real-Time Live Queue**: Socket.IO client listening on room events for instantaneous queue advancements.
- **Offline Reliability**: SQLite cache with offline fallback.
- **Pass Verification**: ZXing barcode & QR Code generator displaying digital procurement passes.
- **Error Reporting**: Integrated uncaught crash and non-fatal error reporter sending reports directly to `/api/errors/report`.
- **Dynamic Config**: Runtime configurable API base URL (Local, Cloudflare Tunnel, Production).

### 2. Admin Web Control Panel (`admin/`)
- **Tech Stack**: React 18, TypeScript, Vite, Tailwind CSS, Lucide Icons, Recharts.
- **Live Monitoring**: Real-time Socket.IO connection for instant queue metrics, alerts, and crash warnings.
- **16 Functional Views**:
  - System Monitoring & Health Subsystems
  - Live Queue Roster & Counter Calls
  - Error Reporting & Stack Trace Inspector
  - Full Audit Trails with JSON metadata
  - Procurement & MSP Weighment Logging
  - Direct Benefit Transfer (DBT) Disbursement Tracking
  - Centre, Counter, and Slot Management
  - Push Notification & SMS Broadcasts
  - Maintenance Mode & System Settings

### 3. Backend REST & WebSocket Engine (`backend/`)
- **Runtime**: Node.js + Express 5 with TypeScript.
- **Database**: MySQL 8.0+ / MariaDB with connection pooling and transactions.
- **Real-Time Engine**: Socket.IO handling segmented room broadcasts (`admin_channel`, `centre_{id}`, `user_{id}`).
- **Observability**: Structured rotating logs (`app.log`, `error.log`, `access.log`) with sensitive PII masking.
- **Tracing**: Custom trace middleware attaching unique `REQ-XXXXXXXX` trace IDs and computing execution latency.

---

## 🔑 Demo Accounts & Credentials

All demo accounts use the standard password: **`password123`**

| Role | Email | Password | Access Level |
|---|---|---|---|
| **Super Admin** | `superadmin@kisan.gov.in` | `password123` | Full control of Admin Panel & System |
| **Procurement Operator** | `operator1@kisan.gov.in` | `password123` | Counter check-in, call next token, weighment |
| **Farmer (Ramesh)** | `ramesh@kisan.in` | `password123` | Mobile app slot booking, QR pass, live queue |
| **Farmer (Suresh)** | `suresh@kisan.in` | `password123` | Mobile app slot booking, QR pass, live queue |

---

## ⚡ Quick Start & Automation Scripts

The `scripts/` directory contains complete one-click automation batch files:

| Script | Purpose |
|---|---|
| `scripts\start-all.bat` | Starts MySQL, Backend, Admin Panel, and Cloudflare Tunnel |
| `scripts\stop-all.bat` | Gracefully stops all Node.js and tunnel processes |
| `scripts\diagnose.bat` | Runs automated environment, port, database, and health checks |
| `scripts\restart-backend.bat` | Recompiles TypeScript and restarts the backend server |
| `scripts\backup-db.bat` | Creates a timestamped `.sql` dump in `backups/` |
| `scripts\reset-demo.bat` | Drops and re-executes database schema and seed data |
| `scripts\build-apk.bat` | Builds the Android debug APK using the Gradle wrapper |
| `scripts\install-apk.bat` | Installs the APK onto a connected device/emulator via ADB |

---

## 🌐 API Overview

| Endpoint | Method | Description |
|---|---|---|
| `/health` | `GET` | Health check for DB, Socket.IO, and uptime |
| `/api/auth/login` | `POST` | Authenticates user and returns JWT token |
| `/api/centres` | `GET` | Retrieves active procurement centres and live queue statistics |
| `/api/centres/:id/slots` | `GET` | Returns available slots for a chosen centre |
| `/api/bookings` | `POST` | Atomically creates slot booking and assigns token |
| `/api/bookings/check-in`| `POST` | Checks in farmer at centre entrance |
| `/api/queue/:centreId` | `GET` | Returns live queue roster and estimated wait times |
| `/api/queue/:centreId/call-next` | `POST` | Calls next farmer to a counter |
| `/api/errors/report` | `POST` | Receives client-side crash and error reports |

---

## 📄 License
Government of India / Open Source Initiative. Built for Smart India Hackathon (SIH).
