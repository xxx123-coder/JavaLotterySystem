@echo off
title 彩票系统修复工具 v1.0
color 0A
chcp 65001 >nul

echo =========================================
echo           彩票系统修复工具
echo =========================================
echo.

set PROJECT_ROOT=%CD%
echo [INFO] 项目根目录: %PROJECT_ROOT%
echo.

:menu
echo 请选择修复选项:
echo 1. 基础修复 (推荐)
echo 2. 深度修复
echo 3. 仅检查问题
echo 4. 手动修复 Excel 文件
echo 5. 退出
echo.

set /p choice="请选择 (1-5): "

if "%choice%"=="1" goto basic_fix
if "%choice%"=="2" goto deep_fix
if "%choice%"=="3" goto check_only
if "%choice%"=="4" goto fix_excel
if "%choice%"=="5" goto exit
echo [ERROR] 无效选择，请重新输入
goto menu

:basic_fix
echo.
echo ===== 执行基础修复 =====
call :check_dirs
call :fix_permissions
call :clear_temp_files
call :create_data_files
echo [INFO] 基础修复完成
goto exit

:deep_fix
echo.
echo ===== 执行深度修复 =====
call :check_dirs
call :fix_permissions
call :clear_temp_files
call :clear_cache
call :create_data_files
call :backup_existing
call :restart_services
echo [INFO] 深度修复完成
goto exit

:check_only
echo.
echo ===== 检查系统问题 =====
call :check_dirs
call :check_permissions
call :check_java
call :check_disk_space
echo [INFO] 检查完成
goto exit

:fix_excel
echo.
echo ===== 手动修复 Excel 文件 =====
call :repair_excel_files
goto exit

:exit
echo.
echo =========================================
echo           修复操作完成
echo =========================================
echo 建议操作:
echo 1. 重启彩票系统
echo 2. 如果问题仍存在，运行深度修复
echo 3. 检查系统日志
echo =========================================
pause
exit /b

:: 功能函数

:check_dirs
echo [INFO] 检查目录结构...
if not exist "data" (
    echo [WARN] data目录不存在，创建中...
    mkdir data
    echo [OK] 创建data目录成功
) else (
    echo [OK] data目录已存在
)

if not exist "logs" (
    echo [WARN] logs目录不存在，创建中...
    mkdir logs
    echo [OK] 创建logs目录成功
) else (
    echo [OK] logs目录已存在
)

if not exist "backup" (
    echo [WARN] backup目录不存在，创建中...
    mkdir backup
    echo [OK] 创建backup目录成功
) else (
    echo [OK] backup目录已存在
)
exit /b

:fix_permissions
echo [INFO] 设置目录权限...
echo [INFO] 注意：权限设置可能需要管理员权限
echo.

:: 获取当前用户
for /f "tokens=2 delims==" %%a in ('wmic os get localname /value') do set "COMPUTERNAME=%%a"
for /f "tokens=2 delims==" %%a in ('wmic computersystem get username /value') do set "USERNAME=%%a"

echo [INFO] 当前用户: %USERNAME%
echo [INFO] 计算机名: %COMPUTERNAME%

:: 设置当前用户对项目目录的完全控制权限
echo [INFO] 设置 %USERNAME% 对项目目录的完全控制权限...

:: 使用icacls设置权限（需要管理员权限）
:: 这里只是显示命令，实际执行需要管理员权限
echo [WARN] 权限命令需要管理员权限运行
echo      icacls "%PROJECT_ROOT%" /grant "%USERNAME%":(OI)(CI)F /T
echo.

exit /b

:clear_temp_files
echo [INFO] 清理临时文件...
set TEMP_DIR=%TEMP%
echo [INFO] 临时目录: %TEMP_DIR%

:: 清理彩票系统临时文件
del /Q "%TEMP_DIR%\lottery_*" 2>nul
del /Q "%TEMP_DIR%\~lottery*" 2>nul
del /Q "%TEMP_DIR%\*.tmp" 2>nul

:: 清理项目中的临时文件
del /Q "%PROJECT_ROOT%\data\*.tmp" 2>nul
del /Q "%PROJECT_ROOT%\*.tmp" 2>nul

echo [INFO] 临时文件清理完成
exit /b

:clear_cache
echo [INFO] 清理系统缓存...
:: 清理Java缓存
if exist "%USERPROFILE%\.m2" (
    echo [INFO] 清理Maven缓存...
    rmdir /S /Q "%USERPROFILE%\.m2\repository" 2>nul
)

if exist "%TEMP%\hsperfdata_%USERNAME%" (
    echo [INFO] 清理Java性能数据...
    rmdir /S /Q "%TEMP%\hsperfdata_%USERNAME%" 2>nul
)

echo [INFO] 缓存清理完成
exit /b

:create_data_files
echo [INFO] 创建数据文件...
echo [INFO] 注意：如果已有数据文件，此操作将删除并重新创建

echo 是否继续？这将删除现有的Excel文件并重新创建 (y/n):
set /p confirm=

if not "%confirm%"=="y" if not "%confirm%"=="Y" (
    echo [INFO] 取消创建数据文件
    exit /b
)

:: 备份现有文件
echo [INFO] 备份现有数据文件...
if exist "%PROJECT_ROOT%\data\users.xlsx" (
    copy "%PROJECT_ROOT%\data\users.xlsx" "%PROJECT_ROOT%\data\users_backup_%date:~0,4%%date:~5,2%%date:~8,2%.xlsx" >nul
)

if exist "%PROJECT_ROOT%\data\tickets.xlsx" (
    copy "%PROJECT_ROOT%\data\tickets.xlsx" "%PROJECT_ROOT%\data\tickets_backup_%date:~0,4%%date:~5,2%%date:~8,2%.xlsx" >nul
)

if exist "%PROJECT_ROOT%\data\results.xlsx" (
    copy "%PROJECT_ROOT%\data\results.xlsx" "%PROJECT_ROOT%\data\results_backup_%date:~0,4%%date:~5,2%%date:~8,2%.xlsx" >nul
)

:: 删除现有文件
del /Q "%PROJECT_ROOT%\data\*.xlsx" 2>nul

echo [INFO] 创建新的Excel文件...
:: 这里可以调用Java程序创建Excel文件，或者手动创建
echo [INFO] 请启动彩票系统程序来自动创建Excel文件
echo [INFO] 或使用以下命令：
echo      java -jar lottery-system.jar
echo.
exit /b

:backup_existing
echo [INFO] 备份现有数据...
set BACKUP_DIR=%PROJECT_ROOT%\backup\%date:~0,4%%date:~5,2%%date:~8,2%
mkdir "%BACKUP_DIR%" 2>nul

xcopy "%PROJECT_ROOT%\data\*.xlsx" "%BACKUP_DIR%\" /Y /I >nul
xcopy "%PROJECT_ROOT%\config.properties" "%BACKUP_DIR%\" /Y >nul

echo [INFO] 数据备份到: %BACKUP_DIR%
exit /b

:restart_services
echo [INFO] 重启相关服务...
echo [INFO] 建议手动重启以下服务：
echo      1. 停止所有Java进程
echo      2. 关闭Excel程序
echo      3. 重启彩票系统
echo.
exit /b

:check_permissions
echo [INFO] 检查目录权限...
echo [INFO] 检查 data 目录...
if exist "%PROJECT_ROOT%\data" (
    echo [INFO] data目录存在
    echo [INFO] 尝试写入测试文件...
    echo test > "%PROJECT_ROOT%\data\test_permission.txt" 2>nul
    if exist "%PROJECT_ROOT%\data\test_permission.txt" (
        del "%PROJECT_ROOT%\data\test_permission.txt" 2>nul
        echo [OK] data目录可写
    ) else (
        echo [ERROR] data目录不可写
    )
) else (
    echo [ERROR] data目录不存在
)

echo [INFO] 检查临时目录权限...
echo test > "%TEMP%\test_permission.txt" 2>nul
if exist "%TEMP%\test_permission.txt" (
    del "%TEMP%\test_permission.txt" 2>nul
    echo [OK] 临时目录可写
) else (
    echo [ERROR] 临时目录不可写
)
exit /b

:check_java
echo [INFO] 检查Java环境...
java -version 2>nul
if errorlevel 1 (
    echo [ERROR] Java未安装或未配置环境变量
) else (
    echo [OK] Java环境正常
)
exit /b

:check_disk_space
echo [INFO] 检查磁盘空间...
:: 使用wmic检查C盘空间
for /f "tokens=1-3" %%a in ('wmic logicaldisk where "DeviceID='C:'" get FreeSpace^,Size /value ^| findstr "="') do (
    for /f "tokens=1* delims==" %%c in ("%%a") do (
        if "%%c"=="FreeSpace" set FREE_SPACE=%%d
        if "%%c"=="Size" set TOTAL_SIZE=%%d
    )
)

:: 计算可用空间（GB）
set /a FREE_GB=FREE_SPACE/1073741824
set /a TOTAL_GB=TOTAL_SIZE/1073741824

echo [INFO] C盘空间: 可用 %FREE_GB%GB / 总共 %TOTAL_GB%GB

if %FREE_GB% LSS 1 (
    echo [WARN] 磁盘空间不足，建议清理
) else (
    echo [OK] 磁盘空间充足
)
exit /b

:repair_excel_files
echo [INFO] 修复Excel文件...
echo.
echo 选项:
echo 1. 删除所有Excel文件并重新创建
echo 2. 仅修复损坏的文件
echo 3. 创建备份并重新创建
echo 4. 返回
echo.

set /p excel_choice="请选择: "

if "%excel_choice%"=="1" goto delete_excel
if "%excel_choice%"=="2" goto repair_corrupted
if "%excel_choice%"=="3" goto backup_and_recreate
if "%excel_choice%"=="4" goto menu
echo [ERROR] 无效选择
goto repair_excel_files

:delete_excel
echo [WARN] 这将删除所有Excel文件！
echo 是否继续? (y/n):
set /p confirm=
if "%confirm%"=="y" if "%confirm%"=="Y" (
    del /Q "%PROJECT_ROOT%\data\*.xlsx" 2>nul
    echo [INFO] Excel文件已删除
    echo [INFO] 请重启彩票系统