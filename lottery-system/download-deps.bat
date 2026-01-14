@echo off
chcp 65001 >nul
echo ========================================
echo  Downloading Lottery System Dependencies
echo ========================================

echo [Step 1] Creating lib directory...
if not exist "lib" mkdir lib

echo [Step 2] Checking for existing dependencies...
set DOWNLOAD_COUNT=0

REM Function to download a file if it doesn't exist
setlocal enabledelayedexpansion
set "DOWNLOAD_URL=https://repo1.maven.org/maven2"

echo.
echo [INFO] Checking and downloading required libraries...
echo.

REM 1. Apache POI (for Excel operations)
if not exist "lib\poi-4.1.2.jar" (
    echo Downloading poi-4.1.2.jar...
    powershell -Command "try { Invoke-WebRequest -Uri '!DOWNLOAD_URL!/org/apache/poi/poi/4.1.2/poi-4.1.2.jar' -OutFile 'lib\poi-4.1.2.jar' -UseBasicParsing; echo '  Success' } catch { echo '  Failed, will use simple file storage' }"
    set /a DOWNLOAD_COUNT+=1
)

if not exist "lib\poi-ooxml-4.1.2.jar" (
    echo Downloading poi-ooxml-4.1.2.jar...
    powershell -Command "try { Invoke-WebRequest -Uri '!DOWNLOAD_URL!/org/apache/poi/poi-ooxml/4.1.2/poi-ooxml-4.1.2.jar' -OutFile 'lib\poi-ooxml-4.1.2.jar' -UseBasicParsing; echo '  Success' } catch { echo '  Failed, will use simple file storage' }"
    set /a DOWNLOAD_COUNT+=1
)

if not exist "lib\commons-io-2.6.jar" (
    echo Downloading commons-io-2.6.jar...
    powershell -Command "try { Invoke-WebRequest -Uri '!DOWNLOAD_URL!/commons-io/commons-io/2.6/commons-io-2.6.jar' -OutFile 'lib\commons-io-2.6.jar' -UseBasicParsing; echo '  Success' } catch { echo '  Failed' }"
    set /a DOWNLOAD_COUNT+=1
)

REM 2. Jetty Server (for web interface)
if not exist "lib\jetty-server-9.4.31.v20200723.jar" (
    echo Downloading jetty-server-9.4.31.v20200723.jar...
    powershell -Command "try { Invoke-WebRequest -Uri '!DOWNLOAD_URL!/org/eclipse/jetty/jetty-server/9.4.31.v20200723/jetty-server-9.4.31.v20200723.jar' -OutFile 'lib\jetty-server-9.4.31.v20200723.jar' -UseBasicParsing; echo '  Success' } catch { echo '  Failed, web interface will not work' }"
    set /a DOWNLOAD_COUNT+=1
)

if not exist "lib\jetty-servlet-9.4.31.v20200723.jar" (
    echo Downloading jetty-servlet-9.4.31.v20200723.jar...
    powershell -Command "try { Invoke-WebRequest -Uri '!DOWNLOAD_URL!/org/eclipse/jetty/jetty-servlet/9.4.31.v20200723/jetty-servlet-9.4.31.v20200723.jar' -OutFile 'lib\jetty-servlet-9.4.31.v20200723.jar' -UseBasicParsing; echo '  Success' } catch { echo '  Failed, web interface will not work' }"
    set /a DOWNLOAD_COUNT+=1
)

if not exist "lib\servlet-api-4.0.1.jar" (
    echo Downloading servlet-api-4.0.1.jar...
    powershell -Command "try { Invoke-WebRequest -Uri '!DOWNLOAD_URL!/javax/servlet/javax.servlet-api/4.0.1/javax.servlet-api-4.0.1.jar' -OutFile 'lib\servlet-api-4.0.1.jar' -UseBasicParsing; echo '  Success' } catch { echo '  Failed' }"
    set /a DOWNLOAD_COUNT+=1
)

echo.
echo ========================================
if %DOWNLOAD_COUNT% GTR 0 (
    echo  Downloaded %DOWNLOAD_COUNT% libraries to lib directory
) else (
    echo  All required libraries already exist
)

REM Check if we have the minimum required libraries
set HAS_MINIMUM=1
if not exist "lib\poi-4.1.2.jar" (
    echo [WARNING] poi-4.1.2.jar not found, will use simple file storage
)
if not exist "lib\jetty-server-9.4.31.v20200723.jar" (
    echo [WARNING] Jetty server not found, web interface may not work
    set HAS_MINIMUM=0
)

if %HAS_MINIMUM%==1 (
    echo [OK] Minimum requirements met
) else (
    echo [WARNING] Some required libraries are missing
    echo [INFO] System will use simple file storage (no Excel support)
)

echo ========================================
echo.
pause