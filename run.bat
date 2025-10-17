@echo off
echo ========================================
echo       Voyager+ Application Launcher
echo ========================================
echo.

cd /d D:\VoyagerPlus\VoyagerPlus

echo Step 1: Cleaning and compiling...
call mvnw.cmd clean compile

if %errorlevel% neq 0 (
    echo.
    echo ERROR: Compilation failed!
    echo Please check the error messages above.
    pause
    exit /b 1
)

echo.
echo Step 2: Running Voyager+ Application...
echo.
echo ========================================
echo  Welcome Screen should appear shortly
echo ========================================
echo.

call mvnw.cmd javafx:run

pause

