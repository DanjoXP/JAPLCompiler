@echo off
setlocal enabledelayedexpansion

echo =========================================================
echo           Installing Stirlang Compiler
echo =========================================================

cd /d "%~dp0"

javac -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] 'javac' not found. Please ensure JDK 17 or higher is installed and on PATH.
    if not "%1"=="--no-pause" pause
    exit /b 1
)

if exist bin rd /s /q bin
mkdir bin

echo Compiling Java source files...
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d bin @sources.txt
set COMPILE_ERR=%ERRORLEVEL%
if exist sources.txt del sources.txt
if %COMPILE_ERR% neq 0 (
    echo.
    echo [ERROR] Java compilation failed.
    if not "%1"=="--no-pause" pause
    exit /b %COMPILE_ERR%
)
echo Java compilation succeeded.

echo.
echo Running Test Suite...
java -cp bin com.stirlang.test.StirlangTestRunner
if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Test suite failed.
    if not "%1"=="--no-pause" pause
    exit /b %ERRORLEVEL%
)

echo.
echo Packaging stirlang.jar...
set JAR_CMD=jar
where jar >nul 2>&1
if %ERRORLEVEL% neq 0 (
    if defined JAVA_HOME (
        if exist "%JAVA_HOME%\bin\jar.exe" set JAR_CMD="%JAVA_HOME%\bin\jar.exe"
    ) else (
        for /d %%D in ("C:\Program Files\Java\jdk*") do (
            if exist "%%D\bin\jar.exe" set JAR_CMD="%%D\bin\jar.exe"
        )
    )
)

%JAR_CMD% --create --file stirlang.jar --main-class com.stirlang.Main -C bin .
if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Failed to package stirlang.jar.
    if not "%1"=="--no-pause" pause
    exit /b %ERRORLEVEL%
)
echo Successfully packaged stirlang.jar.

echo.
echo Installing Stirlang to your User PATH...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$d = '%~dp0'.TrimEnd('\'); $p = [Environment]::GetEnvironmentVariable('Path', 'User'); if (-not $p -or ($p -split ';' -notcontains $d)) { $newPath = if ($p) { $p.TrimEnd(';') + ';' + $d } else { $d }; [Environment]::SetEnvironmentVariable('Path', $newPath, 'User'); Write-Host '  [SUCCESS] Added Stirlang to your User PATH!' -ForegroundColor Green } else { Write-Host '  [INFO] Stirlang is already in your User PATH.' -ForegroundColor Gray }"

echo.
echo =========================================================
echo  [SUCCESS] Stirlang is installed and ready to use.
echo.
echo  You can now open any Command Prompt or Terminal and run:
echo    stirlang -v
echo    stirlang your_program.stirl
echo    stirlang examples\hello.stirl
echo =========================================================
echo.
if not "%1"=="--no-pause" pause
