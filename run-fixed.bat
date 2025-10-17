@echo off
echo ========================================
echo   Fixing Module Configuration & Running
echo ========================================
echo.

cd /d D:\VoyagerPlus\VoyagerPlus

echo Step 1: Cleaning previous build...
call mvnw.cmd clean

echo.
echo Step 2: Compiling with updated modules...
call mvnw.cmd compile

if %errorlevel% neq 0 (
    echo.
    echo ❌ ERROR: Compilation failed!
    echo.
    echo Please make sure:
    echo 1. Java 11+ is installed (java.net.http requires Java 11+)
    echo 2. Your Node.js backend is ready at http://localhost:5000
    echo.
    pause
    exit /b 1
)

echo.
echo ✅ Compilation successful!
echo.
echo Step 3: Running Voyager+ Application...
echo.
echo Make sure your Node.js backend is running!
echo.

call mvnw.cmd javafx:run

pause

