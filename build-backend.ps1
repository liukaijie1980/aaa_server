#requires -version 5.1

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

# Normalize script root path (avoid quoted/odd values)
$RepoRoot = (Resolve-Path -LiteralPath $PSScriptRoot).Path
$RequiredJavaMajor = 18
$DownloadJavaMajor = 21
$ProgressPreference = 'SilentlyContinue'

function Normalize-EnvPathValue([string]$value) {
  if (-not $value) { return $null }
  # Some users set JAVA_HOME with surrounding quotes, e.g. "'D:\Java\jdk-17'"
  return $value.Trim().Trim('"').Trim("'")
}

function Get-JavaMajor([string]$javaExe) {
  if (-not (Test-Path $javaExe)) { return $null }
  # java -version writes to stderr; avoid PowerShell treating it as an error
  $cmdLine = "`"$javaExe`" -version 2>&1"
  $out = & cmd.exe /c $cmdLine
  $text = ($out | Out-String).Trim()

  # Examples:
  # openjdk version "18.0.2" 2022-07-19
  # java version "1.8.0_202"
  if ($text -match 'version\s+"(?:(\d+)\.)?(\d+)') {
    if ($Matches[1]) { return [int]$Matches[1] }
    return [int]$Matches[2]
  }
  return $null
}

function Try-ResolveJavaHomeFromJavaExe([string]$javaExe) {
  # java.exe is usually: <JAVA_HOME>\bin\java.exe
  $bin = Split-Path -Parent $javaExe
  $home = Split-Path -Parent $bin
  if (Test-Path (Join-Path $home 'bin\java.exe')) { return $home }
  return $null
}

function Ensure-Jdk {
  # 1) Prefer existing JAVA_HOME
  $envJavaHome = Normalize-EnvPathValue $env:JAVA_HOME
  if ($envJavaHome) {
    $javaExe = Join-Path $envJavaHome 'bin\java.exe'
    $major = Get-JavaMajor $javaExe
    if ($major -and $major -ge $RequiredJavaMajor) {
      return $envJavaHome
    }
  }

  # 2) Try java on PATH
  $cmd = Get-Command java -ErrorAction SilentlyContinue
  if ($cmd -and $cmd.Source) {
    $major = Get-JavaMajor $cmd.Source
    if ($major -and $major -ge $RequiredJavaMajor) {
      $home = Try-ResolveJavaHomeFromJavaExe $cmd.Source
      if ($home) { return $home }
    }
  }

  # 3) Download Temurin JDK (LTS) from Adoptium to repo-local .tools
  $toolsDir = Join-Path $RepoRoot '.tools'
  $installRoot = Join-Path $toolsDir ("jdk" + $DownloadJavaMajor)
  $zipPath = Join-Path $toolsDir ("temurin-jdk" + $DownloadJavaMajor + "-windows-x64.zip")

  New-Item -ItemType Directory -Force $toolsDir | Out-Null

  # If already downloaded+extracted, reuse
  if (Test-Path $installRoot) {
    $candidate = Get-ChildItem -Directory $installRoot -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($candidate -and (Test-Path (Join-Path $candidate.FullName 'bin\java.exe'))) {
      $major = Get-JavaMajor (Join-Path $candidate.FullName 'bin\java.exe')
      if ($major -and $major -ge $RequiredJavaMajor) {
        return $candidate.FullName
      }
    }
  }

  $uri = "https://api.adoptium.net/v3/binary/latest/$DownloadJavaMajor/ga/windows/x64/jdk/hotspot/normal/eclipse?project=jdk"

  try {
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
  } catch {
    # ignore - best effort on older PS
  }

  Write-Output "Downloading JDK $DownloadJavaMajor from Adoptium..."
  Invoke-WebRequest -Uri $uri -OutFile $zipPath -UseBasicParsing

  if (Test-Path $installRoot) {
    Remove-Item -Recurse -Force $installRoot
  }
  New-Item -ItemType Directory -Force $installRoot | Out-Null

  Write-Output "Extracting to $installRoot ..."
  Expand-Archive -Path $zipPath -DestinationPath $installRoot -Force

  $jdkHome = Get-ChildItem -Directory $installRoot | Select-Object -First 1
  if (-not $jdkHome) {
    throw "JDK extract failed: no extracted directory found."
  }
  if (-not (Test-Path (Join-Path $jdkHome.FullName 'bin\java.exe'))) {
    throw "JDK extract failed: bin\\java.exe not found."
  }

  $major = Get-JavaMajor (Join-Path $jdkHome.FullName 'bin\java.exe')
  if (-not $major -or $major -lt $RequiredJavaMajor) {
    throw "Downloaded JDK version is too old: need >= $RequiredJavaMajor, got $major."
  }

  return $jdkHome.FullName
}

function Set-JavaEnv([string]$javaHome) {
  $javaHome = Normalize-EnvPathValue $javaHome
  if (-not $javaHome) { throw "JAVA_HOME is empty." }
  if (-not (Test-Path (Join-Path $javaHome 'bin\java.exe'))) { throw "Invalid JAVA_HOME: $javaHome" }

  $env:JAVA_HOME = $javaHome
  $javaBin = Join-Path $javaHome 'bin'
  if (-not ($env:Path.Split(';') | Where-Object { $_ -eq $javaBin })) {
    $env:Path = "$javaBin;$($env:Path)"
  }

  # Persist to current user (no admin needed)
  [Environment]::SetEnvironmentVariable('JAVA_HOME', $javaHome, 'User')

  $userPath = [Environment]::GetEnvironmentVariable('Path', 'User')
  if (-not $userPath) { $userPath = '' }
  if (-not ($userPath.Split(';') | Where-Object { $_ -eq $javaBin })) {
    $newUserPath = if ([string]::IsNullOrWhiteSpace($userPath)) { $javaBin } else { "$javaBin;$userPath" }
    [Environment]::SetEnvironmentVariable('Path', $newUserPath, 'User')
  }
}

function Build-Backend {
  $dockerAppDir = Join-Path $RepoRoot 'docker\java\app'
  New-Item -ItemType Directory -Force $dockerAppDir | Out-Null

  $radiusApiDir = Join-Path $RepoRoot 'radiusApi'
  $mvnw = Join-Path $radiusApiDir 'mvnw.cmd'
  if (-not (Test-Path $mvnw)) { throw "Maven wrapper not found: $mvnw" }

  # Try to use system Maven first (if available), otherwise use Maven Wrapper
  $mavenCmd = $null
  $systemMvn = Get-Command mvn -ErrorAction SilentlyContinue
  if ($systemMvn) {
    Write-Output "Using system Maven installation..."
    $mavenCmd = 'mvn'
  } else {
    Write-Output "Using Maven Wrapper from radiusApi..."
    $mavenCmd = $mvnw
    
    # Check if Maven is already downloaded in user's .m2 directory
    $mavenDist = Join-Path $env:USERPROFILE '.m2\wrapper\dists\apache-maven-3.8.6-bin\1ks0nkde5v1pk9vtc31i9d0lcd\apache-maven-3.8.6'
    if (Test-Path (Join-Path $mavenDist 'bin\mvn.cmd')) {
      Write-Output "Maven 3.8.6 found in cache, using it..."
    } else {
      Write-Output "Downloading Maven 3.8.6 (this may take a while on first run)..."
      Push-Location $radiusApiDir
      try {
        & $mvnw -version 2>&1 | Out-Null
        if ($LASTEXITCODE -ne 0) {
          Write-Output "Maven Wrapper download may have failed. Retrying..."
          # Clean potentially corrupted downloads
          $wrapperDists = Join-Path $env:USERPROFILE '.m2\wrapper\dists'
          if (Test-Path $wrapperDists) {
            Get-ChildItem -Path $wrapperDists -Recurse -Filter '*.zip' -ErrorAction SilentlyContinue |
              Where-Object { $_.Length -eq 0 } |
              Remove-Item -Force -ErrorAction SilentlyContinue
          }
          & $mvnw -version 2>&1 | Out-Null
          if ($LASTEXITCODE -ne 0) {
            $msg = @"

[ERROR] Failed to download Maven via Maven Wrapper.

Solutions:
1. Check your network connection and firewall settings
2. Install Maven manually and add it to PATH:
   Download from: https://maven.apache.org/download.cgi
   Extract and add bin directory to PATH
3. Or manually download Maven 3.8.6 to:
   $env:USERPROFILE\.m2\wrapper\dists\apache-maven-3.8.6-bin\1ks0nkde5v1pk9vtc31i9d0lcd\
   From: https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.8.6/apache-maven-3.8.6-bin.zip
"@
            throw $msg
          }
        }
      } finally {
        Pop-Location
      }
    }
  }

  Push-Location $radiusApiDir
  try {
    Write-Output "Building backend (skip tests)..."
    & $mavenCmd -DskipTests package
    if ($LASTEXITCODE -ne 0) {
      throw "Maven build failed with exit code $LASTEXITCODE."
    }
  } finally {
    Pop-Location
  }

  $targetDir = Join-Path $radiusApiDir 'target'
  $jar = Get-ChildItem -Path $targetDir -Filter '*.jar' -File -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -notmatch '^original-.*\.jar$' } |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

  if (-not $jar) {
    throw "Build finished but no jar found in $targetDir."
  }

  # Keep app dir clean so docker-entrypoint can use a stable jar name
  Get-ChildItem -Path $dockerAppDir -Filter '*.jar' -File -ErrorAction SilentlyContinue | Remove-Item -Force -ErrorAction SilentlyContinue
  Copy-Item -Force -Path $jar.FullName -Destination (Join-Path $dockerAppDir $jar.Name)

  Write-Output ("OK: " + (Join-Path $dockerAppDir $jar.Name))
}

try {
  $javaHome = Ensure-Jdk
  Set-JavaEnv $javaHome
  Write-Output "JAVA_HOME=$javaHome"
  Build-Backend
  exit 0
} catch {
  $msg = $_.Exception.Message
  $type = $_.Exception.GetType().FullName
  $line = $_.InvocationInfo.ScriptLineNumber
  $pos  = $_.InvocationInfo.OffsetInLine
  $ctx  = $_.InvocationInfo.Line
  [Console]::Error.WriteLine("ERROR ($type) at line ${line}:${pos}")
  if ($msg) { [Console]::Error.WriteLine($msg) }
  if ($ctx) { [Console]::Error.WriteLine($ctx) }
  exit 1
}

