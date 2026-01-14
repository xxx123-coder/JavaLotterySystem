@echo off
chcp 65001 >nul
title 彩票系统预防性维护
color 0B

echo [INFO] 执行预防性维护
echo [INFO] 时间: %date% %time%
echo.

echo [INFO] 检查必要目录...
if not exist "data" (
    echo [INFO] 创建 data 目录...
    mkdir data
) else (
    echo [OK] data 目录已存在
)

if not exist "logs" (
    echo [INFO] 创建 logs 目录...
    mkdir logs
) else (
    echo [OK] logs 目录已存在
)

if not exist "backup" (
    echo [INFO] 创建 backup 目录...
    mkdir backup
) else (
    echo [OK] backup 目录已存在
)

echo.
echo [INFO] 清理临时文件...
del /Q "%TEMP%\lottery_*" 2>nul
if exist "data\*.tmp" del /Q "data\*.tmp" 2>nul
echo [OK] 临时文件已清理

echo.
echo [INFO] 检查配置文件...
if exist "config.properties" (
    echo [OK] 配置文件存在
) else (
    echo [WARN] 配置文件不存在，请创建 config.properties
)

echo.
echo [INFO] 检查 Excel 文件...
if exist "data\users.xlsx" (
    echo [OK] users.xlsx 存在
) else (
    echo [WARN] users.xlsx 不存在
)

if exist "data\tickets.xlsx" (
    echo [OK] tickets.xlsx 存在
) else (
    echo [WARN] tickets.xlsx 不存在
)

if exist "data\results.xlsx" (
    echo [OK] results.xlsx 存在
) else (
    echo [WARN] results.xlsx 不存在
)

echo.
echo [INFO] 检查 Java 环境...
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java 未安装或未配置环境变量
) else (
    echo [OK] Java 环境正常
)

echo.
echo ========================================
echo [INFO] 预防性维护完成
echo [INFO] 结束时间: %date% %time%
echo ========================================
echo.
pause