@echo off
REM
taskkill /IM java.exe /F
REM
start "" cmd /c start-test.bat
REM
timeout /t 30

set COLLECTION=docs\TestesAutomatizados.postman_collection.json
set ENVIRONMENT=docs\HACK.postman_environment.json
set REPORT=docs\newman-report.html

newman run "%COLLECTION%" -e "%ENVIRONMENT%" -r html --reporter-html-export "%REPORT%"

echo Tests completed. Report generated at %REPORT%