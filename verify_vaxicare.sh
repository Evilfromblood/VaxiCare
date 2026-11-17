#!/usr/bin/env bash
set -e

echo "========================================================================="
echo "  VaxiCare - Automated Verification Suite"
echo "========================================================================="
echo ""

cd "$(dirname "$0")"

mkdir -p out

if [ ! -f "out/com/vaxicare/main/VaxiCareApp.class" ]; then
    echo "[INFO] Compiling VaxiCare codebase..."
    find src -name "*.java" > sources.txt
    javac -d out -cp "lib/*" @sources.txt
fi

echo "[INFO] Running Verification Suite in Headless Mode..."
echo ""
java -cp "out:lib/*" com.vaxicare.main.VaxiCareApp --verify
echo ""
echo "========================================================================="
