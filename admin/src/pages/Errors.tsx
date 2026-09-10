import React, { useEffect, useState } from 'react';
import { AlertTriangle, CheckCircle, Search, Filter, Terminal, ExternalLink, X } from 'lucide-react';
import { api } from '../api/client';

export const ErrorsPage: React.FC = () => {
  const [groupedErrors, setGroupedErrors] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [severityFilter, setSeverityFilter] = useState('');
  const [sourceFilter, setSourceFilter] = useState('');
  const [resolvedFilter, setResolvedFilter] = useState('');
  const [search, setSearch] = useState('');
  const [selectedError, setSelectedError] = useState<any | null>(null);
  const [resolveNotes, setResolveNotes] = useState('');

  const fetchErrors = async () => {
    setLoading(true);
    try {
      const res = await api.get('/api/admin/errors', {
        params: {
          severity: severityFilter || undefined,
          source: sourceFilter || undefined,
          resolved: resolvedFilter !== '' ? resolvedFilter : undefined,
        },
      });
      if (res.data.success) {
        setGroupedErrors(res.data.groups);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchErrors();
  }, [severityFilter, sourceFilter, resolvedFilter]);

  const handleResolve = async (errorId: string) => {
    try {
      await api.patch(`/api/admin/errors/${errorId}/resolve`, {
        notes: resolveNotes || 'Resolved by administrator',
      });
      setSelectedError(null);
      setResolveNotes('');
      fetchErrors();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to resolve error');
    }
  };

  const filtered = groupedErrors.filter((e) => {
    if (!search) return true;
    const term = search.toLowerCase();
    return (
      e.message?.toLowerCase().includes(term) ||
      e.sample_error_id?.toLowerCase().includes(term) ||
      e.endpoint?.toLowerCase().includes(term) ||
      e.error_code?.toLowerCase().includes(term)
    );
  });

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-white">ERROR DIAGNOSTICS & TELEMETRY</h1>
          <p className="text-xs text-slate-400">
            Automated crash interception from Android APK and Backend with error grouping (Requirement 11)
          </p>
        </div>

        {/* Search */}
        <div className="relative w-full sm:w-64">
          <Search className="absolute left-3 top-2.5 h-4 w-4 text-slate-500" />
          <input
            type="text"
            placeholder="Search Error ID, endpoint..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-9 pr-4 py-2 bg-slate-900 border border-slate-700 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:border-emerald-500"
          />
        </div>
      </div>

      {/* Filters Bar */}
      <div className="flex flex-wrap items-center gap-3 p-4 bg-slate-900 border border-slate-800 rounded-2xl text-xs">
        <span className="text-slate-400 font-semibold flex items-center gap-1.5">
          <Filter className="h-3.5 w-3.5" /> Filters:
        </span>

        {/* Severity */}
        <select
          value={severityFilter}
          onChange={(e) => setSeverityFilter(e.target.value)}
          className="px-3 py-1.5 bg-slate-950 border border-slate-700 rounded-lg text-slate-300"
        >
          <option value="">All Severities</option>
          <option value="CRITICAL">CRITICAL</option>
          <option value="ERROR">ERROR</option>
          <option value="WARNING">WARNING</option>
          <option value="INFO">INFO</option>
        </select>

        {/* Source */}
        <select
          value={sourceFilter}
          onChange={(e) => setSourceFilter(e.target.value)}
          className="px-3 py-1.5 bg-slate-950 border border-slate-700 rounded-lg text-slate-300"
        >
          <option value="">All Sources</option>
          <option value="Android">Android Phone</option>
          <option value="Backend">Backend Server</option>
          <option value="Database">MySQL Database</option>
        </select>

        {/* Status */}
        <select
          value={resolvedFilter}
          onChange={(e) => setResolvedFilter(e.target.value)}
          className="px-3 py-1.5 bg-slate-950 border border-slate-700 rounded-lg text-slate-300"
        >
          <option value="">All Statuses</option>
          <option value="0">Open / Unresolved</option>
          <option value="1">Resolved</option>
        </select>
      </div>

      {/* Grouped Errors Table (Requirements 10 & 11) */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-950/60 text-slate-400 uppercase tracking-wider font-semibold border-b border-slate-800">
              <tr>
                <th className="p-4">Sample Error ID</th>
                <th className="p-4">Source</th>
                <th className="p-4">Severity</th>
                <th className="p-4">Message / Endpoint</th>
                <th className="p-4 text-center">Occurrences</th>
                <th className="p-4 text-center">Users Affected</th>
                <th className="p-4">Last Seen</th>
                <th className="p-4">Status</th>
                <th className="p-4 text-right">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {filtered.map((g) => (
                <tr key={g.group_hash} className="hover:bg-slate-800/30 transition-colors">
                  <td className="p-4 font-mono font-bold text-emerald-400">
                    {g.sample_error_id}
                  </td>
                  <td className="p-4">
                    <span className="px-2 py-0.5 rounded bg-slate-800 text-slate-300 font-semibold text-[10px]">
                      {g.source}
                    </span>
                  </td>
                  <td className="p-4">
                    <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold border ${
                      g.severity === 'CRITICAL' ? 'bg-rose-500/20 text-rose-300 border-rose-500/30' :
                      g.severity === 'ERROR' ? 'bg-red-500/20 text-red-300 border-red-500/30' :
                      g.severity === 'WARNING' ? 'bg-amber-500/20 text-amber-300 border-amber-500/30' :
                      'bg-sky-500/20 text-sky-300 border-sky-500/30'
                    }`}>
                      {g.severity}
                    </span>
                  </td>
                  <td className="p-4 max-w-xs">
                    <p className="font-semibold text-white truncate">{g.message}</p>
                    <p className="text-[11px] text-slate-400 font-mono truncate">{g.http_method} {g.endpoint}</p>
                  </td>
                  <td className="p-4 text-center font-black text-amber-400 text-sm">
                    {g.occurrence_count}
                  </td>
                  <td className="p-4 text-center font-bold text-slate-300">
                    {g.affected_users}
                  </td>
                  <td className="p-4 text-slate-400">
                    {new Date(g.last_seen).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })}
                  </td>
                  <td className="p-4">
                    <span className={`px-2 py-0.5 rounded-full text-[10px] font-semibold ${
                      g.resolved ? 'bg-emerald-500/10 text-emerald-400' : 'bg-rose-500/10 text-rose-400'
                    }`}>
                      {g.resolved ? 'RESOLVED' : 'OPEN'}
                    </span>
                  </td>
                  <td className="p-4 text-right">
                    <button
                      onClick={() => setSelectedError(g)}
                      className="px-2.5 py-1 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-lg text-xs font-semibold"
                    >
                      Details
                    </button>
                  </td>
                </tr>
              ))}
              {filtered.length === 0 && (
                <tr>
                  <td colSpan={9} className="p-8 text-center text-slate-500">
                    No matching diagnostic errors found.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Diagnostics Modal with Stack Trace & Resolve Action */}
      {selectedError && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-2xl w-full p-6 space-y-4 max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <div>
                <span className="font-mono text-xs text-emerald-400 block">{selectedError.sample_error_id}</span>
                <h3 className="text-base font-bold text-white mt-0.5">Diagnostic Details & Safe Trace</h3>
              </div>
              <button onClick={() => setSelectedError(null)} className="text-slate-400 hover:text-white">
                <X className="h-5 w-5" />
              </button>
            </div>

            <div className="grid grid-cols-2 gap-3 text-xs">
              <div className="bg-slate-950 p-3 rounded-xl border border-slate-800">
                <span className="text-[10px] text-slate-400 uppercase">Endpoint / HTTP</span>
                <p className="font-mono text-white mt-1">{selectedError.http_method} {selectedError.endpoint} ({selectedError.http_status})</p>
              </div>
              <div className="bg-slate-950 p-3 rounded-xl border border-slate-800">
                <span className="text-[10px] text-slate-400 uppercase">Total Occurrences</span>
                <p className="font-bold text-amber-400 text-sm mt-1">{selectedError.occurrence_count} times</p>
              </div>
            </div>

            <div>
              <span className="text-xs font-semibold text-slate-300 block mb-1">Message</span>
              <div className="p-3 bg-slate-950 rounded-xl border border-slate-800 text-xs text-rose-300 font-mono">
                {selectedError.message}
              </div>
            </div>

            <div>
              <span className="text-xs font-semibold text-slate-300 block mb-1 flex items-center gap-1">
                <Terminal className="h-3.5 w-3.5" /> Stack Trace (Sanitized)
              </span>
              <pre className="p-3 bg-slate-950 rounded-xl border border-slate-800 text-[11px] text-slate-300 font-mono overflow-x-auto max-h-48 whitespace-pre-wrap">
                {selectedError.sample_stack_trace || 'No local stack trace recorded.'}
              </pre>
            </div>

            {/* Resolve Form */}
            <div className="border-t border-slate-800 pt-3 space-y-2">
              <label className="text-xs font-semibold text-slate-300">Resolution Notes</label>
              <textarea
                value={resolveNotes}
                onChange={(e) => setResolveNotes(e.target.value)}
                placeholder="Describe fix or root cause..."
                className="w-full p-2.5 bg-slate-950 border border-slate-700 rounded-xl text-xs text-white focus:outline-none focus:border-emerald-500"
                rows={2}
              />
              <div className="flex justify-end gap-2">
                <button
                  onClick={() => setSelectedError(null)}
                  className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl text-xs font-semibold"
                >
                  Close
                </button>
                <button
                  onClick={() => handleResolve(selectedError.sample_error_id)}
                  className="px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-xs font-semibold flex items-center gap-1.5"
                >
                  <CheckCircle className="h-4 w-4" /> Mark Resolved
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
