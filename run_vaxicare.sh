#!/usr/bin/env bash
set -e

echo "========================================================================="
echo "  VaxiCare - Smart Vaccination Slot Booking & Tracking System"
echo "  Pure Core Java (JDK 21) | Pure AWT | SQLite JDBC"
echo "========================================================================="
echo ""

cd "$(dirname "$0")"

if ! command -v java &> /dev/null; then
    echo "[ERROR] 'java' command not found. Please install JDK 21+."
    exit 1
fi

mkdir -p out

if [ ! -f "out/com/vaxicare/main/VaxiCareApp.class" ]; then
    echo "[INFO] Compiling VaxiCare codebase..."
    find src -name "*.java" > sources.txt
    javac -d out -cp "lib/*" @sources.txt
    echo "[OK] Compilation successful."
    echo ""
fi

echo "[INFO] Starting VaxiCare Pure AWT Application..."
java -cp "out:lib/*" com.vaxicare.main.VaxiCareApp "$@"
