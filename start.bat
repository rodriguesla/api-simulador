@echo off
call mvn clean package
if errorlevel 1 (
    echo Maven build failed. Exiting.
    exit /b 1
)
call docker-compose up --build