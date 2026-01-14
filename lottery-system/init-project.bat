@echo off
echo 初始化彩票系统项目...
echo.

echo 步骤1：检查Java环境...
call java-check.bat
if %errorlevel% neq 0 exit /b 1

echo.
echo 步骤2：创建项目目录结构...
if not exist src mkdir src
if not exist src\lottery mkdir src\lottery
if not exist src\lottery\dao mkdir src\lottery\dao
if not exist src\lottery\model mkdir src\lottery\model
if not exist src\lottery\ui mkdir src\lottery\ui
if not exist src\lottery\util mkdir src\lottery\util
if not exist data mkdir data
if not exist bin mkdir bin
if not exist lib mkdir lib

echo.
echo 步骤3：创建Main.java文件...
if exist src\lottery\Main.java (
    echo Main.java已存在
) else (
    echo 正在创建Main.java...
    echo package lottery;> src\lottery\Main.java
    echo.>> src\lottery\Main.java
    echo public class Main {>> src\lottery\Main.java
    echo     public static void main(String[] args) {>> src\lottery\Main.java
    echo         System.out.println("彩票系统启动成功！");>> src\lottery\Main.java
    echo         System.out.println("数据目录: data");>> src\lottery\Main.java
    echo         System.out.println("当前时间: " + new java.util.Date());>> src\lottery\Main.java
    echo     }>> src\lottery\Main.java
    echo }>> src\lottery\Main.java
    echo Main.java创建完成
)

echo.
echo 步骤4：检查现有Java文件...
dir /b src\lottery\*.java > nul 2>&1
if %errorlevel% equ 0 (
    echo 找到Java文件:
    dir /b src\lottery\*.java
) else (
    echo 警告: src\lottery目录中没有Java文件
)

echo.
echo 项目初始化完成！
echo.
pause