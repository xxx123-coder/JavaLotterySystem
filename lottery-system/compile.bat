@echo off
echo ========================================
echo 彩票系统 - 编译脚本
echo ========================================

echo 步骤1: 清理旧编译文件...
if exist "classes" (
    rmdir /S /Q "classes"
    echo 旧的class文件已清理
)

mkdir "classes"
echo 创建编译目录完成

echo.
echo 步骤2: 检查Java源文件...
set "SOURCE_DIR=src\main\java"
if not exist "%SOURCE_DIR%\lottery\Main.java" (
    echo 错误: 找不到Main.java文件！
    echo 路径: %SOURCE_DIR%\lottery\Main.java
    pause
    exit /b 1
)

echo.
echo 步骤3: 检查POI库...
set "LIB_DIR=lib"
if not exist "%LIB_DIR%" (
    echo 创建lib目录...
    mkdir "%LIB_DIR%"
    echo 请将以下jar文件放入lib目录：
    echo 1. poi-5.2.3.jar
    echo 2. poi-ooxml-5.2.3.jar
    echo 3. poi-ooxml-schemas-4.1.2.jar
    echo 4. xmlbeans-5.0.3.jar
    pause
)

echo.
echo 步骤4: 设置类路径...
if exist "%LIB_DIR%\*.jar" (
    set "CLASSPATH=%LIB_DIR%\*"
) else (
    set "CLASSPATH="
    echo 警告: lib目录中没有jar文件，编译可能失败！
)

echo.
echo 步骤5: 编译Java文件...
echo 正在编译...
echo.

:: 编译主类
javac -cp "%CLASSPATH%" -d "classes" -encoding UTF-8 "%SOURCE_DIR%\lottery\Main.java"
if errorlevel 1 (
    echo 错误: Main.java编译失败！
    pause
    exit /b 1
)

:: 编译dao包
javac -cp "%CLASSPATH%;classes" -d "classes" -encoding UTF-8 "%SOURCE_DIR%\lottery\dao\*.java"
if errorlevel 1 (
    echo 错误: dao包编译失败！
    pause
    exit /b 1
)

:: 编译service包
javac -cp "%CLASSPATH%;classes" -d "classes" -encoding UTF-8 "%SOURCE_DIR%\lottery\service\*.java"
if errorlevel 1 (
    echo 错误: service包编译失败！
    pause
    exit /b 1
)

:: 编译ui包
javac -cp "%CLASSPATH%;classes" -d "classes" -encoding UTF-8 "%SOURCE_DIR%\lottery\ui\*.java"
if errorlevel 1 (
    echo 错误: ui包编译失败！
    pause
    exit /b 1
)

echo.
echo 编译成功！生成的文件：
echo 主类: lottery.Main
dir /B "classes\lottery"
echo dao包:
dir /B "classes\lottery\dao"
echo service包:
dir /B "classes\lottery\service"
echo ui包:
dir /B "classes\lottery\ui"

echo.
echo ========================================
echo 编译完成！可以运行 start.bat 启动系统
echo ========================================
pause