import React, { useEffect, useState } from 'react';
import { Wheat, Search, RefreshCw, Eye } from 'lucide-react';
import { api } from '../api/client';

export const ProcurementPage: React.FC = () => {
  const [records, setRecords] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  const fetchRecords = async () => {
    setLoading(true);
    try {
      const res = await api.get('/api/admin/procurement', {
        params: { search: search || undefined },
      });
      if (res.data.success) {
        setRecords(res.data.records);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRecords();
  }, []);

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-white">PROCUREMENT & WEIGHMENT AUDIT</h1>
          <p className="text-xs text-slate-400">
            Grain quality inspection grades (A/B/C), net weights, and MSP financial calculations (Requirement 37)
          </p>
        </div>

        <div className="flex items-center gap-3">
          <div className="relative w-full sm:w-64">
            <Search className="absolute left-3 top-2.5 h-4 w-4 text-slate-500" />
            <input
              type="text"
              placeholder="Search receipt, farmer..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && fetchRecords()}
              className="w-full pl-9 pr-4 py-2 bg-slate-900 border border-slate-700 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:border-emerald-500"
            />
          </div>
          <button onClick={fetchRecords} className="p-2 bg-slate-800 hover:bg-slate-700 rounded-xl text-slate-300">
            <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
          </button>
        </div>
      </div>

      <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-950/60 text-slate-400 uppercase tracking-wider font-semibold border-b border-slate-800">
              <tr>
                <th className="p-4">Receipt #</th>
                <th className="p-4">Farmer</th>
                <th className="p-4">Centre</th>
                <th className="p-4">Commodity</th>
                <th className="p-4">Grade</th>
                <th className="p-4 text-right">Net Qty</th>
                <th className="p-4 text-right">MSP Rate</th>
                <th className="p-4 text-right">Total Amount</th>
                <th className="p-4">Date</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 font-medium">
              {records.map((r) => (
                <tr key={r.id} className="hover:bg-slate-800/30">
                  <td className="p-4 font-mono font-bold text-emerald-400">{r.receipt_number}</td>
                  <td className="p-4">
                    <p className="font-bold text-white">{r.farmer_name}</p>
                    <p className="text-[11px] text-slate-400">{r.farmer_code}</p>
                  </td>
                  <td className="p-4 text-slate-300">{r.centre_name}</td>
                  <td className="p-4 text-slate-300">{r.crop_name}</td>
                  <td className="p-4">
                    <span className={`px-2 py-0.5 rounded text-[10px] font-bold border ${
                      r.quality_grade === 'A' ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' :
                      r.quality_grade === 'B' ? 'bg-sky-500/10 text-sky-400 border-sky-500/20' :
                      r.quality_grade === 'C' ? 'bg-amber-500/10 text-amber-400 border-amber-500/20' :
                      'bg-rose-500/10 text-rose-400 border-rose-500/20'
                    }`}>
                      Grade {r.quality_grade}
                    </span>
                  </td>
                  <td className="p-4 text-right font-semibold text-white">{r.quantity_received} Qtl</td>
                  <td className="p-4 text-right text-slate-300 font-mono">₹{r.rate_per_unit}</td>
                  <td className="p-4 text-right font-black text-emerald-400 font-mono">
                    ₹{Number(r.procurement_amount).toLocaleString('en-IN')}
                  </td>
                  <td className="p-4 text-slate-400">{new Date(r.processed_at).toLocaleDateString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
