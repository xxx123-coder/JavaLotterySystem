@echo off
echo 启动彩票系统...
echo.

REM 检查是否已编译
if not exist "bin\lottery\Main.class" (
    echo 系统未编译，正在编译...
    call compile-simple.bat
    if errorlevel 1 (
        pause
        exit /b 1
    )
)

REM 创建数据目录
if not exist data mkdir data

echo 启动彩票系统...
echo.
java -cp bin lottery.Main

if errorlevel 1 (
    echo.
    echo 系统启动失败！
    pause
    exit /b 1
)

pause