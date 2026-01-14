@echo off
echo 编译彩票系统...
echo.

echo 步骤1：检查Java环境...
call java-check.bat
if %errorlevel% neq 0 exit /b 1

echo.
echo 步骤2：准备编译目录...
if not exist bin mkdir bin

echo.
echo 步骤3：检查Java源文件...
if not exist src\lottery\Main.java (
    echo 错误: 找不到src\lottery\Main.java
    echo 请先运行init-project.bat初始化项目
    pause
    exit /b 1
)

echo.
echo 步骤4：开始编译...
echo 编译Main.java...
javac -encoding UTF-8 -d bin src\lottery\Main.java

if %errorlevel% equ 0 (
    echo Main.java编译成功
) else (
    echo Main.java编译失败
    pause
    exit /b 1
)

echo.
echo 步骤5：编译dao目录（如果存在Java文件）...
if exist src\lottery\dao\*.java (
    echo 编译dao目录...
    javac -encoding UTF-8 -d bin src\lottery\dao\*.java
    if %errorlevel% equ 0 echo dao目录编译成功
)

echo.
echo 步骤6：编译model目录（如果存在Java文件）...
if exist src\lottery\model\*.java (
    echo 编译model目录...
    javac -encoding UTF-8 -d bin src\lottery\model\*.java
    if %errorlevel% equ 0 echo model目录编译成功
)

echo.
echo 步骤7：编译ui目录（如果存在Java文件）...
if exist src\lottery\ui\*.java (
    echo 编译ui目录...
    javac -encoding UTF-8 -d bin src\lottery\ui\*.java
    if %errorlevel% equ 0 echo ui目录编译成功
)

echo.
echo 步骤8：编译util目录（如果存在Java文件）...
if exist src\lottery\util\*.java (
    echo 编译util目录...
    javac -encoding UTF-8 -d bin src\lottery\util\*.java
    if %errorlevel% equ 0 echo util目录编译成功
)

echo.
echo 步骤9：检查编译结果...
if exist bin\lottery\Main.class (
    echo [成功] 彩票系统编译完成！
    echo 编译输出目录: bin
) else (
    echo [错误] 编译失败，未生成Main.class
)

echo.
pause