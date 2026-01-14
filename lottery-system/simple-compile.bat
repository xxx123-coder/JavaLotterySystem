@echo off
chcp 65001 >nul
echo ========================================
echo  Lottery System Simple Compiler
echo ========================================

echo [Step 1] Checking Java...
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java not found!
    pause
    exit /b 1
)

echo [Step 2] Creating directories...
if not exist "bin" mkdir bin
if not exist "data" mkdir data

echo [Step 3] Compiling source files...
REM Build classpath
set CLASSPATH=bin

REM Add libraries if they exist
if exist "lib\*.jar" (
    for %%i in (lib\*.jar) do (
        set CLASSPATH=!CLASSPATH!;%%i
    )
)

REM Compile main Java files
javac -encoding UTF-8 -d bin -cp "%CLASSPATH%" src\lottery\*.java

REM Compile subdirectories
for /d %%d in (src\lottery\*) do (
    if exist "src\lottery\%%~nxd\*.java" (
        javac -encoding UTF-8 -d bin -cp "%CLASSPATH%" src\lottery\%%~nxd\*.java
    )
)

if errorlevel 1 (
    echo [ERROR] Compilation failed!
    echo [INFO] Trying simple compilation without dependencies...

    REM Try simple compilation (no external dependencies)
    javac -encoding UTF-8 -d bin src\lottery\*.java
    for /d %%d in (src\lottery\*) do (
        if exist "src\lottery\%%~nxd\*.java" (
            javac -encoding UTF-8 -d bin src\lottery\%%~nxd\*.java
        )
    )
)

echo.
echo ========================================
echo  Compilation complete!
echo  Run: java -cp "bin;lib\*" lottery.Main
echo ========================================
echo.
pause