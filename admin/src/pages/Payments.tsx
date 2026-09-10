import React, { useEffect, useState } from 'react';
import { CreditCard, Search, RefreshCw, CheckCircle, AlertCircle } from 'lucide-react';
import { api } from '../api/client';

export const PaymentsPage: React.FC = () => {
  const [payments, setPayments] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  const fetchPayments = async () => {
    setLoading(true);
    try {
      const res = await api.get('/api/admin/payments', {
        params: { search: search || undefined, status: statusFilter || undefined },
      });
      if (res.data.success) {
        setPayments(res.data.payments);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPayments();
  }, [statusFilter]);

  const handleMarkCompleted = async (id: number) => {
    try {
      await api.patch(`/api/admin/payments/${id}`, { status: 'COMPLETED' });
      fetchPayments();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to update payment');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-white">FINANCIAL DISBURSEMENTS & AUDIT</h1>
          <p className="text-xs text-slate-400">
            Direct Benefit Transfer (DBT) bank accounts, UPI payments, and audited financial vouchers (Requirement 37)
          </p>
        </div>

        <div className="flex items-center gap-3">
          <div className="relative w-full sm:w-64">
            <Search className="absolute left-3 top-2.5 h-4 w-4 text-slate-500" />
            <input
              type="text"
              placeholder="Search reference, receipt..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && fetchPayments()}
              className="w-full pl-9 pr-4 py-2 bg-slate-900 border border-slate-700 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:border-emerald-500"
            />
          </div>
          <button onClick={fetchPayments} className="p-2 bg-slate-800 hover:bg-slate-700 rounded-xl text-slate-300">
            <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
          </button>
        </div>
      </div>

      <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-950/60 text-slate-400 uppercase tracking-wider font-semibold border-b border-slate-800">
              <tr>
                <th className="p-4">Transaction Ref</th>
                <th className="p-4">Receipt #</th>
                <th className="p-4">Farmer</th>
                <th className="p-4">Bank Account</th>
                <th className="p-4">Method</th>
                <th className="p-4 text-right">Amount (₹)</th>
                <th className="p-4">Status</th>
                <th className="p-4 text-right">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 font-medium">
              {payments.map((p) => (
                <tr key={p.id} className="hover:bg-slate-800/30">
                  <td className="p-4 font-mono font-bold text-white">{p.transaction_reference}</td>
                  <td className="p-4 font-mono text-slate-300">{p.receipt_number}</td>
                  <td className="p-4">
                    <p className="font-bold text-white">{p.farmer_name}</p>
                    <p className="text-[11px] text-slate-400">{p.farmer_code}</p>
                  </td>
                  <td className="p-4 font-mono text-slate-400">{p.bank_account_masked || 'Primary Bank'}</td>
                  <td className="p-4">
                    <span className="px-2 py-0.5 rounded bg-slate-800 text-slate-300 text-[10px] font-bold">
                      {p.payment_method}
                    </span>
                  </td>
                  <td className="p-4 text-right font-black text-emerald-400 font-mono text-sm">
                    ₹{Number(p.amount).toLocaleString('en-IN')}
                  </td>
                  <td className="p-4">
                    <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold border ${
                      p.status === 'COMPLETED' ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' :
                      p.status === 'PROCESSING' ? 'bg-purple-500/10 text-purple-400 border-purple-500/20' :
                      'bg-amber-500/10 text-amber-400 border-amber-500/20'
                    }`}>
                      {p.status}
                    </span>
                  </td>
                  <td className="p-4 text-right">
                    {p.status !== 'COMPLETED' && (
                      <button
                        onClick={() => handleMarkCompleted(p.id)}
                        className="px-2.5 py-1 bg-emerald-600 hover:bg-emerald-500 text-white rounded text-[11px] font-semibold inline-flex items-center gap-1"
                      >
                        <CheckCircle className="h-3 w-3" /> Clear DBT
                      </button>
                    )}
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
