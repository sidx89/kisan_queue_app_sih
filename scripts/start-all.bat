@echo off
setlocal enabledelayedexpansion
title KisanProcure - Complete System & Cloudflare Tunnel

echo ===================================================
echo     KisanProcure - Smart Farmer Procurement System
echo             1-Click Complete System Launch
echo ===================================================
echo.

set "CONFIG_FILE=%~dp0config.ini"
set "XAMPP_PATH=C:\xampp"
set "BACKEND_PORT=5000"
set "ADMIN_PORT=3000"
set "CLOUDFLARED_EXE=%~dp0cloudflared.exe"

if exist "%CONFIG_FILE%" (
    for /f "tokens=1,2 delims==" %%a in ('type "%CONFIG_FILE%" ^| findstr /r "^[A-Za-z]"') do (
        if "%%a"=="XAMPP_PATH" set "XAMPP_PATH=%%b"
        if "%%a"=="BACKEND_PORT" set "BACKEND_PORT=%%b"
        if "%%a"=="ADMIN_PORT" set "ADMIN_PORT=%%b"
    )
)

echo [1/5] Checking MySQL Database...
netstat -ano | findstr :3306 >nul
if %errorlevel% neq 0 (
    echo Starting MySQL from %XAMPP_PATH%...
    if exist "%XAMPP_PATH%\mysql\bin\mysqld.exe" (
        start "MySQL Daemon" /min "%XAMPP_PATH%\mysql\bin\mysqld.exe" --defaults-file="%XAMPP_PATH%\mysql\bin\my.ini" --standalone
        timeout /t 3 /nobreak >nul
        echo MySQL started.
    ) else (
        echo [WARNING] MySQL executable not found at %XAMPP_PATH%\mysql\bin\mysqld.exe
    )
) else (
    echo MySQL is already active on port 3306.
)

echo.
echo [2/5] Starting Backend REST & Socket.IO Engine (Port %BACKEND_PORT%)...
netstat -ano | findstr :%BACKEND_PORT% >nul
if %errorlevel% neq 0 (
    cd /d "%~dp0..\backend"
    start "KisanProcure Backend" cmd /k "npm start"
    echo Backend process initiated.
    timeout /t 4 /nobreak >nul
) else (
    echo Backend is already running on port %BACKEND_PORT%.
)

echo.
echo [3/5] Starting Admin Web Control Panel (Port %ADMIN_PORT%)...
netstat -ano | findstr :%ADMIN_PORT% >nul
if %errorlevel% neq 0 (
    cd /d "%~dp0..\admin"
    start "KisanProcure Admin" cmd /k "npm run dev"
    echo Admin Vite dev server initiated.
    timeout /t 3 /nobreak >nul
) else (
    echo Admin panel is already running on port %ADMIN_PORT%.
)

echo.
echo [4/5] Enabling USB Fast ADB Reverse Connection...
where adb >nul 2>nul
if %errorlevel% equ 0 (
    adb reverse tcp:%BACKEND_PORT% tcp:%BACKEND_PORT% >nul 2>nul
    echo USB Android connection bridged (0ms direct latency).
) else (
    echo [INFO] ADB not in PATH (skipping USB reverse).
)

echo.
echo [5/5] Launching Cloudflare Public Internet Tunnel...
if exist "%CLOUDFLARED_EXE%" (
    start "KisanProcure Cloudflare Live Tunnel" cmd /k ""%CLOUDFLARED_EXE%" tunnel --url http://localhost:%BACKEND_PORT%"
    echo Cloudflare tunnel started in dedicated window.
) else (
    where cloudflared >nul 2>nul
    if %errorlevel% equ 0 (
        start "KisanProcure Cloudflare Live Tunnel" cmd /k "cloudflared tunnel --url http://localhost:%BACKEND_PORT%"
        echo Cloudflare tunnel started from PATH.
    ) else (
        echo Downloading cloudflared.exe...
        curl.exe -L -o "%CLOUDFLARED_EXE%" "https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-windows-amd64.exe"
        start "KisanProcure Cloudflare Live Tunnel" cmd /k ""%CLOUDFLARED_EXE%" tunnel --url http://localhost:%BACKEND_PORT%"
    )
)

echo.
echo ===================================================
echo   System is ready!
echo   * Admin Panel: http://localhost:%ADMIN_PORT%
echo   * Backend API: http://localhost:%BACKEND_PORT%/health
echo   * Cloudflare: Check the 'Cloudflare Live Tunnel' window for your public HTTPS link
echo ===================================================

timeout /t 2 /nobreak >nul
start http://localhost:%ADMIN_PORT%
pause
