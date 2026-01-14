@echo off
chcp 65001 >nul
echo ========================================
echo  Lottery System Setup Script
echo ========================================
echo.

REM Step 1: Create directory structure
echo [Step 1] Creating project directory structure...
if not exist "src\lottery\dao" mkdir "src\lottery\dao"
if not exist "src\lottery\model" mkdir "src\lottery\model"
if not exist "src\lottery\ui" mkdir "src\lottery\ui"
if not exist "src\lottery\util" mkdir "src\lottery\util"
if not exist "data" mkdir "data"
if not exist "lib" mkdir "lib"

echo [Done] Directory structure created.
echo.

REM Step 2: Check Java environment
echo [Step 2] Checking Java environment...
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java not found!
    echo [INFO] Please install Java 8 or higher
    echo [INFO] Download from: https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html
    pause
    exit /b 1
)

echo [OK] Java environment is ready.
echo.

REM Step 3: Ask if want to download dependencies
echo [Step 3] Download dependencies? (Y/N)
set /p choice=Your choice:
if /i "%choice%"=="Y" (
    echo.
    call download-deps.bat
) else (
    echo [INFO] Skipping dependency download
    echo [NOTE] Make sure required jar files are in lib folder
)

echo.
echo [Step 4] Compiling the system...
call compile.bat

echo.
echo ========================================
echo  Setup Complete!
echo  To run the system: run.bat
echo  To recompile: compile.bat
echo ========================================
echo.
pause