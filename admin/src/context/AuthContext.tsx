import React, { createContext, useContext, useState, useEffect } from 'react';
import { api } from '../api/client';

export interface AdminUser {
  id: number;
  email: string;
  role: 'SUPER_ADMIN' | 'ADMIN' | 'CENTRE_MANAGER' | 'OPERATOR';
  fullName: string;
  phone?: string;
  village?: string;
  district?: string;
}

interface AuthContextType {
  user: AdminUser | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, pass: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<AdminUser | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    const savedToken = localStorage.getItem('kisan_admin_token');
    const savedUser = localStorage.getItem('kisan_admin_user');
    if (savedToken && savedUser) {
      try {
        setToken(savedToken);
        setUser(JSON.parse(savedUser));
      } catch {
        localStorage.removeItem('kisan_admin_token');
        localStorage.removeItem('kisan_admin_user');
      }
    }
    setIsLoading(false);
  }, []);

  const login = async (email: string, pass: string) => {
    const res = await api.post('/api/auth/login', { email, password: pass });
    if (res.data.success) {
      const u = res.data.user;
      if (u.role === 'FARMER') {
        throw new Error('Access denied: Farmer accounts cannot access the administrative portal.');
      }
      setToken(res.data.token);
      setUser(u);
      localStorage.setItem('kisan_admin_token', res.data.token);
      localStorage.setItem('kisan_admin_user', JSON.stringify(u));
    } else {
      throw new Error(res.data.message || 'Login failed');
    }
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('kisan_admin_token');
    localStorage.removeItem('kisan_admin_user');
    window.location.href = '/admin/login';
  };

  return (
    <AuthContext.Provider value={{ user, token, isAuthenticated: !!token, isLoading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within an AuthProvider');
  return context;
};
