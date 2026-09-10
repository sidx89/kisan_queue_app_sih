@echo off
title KisanProcure - Stop All Services

echo ===================================================
echo     KisanProcure - Smart Farmer Procurement System
echo                  Stopping All Services
echo ===================================================
echo.

echo [1/3] Stopping Admin and Backend Node.js processes...
taskkill /f /im node.exe >nul 2>nul
if %errorlevel% equ 0 (
    echo Terminated Node.js processes.
) else (
    echo No active Node.js processes found.
)

echo.
echo [2/3] Stopping Cloudflare Tunnel if running...
taskkill /f /im cloudflared.exe >nul 2>nul
if %errorlevel% equ 0 (
    echo Terminated Cloudflare tunnel.
) else (
    echo No cloudflared process found.
)

echo.
echo [3/3] Checking MySQL status...
echo [NOTE] Leaving MySQL running for database safety.
echo If you wish to stop MySQL manually, run:
echo   taskkill /f /im mysqld.exe

echo.
echo ===================================================
echo       All KisanProcure web services stopped.
echo ===================================================
pause
