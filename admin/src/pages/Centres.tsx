import React, { useEffect, useState } from 'react';
import { Building2, Plus, Edit2, CheckCircle2, Clock, Layers, RefreshCw, X } from 'lucide-react';
import { api } from '../api/client';

export const CentresPage: React.FC = () => {
  const [centres, setCentres] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [formData, setFormData] = useState({
    code: '',
    name: '',
    address: '',
    village: '',
    district: '',
    dailyCapacity: 150,
    openTime: '09:00',
    closeTime: '17:00',
  });

  const fetchCentres = async () => {
    setLoading(true);
    try {
      const res = await api.get('/api/admin/centres');
      if (res.data.success) {
        setCentres(res.data.centres);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCentres();
  }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const res = await api.post('/api/admin/centres', formData);
      if (res.data.success) {
        setShowCreateModal(false);
        fetchCentres();
      }
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to create centre');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-white">PROCUREMENT CENTRES</h1>
          <p className="text-xs text-slate-400">
            APMC yard configurations, daily capacities, operating windows, and counter allocations (Requirement 15)
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={() => setShowCreateModal(true)}
            className="flex items-center gap-2 px-3.5 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-xs font-semibold shadow-lg shadow-emerald-600/20 transition-all"
          >
            <Plus className="h-4 w-4" /> Add Procurement Centre
          </button>
          <button onClick={fetchCentres} className="p-2 bg-slate-800 hover:bg-slate-700 rounded-xl text-slate-300">
            <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
          </button>
        </div>
      </div>

      {/* Centres Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {centres.map((c) => (
          <div key={c.id} className="bg-slate-900 border border-slate-800 rounded-2xl p-6 flex flex-col justify-between space-y-4">
            <div>
              <div className="flex items-center justify-between">
                <span className="font-mono text-xs font-bold text-emerald-400">{c.code}</span>
                <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold border ${
                  c.status === 'OPEN' ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' : 'bg-amber-500/10 text-amber-400'
                }`}>
                  {c.status}
                </span>
              </div>
              <h3 className="text-lg font-bold text-white mt-1">{c.name}</h3>
              <p className="text-xs text-slate-400 mt-0.5">{c.address}, {c.district}</p>

              <div className="grid grid-cols-2 gap-3 mt-4 text-xs">
                <div className="p-2.5 bg-slate-950 rounded-xl border border-slate-800">
                  <span className="text-[10px] text-slate-400 uppercase">Counters</span>
                  <p className="font-bold text-white mt-0.5">{c.counter_count} ({c.active_counters} Active)</p>
                </div>
                <div className="p-2.5 bg-slate-950 rounded-xl border border-slate-800">
                  <span className="text-[10px] text-slate-400 uppercase">Daily Capacity</span>
                  <p className="font-bold text-white mt-0.5">{c.daily_capacity} Farmers</p>
                </div>
                <div className="p-2.5 bg-slate-950 rounded-xl border border-slate-800">
                  <span className="text-[10px] text-slate-400 uppercase">Operating Hours</span>
                  <p className="font-mono text-slate-300 mt-0.5">{c.open_time} - {c.close_time}</p>
                </div>
                <div className="p-2.5 bg-slate-950 rounded-xl border border-slate-800">
                  <span className="text-[10px] text-slate-400 uppercase">Current Queue</span>
                  <p className="font-bold text-amber-400 mt-0.5">{c.current_queue} Waiting</p>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Create Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full p-6 space-y-4">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <h3 className="text-base font-bold text-white">Add New Centre</h3>
              <button onClick={() => setShowCreateModal(false)} className="text-slate-400 hover:text-white">
                <X className="h-5 w-5" />
              </button>
            </div>

            <form onSubmit={handleCreate} className="space-y-3 text-xs">
              <div>
                <label className="block font-semibold text-slate-300 mb-1">Centre Code</label>
                <input
                  type="text"
                  required
                  placeholder="GJ-BHN-01"
                  value={formData.code}
                  onChange={(e) => setFormData({ ...formData, code: e.target.value })}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-xl text-white"
                />
              </div>
              <div>
                <label className="block font-semibold text-slate-300 mb-1">Centre Name</label>
                <input
                  type="text"
                  required
                  placeholder="Bhavnagar APMC Yard"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-xl text-white"
                />
              </div>
              <div>
                <label className="block font-semibold text-slate-300 mb-1">Address</label>
                <input
                  type="text"
                  required
                  placeholder="Market Yard Road"
                  value={formData.address}
                  onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-xl text-white"
                />
              </div>
              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="block font-semibold text-slate-300 mb-1">District</label>
                  <input
                    type="text"
                    required
                    placeholder="Bhavnagar"
                    value={formData.district}
                    onChange={(e) => setFormData({ ...formData, district: e.target.value })}
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-xl text-white"
                  />
                </div>
                <div>
                  <label className="block font-semibold text-slate-300 mb-1">Capacity</label>
                  <input
                    type="number"
                    value={formData.dailyCapacity}
                    onChange={(e) => setFormData({ ...formData, dailyCapacity: Number(e.target.value) })}
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-xl text-white"
                  />
                </div>
              </div>

              <div className="flex justify-end gap-2 pt-3 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="px-4 py-2 bg-slate-800 text-slate-300 rounded-xl"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-emerald-600 text-white font-semibold rounded-xl"
                >
                  Create Centre
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
