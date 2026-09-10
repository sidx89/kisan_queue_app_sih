@echo off
title KisanProcure - Restart Backend

echo ===================================================
echo     KisanProcure - Restart Backend Server
echo ===================================================
echo.

set "CONFIG_FILE=%~dp0config.ini"
set "BACKEND_PORT=5000"

if exist "%CONFIG_FILE%" (
    for /f "tokens=1,2 delims==" %%a in ('type "%CONFIG_FILE%" ^| findstr /r "^[A-Za-z]"') do (
        if "%%a"=="BACKEND_PORT" set "BACKEND_PORT=%%b"
    )
)

echo Finding process on port %BACKEND_PORT%...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":%BACKEND_PORT% "') do (
    if not "%%a"=="0" (
        echo Killing PID %%a...
        taskkill /f /pid %%a >nul 2>nul
    )
)

echo.
echo Rebuilding Backend TypeScript...
cd /d "%~dp0..\backend"
call npm run build

echo.
echo Starting Backend Server...
start "KisanProcure Backend" cmd /k "npm start"
timeout /t 3 /nobreak >nul

curl -s http://localhost:%BACKEND_PORT%/health
echo.
echo Backend restarted successfully.
pause
