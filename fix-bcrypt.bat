@echo off
echo ========================================
echo Downloading BCrypt and all dependencies
echo ========================================
echo.

cd /d D:\VoyagerPlus\VoyagerPlus

echo Step 1: Cleaning previous builds...
call mvnw.cmd clean

echo.
echo Step 2: Downloading dependencies (including BCrypt)...
call mvnw.cmd dependency:resolve

echo.
echo Step 3: Compiling the project...
call mvnw.cmd compile

echo.
echo ========================================
echo Done! You can now run: mvnw.cmd javafx:run
echo ========================================
pause

