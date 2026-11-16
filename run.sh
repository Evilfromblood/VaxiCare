#!/bin/bash
set -e

echo "==================================================="
echo "  Compiling and Launching VaxiCare (Pure Core Java)"
echo "==================================================="

mkdir -p out
find src -name "*.java" > sources.txt

echo "Compiling Java packages into out/..."
javac -d out -cp "lib/*" @sources.txt

echo "Launching VaxiCare..."
java -cp "out:lib/*" com.vaxicare.main.VaxiCareApp
