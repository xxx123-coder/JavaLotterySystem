@echo off
echo ========================================
echo  彩票系统运行脚本
echo ========================================

REM 检查是否已编译
if not exist "bin" (
    echo [警告] 未找到编译文件，尝试自动编译...
    call compile.bat
    if errorlevel 1 (
        pause
        exit /b 1
    )
)

REM 创建数据目录
if not exist "data" mkdir data

echo [步骤1] 构建运行时classpath...
setlocal enabledelayedexpansion
set CLASSPATH=bin

REM 添加lib目录下的所有jar文件到classpath
for %%i in (lib\*.jar) do (
    set CLASSPATH=!CLASSPATH!;%%i
)

echo [步骤2] 启动彩票系统...
echo.
java -cp "%CLASSPATH%" lottery.Main

if errorlevel 1 (
    echo.
    echo [错误] 系统启动失败！
    pause
    exit /b 1
)

pause