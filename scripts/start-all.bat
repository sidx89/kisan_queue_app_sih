@echo off
setlocal enabledelayedexpansion
title KisanProcure - Start All Services

echo ===================================================
echo     KisanProcure - Smart Farmer Procurement System
echo                  Starting All Services
echo ===================================================
echo.

set "CONFIG_FILE=%~dp0config.ini"
set "XAMPP_PATH=C:\xampp"
set "BACKEND_PORT=5000"
set "ADMIN_PORT=3000"

if exist "%CONFIG_FILE%" (
    for /f "tokens=1,2 delims==" %%a in ('type "%CONFIG_FILE%" ^| findstr /r "^[A-Za-z]"') do (
        if "%%a"=="XAMPP_PATH" set "XAMPP_PATH=%%b"
        if "%%a"=="BACKEND_PORT" set "BACKEND_PORT=%%b"
        if "%%a"=="ADMIN_PORT" set "ADMIN_PORT=%%b"
    )
)

echo [1/4] Checking MySQL service...
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
    echo MySQL is already running on port 3306.
)

echo.
echo [2/4] Starting Backend Server (Port %BACKEND_PORT%)...
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
echo [3/4] Starting Admin Web Control Panel (Port %ADMIN_PORT%)...
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
echo [4/4] Checking Cloudflare Tunnel...
where cloudflared >nul 2>nul
if %errorlevel% equ 0 (
    echo Starting Cloudflare quick tunnel for port %BACKEND_PORT%...
    start "Cloudflare Tunnel" cmd /k "cloudflared tunnel --url http://localhost:%BACKEND_PORT%"
) else (
    echo [INFO] 'cloudflared' not found in PATH.
    echo To expose the API to Android devices over the internet without port forwarding:
    echo   1. Download cloudflared from: https://github.com/cloudflare/cloudflared/releases
    echo   2. Run: cloudflared tunnel --url http://localhost:%BACKEND_PORT%
)

echo.
echo ===================================================
echo   System is ready!
echo   Admin Panel: http://localhost:%ADMIN_PORT%
echo   Backend API: http://localhost:%BACKEND_PORT%/health
echo ===================================================

start http://localhost:%ADMIN_PORT%
pause
