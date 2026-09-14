# Stirlang Update Script (PowerShell)
$ErrorActionPreference = "Continue"

Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host "            Updating Stirlang Compiler                   " -ForegroundColor Cyan
Write-Host "=========================================================" -ForegroundColor Cyan

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

Write-Host "`nChecking for updates from GitHub..." -ForegroundColor Yellow

$gitInstalled = Get-Command "git" -ErrorAction SilentlyContinue
$isGitRepo = Test-Path (Join-Path $scriptDir ".git")

if ($gitInstalled -and $isGitRepo) {
    Write-Host "Fetching latest updates via Git..." -ForegroundColor Yellow
    & git pull origin master 2>$null
    if ($LASTEXITCODE -ne 0) {
        & git pull origin main 2>$null
        if ($LASTEXITCODE -ne 0) {
            & git pull
        }
    }
} else {
    Write-Host "Downloading latest version from GitHub..." -ForegroundColor Yellow
    $url = "https://github.com/DanjoXP/JAPLCompiler/archive/refs/heads/master.zip"
    try {
        Invoke-WebRequest -Uri $url -OutFile "stirlang_update.zip"
        Expand-Archive -Path "stirlang_update.zip" -DestinationPath "stirlang_temp" -Force
        $extracted = Get-ChildItem -Path "stirlang_temp" -Directory | Select-Object -First 1
        if ($extracted) {
            Copy-Item -Path (Join-Path $extracted.FullName "*") -Destination "." -Recurse -Force
        }
        Remove-Item -Recurse -Force "stirlang_temp", "stirlang_update.zip" -ErrorAction SilentlyContinue
        Write-Host "Downloaded latest files successfully." -ForegroundColor Green
    } catch {
        Write-Host "Failed to download update: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`nRebuilding Stirlang compiler with latest changes..." -ForegroundColor Yellow
& (Join-Path $scriptDir "build.ps1")

Write-Host "`n=========================================================" -ForegroundColor Cyan
Write-Host " [SUCCESS] Stirlang is up to date!" -ForegroundColor Green
Write-Host "=========================================================" -ForegroundColor Cyan
& (Join-Path $scriptDir "stirlang.ps1") -v
