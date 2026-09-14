#!/usr/bin/env bash
set -e

echo "========================================================="
echo "          Building and Installing Stirlang               "
echo "========================================================="

cd "$(dirname "$0")"
rm -rf bin
mkdir -p bin

echo "Compiling Java source files..."
javac -encoding UTF-8 -d bin $(find src -name "*.java")

echo "Running Test Suite..."
java -cp bin com.stirlang.test.StirlangTestRunner

echo "Packaging stirlang.jar..."
jar --create --file stirlang.jar --main-class com.stirlang.Main -C bin .

chmod +x stirlang stirl update.sh uninstall.sh 2>/dev/null || true
echo "========================================================="
echo "[SUCCESS] Successfully built stirlang.jar!"
echo "Run: ./stirlang examples/hello.stirl"
echo "========================================================="
