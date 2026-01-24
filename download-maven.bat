@echo off
setlocal EnableExtensions

echo ========================================
echo Maven 3.8.6 Manual Download Helper
echo ========================================
echo.

set "MAVEN_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.8.6/apache-maven-3.8.6-bin.zip"
set "TARGET_DIR=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.8.6-bin\1ks0nkde5v1pk9vtc31i9d0lcd"
set "ZIP_FILE=%TARGET_DIR%\apache-maven-3.8.6-bin.zip"

echo Target directory: %TARGET_DIR%
echo.

if exist "%TARGET_DIR%\apache-maven-3.8.6\bin\mvn.cmd" (
  echo [OK] Maven 3.8.6 already exists in cache.
  echo Location: %TARGET_DIR%\apache-maven-3.8.6
  pause
  exit /b 0
)

if not exist "%TARGET_DIR%" (
  echo Creating directory: %TARGET_DIR%
  mkdir "%TARGET_DIR%" >nul 2>nul
  if errorlevel 1 (
    echo [ERROR] Failed to create directory.
    pause
    exit /b 1
  )
)

if exist "%ZIP_FILE%" (
  echo [INFO] ZIP file already exists: %ZIP_FILE%
  echo Checking file size
  for %%F in ("%ZIP_FILE%") do set "ZIP_SIZE=%%~zF"
  if "%ZIP_SIZE%"=="0" (
    echo [WARN] ZIP file is empty, re-downloading
    del "%ZIP_FILE%" >nul 2>nul
  ) else (
    echo [OK] ZIP file exists (%ZIP_SIZE% bytes)
    goto :extract
  )
)

echo.
echo Downloading Maven 3.8.6 from:
echo %MAVEN_URL%
echo.
echo This may take a few minutes depending on your network speed
echo.

REM Create temporary PowerShell script to avoid escaping issues
set "TEMP_PS=%TEMP%\download-maven-temp.ps1"
(
  echo try { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12 } catch {}
  echo $ProgressPreference = 'SilentlyContinue'
  echo Invoke-WebRequest -Uri '%MAVEN_URL%' -OutFile '%ZIP_FILE%' -UseBasicParsing
  echo if ($LASTEXITCODE -ne 0) { exit 1 }
) > "%TEMP_PS%"

powershell -NoProfile -ExecutionPolicy Bypass -File "%TEMP_PS%"
set "DL_EC=%ERRORLEVEL%"
del "%TEMP_PS%" >nul 2>nul

if "%DL_EC%"=="1" (
  echo.
  echo [ERROR] Download failed!
  echo.
  echo Please try:
  echo 1. Check your network connection
  echo 2. Manually download from: %MAVEN_URL%
  echo 3. Save it to: %ZIP_FILE%
  echo 4. Run this script again to unzip
  pause
  exit /b 1
)

echo [OK] Download completed.

:extract
echo.
echo Unzipping Maven 3.8.6
set "TEMP_PS=%TEMP%\extract-maven-temp.ps1"
(
  echo Expand-Archive -Path '%ZIP_FILE%' -DestinationPath '%TARGET_DIR%' -Force
) > "%TEMP_PS%"
powershell -NoProfile -ExecutionPolicy Bypass -File "%TEMP_PS%"
set "EXT_EC=%ERRORLEVEL%"
del "%TEMP_PS%" >nul 2>nul

if "%EXT_EC%"=="1" (
  echo [ERROR] Extraction failed!
  pause
  exit /b 1
)

if exist "%TARGET_DIR%\apache-maven-3.8.6\bin\mvn.cmd" (
  echo.
  echo [SUCCESS] Maven 3.8.6 installed successfully!
  echo Location: %TARGET_DIR%\apache-maven-3.8.6
  echo.
  echo You can now run build-backend.bat
) else (
  echo [ERROR] Extraction completed but mvn.cmd not found!
  echo Please check: %TARGET_DIR%
)

pause
