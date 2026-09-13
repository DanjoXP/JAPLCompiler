#!/usr/bin/env bash
set -e

echo "========================================================="
echo "            Uninstalling JAPL Compiler                   "
echo "========================================================="

cd "$(dirname "$0")"

# 1. Remove symlinks
echo "Removing global symlinks..."
if [ -L "/usr/local/bin/japl" ]; then
    sudo rm -f "/usr/local/bin/japl" || rm -f "/usr/local/bin/japl" 2>/dev/null || true
fi
if [ -L "$HOME/.local/bin/japl" ]; then
    rm -f "$HOME/.local/bin/japl"
fi

# 2. Clean build artifacts
echo "Cleaning build artifacts..."
rm -rf bin build japl.jar

echo "========================================================="
echo "[SUCCESS] JAPL has been uninstalled!"
echo "(To reinstall, simply run ./build.sh)"
echo "========================================================="
