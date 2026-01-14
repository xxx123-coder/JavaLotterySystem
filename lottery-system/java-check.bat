@echo off
echo 检查Java环境...
echo.

REM 尝试执行java命令
java -version > nul 2>&1

if %errorlevel% equ 0 (
    echo [成功] Java环境正常
    echo Java版本:
    java -version
    exit /b 0
) else (
    echo [错误] 未找到Java环境！
    echo.
    echo 解决方案：
    echo 1. 请先安装Java 8或更高版本
    echo 2. 下载地址: https://www.oracle.com/java/technologies/javase-jdk8-downloads.html
    echo 3. 安装后需要配置环境变量
    echo.
    pause
    exit /b 1
)