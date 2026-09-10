@echo off
title KisanProcure - Reset Demo Database

echo ===================================================
echo     KisanProcure - Reset Database to Fresh State
echo ===================================================
echo.
echo WARNING: This will drop and recreate all tables in 'smart_procurement'
echo and re-seed clean demo data.
echo.
set /p CONFIRM="Are you sure you want to proceed? (Y/N): "
if /i not "%CONFIRM%"=="Y" (
    echo Reset cancelled.
    pause
    exit /b
)

set "CONFIG_FILE=%~dp0config.ini"
set "XAMPP_PATH=C:\xampp"

if exist "%CONFIG_FILE%" (
    for /f "tokens=1,2 delims==" %%a in ('type "%CONFIG_FILE%" ^| findstr /r "^[A-Za-z]"') do (
        if "%%a"=="XAMPP_PATH" set "XAMPP_PATH=%%b"
    )
)

set "SCHEMA_SQL=%~dp0..\backend\src\database\schema.sql"
set "SEED_SQL=%~dp0..\backend\src\database\seed.sql"

if exist "%XAMPP_PATH%\mysql\bin\mysql.exe" (
    echo [1/2] Applying database schema...
    "%XAMPP_PATH%\mysql\bin\mysql.exe" -u root < "%SCHEMA_SQL%"

    echo [2/2] Seeding initial test data...
    "%XAMPP_PATH%\mysql\bin\mysql.exe" -u root < "%SEED_SQL%"

    echo.
    echo [SUCCESS] Database reset complete!
    echo All demo accounts (Farmer, Operator, Admin) restored with password: password123
) else (
    echo [ERROR] mysql.exe not found at %XAMPP_PATH%\mysql\bin\mysql.exe
)

echo.
pause
