@echo off
echo ========================================
echo 彩票系统 - 库文件下载说明
echo ========================================
echo.
echo 请下载以下Apache POI库文件到 lib 目录：
echo.
echo 项目根目录: C:\Users\Administrator\Desktop\JAVA课设\lottery-system
echo lib目录: C:\Users\Administrator\Desktop\JAVA课设\lottery-system\lib
echo.
echo 必需文件（请下载后放入lib目录）：
echo 1. poi-5.2.3.jar
echo    下载地址: https://repo1.maven.org/maven2/org/apache/poi/poi/5.2.3/poi-5.2.3.jar
echo.
echo 2. poi-ooxml-5.2.3.jar
echo    下载地址: https://repo1.maven.org/maven2/org/apache/poi/poi-ooxml/5.2.3/poi-ooxml-5.2.3.jar
echo.
echo 3. poi-ooxml-schemas-4.1.2.jar
echo    下载地址: https://repo1.maven.org/maven2/org/apache/poi/poi-ooxml-schemas/4.1.2/poi-ooxml-schemas-4.1.2.jar
echo.
echo 4. xmlbeans-5.0.3.jar
echo    下载地址: https://repo1.maven.org/maven2/org/apache/xmlbeans/xmlbeans/5.0.3/xmlbeans-5.0.3.jar
echo.
echo 操作步骤：
echo 1. 创建lib目录（如果不存在）
echo 2. 下载上述4个jar文件
echo 3. 将jar文件放入 lib 目录
echo 4. 运行 compile.bat 测试编译
echo.
echo 当前目录：%cd%
if not exist "lib" (
    echo 创建lib目录...
    mkdir "lib"
    echo lib目录已创建
)

echo.
echo lib目录内容：
if exist "lib" (
    dir /B "lib"
) else (
    echo lib目录不存在
)

echo.
pause