# Stirlang Uninstaller Script (PowerShell)
$ErrorActionPreference = "SilentlyContinue"

Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host "          Uninstalling Stirlang Compiler                 " -ForegroundColor Cyan
Write-Host "=========================================================" -ForegroundColor Cyan

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# 1. Remove from User PATH
Write-Host "`nRemoving Stirlang from your User PATH..." -ForegroundColor Yellow
$userPath = [Environment]::GetEnvironmentVariable("Path", "User")
if ($userPath) {
    $parts = $userPath -split ';' | Where-Object { $_ -and $_ -ne $scriptDir -and $_ -notlike '*JAPLCompiler*' -and $_ -notlike '*Stirlang*' }
    $newPath = $parts -join ';'
    [Environment]::SetEnvironmentVariable("Path", $newPath, "User")
    Write-Host "  [SUCCESS] Removed Stirlang from your User PATH." -ForegroundColor Green
}

# 2. Clean temporary global launchers if present
$agyBin = Join-Path $env:LOCALAPPDATA "agy\bin"
if (Test-Path $agyBin) {
    Remove-Item (Join-Path $agyBin "stirl*") -Force
    Remove-Item (Join-Path $agyBin "japl*") -Force
}

# 3. Clean compiled artifacts
Write-Host "`nCleaning build artifacts..." -ForegroundColor Yellow
$binDir = Join-Path $scriptDir "bin"
$buildDir = Join-Path $scriptDir "build"
$jarFile = Join-Path $scriptDir "stirlang.jar"

if (Test-Path $binDir) { Remove-Item -Recurse -Force $binDir }
if (Test-Path $buildDir) { Remove-Item -Recurse -Force $buildDir }
if (Test-Path $jarFile) { Remove-Item -Force $jarFile }

Write-Host "`n=========================================================" -ForegroundColor Cyan
Write-Host " [SUCCESS] Stirlang has been uninstalled!" -ForegroundColor Green
Write-Host "`n 1. 'stirlang' command removed from PATH." -ForegroundColor White
Write-Host " 2. Build artifacts and stirlang.jar have been cleaned." -ForegroundColor White
Write-Host " (To reinstall anytime, simply run build.bat or .\build.ps1)" -ForegroundColor Gray
Write-Host "=========================================================" -ForegroundColor Cyan
