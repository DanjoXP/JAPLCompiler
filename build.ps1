# Stirlang Compiler Build and Test Script (PowerShell)
$ErrorActionPreference = "Stop"

Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host "          Building and Installing Stirlang               " -ForegroundColor Cyan
Write-Host "=========================================================" -ForegroundColor Cyan

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

# 1. Clean previous build directories
$binDir = Join-Path $scriptDir "bin"
$buildDir = Join-Path $scriptDir "build"
$jarFile = Join-Path $scriptDir "stirlang.jar"

if (Test-Path $binDir) { Remove-Item -Recurse -Force $binDir }
New-Item -ItemType Directory -Path $binDir | Out-Null

# 2. Gather all Java source files
$sources = Get-ChildItem -Path "src" -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }

Write-Host "Compiling $($sources.Count) Java source files..." -ForegroundColor Yellow
javac -encoding UTF-8 -d $binDir $sources

if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation failed with exit code $LASTEXITCODE" -ForegroundColor Red
    exit 1
}
Write-Host "Java compilation succeeded." -ForegroundColor Green

# 3. Run Unit Tests
Write-Host "`nRunning Test Suite..." -ForegroundColor Yellow
java -cp $binDir com.stirlang.test.StirlangTestRunner

if ($LASTEXITCODE -ne 0) {
    Write-Host "Tests failed!" -ForegroundColor Red
    exit 1
}

# 4. Package executable JAR
Write-Host "`nPackaging stirlang.jar..." -ForegroundColor Yellow

$jarCmd = "jar"
if (-not (Get-Command "jar" -ErrorAction SilentlyContinue)) {
    if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\jar.exe")) {
        $jarCmd = "$env:JAVA_HOME\bin\jar.exe"
    } else {
        $jdkDirs = Get-ChildItem "C:\Program Files\Java" -Filter "jdk*" -Directory -ErrorAction SilentlyContinue
        if ($jdkDirs) {
            $candidate = Join-Path $jdkDirs[0].FullName "bin\jar.exe"
            if (Test-Path $candidate) {
                $jarCmd = $candidate
            }
        }
    }
}

& $jarCmd --create --file $jarFile --main-class com.stirlang.Main -C $binDir .

if ($LASTEXITCODE -eq 0 -and (Test-Path $jarFile)) {
    Write-Host "Successfully built $jarFile" -ForegroundColor Green

    # 5. Automatically install to User PATH
    Write-Host "`nInstalling Stirlang to your User PATH..." -ForegroundColor Yellow
    $userPath = [Environment]::GetEnvironmentVariable("Path", "User")
    if (-not $userPath -or ($userPath -split ';' -notcontains $scriptDir)) {
        $newPath = if ($userPath) { $userPath.TrimEnd(';') + ';' + $scriptDir } else { $scriptDir }
        [Environment]::SetEnvironmentVariable("Path", $newPath, "User")
        Write-Host "  [SUCCESS] Added $scriptDir to your User PATH!" -ForegroundColor Green
    } else {
        Write-Host "  [INFO] Stirlang directory is already in your User PATH." -ForegroundColor Gray
    }

    Write-Host "`n=========================================================" -ForegroundColor Cyan
    Write-Host " [SUCCESS] Stirlang is ready to use!" -ForegroundColor Green
    Write-Host "`n You can now open any Command Prompt or Terminal and run:" -ForegroundColor White
    Write-Host "   stirlang -v" -ForegroundColor Yellow
    Write-Host "   stirlang -update" -ForegroundColor Yellow
    Write-Host "   stirlang program.stirl" -ForegroundColor Yellow
    Write-Host "   stirlang examples\hello.stirl" -ForegroundColor Yellow
    Write-Host "=========================================================" -ForegroundColor Cyan
} else {
    Write-Host "Failed to create JAR file." -ForegroundColor Red
    exit 1
}
