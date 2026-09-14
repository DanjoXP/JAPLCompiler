@echo off
setlocal
if /i "%~1"=="-update" goto do_update
if /i "%~1"=="--update" goto do_update
if /i "%~1"=="update" goto do_update

java -jar "%~dp0stirlang.jar" %*
exit /b %ERRORLEVEL%

:do_update
call "%~dp0update.bat" %2 %3 %4 %5 %6
exit /b %ERRORLEVEL%
