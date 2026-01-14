@echo off
chcp 65001 >nul
echo ========================================
echo  Lottery System Quick Start
echo ========================================

REM Check if compiled
if not exist "bin\lottery\Main.class" (
    echo [INFO] Not compiled yet, compiling...
    call simple-compile.bat
    if errorlevel 1 (
        pause
        exit /b 1
    )
)

REM Create data directory
if not exist "data" mkdir data

echo [INFO] Starting Lottery System...
echo.

REM Run with or without dependencies
if exist "lib\*.jar" (
    java -cp "bin;lib\*" lottery.Main
) else (
    java -cp "bin" lottery.Main
)

if errorlevel 1 (
    echo.
    echo [ERROR] System failed to start!
    pause
    exit /b 1
)

pause