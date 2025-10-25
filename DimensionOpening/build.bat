@echo off
echo Building DimensionOpening Plugin...
echo.

REM Check if Maven is installed
mvn --version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Maven is not installed or not in PATH
    echo Please install Maven and try again
    pause
    exit /b 1
)

REM Clean and build the project
echo Cleaning previous builds...
mvn clean

echo.
echo Compiling and packaging...
mvn package

if %errorlevel% equ 0 (
    echo.
    echo ================================
    echo Build successful!
    echo Plugin JAR created in target/ directory
    echo ================================
) else (
    echo.
    echo ================================
    echo Build failed!
    echo Check the error messages above
    echo ================================
)

echo.
pause