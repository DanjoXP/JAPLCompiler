#!/usr/bin/env bash
set -e

echo "========================================================="
echo "            Building JAPL Compiler                       "
echo "========================================================="

cd "$(dirname "$0")"
rm -rf bin
mkdir -p bin

echo "Compiling Java source files..."
javac -encoding UTF-8 -d bin $(find src -name "*.java")

echo "Running Test Suite..."
java -cp bin com.japl.test.JAPLTestRunner

echo "Packaging japl.jar..."
jar --create --file japl.jar --main-class com.japl.Main -C bin .

chmod +x japl 2>/dev/null || true
echo "========================================================="
echo "[SUCCESS] Successfully built japl.jar!"
echo "Run: ./japl examples/hello.japl"
echo "========================================================="
