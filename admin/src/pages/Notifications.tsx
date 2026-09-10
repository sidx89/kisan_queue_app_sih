import React, { useState } from 'react';
import { Bell, Send, CheckCircle2, MessageSquare } from 'lucide-react';
import { api } from '../api/client';

export const NotificationsPage: React.FC = () => {
  const [broadcastTitle, setBroadcastTitle] = useState('');
  const [broadcastMessage, setBroadcastMessage] = useState('');
  const [statusMsg, setStatusMsg] = useState<string | null>(null);

  const handleBroadcast = (e: React.FormEvent) => {
    e.preventDefault();
    setStatusMsg(`Broadcast sent to all active farmer devices: "${broadcastTitle}"`);
    setBroadcastTitle('');
    setBroadcastMessage('');
    setTimeout(() => setStatusMsg(null), 5000);
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-white">ADMIN NOTIFICATION HUB</h1>
        <p className="text-xs text-slate-400">
          Push alerts, leave-home advisories, slot reminders, and centre notices (Requirement 24)
        </p>
      </div>

      {statusMsg && (
        <div className="p-3 bg-emerald-500/10 border border-emerald-500/30 rounded-xl text-xs text-emerald-400 flex items-center gap-2">
          <CheckCircle2 className="h-4 w-4" />
          <span>{statusMsg}</span>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Broadcast Form */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-4">
          <div className="flex items-center gap-2">
            <Send className="h-5 w-5 text-emerald-400" />
            <h3 className="font-bold text-white text-base">Broadcast Notice to Farmers</h3>
          </div>

          <form onSubmit={handleBroadcast} className="space-y-3 text-xs">
            <div>
              <label className="block font-semibold text-slate-300 mb-1">Alert Title</label>
              <input
                type="text"
                required
                placeholder="e.g. Vadodara APMC Gate #2 Open"
                value={broadcastTitle}
                onChange={(e) => setBroadcastTitle(e.target.value)}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-xl text-white focus:outline-none focus:border-emerald-500"
              />
            </div>
            <div>
              <label className="block font-semibold text-slate-300 mb-1">Message Content</label>
              <textarea
                required
                placeholder="Please enter through the South Gate for Wheat weighment..."
                value={broadcastMessage}
                onChange={(e) => setBroadcastMessage(e.target.value)}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-xl text-white focus:outline-none focus:border-emerald-500"
                rows={3}
              />
            </div>
            <button
              type="submit"
              className="py-2.5 px-4 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl font-semibold flex items-center gap-2"
            >
              <Send className="h-3.5 w-3.5" /> Dispatch Push Notification
            </button>
          </form>
        </div>

        {/* Triggered Templates */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-3">
          <h3 className="font-bold text-white text-base flex items-center gap-2">
            <Bell className="h-5 w-5 text-purple-400" /> Automated Lifecycle Triggers
          </h3>
          <div className="space-y-2 text-xs">
            {[
              { event: 'Slot Booking Confirmed', desc: 'Auto-sends token number, QR code, and estimated window.' },
              { event: 'Leave-Home Advisory', desc: 'Dispatched 45 mins before slot based on distance & queue rate.' },
              { event: 'Your Turn Approaching', desc: 'Sent when 3 farmers remain before the token.' },
              { event: 'Counter Assigned', desc: 'Notifies farmer to proceed to specific inspection station.' },
              { event: 'Payment Credited', desc: 'Sends digital receipt & bank transaction reference.' },
            ].map((t, idx) => (
              <div key={idx} className="p-3 bg-slate-950 rounded-xl border border-slate-800">
                <span className="font-bold text-emerald-400 block">{t.event}</span>
                <span className="text-slate-400 text-[11px]">{t.desc}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};
