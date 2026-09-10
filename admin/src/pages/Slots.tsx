import React, { useEffect, useState } from 'react';
import { Calendar, Clock, RefreshCw, AlertCircle, Edit2, Check } from 'lucide-react';
import { api } from '../api/client';

export const SlotsPage: React.FC = () => {
  const [centres, setCentres] = useState<any[]>([]);
  const [selectedCentre, setSelectedCentre] = useState<number>(1);
  const [selectedDate, setSelectedDate] = useState<string>(new Date().toISOString().split('T')[0]);
  const [slots, setSlots] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [editingSlotId, setEditingSlotId] = useState<number | null>(null);
  const [newCapacity, setNewCapacity] = useState<number>(10);

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

  const fetchSlots = async () => {
    if (!selectedCentre) return;
    setLoading(true);
    try {
      const res = await api.get('/api/slots', {
        params: { centreId: selectedCentre, date: selectedDate },
      });
      if (res.data.success) {
        setSlots(res.data.slots);
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
    fetchSlots();
  }, [selectedCentre, selectedDate]);

  const handleUpdateCapacity = async (slotId: number) => {
    try {
      await api.patch(`/api/admin/slots/${slotId}`, { capacity: newCapacity });
      setEditingSlotId(null);
      fetchSlots();
    } catch (err) {
      alert('Failed to update capacity');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-white">SLOT MANAGEMENT</h1>
          <p className="text-xs text-slate-400">
            Configure 30-minute arrival windows, farmer capacity quotas, and closure rules (Requirement 16)
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          <select
            value={selectedCentre}
            onChange={(e) => setSelectedCentre(Number(e.target.value))}
            className="px-3 py-2 bg-slate-900 border border-slate-700 rounded-xl text-xs font-semibold text-white"
          >
            {centres.map((c) => (
              <option key={c.id} value={c.id}>{c.name}</option>
            ))}
          </select>

          <input
            type="date"
            value={selectedDate}
            onChange={(e) => setSelectedDate(e.target.value)}
            className="px-3 py-2 bg-slate-900 border border-slate-700 rounded-xl text-xs font-semibold text-white"
          />

          <button onClick={fetchSlots} className="p-2 bg-slate-800 hover:bg-slate-700 rounded-xl text-slate-300">
            <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {slots.map((s) => (
          <div key={s.id} className="bg-slate-900 border border-slate-800 rounded-2xl p-5 flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between">
                <span className="font-mono font-bold text-white text-sm flex items-center gap-1.5">
                  <Clock className="h-4 w-4 text-emerald-400" />
                  {s.start_time} - {s.end_time}
                </span>
                <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold border ${
                  s.status === 'AVAILABLE' ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' :
                  s.status === 'ALMOST_FULL' ? 'bg-amber-500/10 text-amber-400 border-amber-500/20' :
                  'bg-rose-500/10 text-rose-400 border-rose-500/20'
                }`}>
                  {s.status}
                </span>
              </div>

              <div className="mt-4 space-y-1">
                <div className="flex justify-between text-xs">
                  <span className="text-slate-400">Booked:</span>
                  <span className="font-bold text-white">{s.booked_count} / {s.capacity}</span>
                </div>
                <div className="w-full bg-slate-800 h-2 rounded-full overflow-hidden">
                  <div
                    className={`h-full rounded-full transition-all ${
                      s.booked_count >= s.capacity ? 'bg-rose-500' :
                      s.booked_count >= s.capacity * 0.7 ? 'bg-amber-500' : 'bg-emerald-500'
                    }`}
                    style={{ width: `${Math.min(100, (s.booked_count / s.capacity) * 100)}%` }}
                  ></div>
                </div>
              </div>
            </div>

            <div className="mt-4 pt-3 border-t border-slate-800 flex items-center justify-between">
              {editingSlotId === s.id ? (
                <div className="flex items-center gap-2 w-full">
                  <input
                    type="number"
                    value={newCapacity}
                    onChange={(e) => setNewCapacity(Number(e.target.value))}
                    className="w-16 px-2 py-1 bg-slate-950 border border-slate-700 rounded text-xs text-white"
                  />
                  <button
                    onClick={() => handleUpdateCapacity(s.id)}
                    className="p-1.5 bg-emerald-600 rounded text-white text-xs font-semibold"
                  >
                    <Check className="h-3.5 w-3.5" />
                  </button>
                </div>
              ) : (
                <button
                  onClick={() => {
                    setEditingSlotId(s.id);
                    setNewCapacity(s.capacity);
                  }}
                  className="text-xs text-slate-400 hover:text-emerald-400 flex items-center gap-1 transition-colors"
                >
                  <Edit2 className="h-3 w-3" /> Adjust Capacity
                </button>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
