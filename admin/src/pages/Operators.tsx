import React, { useEffect, useState } from 'react';
import { UserCheck, RefreshCw, Mail, Phone, Building2 } from 'lucide-react';
import { api } from '../api/client';

export const OperatorsPage: React.FC = () => {
  const [counters, setCounters] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchOperators = async () => {
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
    fetchOperators();
  }, []);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-white">CENTRE OPERATORS & INSPECTORS</h1>
          <p className="text-xs text-slate-400">
            Weighment staff, quality inspectors, and counter dispatch assignments
          </p>
        </div>
        <button onClick={fetchOperators} className="p-2 bg-slate-800 hover:bg-slate-700 rounded-xl text-slate-300">
          <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {counters.map((c) => (
          <div key={c.id} className="bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-3">
            <div className="flex items-center justify-between">
              <span className="font-bold text-white text-sm flex items-center gap-2">
                <UserCheck className="h-4 w-4 text-emerald-400" />
                {c.operator_name || 'Unassigned Operator'}
              </span>
              <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${
                c.status === 'ACTIVE' ? 'bg-emerald-500/20 text-emerald-300' : 'bg-slate-800 text-slate-400'
              }`}>
                {c.status}
              </span>
            </div>
            <div className="text-xs space-y-1 text-slate-400">
              <p className="flex items-center gap-1.5"><Building2 className="h-3.5 w-3.5" /> {c.centre_name}</p>
              <p className="flex items-center gap-1.5"><Mail className="h-3.5 w-3.5" /> {c.operator_email || 'No email registered'}</p>
            </div>
            <div className="pt-2 border-t border-slate-800 flex justify-between items-center text-xs">
              <span className="text-slate-400">Assigned Station:</span>
              <span className="font-mono font-bold text-emerald-400">Counter #{c.counter_number}</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
