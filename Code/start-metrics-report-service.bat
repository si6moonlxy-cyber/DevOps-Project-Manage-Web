@echo off
setlocal
call "%~dp0scripts\set-java8.bat" || exit /b 1
pushd "%~dp0" || exit /b 1
if /I "%RUN_DB_INIT%"=="1" if /I not "%SKIP_DB_INIT%"=="1" (
    call "%~dp0init-database.bat" || exit /b 1
)
set "JAR_PATH=metrics-report-service\target\metrics-report-service-1.0.1-SNAPSHOT.jar"
if not exist "%JAR_PATH%" (
    echo [INFO] Service jar not found, packaging metrics-report-service...
    call "%~dp0mvnw.cmd" -o -pl metrics-report-service -am -DskipTests package || exit /b 1
)
java -jar "%JAR_PATH%"
