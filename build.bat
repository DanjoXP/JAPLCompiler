@echo off
setlocal enabledelayedexpansion

echo =========================================================
echo             Building and Installing JAPL Compiler
echo =========================================================

cd /d "%~dp0"

if exist bin rd /s /q bin
mkdir bin

echo Finding source files...
dir /s /b src\*.java > sources.txt

echo Compiling Java source files...
javac -encoding UTF-8 -d bin @sources.txt
if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Java compilation failed. Make sure JDK 17+ is installed.
    if exist sources.txt del sources.txt
    pause
    exit /b %ERRORLEVEL%
)
if exist sources.txt del sources.txt

echo.
echo Running Test Suite...
java -cp bin com.japl.test.JAPLTestRunner
if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Tests failed.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo Packaging japl.jar...

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

%JAR_CMD% --create --file japl.jar --main-class com.japl.Main -C bin .
if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Failed to create JAR file.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo Installing JAPL to your User PATH...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$d = '%~dp0'.TrimEnd('\'); $p = [Environment]::GetEnvironmentVariable('Path', 'User'); if (-not $p -or ($p -split ';' -notcontains $d)) { $newPath = if ($p) { $p.TrimEnd(';') + ';' + $d } else { $d }; [Environment]::SetEnvironmentVariable('Path', $newPath, 'User'); Write-Host '  [SUCCESS] Added JAPL to your User PATH!' -ForegroundColor Green } else { Write-Host '  [INFO] JAPL is already in your User PATH.' -ForegroundColor Gray }"

echo.
echo =========================================================
echo  [SUCCESS] JAPL is ready to use!
echo.
echo  You can now open any Command Prompt or Terminal and run:
echo    japl -v
echo    japl program.japl
echo    japl examples\hello.japl
echo =========================================================
echo.
pause
