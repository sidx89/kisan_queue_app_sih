import React, { useEffect, useState } from 'react';
import { Calendar, Search, Filter, RefreshCw } from 'lucide-react';
import { api } from '../api/client';

export const BookingsPage: React.FC = () => {
  const [bookings, setBookings] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  const fetchBookings = async () => {
    setLoading(true);
    try {
      const res = await api.get('/api/admin/bookings', {
        params: { search: search || undefined, status: statusFilter || undefined },
      });
      if (res.data.success) {
        setBookings(res.data.bookings);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBookings();
  }, [statusFilter]);

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-white">SLOT BOOKINGS</h1>
          <p className="text-xs text-slate-400">
            Active and archived farmer slot reservations across all centres
          </p>
        </div>

        <div className="flex items-center gap-3">
          <div className="relative w-full sm:w-64">
            <Search className="absolute left-3 top-2.5 h-4 w-4 text-slate-500" />
            <input
              type="text"
              placeholder="Search reference, farmer..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && fetchBookings()}
              className="w-full pl-9 pr-4 py-2 bg-slate-900 border border-slate-700 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:border-emerald-500"
            />
          </div>
          <button onClick={fetchBookings} className="p-2 bg-slate-800 hover:bg-slate-700 rounded-xl text-slate-300">
            <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
          </button>
        </div>
      </div>

      <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-950/60 text-slate-400 uppercase tracking-wider font-semibold border-b border-slate-800">
              <tr>
                <th className="p-4">Reference</th>
                <th className="p-4">Token</th>
                <th className="p-4">Farmer</th>
                <th className="p-4">Centre</th>
                <th className="p-4">Crop</th>
                <th className="p-4">Slot Time</th>
                <th className="p-4">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 font-medium">
              {bookings.map((b) => (
                <tr key={b.id} className="hover:bg-slate-800/30">
                  <td className="p-4 font-mono font-bold text-white">{b.booking_ref}</td>
                  <td className="p-4 font-mono font-black text-emerald-400">#{b.token_number}</td>
                  <td className="p-4">
                    <p className="font-bold text-white">{b.farmer_name}</p>
                    <p className="text-[11px] text-slate-400">{b.farmer_code}</p>
                  </td>
                  <td className="p-4 text-slate-300">{b.centre_name}</td>
                  <td className="p-4 text-slate-300 font-semibold">{b.crop_name} ({b.expected_quantity} Qtl)</td>
                  <td className="p-4 text-slate-400">{b.start_time} - {b.end_time}</td>
                  <td className="p-4">
                    <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold border ${
                      b.status === 'COMPLETED' ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' :
                      b.status === 'PROCESSING' ? 'bg-purple-500/10 text-purple-400 border-purple-500/20' :
                      b.status === 'CALLED' ? 'bg-amber-500/10 text-amber-400 border-amber-500/20' :
                      'bg-slate-800 text-slate-400'
                    }`}>
                      {b.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
