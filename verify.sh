#!/bin/bash
set -e

echo "======================================================="
echo "  Running VaxiCare Automated Verification Test Suite"
echo "======================================================="

mkdir -p out
find src -name "*.java" > sources.txt

javac -d out -cp "lib/*" @sources.txt

java -cp "out:lib/*" com.vaxicare.main.VaxiCareApp --verify
