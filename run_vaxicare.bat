@echo off
setlocal
title VaxiCare - Smart Vaccination Slot Booking and Tracking System

echo =========================================================================
echo   VaxiCare - Smart Vaccination Slot Booking & Tracking System
echo   Pure Core Java (JDK 21) ^| Pure AWT ^| SQLite JDBC
echo =========================================================================
echo.

java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java is not installed or not in your system PATH.
    echo Please install JDK 21+ and verify with 'java -version'.
    pause
    exit /b 1
)

if not exist "out" mkdir out

if not exist "out\com\vaxicare\main\VaxiCareApp.class" (
    echo [INFO] Compiling VaxiCare codebase for the first time...
    dir /s /b src\*.java > sources.txt
    javac -d out -cp "lib/*" @sources.txt
    if %errorlevel% neq 0 (
        echo [ERROR] Compilation failed! Please check source files and lib dependencies.
        pause
        exit /b 1
    )
    echo [OK] Compilation successful.
    echo.
)

echo [INFO] Starting VaxiCare Pure AWT Application...
java -cp "out;lib/*" com.vaxicare.main.VaxiCareApp %*
if %errorlevel% neq 0 (
    echo.
    echo [NOTICE] Application closed with code %errorlevel%.
    pause
)
