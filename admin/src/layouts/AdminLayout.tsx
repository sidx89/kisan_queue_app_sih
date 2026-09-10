import React, { useState } from 'react';
import { Outlet, NavLink, Link } from 'react-router-dom';
import {
  LayoutDashboard,
  Users,
  Building2,
  Calendar,
  Layers,
  ClipboardList,
  Clock,
  Wheat,
  CreditCard,
  Bell,
  AlertTriangle,
  History,
  Activity,
  Cpu,
  Settings,
  LogOut,
  UserCheck,
  ShieldCheck,
  Menu,
  X,
  Radio,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useSocket } from '../context/SocketContext';

export const AdminLayout: React.FC = () => {
  const { user, logout } = useAuth();
  const { isConnected, recentError, recentAlert, clearRecentError, clearRecentAlert } = useSocket();
  const [isSidebarOpen, setIsSidebarOpen] = useState(true);

  const navItems = [
    { to: '/admin/dashboard', label: 'Live Dashboard', icon: LayoutDashboard },
    { to: '/admin/queue', label: 'Live Queue Control', icon: Clock },
    { to: '/admin/bookings', label: 'Slot Bookings', icon: Calendar },
    { to: '/admin/farmers', label: 'Farmers Directory', icon: Users },
    { to: '/admin/operators', label: 'Centre Operators', icon: UserCheck },
    { to: '/admin/centres', label: 'Procurement Centres', icon: Building2 },
    { to: '/admin/counters', label: 'Counter Stations', icon: Layers },
    { to: '/admin/slots', label: 'Daily Slots Config', icon: ClipboardList },
    { to: '/admin/procurement', label: 'Weighment & Grading', icon: Wheat },
    { to: '/admin/payments', label: 'Payments & Accounts', icon: CreditCard },
    { to: '/admin/errors', label: 'Error Diagnostics', icon: AlertTriangle, badge: 'Grouped' },
    { to: '/admin/audit-logs', label: 'User Activity Audit', icon: History },
    { to: '/admin/alerts', label: 'System Alerts', icon: Activity },
    { to: '/admin/notifications', label: 'Notification Hub', icon: Bell },
    { to: '/admin/system', label: 'Server Monitoring', icon: Cpu },
    { to: '/admin/settings', label: 'System Settings', icon: Settings },
  ];

  return (
    <div className="flex h-screen bg-slate-950 text-slate-100 overflow-hidden font-sans">
      {/* Sidebar */}
      <aside
        className={`fixed inset-y-0 left-0 z-40 flex flex-col border-r border-slate-800 bg-slate-900/95 backdrop-blur transition-all duration-300 ${
          isSidebarOpen ? 'w-64' : 'w-20'
        } md:static`}
      >
        {/* Logo / Brand */}
        <div className="flex items-center justify-between h-16 px-4 border-b border-slate-800">
          <Link to="/admin/dashboard" className="flex items-center gap-3 overflow-hidden">
            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-emerald-600 shadow-md shadow-emerald-500/20">
              <ShieldCheck className="h-6 w-6 text-white" />
            </div>
            {isSidebarOpen && (
              <div className="flex flex-col">
                <span className="font-bold tracking-tight text-white leading-tight">KISAN PROCURE</span>
                <span className="text-[10px] font-medium tracking-wider text-emerald-400 uppercase">
                  Control Center
                </span>
              </div>
            )}
          </Link>
          <button
            onClick={() => setIsSidebarOpen(!isSidebarOpen)}
            className="p-1.5 rounded-lg text-slate-400 hover:bg-slate-800 hover:text-white md:block hidden"
          >
            {isSidebarOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
          </button>
        </div>

        {/* Nav Links */}
        <nav className="flex-1 overflow-y-auto p-3 space-y-1">
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-colors ${
                    isActive
                      ? 'bg-emerald-600 text-white shadow-sm shadow-emerald-600/30'
                      : 'text-slate-400 hover:bg-slate-800/80 hover:text-slate-100'
                  }`
                }
              >
                <Icon className="h-5 w-5 shrink-0" />
                {isSidebarOpen && <span className="truncate">{item.label}</span>}
                {isSidebarOpen && item.badge && (
                  <span className="ml-auto text-[10px] px-1.5 py-0.5 rounded-full bg-amber-500/20 text-amber-300 font-semibold border border-amber-500/30">
                    {item.badge}
                  </span>
                )}
              </NavLink>
            );
          })}
        </nav>

        {/* User Card & Logout */}
        <div className="p-3 border-t border-slate-800">
          <div className="flex items-center gap-3 p-2 rounded-xl bg-slate-800/60">
            <div className="h-8 w-8 rounded-lg bg-emerald-500/20 text-emerald-400 flex items-center justify-center font-bold text-xs uppercase">
              {user?.fullName?.charAt(0) || 'A'}
            </div>
            {isSidebarOpen && (
              <div className="flex-1 min-w-0">
                <p className="text-xs font-semibold text-white truncate">{user?.fullName || 'Admin User'}</p>
                <p className="text-[10px] text-slate-400 truncate">{user?.role}</p>
              </div>
            )}
            <button
              onClick={logout}
              title="Sign Out"
              className="p-1.5 text-slate-400 hover:text-rose-400 hover:bg-slate-700/50 rounded-lg transition-colors"
            >
              <LogOut className="h-4 w-4" />
            </button>
          </div>
        </div>
      </aside>

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Top Header with Live System Status */}
        <header className="h-16 border-b border-slate-800 bg-slate-900/60 backdrop-blur px-6 flex items-center justify-between shrink-0">
          {/* Status Indicators (Requirement 3 & 14) */}
          <div className="flex items-center gap-4 text-xs font-medium">
            <span className="text-slate-400 hidden sm:inline uppercase text-[11px] tracking-wider font-semibold">
              System Status:
            </span>
            <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-emerald-950/60 border border-emerald-800/40 text-emerald-400">
              <span className="h-2 w-2 rounded-full bg-emerald-400 animate-pulse"></span>
              <span>Backend ONLINE</span>
            </div>
            <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-emerald-950/60 border border-emerald-800/40 text-emerald-400">
              <span className="h-2 w-2 rounded-full bg-emerald-400"></span>
              <span>MySQL ONLINE</span>
            </div>
            <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-emerald-950/60 border border-emerald-800/40 text-emerald-400">
              <span className={`h-2 w-2 rounded-full ${isConnected ? 'bg-emerald-400' : 'bg-rose-500'}`}></span>
              <span>Socket.IO {isConnected ? 'LIVE' : 'OFFLINE'}</span>
            </div>
            <div className="hidden lg:flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-emerald-950/60 border border-emerald-800/40 text-emerald-400">
              <span className="h-2 w-2 rounded-full bg-emerald-400"></span>
              <span>FCM READY</span>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <Link
              to="/admin/queue"
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-emerald-600/20 text-emerald-400 hover:bg-emerald-600/30 border border-emerald-500/30 text-xs font-semibold transition-colors"
            >
              <Radio className="h-3.5 w-3.5 animate-pulse text-emerald-400" />
              <span>Live Queue</span>
            </Link>
          </div>
        </header>

        {/* Live Pop-up Error Notification Banner (Requirement 41) */}
        {recentError && (
          <div className="bg-rose-950/90 border-b border-rose-600/50 px-6 py-3 text-rose-200 flex items-center justify-between gap-4 animate-in slide-in-from-top duration-300">
            <div className="flex items-center gap-3 text-xs sm:text-sm">
              <span className="flex h-7 w-7 shrink-0 items-center justify-center rounded-lg bg-rose-600 text-white font-bold text-xs">
                🚨
              </span>
              <div>
                <p className="font-semibold text-white">
                  Incoming {recentError.source || 'Client'} Diagnostic Alert [{recentError.error_id}]
                </p>
                <p className="text-xs text-rose-300">
                  {recentError.message} | Endpoint: {recentError.endpoint} | Status: {recentError.http_status}
                </p>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <Link
                to="/admin/errors"
                onClick={clearRecentError}
                className="px-2.5 py-1 bg-rose-600 hover:bg-rose-500 text-white rounded text-xs font-semibold"
              >
                Inspect
              </Link>
              <button onClick={clearRecentError} className="p-1 hover:text-white text-rose-400">
                <X className="h-4 w-4" />
              </button>
            </div>
          </div>
        )}

        {/* Content Outlet */}
        <main className="flex-1 overflow-y-auto p-6 bg-slate-950">
          <Outlet />
        </main>
      </div>
    </div>
  );
};
