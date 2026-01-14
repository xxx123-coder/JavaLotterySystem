@echo off
echo ========================================
echo  彩票系统构建脚本
echo ========================================

REM 1. 清理项目
echo [步骤1] 清理项目...
call mvn clean
if %ERRORLEVEL% neq 0 (
    echo [错误] 清理失败！
    pause
    exit /b 1
)

REM 2. 编译项目
echo [步骤2] 编译项目...
call mvn compile
if %ERRORLEVEL% neq 0 (
    echo [错误] 编译失败！
    pause
    exit /b 1
)

REM 3. 打包项目
echo [步骤3] 打包项目...
call mvn package -DskipTests
if %ERRORLEVEL% neq 0 (
    echo [错误] 打包失败！
    pause
    exit /b 1
)

echo.
echo ========================================
echo  构建成功！
echo  生成的文件在: target/lottery-system-1.0.0-shaded.jar
echo  运行命令: java -jar target/lottery-system-1.0.0-shaded.jar
echo ========================================
echo.
pause