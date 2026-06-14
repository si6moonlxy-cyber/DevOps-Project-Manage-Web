@echo off
setlocal

if exist "D:\java\jdk1.8\bin\java.exe" (
    set "JAVA_HOME=D:\java\jdk1.8"
    set "PATH=D:\java\jdk1.8\bin;%PATH%"
    endlocal & (
        set "JAVA_HOME=D:\java\jdk1.8"
        set "PATH=D:\java\jdk1.8\bin;%PATH%"
    )
    exit /b 0
)

echo [ERROR] Java 8 JDK was not found at D:\java\jdk1.8\bin\java.exe
echo [ERROR] Please install JDK 1.8 or update this script before starting services.
exit /b 1
