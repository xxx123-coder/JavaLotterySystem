@echo off
chcp 65001 >nul
echo Creating Lottery System Project Structure...

REM Create main directories
mkdir src\lottery\dao 2>nul
mkdir src\lottery\model 2>nul
mkdir src\lottery\ui 2>nul
mkdir src\lottery\util 2>nul
mkdir data 2>nul
mkdir lib 2>nul

REM Create minimal required files if they don't exist
if not exist "src\lottery\Main.java" (
    echo Creating Main.java...
    (
    echo package lottery;
    echo.
    echo public class Main {
    echo     public static void main(String[] args) {
    echo         System.out.println("Lottery System Starting...");
    echo         System.out.println("Data directory: data/");
    echo         System.out.println("System ready!");
    echo     }
    echo }
    ) > src\lottery\Main.java
)

echo.
echo Project structure created!
echo Run quick-run.bat to start the system.
echo.
pause