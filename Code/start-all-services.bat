@echo off
setlocal

call "%~dp0scripts\set-java8.bat" || exit /b 1
if /I "%RUN_DB_INIT%"=="1" (
    call "%~dp0init-database.bat" || exit /b 1
) else (
    echo [INFO] Skipping database bootstrap. Run init-database.bat manually when schema or seed data must be refreshed.
)

set "SKIP_DB_INIT=1"

echo [INFO] Packaging all services before startup...
call "%~dp0mvnw.cmd" -o -DskipTests package || exit /b 1

start "organization-permission-service" cmd /k call "%~dp0start-organization-permission-service.bat"
timeout /t 2 /nobreak >nul
start "project-delivery-service" cmd /k call "%~dp0start-project-delivery-service.bat"
timeout /t 2 /nobreak >nul
start "devops-data-service" cmd /k call "%~dp0start-devops-data-service.bat"
timeout /t 2 /nobreak >nul
start "metrics-report-service" cmd /k call "%~dp0start-metrics-report-service.bat"
timeout /t 2 /nobreak >nul
start "audit-config-service" cmd /k call "%~dp0start-audit-config-service.bat"
