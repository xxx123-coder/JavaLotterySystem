@echo off
echo ========================================
echo 彩票系统 - 数据清理工具
echo ========================================

echo 正在停止彩票系统服务...
taskkill /F /IM java.exe >nul 2>&1
timeout /t 2 /nobreak >nul

echo.
echo 清理数据目录...
if exist "data" (
    rmdir /S /Q "data"
    echo 数据目录已删除
) else (
    echo 数据目录不存在，无需清理
)

echo.
echo 重新创建数据目录...
mkdir "data"
echo 数据目录已创建

echo.
echo ========================================
echo 数据清理完成！
echo 下一步：运行 compile.bat 编译项目
echo ========================================
pause