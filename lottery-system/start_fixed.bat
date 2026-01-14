@echo off
title 彩票系统 (修复版启动)
color 0E

echo [INFO] 彩票系统启动器 (带自动修复)
echo =================================

:: 检查Java
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java未安装或未配置环境变量!
    echo 请安装Java 8或更高版本
    pause
    exit /b 1
)

:: 检查必要目录
if not exist "data" (
    echo [WARN] data目录不存在，创建中...
    mkdir data
)

:: 清理临时文件
echo [INFO] 清理之前的临时文件...
del /Q "%TEMP%\lottery_*.tmp" 2>nul
del /Q "data\*.tmp" 2>nul

:: 检查Excel文件是否被占用
echo [INFO] 检查Excel文件状态...
tasklist | findstr /i "excel" >nul
if not errorlevel 1 (
    echo [WARN] Excel程序正在运行！
    echo 请关闭Excel程序后重试
    echo 是否继续？(y/n):
    set /p continue=
    if not "%continue%"=="y" (
        exit /b
    )
)

:: 启动程序
echo.
echo [INFO] 启动彩票系统...
echo =================================

:: 设置JVM参数
set JAVA_OPTS=-Xms256m -Xmx512m -Dfile.encoding=UTF-8

:: 启动程序
java %JAVA_OPTS% -jar lottery-system.jar

:: 如果启动失败
if errorlevel 1 (
    echo.
    echo [ERROR] 启动失败!
    echo 请选择:
    echo 1. 运行修复工具
    echo 2. 查看日志
    echo 3. 退出
    echo.
    set /p choice="选择: "

    if "%choice%"=="1" (
        call fix.bat
    ) else if "%choice%"=="2" (
        if exist "logs" (
            dir logs
            pause
        ) else (
            echo [INFO] 无日志文件
            pause
        )
    )
)

pause