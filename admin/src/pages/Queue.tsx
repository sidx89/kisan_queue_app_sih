import React, { useEffect, useState } from 'react';
import { Clock, PhoneCall, CheckCircle, AlertCircle, Play, Pause, RefreshCw, User, Sparkles } from 'lucide-react';
import { api } from '../api/client';
import { useSocket } from '../context/SocketContext';

export const QueuePage: React.FC = () => {
  const { socket } = useSocket();
  const [centres, setCentres] = useState<any[]>([]);
  const [selectedCentre, setSelectedCentre] = useState<number>(1);
  const [queue, setQueue] = useState<any[]>([]);
  const [counters, setCounters] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionMsg, setActionMsg] = useState<string | null>(null);

  const fetchCentres = async () => {
    try {
      const res = await api.get('/api/centres');
      if (res.data.success) {
        setCentres(res.data.centres);
        if (res.data.centres.length > 0 && !selectedCentre) {
          setSelectedCentre(res.data.centres[0].id);
        }
      }
    } catch (err) {
      console.error(err);
    }
  };

  const fetchQueue = async () => {
    if (!selectedCentre) return;
    try {
      const res = await api.get(`/api/queue/${selectedCentre}`);
      if (res.data.success) {
        setQueue(res.data.queue);
        setCounters(res.data.counters);
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

  useEffect(() => {
    fetchQueue();

    if (socket) {
      socket.on('queue:update', fetchQueue);
    }
    return () => {
      if (socket) socket.off('queue:update', fetchQueue);
    };
  }, [selectedCentre, socket]);

  const handleCallNext = async (counterId: number) => {
    try {
      setActionMsg(null);
      const res = await api.post('/api/operator/queue/call-next', {
        centreId: selectedCentre,
        counterId,
      });
      if (res.data.success) {
        setActionMsg(res.data.message);
        fetchQueue();
      }
    } catch (err: any) {
      setActionMsg(err.response?.data?.message || 'Failed to call next farmer');
    }
  };

  const handleStatusChange = async (bookingId: number, status: string) => {
    try {
      await api.patch(`/api/operator/queue/${bookingId}/status`, { status });
      fetchQueue();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Status transition failed');
    }
  };

  const handleEmergency = async (action: 'PAUSE' | 'RESUME') => {
    if (!confirm(`Are you sure you want to ${action} the queue for this centre?`)) return;
    try {
      await api.post('/api/operator/queue/emergency', {
        centreId: selectedCentre,
        action,
      });
      fetchQueue();
      fetchCentres();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Action failed');
    }
  };

  const currentCentre = centres.find((c) => c.id === selectedCentre);

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-white">LIVE QUEUE CONTROL</h1>
          <p className="text-xs text-slate-400">Multi-counter dispatch, calling engine, and emergency controls</p>
        </div>

        {/* Centre Picker */}
        <div className="flex items-center gap-3">
          <select
            value={selectedCentre}
            onChange={(e) => setSelectedCentre(Number(e.target.value))}
            className="px-3 py-2 bg-slate-900 border border-slate-700 rounded-xl text-xs font-semibold text-white focus:outline-none focus:border-emerald-500"
          >
            {centres.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name} ({c.code})
              </option>
            ))}
          </select>

          <button
            onClick={fetchQueue}
            className="p-2 bg-slate-800 hover:bg-slate-700 rounded-xl text-slate-300 transition-colors"
          >
            <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
          </button>
        </div>
      </div>

      {actionMsg && (
        <div className="p-3 bg-emerald-500/10 border border-emerald-500/30 rounded-xl text-xs text-emerald-400 flex items-center gap-2">
          <Sparkles className="h-4 w-4" />
          <span>{actionMsg}</span>
        </div>
      )}

      {/* Centre Emergency Controls & Counter Stations */}
      <div className="grid grid-cols-1 lg:grid-cols-4 gap-4">
        {/* Emergency Control Card */}
        <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl space-y-4">
          <div>
            <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">Centre Status</span>
            <div className="flex items-center gap-2 mt-1">
              <span className={`h-2.5 w-2.5 rounded-full ${currentCentre?.status === 'OPEN' ? 'bg-emerald-400' : 'bg-amber-400'}`}></span>
              <span className="text-base font-bold text-white">{currentCentre?.status || 'OPEN'}</span>
            </div>
          </div>

          <div className="space-y-2">
            <span className="text-[10px] font-semibold text-slate-400 uppercase">Emergency Actions</span>
            <div className="grid grid-cols-2 gap-2">
              <button
                onClick={() => handleEmergency('PAUSE')}
                className="flex items-center justify-center gap-1.5 py-2 px-3 bg-amber-500/20 hover:bg-amber-500/30 text-amber-300 border border-amber-500/30 rounded-xl text-xs font-semibold"
              >
                <Pause className="h-3.5 w-3.5" /> Pause
              </button>
              <button
                onClick={() => handleEmergency('RESUME')}
                className="flex items-center justify-center gap-1.5 py-2 px-3 bg-emerald-500/20 hover:bg-emerald-500/30 text-emerald-300 border border-emerald-500/30 rounded-xl text-xs font-semibold"
              >
                <Play className="h-3.5 w-3.5" /> Resume
              </button>
            </div>
          </div>
        </div>

        {/* Counter Stations List */}
        <div className="lg:col-span-3 grid grid-cols-1 sm:grid-cols-3 gap-3">
          {counters.map((ct) => (
            <div key={ct.id} className="bg-slate-900 border border-slate-800 p-4 rounded-2xl flex flex-col justify-between">
              <div>
                <div className="flex items-center justify-between">
                  <span className="font-bold text-white text-sm">Counter #{ct.counter_number}</span>
                  <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${
                    ct.status === 'CALLING' ? 'bg-amber-500/20 text-amber-400 animate-pulse' :
                    ct.status === 'ACTIVE' ? 'bg-purple-500/20 text-purple-400' : 'bg-slate-800 text-slate-400'
                  }`}>
                    {ct.status}
                  </span>
                </div>
                <p className="text-xs text-slate-400 mt-1 flex items-center gap-1">
                  <User className="h-3 w-3" /> {ct.operator_name || 'Unassigned'}
                </p>
                <div className="mt-3">
                  <span className="text-[10px] text-slate-400">Serving Token:</span>
                  <p className="text-xl font-black text-emerald-400">{ct.current_token ? `#${ct.current_token}` : '—'}</p>
                </div>
              </div>

              <button
                onClick={() => handleCallNext(ct.id)}
                className="mt-3 w-full py-1.5 px-3 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-xs font-semibold flex items-center justify-center gap-1.5 transition-colors"
              >
                <PhoneCall className="h-3.5 w-3.5" /> Call Next Farmer
              </button>
            </div>
          ))}
        </div>
      </div>

      {/* Live Queue Table */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6">
        <h2 className="text-base font-bold text-white mb-4">TODAY QUEUE ROSTER ({queue.length} Total)</h2>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="text-slate-400 border-b border-slate-800 uppercase tracking-wider font-semibold">
              <tr>
                <th className="pb-3">Token #</th>
                <th className="pb-3">Farmer</th>
                <th className="pb-3">Crop</th>
                <th className="pb-3">Est. Qty</th>
                <th className="pb-3">Slot Window</th>
                <th className="pb-3">Status</th>
                <th className="pb-3">Wait Time</th>
                <th className="pb-3 text-right">Workflow Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {queue.map((b) => (
                <tr key={b.id} className="hover:bg-slate-800/30">
                  <td className="py-3.5 font-black text-sm text-emerald-400">
                    #{b.token_number}
                  </td>
                  <td className="py-3.5">
                    <p className="font-bold text-white">{b.farmer_name}</p>
                    <p className="text-[11px] text-slate-400">{b.farmer_code} | {b.farmer_phone}</p>
                  </td>
                  <td className="py-3.5 text-slate-300 font-medium">{b.crop_name}</td>
                  <td className="py-3.5 text-slate-300 font-semibold">{b.expected_quantity} Qtl</td>
                  <td className="py-3.5 text-slate-400">{b.start_time} - {b.end_time}</td>
                  <td className="py-3.5">
                    <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold border ${
                      b.status === 'COMPLETED' ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' :
                      b.status === 'PROCESSING' ? 'bg-purple-500/10 text-purple-400 border-purple-500/20' :
                      b.status === 'CALLED' ? 'bg-amber-500/10 text-amber-400 border-amber-500/20' :
                      b.status === 'ARRIVED' ? 'bg-sky-500/10 text-sky-400 border-sky-500/20' :
                      'bg-slate-800 text-slate-400 border-slate-700'
                    }`}>
                      {b.status} {b.counter_number ? `@ C-${b.counter_number}` : ''}
                    </span>
                  </td>
                  <td className="py-3.5 text-slate-400">
                    {b.status === 'COMPLETED' ? 'Done' : `~${b.estimated_wait_minutes} min`}
                  </td>
                  <td className="py-3.5 text-right space-x-1.5">
                    {b.status === 'CALLED' && (
                      <button
                        onClick={() => handleStatusChange(b.id, 'PROCESSING')}
                        className="px-2.5 py-1 bg-purple-600 hover:bg-purple-500 text-white rounded text-[11px] font-semibold"
                      >
                        Start Processing
                      </button>
                    )}
                    {b.status === 'WAITING' && (
                      <button
                        onClick={() => handleStatusChange(b.id, 'ARRIVED')}
                        className="px-2.5 py-1 bg-sky-600 hover:bg-sky-500 text-white rounded text-[11px] font-semibold"
                      >
                        Mark Arrived
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
