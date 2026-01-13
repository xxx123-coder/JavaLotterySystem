@echo off
chcp 65001 >nul
echo 启动彩票系统...
java -jar "%~dp0../target/lottery-system-1.0.0-jar-with-dependencies.jar"
pause