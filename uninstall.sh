#!/usr/bin/env bash
set -e

echo "========================================================="
echo "          Uninstalling Stirlang Compiler                 "
echo "========================================================="

cd "$(dirname "$0")"

# 1. Remove symlinks
echo "Removing global symlinks..."
if [ -L "/usr/local/bin/stirlang" ]; then
    sudo rm -f "/usr/local/bin/stirlang" "/usr/local/bin/stirl" || rm -f "/usr/local/bin/stirlang" "/usr/local/bin/stirl" 2>/dev/null || true
fi
if [ -L "$HOME/.local/bin/stirlang" ]; then
    rm -f "$HOME/.local/bin/stirlang" "$HOME/.local/bin/stirl"
fi

# 2. Clean build artifacts
echo "Cleaning build artifacts..."
rm -rf bin build stirlang.jar

echo "========================================================="
echo "[SUCCESS] Stirlang has been uninstalled!"
echo "(To reinstall, simply run ./build.sh)"
echo "========================================================="
