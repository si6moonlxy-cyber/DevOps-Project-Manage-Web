@echo off
setlocal
call "%~dp0scripts\set-java8.bat" || exit /b 1
call "%~dp0mvnw.cmd" -o -pl database-bootstrap org.springframework.boot:spring-boot-maven-plugin:2.7.18:run "-Dspring-boot.run.fork=false" "-DskipTests"
