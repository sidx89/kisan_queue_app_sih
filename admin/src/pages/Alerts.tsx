import React, { useEffect, useState } from 'react';
import { Activity, AlertTriangle, CheckCircle, RefreshCw } from 'lucide-react';
import { api } from '../api/client';

export const AlertsPage: React.FC = () => {
  const [alerts, setAlerts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchAlerts = async () => {
    setLoading(true);
    try {
      const res = await api.get('/api/admin/alerts');
      if (res.data.success) {
        setAlerts(res.data.alerts);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAlerts();
  }, []);

  const handleStatus = async (id: number, status: string) => {
    try {
      await api.patch(`/api/admin/alerts/${id}`, { status });
      fetchAlerts();
    } catch (err) {
      alert('Failed to update alert');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-white">CRITICAL SYSTEM ALERTS</h1>
          <p className="text-xs text-slate-400">
            Real-time threshold surveillance: DB disconnects, queue surges, and error spikes (Requirement 18)
          </p>
        </div>
        <button onClick={fetchAlerts} className="p-2 bg-slate-800 hover:bg-slate-700 rounded-xl text-slate-300">
          <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
        </button>
      </div>

      <div className="space-y-3">
        {alerts.map((a) => (
          <div
            key={a.id}
            className={`p-5 rounded-2xl border flex items-center justify-between gap-4 ${
              a.severity === 'CRITICAL'
                ? 'bg-rose-950/30 border-rose-800/40 text-rose-200'
                : 'bg-amber-950/20 border-amber-800/40 text-amber-200'
            }`}
          >
            <div className="flex items-center gap-3.5">
              <span className="p-2.5 rounded-xl bg-slate-900 border border-slate-800">
                <AlertTriangle className={`h-5 w-5 ${a.severity === 'CRITICAL' ? 'text-rose-400' : 'text-amber-400'}`} />
              </span>
              <div>
                <div className="flex items-center gap-2">
                  <span className="font-bold text-white text-sm">{a.title}</span>
                  <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-slate-900 border border-slate-700">
                    {a.alert_type}
                  </span>
                </div>
                <p className="text-xs text-slate-400 mt-1">{a.description}</p>
                <span className="text-[10px] text-slate-500 mt-1 block">
                  {new Date(a.timestamp).toLocaleString()}
                </span>
              </div>
            </div>

            <div className="flex items-center gap-2 shrink-0">
              {a.status === 'ACTIVE' && (
                <button
                  onClick={() => handleStatus(a.id, 'RESOLVED')}
                  className="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-xs font-semibold"
                >
                  Acknowledge & Resolve
                </button>
              )}
              {a.status === 'RESOLVED' && (
                <span className="px-3 py-1 rounded-xl bg-emerald-950/60 text-emerald-400 text-xs font-semibold border border-emerald-800/40">
                  Resolved
                </span>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
