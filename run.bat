@echo off
setlocal
echo ===================================================
echo   Compiling and Launching VaxiCare (Pure Core Java)
echo ===================================================

if not exist out mkdir out

echo Generating source file list...
powershell -NoProfile -Command "Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { ($_.FullName.Substring($PWD.Path.Length + 1) -replace '\\', '/') } | Set-Content -Encoding ascii sources.txt"

echo Compiling Java packages into out/...
javac -d out -cp "lib/*" "@sources.txt"
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Compilation failed.
    pause
    exit /b %ERRORLEVEL%
)

echo Launching VaxiCare AWT GUI...
java -cp "out;lib/*" com.vaxicare.main.VaxiCareApp
pause
