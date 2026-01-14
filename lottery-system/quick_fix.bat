@echo off
title 彩票系统快速修复
color 0A

echo [INFO] 彩票系统快速修复工具
echo [INFO] 开始时间: %date% %time%
echo.

echo 正在执行快速修复...

:: 1. 确保目录存在
if not exist "data" mkdir data
if not exist "logs" mkdir logs
if not exist "backup" mkdir backup

:: 2. 清理临时文件
echo [INFO] 清理临时文件...
del /Q "%TEMP%\lottery_*" 2>nul
del /Q "%TEMP%\*.tmp" 2>nul
del /Q "data\*.tmp" 2>nul

:: 3. 检查配置文件
if not exist "config.properties" (
    echo [ERROR] 配置文件不存在！
    echo 正在创建默认配置文件...

    echo # 服务器配置> config.properties
    echo server.port=8080>> config.properties
    echo.>> config.properties
    echo # 数据文件配置>> config.properties
    echo data.dir=data>> config.properties
    echo excel.users.file=users.xlsx>> config.properties
    echo excel.tickets.file=tickets.xlsx>> config.properties
    echo excel.results.file=results.xlsx>> config.properties
    echo.>> config.properties
    echo # 彩票配置>> config.properties
    echo lottery.number.count=7>> config.properties
    echo lottery.number.min=1>> config.properties
    echo lottery.number.max=36>> config.properties
    echo ticket.price=2.0>> config.properties
    echo.>> config.properties
    echo # 日志配置>> config.properties
    echo log.level=INFO>> config.properties
)

:: 4. 修复Excel文件权限
echo [INFO] 修复Excel文件...
for %%f in (data\*.xlsx) do (
    if exist "%%f" (
        attrib -R "%%f" 2>nul
        echo 修复权限: %%f
    )
)

:: 5. 创建README文件
if not exist "README.md" (
    echo # 彩票系统修复说明> README.md
    echo.>> README.md
    echo 如果程序无法启动，请执行以下步骤：>> README.md
    echo 1. 运行 fix.bat 修复工具>> README.md
    echo 2. 确保Java已安装>> README.md
    echo 3. 检查data目录权限>> README.md
    echo 4. 清理临时文件>> README.md
)

echo.
echo [INFO] 快速修复完成!
echo [INFO] 结束时间: %date% %time%
echo.
echo 建议操作:
echo 1. 重启彩票系统
echo 2. 如果问题仍然存在，运行完整修复 (fix.bat)
echo 3. 检查系统日志
echo.

pause