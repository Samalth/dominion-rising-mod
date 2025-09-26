@echo off
REM Script to test Dominion Rising mod with different Java versions

echo Testing Dominion Rising Mod - Java Compatibility

echo.
echo Checking Java versions...
java -version
echo.

REM Try to find Java 17
set JAVA17_HOME=
if exist "C:\Program Files\Java\jdk-17" (
    set JAVA17_HOME=C:\Program Files\Java\jdk-17
    echo Found Java 17 at: %JAVA17_HOME%
)
if exist "C:\Program Files\Eclipse Adoptium\jdk-17*" (
    for /d %%i in ("C:\Program Files\Eclipse Adoptium\jdk-17*") do set JAVA17_HOME=%%i
    echo Found Java 17 at: %JAVA17_HOME%
)

echo.
echo ==========================================
echo Testing build with current Java version:
echo ==========================================
gradlew build
if %ERRORLEVEL% neq 0 (
    echo Build failed with current Java version
    exit /b 1
)

echo.
echo Build successful! Testing client runs...
echo.

echo ==========================================
echo Attempting NeoForge client:
echo ==========================================
timeout /t 3 >nul
gradlew :neoforge:runClient --no-daemon --console=plain
if %ERRORLEVEL% neq 0 (
    echo NeoForge client failed with current Java
    
    if defined JAVA17_HOME (
        echo.
        echo ==========================================
        echo Retrying with Java 17:
        echo ==========================================
        gradlew -Dorg.gradle.java.home="%JAVA17_HOME%" :neoforge:runClient --no-daemon --console=plain
    ) else (
        echo Java 17 not found. Please install Java 17 for better compatibility.
    )
) else (
    echo NeoForge client started successfully!
)

echo.
echo ==========================================
echo Testing complete.
echo ==========================================
pause