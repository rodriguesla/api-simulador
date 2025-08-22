@echo off
REM Run Newman tests with Postman collection and environment

set COLLECTION=docs\TestesAutomatizados.postman_collection.json
set ENVIRONMENT=docs\HACK.postman_environment.json
set REPORT=docs\newman-report.html

newman run "%COLLECTION%" -e "%ENVIRONMENT%" -r html --reporter-html-export "%REPORT%"

echo Tests completed. Report generated at %REPORT%