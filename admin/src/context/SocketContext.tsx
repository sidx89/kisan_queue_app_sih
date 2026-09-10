import React, { createContext, useContext, useEffect, useState } from 'react';
import { io, Socket } from 'socket.io-client';
import { useAuth } from './AuthContext';

interface SocketContextType {
  socket: Socket | null;
  isConnected: boolean;
  recentError: any | null;
  recentAlert: any | null;
  clearRecentError: () => void;
  clearRecentAlert: () => void;
}

const SocketContext = createContext<SocketContextType | undefined>(undefined);

export const SocketProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAuth();
  const [socket, setSocket] = useState<Socket | null>(null);
  const [isConnected, setIsConnected] = useState<boolean>(false);
  const [recentError, setRecentError] = useState<any | null>(null);
  const [recentAlert, setRecentAlert] = useState<any | null>(null);

  useEffect(() => {
    // Connect to origin or localhost:5000
    const s = io(window.location.origin, {
      transports: ['websocket', 'polling'],
    });

    s.on('connect', () => {
      setIsConnected(true);
      s.emit('join:admin');
    });

    s.on('disconnect', () => {
      setIsConnected(false);
    });

    s.on('system:error', (data) => {
      setRecentError(data);
    });

    s.on('system:alert', (data) => {
      setRecentAlert(data);
    });

    setSocket(s);

    return () => {
      s.disconnect();
    };
  }, [isAuthenticated]);

  return (
    <SocketContext.Provider
      value={{
        socket,
        isConnected,
        recentError,
        recentAlert,
        clearRecentError: () => setRecentError(null),
        clearRecentAlert: () => setRecentAlert(null),
      }}
    >
      {children}
    </SocketContext.Provider>
  );
};

export const useSocket = () => {
  const context = useContext(SocketContext);
  if (!context) throw new Error('useSocket must be used within a SocketProvider');
  return context;
};
