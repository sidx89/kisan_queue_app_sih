@echo off
setlocal enabledelayedexpansion
title KisanProcure - Cloudflare Public Tunnel

echo ===================================================
echo     KisanProcure - Cloudflare Live Internet Tunnel
echo ===================================================
echo.

set "CLOUDFLARED_EXE=%~dp0cloudflared.exe"
set "BACKEND_PORT=5000"

:: 1. Verify Backend is running
netstat -ano | findstr :%BACKEND_PORT% >nul
if %errorlevel% neq 0 (
    echo [INFO] Backend is not running on port %BACKEND_PORT%.
    echo Starting backend first...
    cd /d "%~dp0..\backend"
    start "KisanProcure Backend" cmd /k "npm start"
    timeout /t 4 /nobreak >nul
) else (
    echo [OK] Backend is already active on port %BACKEND_PORT%.
)

:: 2. Check for cloudflared executable
if not exist "%CLOUDFLARED_EXE%" (
    where cloudflared >nul 2>nul
    if %errorlevel% equ 0 (
        set "CLOUDFLARED_EXE=cloudflared"
    ) else (
        echo [ERROR] cloudflared.exe not found at %CLOUDFLARED_EXE%.
        echo Downloading cloudflared.exe...
        curl.exe -L -o "%CLOUDFLARED_EXE%" "https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-windows-amd64.exe"
    )
)

echo.
echo ===================================================
echo   LAUNCHING CLOUDFLARE PUBLIC TUNNEL...
echo ===================================================
echo.
echo   * Looking for the Public HTTPS link...
echo   * Once created, your URL will appear below:
echo     e.g., https://xxxxx.trycloudflare.com
echo.
echo   * Copy that URL and paste it into the Android App:
echo     App -> Profile -> API Base URL
echo ===================================================
echo.

"%CLOUDFLARED_EXE%" tunnel --url http://localhost:%BACKEND_PORT%

pause
