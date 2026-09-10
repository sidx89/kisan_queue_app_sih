import React, { useEffect, useState } from 'react';
import { History, Search, Filter, RefreshCw, Database, Eye, X } from 'lucide-react';
import { api } from '../api/client';

export const AuditLogsPage: React.FC = () => {
  const [logs, setLogs] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [roleFilter, setRoleFilter] = useState('');
  const [actionFilter, setActionFilter] = useState('');
  const [search, setSearch] = useState('');
  const [selectedMeta, setSelectedMeta] = useState<any | null>(null);

  const fetchLogs = async () => {
    setLoading(true);
    try {
      const res = await api.get('/api/admin/audit-logs', {
        params: {
          role: roleFilter || undefined,
          action: actionFilter || undefined,
          search: search || undefined,
        },
      });
      if (res.data.success) {
        setLogs(res.data.logs);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs();
  }, [roleFilter, actionFilter]);

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-white">USER ACTIVITY & AUDIT TRAIL</h1>
          <p className="text-xs text-slate-400">
            Immutable log of all farmer, operator, admin and financial transactions (Requirements 12 & 13)
          </p>
        </div>

        <div className="flex items-center gap-3">
          <div className="relative w-full sm:w-64">
            <Search className="absolute left-3 top-2.5 h-4 w-4 text-slate-500" />
            <input
              type="text"
              placeholder="Search actor, action..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && fetchLogs()}
              className="w-full pl-9 pr-4 py-2 bg-slate-900 border border-slate-700 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:border-emerald-500"
            />
          </div>
          <button
            onClick={fetchLogs}
            className="p-2 bg-slate-800 hover:bg-slate-700 rounded-xl text-slate-300 transition-colors"
          >
            <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
          </button>
        </div>
      </div>

      {/* Filter Row */}
      <div className="flex flex-wrap items-center gap-3 p-4 bg-slate-900 border border-slate-800 rounded-2xl text-xs">
        <span className="text-slate-400 font-semibold flex items-center gap-1.5">
          <Filter className="h-3.5 w-3.5" /> Filter Role:
        </span>
        <select
          value={roleFilter}
          onChange={(e) => setRoleFilter(e.target.value)}
          className="px-3 py-1.5 bg-slate-950 border border-slate-700 rounded-lg text-slate-300"
        >
          <option value="">All Roles</option>
          <option value="SUPER_ADMIN">SUPER_ADMIN</option>
          <option value="ADMIN">ADMIN</option>
          <option value="OPERATOR">OPERATOR</option>
          <option value="FARMER">FARMER</option>
          <option value="SYSTEM">SYSTEM</option>
        </select>
      </div>

      {/* Audit Log Table */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-950/60 text-slate-400 uppercase tracking-wider font-semibold border-b border-slate-800">
              <tr>
                <th className="p-4">Timestamp</th>
                <th className="p-4">Actor</th>
                <th className="p-4">Role</th>
                <th className="p-4">Action</th>
                <th className="p-4">Entity</th>
                <th className="p-4">IP Address</th>
                <th className="p-4 text-right">Metadata</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 font-medium">
              {logs.map((l) => (
                <tr key={l.id} className="hover:bg-slate-800/30 transition-colors">
                  <td className="p-4 text-slate-400 whitespace-nowrap">
                    {new Date(l.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })}
                    <span className="block text-[10px] text-slate-500">{new Date(l.timestamp).toLocaleDateString()}</span>
                  </td>
                  <td className="p-4 font-bold text-white whitespace-nowrap">{l.actor}</td>
                  <td className="p-4">
                    <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                      l.role === 'SUPER_ADMIN' ? 'bg-purple-500/20 text-purple-300' :
                      l.role === 'ADMIN' ? 'bg-sky-500/20 text-sky-300' :
                      l.role === 'OPERATOR' ? 'bg-amber-500/20 text-amber-300' :
                      l.role === 'FARMER' ? 'bg-emerald-500/20 text-emerald-300' :
                      'bg-slate-800 text-slate-400'
                    }`}>
                      {l.role}
                    </span>
                  </td>
                  <td className="p-4">
                    <span className="font-mono text-[11px] text-emerald-400 font-semibold">{l.action}</span>
                  </td>
                  <td className="p-4 text-slate-300">
                    {l.entity} {l.entity_id ? `(#${l.entity_id})` : ''}
                  </td>
                  <td className="p-4 font-mono text-[11px] text-slate-400">{l.ip_address || '127.0.0.1'}</td>
                  <td className="p-4 text-right">
                    {l.metadata ? (
                      <button
                        onClick={() => setSelectedMeta(l.metadata)}
                        className="px-2.5 py-1 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded text-[11px] font-semibold flex items-center gap-1 ml-auto"
                      >
                        <Eye className="h-3 w-3" /> View Data
                      </button>
                    ) : (
                      <span className="text-slate-600">—</span>
                    )}
                  </td>
                </tr>
              ))}
              {logs.length === 0 && (
                <tr>
                  <td colSpan={7} className="p-8 text-center text-slate-500">
                    No activity logs match the selected criteria.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Metadata Viewer Modal */}
      {selectedMeta && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-lg w-full p-6 space-y-4">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <h3 className="text-sm font-bold text-white flex items-center gap-2">
                <Database className="h-4 w-4 text-emerald-400" /> Audit Payload Parameters
              </h3>
              <button onClick={() => setSelectedMeta(null)} className="text-slate-400 hover:text-white">
                <X className="h-4 w-4" />
              </button>
            </div>
            <pre className="p-3 bg-slate-950 border border-slate-800 rounded-xl text-xs text-emerald-400 font-mono overflow-auto max-h-72">
              {JSON.stringify(selectedMeta, null, 2)}
            </pre>
            <div className="flex justify-end">
              <button
                onClick={() => setSelectedMeta(null)}
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
