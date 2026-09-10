import React, { useEffect, useState } from 'react';
import { Layers, Plus, RefreshCw, UserCheck } from 'lucide-react';
import { api } from '../api/client';

export const CountersPage: React.FC = () => {
  const [counters, setCounters] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchCounters = async () => {
    setLoading(true);
    try {
      const res = await api.get('/api/admin/counters');
      if (res.data.success) {
        setCounters(res.data.counters);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCounters();
  }, []);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-white">COUNTER STATIONS</h1>
          <p className="text-xs text-slate-400">
            Physical inspection, weighbridge, and billing counters across all APMC yards
          </p>
        </div>
        <button onClick={fetchCounters} className="p-2 bg-slate-800 hover:bg-slate-700 rounded-xl text-slate-300">
          <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
        </button>
      </div>

      <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden">
        <table className="w-full text-left text-xs">
          <thead className="bg-slate-950/60 text-slate-400 uppercase tracking-wider font-semibold border-b border-slate-800">
            <tr>
              <th className="p-4">Centre</th>
              <th className="p-4">Counter #</th>
              <th className="p-4">Assigned Operator</th>
              <th className="p-4">Current Serving Token</th>
              <th className="p-4">Status</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800/60 font-medium">
            {counters.map((c) => (
              <tr key={c.id} className="hover:bg-slate-800/30">
                <td className="p-4 font-bold text-white">{c.centre_name}</td>
                <td className="p-4 font-mono font-bold text-emerald-400">Counter #{c.counter_number}</td>
                <td className="p-4 text-slate-300">{c.operator_name || 'Unassigned'}</td>
                <td className="p-4 font-mono text-amber-400 font-bold">
                  {c.current_token ? `#${c.current_token}` : 'Idle'}
                </td>
                <td className="p-4">
                  <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${
                    c.status === 'ACTIVE' ? 'bg-purple-500/20 text-purple-300' :
                    c.status === 'CALLING' ? 'bg-amber-500/20 text-amber-300 animate-pulse' :
                    'bg-slate-800 text-slate-400'
                  }`}>
                    {c.status}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};
