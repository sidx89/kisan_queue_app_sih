@echo off
setlocal enabledelayedexpansion
title KisanProcure - System Diagnostics

echo ===================================================
echo     KisanProcure - System Health Diagnostics
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

echo [1] Checking Node.js and NPM...
node -v >nul 2>nul
if %errorlevel% equ 0 (
    for /f "tokens=*" %%v in ('node -v') do echo   ✓ Node.js: %%v
) else (
    echo   ✗ Node.js NOT found in PATH.
)
cmd /c "npm -v" >nul 2>nul
if %errorlevel% equ 0 (
    for /f "tokens=*" %%v in ('cmd /c "npm -v"') do echo   ✓ NPM: %%v
) else (
    echo   ✗ NPM NOT found in PATH.
)

echo.
echo [2] Checking Java JDK...
java -version >nul 2>nul
if %errorlevel% equ 0 (
    echo   ✓ Java JDK is installed and in PATH.
) else (
    echo   ✗ Java JDK NOT found in PATH.
)

echo.
echo [3] Checking Android Debug Bridge (ADB)...
adb version >nul 2>nul
if %errorlevel% equ 0 (
    echo   ✓ ADB is available. Connected devices:
    adb devices
) else (
    echo   ✗ ADB NOT found in PATH.
)

echo.
echo [4] Checking MySQL Database Service...
netstat -ano | findstr :3306 >nul
if %errorlevel% equ 0 (
    echo   ✓ MySQL listening on port 3306.
) else (
    echo   ✗ MySQL NOT running on port 3306.
)

echo.
echo [5] Checking Backend Server (Port %BACKEND_PORT%)...
curl -s http://localhost:%BACKEND_PORT%/health >nul 2>nul
if %errorlevel% equ 0 (
    echo   ✓ Backend is HEALTHY at http://localhost:%BACKEND_PORT%/health
    curl -s http://localhost:%BACKEND_PORT%/health
    echo.
) else (
    echo   ✗ Backend is not responding on port %BACKEND_PORT%.
)

echo.
echo [6] Checking Admin Web Panel (Port %ADMIN_PORT%)...
netstat -ano | findstr :%ADMIN_PORT% >nul
if %errorlevel% equ 0 (
    echo   ✓ Admin Panel is listening on port %ADMIN_PORT%.
) else (
    echo   ✗ Admin Panel NOT running on port %ADMIN_PORT%.
)

echo.
echo [7] Checking Log Files...
if exist "%~dp0..\backend\logs\app.log" (
    echo   ✓ backend\logs\app.log exists.
)
if exist "%~dp0..\backend\logs\error.log" (
    echo   ✓ backend\logs\error.log exists.
)

echo.
echo ===================================================
echo               Diagnostics Complete
echo ===================================================
pause
