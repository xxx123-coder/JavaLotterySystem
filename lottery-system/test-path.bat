@echo off
chcp 65001 >nul
echo.
echo =========================================
echo       彩票系统路径配置测试脚本 v1.0
echo =========================================
echo.

REM 设置Java路径
set JAVA_CMD=java
if exist "%JAVA_HOME%\bin\java.exe" (
    set JAVA_CMD="%JAVA_HOME%\bin\java.exe"
)

REM 检查Maven编译
if not exist "target\classes" (
    echo [INFO] 检测到未编译，正在编译项目...
    call mvn clean compile -q
    if errorlevel 1 (
        echo [ERROR] 编译失败
        pause
        exit /b 1
    )
)

REM 运行PathManager测试
echo [INFO] 正在测试路径配置...
echo.

REM 创建测试类
echo [INFO] 创建临时测试类...
(
echo package lottery.util;
echo.
echo public class TestPathManager {
echo     public static void main(String[] args) {
echo         System.out.println("测试PathManager...");
echo         System.out.println();
echo         PathManager.printPathInfo();
echo         System.out.println();
echo         System.out.println("检查文件是否存在:");
echo         System.out.println("用户文件: " + PathManager.fileExists(PathManager.getUserFilePath()));
echo         System.out.println("彩票文件: " + PathManager.fileExists(PathManager.getTicketFilePath()));
echo         System.out.println("结果文件: " + PathManager.fileExists(PathManager.getResultFilePath()));
echo         System.out.println("配置文件: " + PathManager.fileExists(PathManager.getConfigFilePath()));
echo         System.out.println();
echo         System.out.println("测试完成!");
echo     }
echo }
) > src\main\java\lottery\util\TestPathManager.java

REM 编译测试类
echo [INFO] 编译测试类...
call mvn compile -q

REM 运行测试
echo [INFO] 运行路径测试...
%JAVA_CMD% -cp "target/classes" lottery.util.TestPathManager

REM 清理临时文件
echo.
echo [INFO] 清理临时文件...
del src\main\java\lottery\util\TestPathManager.java

pause