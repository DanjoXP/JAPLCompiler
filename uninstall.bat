@echo off
setlocal enabledelayedexpansion

echo =========================================================
echo             Uninstalling JAPL Compiler
echo =========================================================

cd /d "%~dp0"

echo.
echo Removing JAPL from your User PATH...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$dir = '%~dp0'.TrimEnd('\'); $path = [Environment]::GetEnvironmentVariable('Path', 'User'); if ($path) { $parts = $path -split ';' | Where-Object { $_ -and $_ -ne $dir -and $_ -notlike '*JAPLCompiler*' }; $newPath = $parts -join ';'; [Environment]::SetEnvironmentVariable('Path', $newPath, 'User'); Write-Host '  [SUCCESS] Removed JAPL from your User PATH.' -ForegroundColor Green; } else { Write-Host '  [INFO] User PATH is empty.' -ForegroundColor Gray; }; $agyBin = Join-Path $env:LOCALAPPDATA 'agy\bin'; if (Test-Path $agyBin) { Remove-Item (Join-Path $agyBin 'japl.*') -Force -ErrorAction SilentlyContinue; }"

echo.
echo Cleaning generated build artifacts...
if exist bin rd /s /q bin
if exist build rd /s /q build
if exist japl.jar del /f /q japl.jar

echo.
echo =========================================================
echo  [SUCCESS] JAPL has been uninstalled!
echo.
echo  1. The 'japl' command has been removed from your PATH.
echo  2. Compiled binaries and japl.jar have been cleaned.
echo  (To reinstall at any time, simply run build.bat again)
echo =========================================================
echo.
pause
