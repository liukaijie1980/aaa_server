@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "ROOT=%~dp0"
set "ROOT=%ROOT:~0,-1%"

set "RADIUS_API_DIR=%ROOT%\radiusApi"
set "DOCKER_APP_DIR=%ROOT%\docker\java\app"
set "TOOLS_DIR=%ROOT%\.tools"

set "REQUIRED_JAVA_MAJOR=18"
set "DOWNLOAD_JAVA_MAJOR=21"

call :ensure_java || goto :err

if not exist "%DOCKER_APP_DIR%" mkdir "%DOCKER_APP_DIR%" >nul 2>nul

REM Try to use system Maven first (if available), otherwise use Maven Wrapper
set "MAVEN_CMD="
where mvn >nul 2>&1
if "%ERRORLEVEL%"=="0" (
  echo Using system Maven installation
  set "MAVEN_CMD=mvn"
) else (
  echo Using Maven Wrapper from radiusApi
  set "MAVEN_CMD=mvnw.cmd"
  pushd "%RADIUS_API_DIR%" || goto :err
  
  REM Check if Maven is already downloaded in user's .m2 directory
  set "MAVEN_DIST=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.8.6-bin\1ks0nkde5v1pk9vtc31i9d0lcd\apache-maven-3.8.6"
  if exist "%MAVEN_DIST%\bin\mvn.cmd" (
    echo Maven 3.8.6 found in cache, using it
  ) else (
    echo Downloading Maven 3.8.6 (this may take a while on first run)
    REM Trigger Maven Wrapper download by running -version first
    call mvnw.cmd -version >nul 2>&1
    set "EC=%ERRORLEVEL%"
    if not "%EC%"=="0" (
      echo [WARN] Maven Wrapper download may have failed. Retrying
      REM Clean potentially corrupted download and retry
      if exist "%USERPROFILE%\.m2\wrapper\dists" (
        powershell -NoProfile -ExecutionPolicy Bypass -Command "Get-ChildItem '%USERPROFILE%\.m2\wrapper\dists' -Recurse -Filter '*.zip' | Where-Object { $_.Length -eq 0 } | Remove-Item -Force -ErrorAction SilentlyContinue"
      )
      call mvnw.cmd -version >nul 2>&1
      set "EC=%ERRORLEVEL%"
      if not "%EC%"=="0" (
        echo.
        echo [ERROR] Failed to download Maven via Maven Wrapper.
        echo.
        echo Solutions:
        echo 1. Check your network connection and firewall settings
        echo 2. Install Maven manually and add it to PATH:
        echo    Download from: https://maven.apache.org/download.cgi
        echo    Extract and add bin directory to PATH
        echo 3. Or manually download Maven 3.8.6 to:
        echo    %USERPROFILE%\.m2\wrapper\dists\apache-maven-3.8.6-bin\1ks0nkde5v1pk9vtc31i9d0lcd\
        echo    From: https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.8.6/apache-maven-3.8.6-bin.zip
        echo.
        popd
        goto :err
      )
    )
  )
  popd
)

echo Building backend (skip tests)
pushd "%RADIUS_API_DIR%" || goto :err
call %MAVEN_CMD% -DskipTests package
set "EC=%ERRORLEVEL%"
popd
if not "%EC%"=="0" (
  echo [ERROR] Maven build failed with exit code %EC%
  goto :err
)

REM Copy latest jar to docker\java\app (keep directory clean)
del /q "%DOCKER_APP_DIR%\*.jar" >nul 2>nul

set "JAR_NAME="
for /f "delims=" %%F in ('dir /b /o-d "%RADIUS_API_DIR%\target\*.jar" 2^>nul ^| findstr /v /i "^original-"') do (
  set "JAR_NAME=%%F"
  goto :gotjar
)

:gotjar
if not defined JAR_NAME (
  echo [ERROR] No jar found in "%RADIUS_API_DIR%\target"
  goto :err
)

copy /y "%RADIUS_API_DIR%\target\%JAR_NAME%" "%DOCKER_APP_DIR%\%JAR_NAME%" >nul
echo OK: %DOCKER_APP_DIR%\%JAR_NAME%
exit /b 0

:ensure_java
REM 1) If JAVA_HOME already valid, use it
if defined JAVA_HOME (
  REM normalize quotes/apostrophes in JAVA_HOME (e.g. "'D:\Java\jdk'")
  set "JAVA_HOME=%JAVA_HOME:"=%"
  set "JAVA_HOME=%JAVA_HOME:'=%"
)
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" goto :java_ok

REM 2) Try java on PATH and derive JAVA_HOME
for /f "delims=" %%J in ('where java 2^>nul') do (
  set "JAVA_EXE=%%J"
  goto :have_java_exe
)
goto :download_java

:have_java_exe
for %%P in ("%JAVA_EXE%") do set "JAVA_BIN=%%~dpP"
set "JAVA_HOME=%JAVA_BIN%\.."
for %%H in ("%JAVA_HOME%") do set "JAVA_HOME=%%~fH"
if exist "%JAVA_HOME%\bin\java.exe" goto :java_ok

:download_java
echo No usable JDK found. Downloading Temurin JDK %DOWNLOAD_JAVA_MAJOR%
if not exist "%TOOLS_DIR%" mkdir "%TOOLS_DIR%" >nul 2>nul

set "ZIP=%TOOLS_DIR%\temurin-jdk%DOWNLOAD_JAVA_MAJOR%-windows-x64.zip"
set "INSTALL_ROOT=%TOOLS_DIR%\jdk%DOWNLOAD_JAVA_MAJOR%"
set "URI=https://api.adoptium.net/v3/binary/latest/%DOWNLOAD_JAVA_MAJOR%/ga/windows/x64/jdk/hotspot/normal/eclipse?project=jdk"

REM Download (PowerShell used internally; user does NOT need parameters)
powershell -NoProfile -ExecutionPolicy Bypass -Command "try{[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12}catch{}; Invoke-WebRequest -Uri '%URI%' -OutFile '%ZIP%' -UseBasicParsing"
if errorlevel 1 exit /b 1

REM Extract
if exist "%INSTALL_ROOT%" rmdir /s /q "%INSTALL_ROOT%"
mkdir "%INSTALL_ROOT%" >nul 2>nul
powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%ZIP%' -DestinationPath '%INSTALL_ROOT%' -Force"
if errorlevel 1 exit /b 1

for /d %%D in ("%INSTALL_ROOT%\*") do (
  if exist "%%D\bin\java.exe" (
    set "JAVA_HOME=%%~fD"
    goto :java_ok
  )
)
exit /b 1

:java_ok
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo Using JAVA_HOME=%JAVA_HOME%
REM Persist JAVA_HOME for current user (safe). Do NOT persist PATH to avoid truncation issues.
setx JAVA_HOME "%JAVA_HOME%" >nul 2>nul
exit /b 0

:err
echo.
echo [ERROR] build-backend failed.
pause
exit /b 1

