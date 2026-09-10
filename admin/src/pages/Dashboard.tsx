import React, { useEffect, useState } from 'react';
import {
  Users,
  CalendarCheck,
  Clock,
  CheckCircle2,
  Hourglass,
  IndianRupee,
  Activity,
  AlertTriangle,
  Building2,
  RefreshCw,
  TrendingUp,
} from 'lucide-react';
import { api } from '../api/client';
import { useSocket } from '../context/SocketContext';

export const DashboardPage: React.FC = () => {
  const { socket } = useSocket();
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [lastRefreshed, setLastRefreshed] = useState<Date>(new Date());

  const fetchDashboard = async () => {
    try {
      const res = await api.get('/api/admin/dashboard');
      if (res.data.success) {
        setData(res.data.data);
        setLastRefreshed(new Date());
      }
    } catch (err) {
      console.error('Failed to load dashboard metrics', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboard();

    // Set up Socket.IO live listeners (Requirement 25)
    if (socket) {
      socket.on('stats:refresh', fetchDashboard);
      socket.on('queue:update', fetchDashboard);
      socket.on('procurement:new', fetchDashboard);
      socket.on('booking:new', fetchDashboard);
      socket.on('stats:farmer_online', fetchDashboard);
    }

    const interval = setInterval(fetchDashboard, 15000); // 15s fallback poll
    return () => {
      clearInterval(interval);
      if (socket) {
        socket.off('stats:refresh', fetchDashboard);
        socket.off('queue:update', fetchDashboard);
        socket.off('procurement:new', fetchDashboard);
        socket.off('booking:new', fetchDashboard);
        socket.off('stats:farmer_online', fetchDashboard);
      }
    };
  }, [socket]);

  const counts = data?.counts || {};
  const errorCounts = data?.errorCounts || {};

  return (
    <div className="space-y-6">
      {/* Top Banner with Title and Refresh */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-white">SMART PROCUREMENT CONTROL PANEL</h1>
          <p className="text-xs text-slate-400">Live operational command center and multi-centre telemetry</p>
        </div>
        <div className="flex items-center gap-3">
          <span className="text-xs text-slate-500">
            Updated: {lastRefreshed.toLocaleTimeString()}
          </span>
          <button
            onClick={fetchDashboard}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-semibold transition-colors"
          >
            <RefreshCw className={`h-3.5 w-3.5 ${loading ? 'animate-spin' : ''}`} />
            <span>Refresh</span>
          </button>
        </div>
      </div>

      {/* Main KPI Stat Cards (Requirement 3) */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Farmers Online</span>
            <span className="h-2 w-2 rounded-full bg-emerald-400 animate-pulse"></span>
          </div>
          <p className="text-3xl font-black text-white mt-2">{counts.farmersOnline || 0}</p>
          <p className="text-[11px] text-emerald-400 mt-1 flex items-center gap-1">
            <Users className="h-3 w-3" /> Live connected app users
          </p>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Active Bookings</span>
            <CalendarCheck className="h-4 w-4 text-sky-400" />
          </div>
          <p className="text-3xl font-black text-sky-400 mt-2">{counts.activeBookings || 0}</p>
          <p className="text-[11px] text-slate-400 mt-1">Confirmed for today</p>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Active Queue</span>
            <Clock className="h-4 w-4 text-amber-400" />
          </div>
          <p className="text-3xl font-black text-amber-400 mt-2">{counts.activeQueue || 0}</p>
          <p className="text-[11px] text-slate-400 mt-1">Waiting at yards</p>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Processing</span>
            <Hourglass className="h-4 w-4 text-purple-400 animate-spin" />
          </div>
          <p className="text-3xl font-black text-purple-400 mt-2">{counts.processing || 0}</p>
          <p className="text-[11px] text-slate-400 mt-1">Weighment & quality inspection</p>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Completed Today</span>
            <CheckCircle2 className="h-4 w-4 text-emerald-400" />
          </div>
          <p className="text-3xl font-black text-emerald-400 mt-2">{counts.completedToday || 0}</p>
          <p className="text-[11px] text-slate-400 mt-1">Procurement slips cleared</p>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Average Wait</span>
            <Clock className="h-4 w-4 text-slate-400" />
          </div>
          <p className="text-3xl font-black text-white mt-2">{counts.averageWaitMin || 0} <span className="text-base font-normal text-slate-400">min</span></p>
          <p className="text-[11px] text-slate-400 mt-1">Check-in to counter call</p>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Average Processing</span>
            <Activity className="h-4 w-4 text-slate-400" />
          </div>
          <p className="text-3xl font-black text-white mt-2">{counts.averageProcessingMin || 0} <span className="text-base font-normal text-slate-400">min</span></p>
          <p className="text-[11px] text-slate-400 mt-1">Inspection & weighment</p>
        </div>

        <div className="bg-emerald-950/40 border border-emerald-800/40 p-5 rounded-2xl">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-emerald-400 uppercase tracking-wider">Today's Procurement</span>
            <IndianRupee className="h-4 w-4 text-emerald-400" />
          </div>
          <p className="text-3xl font-black text-emerald-400 mt-2">
            ₹{Number(counts.todayProcurement || 0).toLocaleString('en-IN')}
          </p>
          <p className="text-[11px] text-emerald-300 mt-1 flex items-center gap-1">
            <TrendingUp className="h-3 w-3" /> {counts.todayQuintals || 0} Quintals cleared
          </p>
        </div>
      </div>

      {/* Two Column Layout: Centres Live Queue + Unresolved Errors */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Centres Live Queue Table */}
        <div className="lg:col-span-2 bg-slate-900 border border-slate-800 rounded-2xl p-6">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2">
              <Building2 className="h-5 w-5 text-emerald-400" />
              <h2 className="text-base font-bold text-white">PROCUREMENT CENTRES QUEUE OVERVIEW</h2>
            </div>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="text-slate-400 border-b border-slate-800 font-semibold uppercase tracking-wider">
                <tr>
                  <th className="pb-3">Centre</th>
                  <th className="pb-3">Status</th>
                  <th className="pb-3 text-center">Waiting</th>
                  <th className="pb-3 text-center">Processing</th>
                  <th className="pb-3 text-center">Completed</th>
                  <th className="pb-3 text-right">Daily Cap</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 font-medium">
                {data?.centres?.map((c: any) => (
                  <tr key={c.id} className="hover:bg-slate-800/30 transition-colors">
                    <td className="py-3.5 pr-3">
                      <p className="font-bold text-white">{c.name}</p>
                      <p className="text-[11px] text-slate-400">{c.code}</p>
                    </td>
                    <td className="py-3.5">
                      <span className={`px-2 py-0.5 rounded-full text-[10px] font-semibold border ${
                        c.status === 'OPEN'
                          ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                          : 'bg-amber-500/10 text-amber-400 border-amber-500/20'
                      }`}>
                        {c.status}
                      </span>
                    </td>
                    <td className="py-3.5 text-center font-bold text-amber-400">{c.waiting_count || 0}</td>
                    <td className="py-3.5 text-center font-bold text-purple-400">{c.processing_count || 0}</td>
                    <td className="py-3.5 text-center font-bold text-emerald-400">{c.completed_count || 0}</td>
                    <td className="py-3.5 text-right text-slate-300 font-semibold">{c.daily_capacity}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* System Error Telemetry Counters (Requirement 9) */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <AlertTriangle className="h-5 w-5 text-rose-400" />
              <h2 className="text-base font-bold text-white">ERROR SEVERITY</h2>
            </div>
            <span className="text-[10px] px-2 py-0.5 rounded bg-slate-800 text-slate-400 uppercase font-semibold">
              Live DB
            </span>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div className="bg-rose-950/30 border border-rose-800/30 p-3.5 rounded-xl">
              <span className="text-[11px] font-bold text-rose-400 uppercase tracking-wider block">CRITICAL</span>
              <p className="text-2xl font-black text-rose-300 mt-1">{errorCounts.critical || 0}</p>
            </div>
            <div className="bg-red-950/20 border border-red-800/30 p-3.5 rounded-xl">
              <span className="text-[11px] font-bold text-red-400 uppercase tracking-wider block">ERROR</span>
              <p className="text-2xl font-black text-red-300 mt-1">{errorCounts.error || 0}</p>
            </div>
            <div className="bg-amber-950/20 border border-amber-800/30 p-3.5 rounded-xl">
              <span className="text-[11px] font-bold text-amber-400 uppercase tracking-wider block">WARNING</span>
              <p className="text-2xl font-black text-amber-300 mt-1">{errorCounts.warning || 0}</p>
            </div>
            <div className="bg-sky-950/20 border border-sky-800/30 p-3.5 rounded-xl">
              <span className="text-[11px] font-bold text-sky-400 uppercase tracking-wider block">INFO</span>
              <p className="text-2xl font-black text-sky-300 mt-1">{errorCounts.info || 0}</p>
            </div>
          </div>

          <div className="p-3.5 bg-slate-950/80 rounded-xl border border-slate-800 space-y-1.5 text-xs text-slate-400">
            <p className="font-semibold text-slate-200">Central Error Observer</p>
            <p className="text-[11px] leading-relaxed">
              Diagnostic crashes from Android APK and Backend exceptions are intercepted, stamped with unique <code className="text-emerald-400">ERR-XXXX</code> IDs, and auto-grouped in the error manager.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
