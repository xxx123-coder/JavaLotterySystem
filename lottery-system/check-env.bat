@echo off
echo ====================================
echo    环境检查工具
echo ====================================

echo.
echo 检查Java环境...
java -version 2>nul
if errorlevel 1 (
    echo [错误] Java未安装或环境变量未配置！
    echo.
    echo 解决方案：
    echo 1. 下载Java 8或更高版本
    echo 2. 设置JAVA_HOME环境变量
    echo 3. 在Path中添加 %JAVA_HOME%\bin
    goto :end_error
)

echo [成功] Java环境正常

echo.
echo 检查Maven环境...
mvn -version 2>nul
if errorlevel 1 (
    echo [警告] Maven未安装，将使用简单编译方式
    set USE_MAVEN=0
) else (
    echo [成功] Maven环境正常
    set USE_MAVEN=1
)

echo.
echo 检查项目结构...
if not exist "src\main\java\lottery\Main.java" (
    echo [错误] 找不到主类：src\main\java\lottery\Main.java
    goto :end_error
)
echo [成功] 项目结构完整

if not exist "pom.xml" (
    echo [警告] 找不到pom.xml，将使用简单编译
    set USE_MAVEN=0
)

echo.
echo ====================================
echo 检查完成！
if %USE_MAVEN%==1 (
    echo 将使用Maven进行编译
) else (
    echo 将使用简单编译方式
)
echo ====================================
exit /b 0

:end_error
echo.
echo 按任意键退出...
pause >nul
exit /b 1