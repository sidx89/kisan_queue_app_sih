import React, { useEffect, useState } from 'react';
import { Settings, ShieldAlert, Sliders, CheckCircle2, RotateCcw } from 'lucide-react';
import { api } from '../api/client';

export const SettingsPage: React.FC = () => {
  const [settings, setSettings] = useState<any>({});
  const [loading, setLoading] = useState(true);
  const [statusMsg, setStatusMsg] = useState<string | null>(null);

  const fetchSettings = async () => {
    try {
      const res = await api.get('/api/admin/settings');
      if (res.data.success) {
        setSettings(res.data.settings);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSettings();
  }, []);

  const handleUpdate = async (keyName: string, valueString: string) => {
    try {
      const res = await api.post('/api/admin/settings', { keyName, valueString });
      if (res.data.success) {
        setSettings((prev: any) => ({ ...prev, [keyName]: valueString }));
        setStatusMsg(`Setting '${keyName}' updated.`);
        setTimeout(() => setStatusMsg(null), 3000);
      }
    } catch (err: any) {
      alert('Failed to update setting');
    }
  };

  return (
    <div className="space-y-6 max-w-3xl">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-white">SYSTEM SETTINGS & MAINTENANCE</h1>
        <p className="text-xs text-slate-400">
          Global processing parameters, operational thresholds, and emergency maintenance controls (Requirements 27 & 28)
        </p>
      </div>

      {statusMsg && (
        <div className="p-3 bg-emerald-500/10 border border-emerald-500/30 rounded-xl text-xs text-emerald-400 flex items-center gap-2">
          <CheckCircle2 className="h-4 w-4" />
          <span>{statusMsg}</span>
        </div>
      )}

      {/* Maintenance Mode Box (Requirement 28) */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <span className="p-2 bg-amber-500/10 text-amber-400 rounded-xl border border-amber-500/20">
              <ShieldAlert className="h-6 w-6" />
            </span>
            <div>
              <h3 className="font-bold text-white text-base">Farmer Maintenance Mode</h3>
              <p className="text-xs text-slate-400">
                When active, all farmer APK endpoints return HTTP 503 with maintenance advisory. Admins remain connected.
              </p>
            </div>
          </div>
          <button
            onClick={() => handleUpdate('maintenance_mode', settings.maintenance_mode === 'true' ? 'false' : 'true')}
            className={`px-4 py-2 rounded-xl text-xs font-bold transition-all ${
              settings.maintenance_mode === 'true'
                ? 'bg-rose-600 hover:bg-rose-500 text-white shadow-lg shadow-rose-600/30'
                : 'bg-slate-800 hover:bg-slate-700 text-slate-300'
            }`}
          >
            {settings.maintenance_mode === 'true' ? 'MAINTENANCE ACTIVE' : 'ENABLE MAINTENANCE'}
          </button>
        </div>
      </div>

      {/* Operational Parameters Form */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-4">
        <h3 className="font-bold text-white text-base flex items-center gap-2">
          <Sliders className="h-5 w-5 text-emerald-400" /> Operational Thresholds
        </h3>

        <div className="space-y-4 text-xs">
          <div className="flex items-center justify-between py-2 border-b border-slate-800">
            <div>
              <span className="font-bold text-white block">Average Processing Time (Minutes)</span>
              <span className="text-slate-400">Used by live ETA calculation algorithm</span>
            </div>
            <input
              type="number"
              value={settings.average_processing_time_mins || 12}
              onChange={(e) => handleUpdate('average_processing_time_mins', e.target.value)}
              className="w-20 px-3 py-1.5 bg-slate-950 border border-slate-700 rounded-lg text-white font-mono text-center"
            />
          </div>

          <div className="flex items-center justify-between py-2 border-b border-slate-800">
            <div>
              <span className="font-bold text-white block">Default Slot Capacity</span>
              <span className="text-slate-400">Default farmers allocated per 30-min window</span>
            </div>
            <input
              type="number"
              value={settings.default_slot_capacity || 10}
              onChange={(e) => handleUpdate('default_slot_capacity', e.target.value)}
              className="w-20 px-3 py-1.5 bg-slate-950 border border-slate-700 rounded-lg text-white font-mono text-center"
            />
          </div>

          <div className="flex items-center justify-between py-2">
            <div>
              <span className="font-bold text-white block">Demo Quick Authentication</span>
              <span className="text-slate-400">Allows password123 bypass for testing</span>
            </div>
            <button
              onClick={() => handleUpdate('demo_mode', settings.demo_mode === 'true' ? 'false' : 'true')}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold ${
                settings.demo_mode === 'true' ? 'bg-emerald-600 text-white' : 'bg-slate-800 text-slate-400'
              }`}
            >
              {settings.demo_mode === 'true' ? 'Active' : 'Disabled'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
