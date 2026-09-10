import React, { useEffect, useState } from 'react';
import { Cpu, HardDrive, Clock, Activity, Database, Radio, RefreshCw, Server, AlertCircle } from 'lucide-react';
import { api } from '../api/client';

export const SystemMonitorPage: React.FC = () => {
  const [metrics, setMetrics] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  const fetchMetrics = async () => {
    try {
      const res = await api.get('/api/admin/system/metrics');
      if (res.data.success) {
        setMetrics(res.data.metrics);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMetrics();
    const interval = setInterval(fetchMetrics, 3000); // 3-second live heartbeat
    return () => clearInterval(interval);
  }, []);

  const formatUptime = (seconds = 0) => {
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    const s = Math.floor(seconds % 60);
    return `${String(h).padStart(2, '0')}h ${String(m).padStart(2, '0')}m ${String(s).padStart(2, '0')}s`;
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-white">SERVER HARDWARE & RUNTIME TELEMETRY</h1>
          <p className="text-xs text-slate-400">
            Real-time Node.js process load, MySQL pool health, and Socket.IO connection metrics (Requirement 4)
          </p>
        </div>

        <button
          onClick={fetchMetrics}
          className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-semibold"
        >
          <RefreshCw className={`h-3.5 w-3.5 ${loading ? 'animate-spin' : ''}`} />
          <span>Refresh</span>
        </button>
      </div>

      {/* Main Hardware Gauges */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        {/* Uptime */}
        <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Server Uptime</span>
            <Clock className="h-4 w-4 text-emerald-400" />
          </div>
          <p className="text-2xl font-black text-white mt-2 font-mono">{formatUptime(metrics?.uptimeSeconds)}</p>
          <p className="text-[11px] text-emerald-400 mt-1">Continuous daemon session</p>
        </div>

        {/* CPU */}
        <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">CPU Utilization</span>
            <Cpu className="h-4 w-4 text-sky-400" />
          </div>
          <p className="text-2xl font-black text-sky-400 mt-2 font-mono">{metrics?.cpuPercent || 0}%</p>
          <div className="w-full bg-slate-800 h-2 rounded-full mt-2 overflow-hidden">
            <div
              className="bg-sky-500 h-full rounded-full transition-all duration-500"
              style={{ width: `${metrics?.cpuPercent || 5}%` }}
            ></div>
          </div>
        </div>

        {/* Memory */}
        <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">RAM Memory</span>
            <HardDrive className="h-4 w-4 text-purple-400" />
          </div>
          <p className="text-2xl font-black text-purple-400 mt-2 font-mono">
            {metrics?.memory?.usedGb || 0} / {metrics?.memory?.totalGb || 0} GB
          </p>
          <p className="text-[11px] text-slate-400 mt-1">Node Heap: {metrics?.memory?.processMb || 0} MB</p>
        </div>

        {/* Average Response Time */}
        <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Avg Latency</span>
            <Activity className="h-4 w-4 text-emerald-400" />
          </div>
          <p className="text-2xl font-black text-white mt-2 font-mono">
            {metrics?.api?.averageResponseTimeMs || 0} <span className="text-base font-normal text-slate-400">ms</span>
          </p>
          <p className="text-[11px] text-emerald-400 mt-1">Rolling 100 API calls</p>
        </div>
      </div>

      {/* Subsystem Health Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* MySQL Database Card */}
        <div className="bg-slate-900 border border-slate-800 p-6 rounded-2xl space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Database className="h-5 w-5 text-emerald-400" />
              <h3 className="font-bold text-white text-sm">MySQL Database (XAMPP)</h3>
            </div>
            <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${
              metrics?.database?.connected ? 'bg-emerald-500/20 text-emerald-300' : 'bg-rose-500/20 text-rose-300'
            }`}>
              {metrics?.database?.connected ? 'CONNECTED' : 'DISCONNECTED'}
            </span>
          </div>

          <div className="space-y-2 text-xs">
            <div className="flex justify-between py-1 border-b border-slate-800">
              <span className="text-slate-400">Ping Response:</span>
              <span className="font-mono text-emerald-400 font-bold">{metrics?.database?.latencyMs} ms</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-800">
              <span className="text-slate-400">Database Name:</span>
              <span className="font-mono text-white">smart_procurement</span>
            </div>
            <div className="flex justify-between py-1">
              <span className="text-slate-400">Host / Port:</span>
              <span className="font-mono text-white">localhost:3306</span>
            </div>
          </div>
        </div>

        {/* API Traffic Card */}
        <div className="bg-slate-900 border border-slate-800 p-6 rounded-2xl space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Server className="h-5 w-5 text-sky-400" />
              <h3 className="font-bold text-white text-sm">HTTP Express Traffic</h3>
            </div>
            <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-sky-500/20 text-sky-300">
              ACTIVE
            </span>
          </div>

          <div className="space-y-2 text-xs">
            <div className="flex justify-between py-1 border-b border-slate-800">
              <span className="text-slate-400">Total API Invocations:</span>
              <span className="font-mono text-white font-bold">{metrics?.api?.totalRequests || 0}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-800">
              <span className="text-slate-400">Intercepted Errors:</span>
              <span className="font-mono text-rose-400 font-bold">{metrics?.api?.totalErrors || 0}</span>
            </div>
            <div className="flex justify-between py-1">
              <span className="text-slate-400">Trace Middleware:</span>
              <span className="font-mono text-emerald-400 font-bold">REQ-XXXXXXXX</span>
            </div>
          </div>
        </div>

        {/* Socket.IO Real-Time Card */}
        <div className="bg-slate-900 border border-slate-800 p-6 rounded-2xl space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Radio className="h-5 w-5 text-purple-400" />
              <h3 className="font-bold text-white text-sm">Socket.IO Engine</h3>
            </div>
            <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/20 text-emerald-300">
              ONLINE
            </span>
          </div>

          <div className="space-y-2 text-xs">
            <div className="flex justify-between py-1 border-b border-slate-800">
              <span className="text-slate-400">Connected Sockets:</span>
              <span className="font-mono text-purple-400 font-bold">{metrics?.socket?.connectedClients || 0}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-800">
              <span className="text-slate-400">Transport:</span>
              <span className="font-mono text-white">WebSocket / Polling</span>
            </div>
            <div className="flex justify-between py-1">
              <span className="text-slate-400">Admin Broadcast Room:</span>
              <span className="font-mono text-emerald-400 font-bold">admin_channel</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
