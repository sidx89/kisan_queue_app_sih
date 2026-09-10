import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ShieldCheck, AlertCircle, ArrowRight, KeyRound } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export const LoginPage: React.FC = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('superadmin@kisan.gov.in');
  const [password, setPassword] = useState('password123');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await login(email, password);
      navigate('/admin/dashboard');
    } catch (err: any) {
      setError(err.message || 'Login failed. Please check credentials.');
    } finally {
      setLoading(false);
    }
  };

  const handleQuickFill = (demoEmail: string) => {
    setEmail(demoEmail);
    setPassword('password123');
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-950 px-4">
      <div className="w-full max-w-md space-y-8 bg-slate-900 border border-slate-800 p-8 rounded-2xl shadow-2xl">
        <div className="text-center space-y-2">
          <div className="inline-flex h-14 w-14 items-center justify-center rounded-2xl bg-emerald-600/20 text-emerald-400 border border-emerald-500/30 mb-2">
            <ShieldCheck className="h-8 w-8" />
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-white">Smart Procurement Admin</h1>
          <p className="text-xs text-slate-400">Control Panel & System Observability Portal</p>
        </div>

        {error && (
          <div className="flex items-center gap-2 p-3 bg-rose-500/10 border border-rose-500/30 rounded-xl text-xs text-rose-400">
            <AlertCircle className="h-4 w-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
              Email Address
            </label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full px-4 py-2.5 bg-slate-950 border border-slate-700 rounded-xl text-white text-sm focus:outline-none focus:border-emerald-500 transition-colors"
              placeholder="admin@kisan.gov.in"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
              Password
            </label>
            <input
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-4 py-2.5 bg-slate-950 border border-slate-700 rounded-xl text-white text-sm focus:outline-none focus:border-emerald-500 transition-colors"
              placeholder="••••••••"
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full flex items-center justify-center gap-2 py-3 px-4 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-semibold text-sm transition-all shadow-lg shadow-emerald-600/20 disabled:opacity-50"
          >
            {loading ? 'Authenticating...' : 'Sign In to Control Center'}
            <ArrowRight className="h-4 w-4" />
          </button>
        </form>

        {/* Demo Quick-Fill Accounts */}
        <div className="border-t border-slate-800 pt-5 space-y-3">
          <div className="flex items-center gap-1.5 text-xs text-slate-400 font-medium">
            <KeyRound className="h-3.5 w-3.5" />
            <span>Quick Login Demo Accounts:</span>
          </div>
          <div className="grid grid-cols-2 gap-2 text-xs">
            <button
              onClick={() => handleQuickFill('superadmin@kisan.gov.in')}
              className="p-2 text-left bg-slate-800/60 hover:bg-slate-800 border border-slate-700/60 rounded-lg text-slate-300 transition-colors"
            >
              <span className="font-semibold text-emerald-400 block">SUPER_ADMIN</span>
              <span className="text-[10px] text-slate-400 truncate block">superadmin@kisan.gov.in</span>
            </button>
            <button
              onClick={() => handleQuickFill('admin.rajkot@kisan.gov.in')}
              className="p-2 text-left bg-slate-800/60 hover:bg-slate-800 border border-slate-700/60 rounded-lg text-slate-300 transition-colors"
            >
              <span className="font-semibold text-sky-400 block">ADMIN</span>
              <span className="text-[10px] text-slate-400 truncate block">admin.rajkot@kisan.gov.in</span>
            </button>
            <button
              onClick={() => handleQuickFill('manager.vadodara@kisan.gov.in')}
              className="p-2 text-left bg-slate-800/60 hover:bg-slate-800 border border-slate-700/60 rounded-lg text-slate-300 transition-colors"
            >
              <span className="font-semibold text-purple-400 block">CENTRE_MANAGER</span>
              <span className="text-[10px] text-slate-400 truncate block">manager.vadodara@...</span>
            </button>
            <button
              onClick={() => handleQuickFill('operator1@kisan.gov.in')}
              className="p-2 text-left bg-slate-800/60 hover:bg-slate-800 border border-slate-700/60 rounded-lg text-slate-300 transition-colors"
            >
              <span className="font-semibold text-amber-400 block">OPERATOR</span>
              <span className="text-[10px] text-slate-400 truncate block">operator1@kisan.gov.in</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
