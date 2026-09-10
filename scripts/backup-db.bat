@echo off
setlocal enabledelayedexpansion
title KisanProcure - Backup Database

echo ===================================================
echo     KisanProcure - Database Backup Utility
echo ===================================================
echo.

set "CONFIG_FILE=%~dp0config.ini"
set "XAMPP_PATH=C:\xampp"
set "DB_NAME=smart_procurement"

if exist "%CONFIG_FILE%" (
    for /f "tokens=1,2 delims==" %%a in ('type "%CONFIG_FILE%" ^| findstr /r "^[A-Za-z]"') do (
        if "%%a"=="XAMPP_PATH" set "XAMPP_PATH=%%b"
        if "%%a"=="DB_NAME" set "DB_NAME=%%b"
    )
)

set "BACKUP_DIR=%~dp0..\backups"
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

for /f "tokens=2 delims==" %%I in ('wmic os get localdatetime /value') do set datetime=%%I
set "TIMESTAMP=%datetime:~0,8%_%datetime:~8,6%"
set "BACKUP_FILE=%BACKUP_DIR%\backup_%DB_NAME%_%TIMESTAMP%.sql"

echo Exporting %DB_NAME% to:
echo   %BACKUP_FILE%
echo.

if exist "%XAMPP_PATH%\mysql\bin\mysqldump.exe" (
    "%XAMPP_PATH%\mysql\bin\mysqldump.exe" -u root %DB_NAME% > "%BACKUP_FILE%"
    if %errorlevel% equ 0 (
        echo [SUCCESS] Backup created successfully.
    ) else (
        echo [ERROR] mysqldump failed with code %errorlevel%.
    )
) else (
    echo [ERROR] mysqldump.exe not found at %XAMPP_PATH%\mysql\bin\mysqldump.exe
)

echo.
pause
