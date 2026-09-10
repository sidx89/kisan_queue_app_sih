import React, { useEffect, useState } from 'react';
import { Users, Search, Filter, ShieldCheck, ShieldAlert, Eye, RefreshCw, X, Wheat } from 'lucide-react';
import { api } from '../api/client';

export const FarmersPage: React.FC = () => {
  const [farmers, setFarmers] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [selectedFarmer, setSelectedFarmer] = useState<any | null>(null);

  const fetchFarmers = async () => {
    setLoading(true);
    try {
      const res = await api.get('/api/admin/farmers', {
        params: { search: search || undefined, status: statusFilter || undefined },
      });
      if (res.data.success) {
        setFarmers(res.data.farmers);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchFarmers();
  }, [statusFilter]);

  const viewFarmerProfile = async (id: number) => {
    try {
      const res = await api.get(`/api/admin/farmers/${id}`);
      if (res.data.success) {
        setSelectedFarmer(res.data);
      }
    } catch (err) {
      alert('Failed to load farmer profile');
    }
  };

  const toggleStatus = async (id: number, currentStatus: string) => {
    const newStatus = currentStatus === 'ACTIVE' ? 'DISABLED' : 'ACTIVE';
    if (!confirm(`Are you sure you want to set this farmer account to ${newStatus}?`)) return;
    try {
      await api.patch(`/api/admin/farmers/${id}/status`, { status: newStatus });
      fetchFarmers();
      if (selectedFarmer?.farmer?.id === id) {
        setSelectedFarmer({
          ...selectedFarmer,
          farmer: { ...selectedFarmer.farmer, status: newStatus },
        });
      }
    } catch (err: any) {
      alert(err.response?.data?.message || 'Action failed');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-white">FARMERS MANAGEMENT</h1>
          <p className="text-xs text-slate-400">
            Registered farmers, masked contact details, account permissions, and procurement histories (Requirement 14)
          </p>
        </div>

        <div className="flex items-center gap-3">
          <div className="relative w-full sm:w-64">
            <Search className="absolute left-3 top-2.5 h-4 w-4 text-slate-500" />
            <input
              type="text"
              placeholder="Search name, village, ID..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && fetchFarmers()}
              className="w-full pl-9 pr-4 py-2 bg-slate-900 border border-slate-700 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:border-emerald-500"
            />
          </div>
          <button
            onClick={fetchFarmers}
            className="p-2 bg-slate-800 hover:bg-slate-700 rounded-xl text-slate-300"
          >
            <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
          </button>
        </div>
      </div>

      {/* Filter Bar */}
      <div className="flex items-center gap-3 p-4 bg-slate-900 border border-slate-800 rounded-2xl text-xs">
        <span className="text-slate-400 font-semibold flex items-center gap-1.5">
          <Filter className="h-3.5 w-3.5" /> Account Status:
        </span>
        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="px-3 py-1.5 bg-slate-950 border border-slate-700 rounded-lg text-slate-300"
        >
          <option value="">All Farmers</option>
          <option value="ACTIVE">Active Accounts</option>
          <option value="DISABLED">Disabled Accounts</option>
        </select>
      </div>

      {/* Farmers Table */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-950/60 text-slate-400 uppercase tracking-wider font-semibold border-b border-slate-800">
              <tr>
                <th className="p-4">Farmer Code</th>
                <th className="p-4">Full Name</th>
                <th className="p-4">Phone (Masked)</th>
                <th className="p-4">Village / District</th>
                <th className="p-4 text-center">Land (Acres)</th>
                <th className="p-4 text-center">Total Bookings</th>
                <th className="p-4">Lifetime Procured</th>
                <th className="p-4">Status</th>
                <th className="p-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 font-medium">
              {farmers.map((f) => (
                <tr key={f.id} className="hover:bg-slate-800/30 transition-colors">
                  <td className="p-4 font-mono font-bold text-emerald-400">{f.farmer_code || '—'}</td>
                  <td className="p-4 font-bold text-white">{f.full_name}</td>
                  <td className="p-4 font-mono text-slate-300">{f.masked_phone}</td>
                  <td className="p-4 text-slate-400">{f.village}, {f.district}</td>
                  <td className="p-4 text-center text-slate-300 font-semibold">{f.land_size_acres}</td>
                  <td className="p-4 text-center font-bold text-sky-400">{f.total_bookings}</td>
                  <td className="p-4 font-bold text-emerald-400">
                    ₹{Number(f.total_procured_amount || 0).toLocaleString('en-IN')}
                  </td>
                  <td className="p-4">
                    <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold border ${
                      f.status === 'ACTIVE'
                        ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                        : 'bg-rose-500/10 text-rose-400 border-rose-500/20'
                    }`}>
                      {f.status}
                    </span>
                  </td>
                  <td className="p-4 text-right space-x-2">
                    <button
                      onClick={() => viewFarmerProfile(f.id)}
                      className="px-2.5 py-1 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded text-xs font-semibold inline-flex items-center gap-1"
                    >
                      <Eye className="h-3 w-3" /> Profile
                    </button>
                    <button
                      onClick={() => toggleStatus(f.id, f.status)}
                      className={`px-2.5 py-1 rounded text-xs font-semibold inline-flex items-center gap-1 ${
                        f.status === 'ACTIVE'
                          ? 'bg-rose-950/40 text-rose-400 hover:bg-rose-900/40 border border-rose-800/40'
                          : 'bg-emerald-950/40 text-emerald-400 hover:bg-emerald-900/40 border border-emerald-800/40'
                      }`}
                    >
                      {f.status === 'ACTIVE' ? 'Disable' : 'Enable'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Farmer Profile Modal */}
      {selectedFarmer && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-2xl w-full p-6 space-y-4 max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <div>
                <span className="font-mono text-xs text-emerald-400 block">{selectedFarmer.farmer?.farmer_code}</span>
                <h3 className="text-base font-bold text-white mt-0.5">{selectedFarmer.farmer?.full_name}</h3>
              </div>
              <button onClick={() => setSelectedFarmer(null)} className="text-slate-400 hover:text-white">
                <X className="h-5 w-5" />
              </button>
            </div>

            <div className="grid grid-cols-2 gap-3 text-xs">
              <div className="bg-slate-950 p-3 rounded-xl border border-slate-800">
                <span className="text-[10px] text-slate-400 uppercase">Contact & Location</span>
                <p className="font-medium text-white mt-1">{selectedFarmer.farmer?.masked_phone}</p>
                <p className="text-slate-400">{selectedFarmer.farmer?.village}, {selectedFarmer.farmer?.district}</p>
              </div>
              <div className="bg-slate-950 p-3 rounded-xl border border-slate-800">
                <span className="text-[10px] text-slate-400 uppercase">Land & Account</span>
                <p className="font-medium text-white mt-1">{selectedFarmer.farmer?.land_size_acres} Acres</p>
                <p className="text-slate-400">{selectedFarmer.farmer?.bank_account_masked || 'No Bank Linked'}</p>
              </div>
            </div>

            <div>
              <h4 className="text-xs font-bold text-white uppercase tracking-wider mb-2">Recent Procurement History</h4>
              <div className="space-y-1.5 max-h-48 overflow-y-auto">
                {selectedFarmer.procurements?.map((pr: any) => (
                  <div key={pr.id} className="flex items-center justify-between p-2.5 bg-slate-950 rounded-xl border border-slate-800 text-xs">
                    <div>
                      <span className="font-semibold text-white">{pr.crop_name}</span>
                      <span className="text-[10px] text-slate-400 ml-2">Grade {pr.quality_grade} | {pr.quantity_received} Qtl</span>
                    </div>
                    <span className="font-mono font-bold text-emerald-400">
                      ₹{Number(pr.procurement_amount).toLocaleString('en-IN')}
                    </span>
                  </div>
                ))}
                {(!selectedFarmer.procurements || selectedFarmer.procurements.length === 0) && (
                  <p className="text-xs text-slate-500 py-2">No completed procurement records found.</p>
                )}
              </div>
            </div>

            <div className="flex justify-end pt-2">
              <button
                onClick={() => setSelectedFarmer(null)}
                className="px-4 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-xl text-xs font-semibold"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
