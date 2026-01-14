@echo off
echo ========================================
echo  彩票系统一键安装脚本
echo ========================================
echo.

REM 检查并创建目录结构
echo [步骤1] 创建项目目录结构...
if not exist "src\lottery\dao" mkdir "src\lottery\dao"
if not exist "src\lottery\model" mkdir "src\lottery\model"
if not exist "src\lottery\ui" mkdir "src\lottery\ui"
if not exist "src\lottery\util" mkdir "src\lottery\util"
if not exist "data" mkdir "data"
if not exist "lib" mkdir "lib"

echo [步骤2] 检查Java环境...
java -version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到Java环境！
    echo [信息] 请先安装Java 8或更高版本
    echo [信息] 下载地址: https://www.oracle.com/java/technologies/javase-jdk8-downloads.html
    pause
    exit /b 1
)

echo [信息] Java环境正常
echo.

echo [步骤3] 是否下载依赖库？(Y/N)
set /p choice=请选择：
if /i "%choice%"=="Y" (
    echo.
    call download-libs.bat
) else (
    echo [信息] 跳过依赖库下载
    echo [提示] 请确保lib目录中已有必要的jar文件
)

echo.
echo [步骤4] 编译系统...
call compile.bat

echo.
echo ========================================
echo  安装完成！
echo  运行系统: run.bat
echo  重新编译: compile.bat
echo ========================================
echo.
pause