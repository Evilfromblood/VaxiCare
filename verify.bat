@echo off
setlocal
echo =======================================================
echo   Running VaxiCare Automated Verification Test Suite
echo =======================================================

if not exist out mkdir out

powershell -NoProfile -Command "Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { ($_.FullName.Substring($PWD.Path.Length + 1) -replace '\\', '/') } | Set-Content -Encoding ascii sources.txt"

javac -d out -cp "lib/*" "@sources.txt"
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Compilation failed.
    pause
    exit /b %ERRORLEVEL%
)

java -cp "out;lib/*" com.vaxicare.main.VaxiCareApp --verify
pause
