@echo off
setlocal enabledelayedexpansion

echo =========================================================
echo            Uninstalling Stirlang Compiler
echo =========================================================

cd /d "%~dp0"

echo.
echo Removing Stirlang from your User PATH...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$dir = '%~dp0'.TrimEnd('\'); $path = [Environment]::GetEnvironmentVariable('Path', 'User'); if ($path) { $parts = $path -split ';' | Where-Object { $_ -and $_ -ne $dir -and $_ -notlike '*JAPLCompiler*' -and $_ -notlike '*Stirlang*' }; $newPath = $parts -join ';'; [Environment]::SetEnvironmentVariable('Path', $newPath, 'User'); Write-Host '  [SUCCESS] Removed Stirlang from your User PATH.' -ForegroundColor Green; } else { Write-Host '  [INFO] User PATH is empty.' -ForegroundColor Gray; }; $agyBin = Join-Path $env:LOCALAPPDATA 'agy\bin'; if (Test-Path $agyBin) { Remove-Item (Join-Path $agyBin 'stirl*') -Force -ErrorAction SilentlyContinue; Remove-Item (Join-Path $agyBin 'japl*') -Force -ErrorAction SilentlyContinue; }"

echo.
echo Cleaning generated build artifacts...
if exist bin rd /s /q bin
if exist build rd /s /q build
if exist stirlang.jar del /f /q stirlang.jar

echo.
echo =========================================================
echo  [SUCCESS] Stirlang has been uninstalled!
echo.
echo  1. The 'stirlang' command has been removed from your PATH.
echo  2. Compiled binaries and stirlang.jar have been cleaned.
echo  (To reinstall at any time, simply run build.bat again)
echo =========================================================
echo.
pause
