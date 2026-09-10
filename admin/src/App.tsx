import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { SocketProvider } from './context/SocketContext';
import { AdminLayout } from './layouts/AdminLayout';

import { LoginPage } from './pages/Login';
import { DashboardPage } from './pages/Dashboard';
import { QueuePage } from './pages/Queue';
import { BookingsPage } from './pages/Bookings';
import { FarmersPage } from './pages/Farmers';
import { OperatorsPage } from './pages/Operators';
import { CentresPage } from './pages/Centres';
import { CountersPage } from './pages/Counters';
import { SlotsPage } from './pages/Slots';
import { ProcurementPage } from './pages/Procurement';
import { PaymentsPage } from './pages/Payments';
import { ErrorsPage } from './pages/Errors';
import { AuditLogsPage } from './pages/AuditLogs';
import { AlertsPage } from './pages/Alerts';
import { NotificationsPage } from './pages/Notifications';
import { SystemMonitorPage } from './pages/SystemMonitor';
import { SettingsPage } from './pages/Settings';

const ProtectedRoute: React.FC<{ children: React.ReactElement }> = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();
  if (isLoading) {
    return <div className="min-h-screen bg-slate-950 flex items-center justify-center text-slate-400">Loading portal...</div>;
  }
  if (!isAuthenticated) {
    return <Navigate to="/admin/login" replace />;
  }
  return children;
};

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <SocketProvider>
          <Routes>
            <Route path="/admin/login" element={<LoginPage />} />

            <Route
              path="/admin"
              element={
                <ProtectedRoute>
                  <AdminLayout />
                </ProtectedRoute>
              }
            >
              <Route index element={<Navigate to="/admin/dashboard" replace />} />
              <Route path="dashboard" element={<DashboardPage />} />
              <Route path="queue" element={<QueuePage />} />
              <Route path="bookings" element={<BookingsPage />} />
              <Route path="farmers" element={<FarmersPage />} />
              <Route path="operators" element={<OperatorsPage />} />
              <Route path="centres" element={<CentresPage />} />
              <Route path="counters" element={<CountersPage />} />
              <Route path="slots" element={<SlotsPage />} />
              <Route path="procurement" element={<ProcurementPage />} />
              <Route path="payments" element={<PaymentsPage />} />
              <Route path="errors" element={<ErrorsPage />} />
              <Route path="audit-logs" element={<AuditLogsPage />} />
              <Route path="alerts" element={<AlertsPage />} />
              <Route path="notifications" element={<NotificationsPage />} />
              <Route path="system" element={<SystemMonitorPage />} />
              <Route path="settings" element={<SettingsPage />} />
            </Route>

            <Route path="*" element={<Navigate to="/admin/dashboard" replace />} />
          </Routes>
        </SocketProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}
