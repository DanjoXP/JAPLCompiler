@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0update.ps1"
if not "%1"=="--no-pause" pause
