#!/usr/bin/env bash
set -e

echo "========================================================="
echo "            Updating Stirlang Compiler                   "
echo "========================================================="

cd "$(dirname "$0")"

echo "Checking for updates from GitHub..."

if command -v git >/dev/null 2>&1 && [ -d ".git" ]; then
    echo "Fetching latest updates via Git..."
    git pull origin master || git pull
else
    echo "Downloading latest version from GitHub..."
    curl -L "https://github.com/DanjoXP/JAPLCompiler/archive/refs/heads/master.zip" -o stirlang_update.zip
    unzip -q -o stirlang_update.zip
    cp -rf JAPLCompiler-master/* . 2>/dev/null || cp -rf *master/* .
    rm -rf stirlang_update.zip *master
fi

echo "Rebuilding compiler..."
./build.sh

echo "========================================================="
echo "[SUCCESS] Stirlang is up to date!"
echo "========================================================="
./stirlang -v
