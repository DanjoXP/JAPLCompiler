@echo off
setlocal
java -jar "%~dp0stirlang.jar" %*
exit /b %ERRORLEVEL%
