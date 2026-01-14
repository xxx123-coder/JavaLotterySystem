@echo off
echo ====================================
echo    简单编译工具
echo ====================================

echo.
echo 步骤1：检查Java环境...
java -version 2>nul
if errorlevel 1 (
    echo [错误] Java未安装！
    pause
    exit /b 1
)

echo.
echo 步骤2：准备目录...
if not exist "target\classes" mkdir "target\classes"
if not exist "data" mkdir "data"
if not exist "logs" mkdir "logs"

echo.
echo 步骤3：编译主类...
javac -encoding UTF-8 -d "target\classes" -cp "." "src\main\java\lottery\Main.java"

echo.
echo 步骤4：编译dao包...
if exist "src\main\java\lottery\dao\*.java" (
    echo 编译dao包...
    javac -encoding UTF-8 -d "target\classes" -cp "target\classes" "src\main\java\lottery\dao\*.java"
)

echo.
echo 步骤5：编译model包...
if exist "src\main\java\lottery\model\*.java" (
    echo 编译model包...
    javac -encoding UTF-8 -d "target\classes" -cp "target\classes" "src\main\java\lottery\model\*.java"
)

echo.
echo 步骤6：编译service包...
if exist "src\main\java\lottery\service\*.java" (
    echo 编译service包...
    javac -encoding UTF-8 -d "target\classes" -cp "target\classes" "src\main\java\lottery\service\*.java"
)

echo.
echo 步骤7：编译ui包...
if exist "src\main\java\lottery\ui\*.java" (
    echo 编译ui包...
    javac -encoding UTF-8 -d "target\classes" -cp "target\classes" "src\main\java\lottery\ui\*.java"
)

echo.
echo 步骤8：编译util包...
if exist "src\main\java\lottery\util\*.java" (
    echo 编译util包...
    javac -encoding UTF-8 -d "target\classes" -cp "target\classes" "src\main\java\lottery\util\*.java"
)

echo.
echo 步骤9：编译filter包...
if exist "src\main\java\lottery\filter\*.java" (
    echo 编译filter包...
    javac -encoding UTF-8 -d "target\classes" -cp "target\classes" "src\main\java\lottery\filter\*.java"
)

echo.
echo ====================================
echo 编译完成！
echo 输出目录：target\classes
echo ====================================
echo.
pause