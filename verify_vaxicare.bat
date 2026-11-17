@echo off
setlocal
title VaxiCare - Headless Verification Suite

echo =========================================================================
echo   VaxiCare - Automated Verification Suite
echo   Testing SQLite JDBC, Collections, Binary Streams, Threads & Exceptions
echo =========================================================================
echo.

if not exist "out\com\vaxicare\main\VaxiCareApp.class" (
    echo [INFO] Compiling VaxiCare codebase...
    if not exist "out" mkdir out
    dir /s /b src\*.java > sources.txt
    javac -d out -cp "lib/*" @sources.txt
    if %errorlevel% neq 0 (
        echo [ERROR] Compilation failed!
        pause
        exit /b 1
    )
)

echo [INFO] Running Verification Suite in Headless Mode...
echo.
java -cp "out;lib/*" com.vaxicare.main.VaxiCareApp --verify
echo.
echo =========================================================================
echo   Verification execution complete. Press any key to exit.
echo =========================================================================
pause
