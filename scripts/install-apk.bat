@echo off
title KisanProcure - Install APK to Device

echo ===================================================
echo     KisanProcure - Install APK via ADB
echo ===================================================
echo.

set "APK_PATH=%~dp0..\android\app\build\outputs\apk\debug\app-debug.apk"
set "PACKAGE=com.kisanprocure.app"
set "ACTIVITY=com.kisanprocure.app.MainActivity"

where adb >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] adb is not found in your PATH.
    pause
    exit /b
)

echo Checking for connected Android devices...
adb devices
echo.

if not exist "%APK_PATH%" (
    echo [ERROR] APK not found at %APK_PATH%.
    echo Please run build-apk.bat first.
    pause
    exit /b
)

echo Installing %APK_PATH%...
adb install -r "%APK_PATH%"

if %errorlevel% equ 0 (
    echo.
    echo [SUCCESS] APK installed.
    echo Launching KisanProcure on device...
    adb shell am start -n "%PACKAGE%/%ACTIVITY%"
) else (
    echo.
    echo [ERROR] ADB install failed.
)

echo.
pause
