@echo off
title KisanProcure - Build Android APK

echo ===================================================
echo     KisanProcure - Build Android Debug APK
echo ===================================================
echo.

cd /d "%~dp0..\android"

if exist "gradlew.bat" (
    echo Running Gradle assembleDebug...
    call gradlew.bat assembleDebug
    if %errorlevel% equ 0 (
        echo.
        echo [SUCCESS] APK built successfully!
        echo Location: android\app\build\outputs\apk\debug\app-debug.apk
    ) else (
        echo.
        echo [ERROR] Gradle build failed with code %errorlevel%.
    )
) else (
    echo [ERROR] gradlew.bat not found in android directory.
)

echo.
pause
