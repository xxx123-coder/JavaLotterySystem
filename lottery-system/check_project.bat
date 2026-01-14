@echo off
echo ========================================
echo 彩票系统 - 项目结构检查
echo ========================================

echo 检查时间: %date% %time%
echo.
echo 当前工作目录: %cd%
echo.
echo 检查目录结构...
echo.

echo [1] Java源文件:
if exist "src\main\java\lottery\Main.java" (
    echo   ✓ Main.java (主入口)
) else (
    echo   ✗ Main.java 不存在
)

if exist "src\main\java\lottery\dao\ExcelDao.java" (
    echo   ✓ ExcelDao.java (DAO层)
) else (
    echo   ✗ ExcelDao.java 不存在
)

if exist "src\main\java\lottery\dao\DataManager.java" (
    echo   ✓ DataManager.java (数据管理器)
) else (
    echo   ✗ DataManager.java 不存在
)

if exist "src\main\java\lottery\service\UserService.java" (
    echo   ✓ UserService.java (用户服务)
) else (
    echo   ✗ UserService.java 不存在
)

if exist "src\main\java\lottery\service\TicketService.java" (
    echo   ✓ TicketService.java (彩票服务)
) else (
    echo   ✗ TicketService.java 不存在
)

if exist "src\main\java\lottery\service\LotteryService.java" (
    echo   ✓ LotteryService.java (抽奖服务)
) else (
    echo   ✗ LotteryService.java 不存在
)

if exist "src\main\java\lottery\ui\WebServer.java" (
    echo   ✓ WebServer.java (Web服务器)
) else (
    echo   ✗ WebServer.java 不存在
)

if exist "src\main\java\lottery\ui\ServletHandler.java" (
    echo   ✓ ServletHandler.java (Servlet处理器)
) else (
    echo   ✗ ServletHandler.java 不存在
)

if exist "src\main\java\lottery\ui\PageGenerator.java" (
    echo   ✓ PageGenerator.java (页面生成器)
) else (
    echo   ✗ PageGenerator.java 不存在
)

echo.
echo [2] 编译目录:
if exist "classes" (
    echo   ✓ classes目录存在
    dir /B "classes\lottery" >nul 2>&1
    if errorlevel 1 (
        echo   ✗ classes目录为空
    ) else (
        echo   ✓ classes目录有内容
    )
) else (
    echo   ✗ classes目录不存在
)

echo.
echo [3] 库文件目录:
if exist "lib" (
    echo   ✓ lib目录存在
    dir /B "lib" >nul 2>&1
    if errorlevel 1 (
        echo   ✗ lib目录为空
    ) else (
        echo   ✓ lib目录有内容
        echo   当前jar文件:
        dir /B "lib\*.jar"
    )
) else (
    echo   ✗ lib目录不存在
)

echo.
echo [4] 数据目录:
if exist "data" (
    echo   ✓ data目录存在
    dir /B "data" >nul 2>&1
    if errorlevel 1 (
        echo   ✗ data目录为空
    ) else (
        echo   ✓ data目录有内容
        echo   当前文件:
        dir /B "data\*.xlsx"
    )
) else (
    echo   ✗ data目录不存在
)

echo.
echo ========================================
echo 检查完成！
echo.
echo 建议操作:
if not exist "lib\*.jar" echo 1. 运行 download_libs.bat 下载库文件
if not exist "classes\lottery\Main.class" echo 2. 运行 compile.bat 编译项目
echo 3. 运行 start.bat 启动系统
echo ========================================
echo.
pause