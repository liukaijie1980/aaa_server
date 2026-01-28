#!/bin/bash
set -euo pipefail

# Normalize script root path
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REQUIRED_JAVA_MAJOR=18
DOWNLOAD_JAVA_MAJOR=18

function normalize_env_path_value() {
  local value="$1"
  if [ -z "$value" ]; then
    return
  fi
  # Remove surrounding quotes if any
  value="${value#"${value%%[![:space:]]*}"}"  # trim leading whitespace
  value="${value%"${value##*[![:space:]]}"}"  # trim trailing whitespace
  value="${value%\"}"  # remove trailing quote
  value="${value#\"}"  # remove leading quote
  value="${value%\'}"  # remove trailing single quote
  value="${value#\'}"  # remove leading single quote
  echo "$value"
}

function get_java_major() {
  local java_exe="$1"
  if [ ! -f "$java_exe" ]; then
    return
  fi
  # java -version writes to stderr
  local version_output
  version_output=$("$java_exe" -version 2>&1 || true)
  
  # Examples:
  # openjdk version "18.0.2" 2022-07-19
  # java version "1.8.0_202"
  if [[ $version_output =~ version\ \"([0-9]+)\.([0-9]+) ]]; then
    local major="${BASH_REMATCH[1]}"
    if [ -n "$major" ] && [ "$major" != "1" ]; then
      echo "$major"
      return
    fi
    # For Java 8 and below, major is in second group
    if [ -n "${BASH_REMATCH[2]}" ]; then
      echo "${BASH_REMATCH[2]}"
      return
    fi
  fi
  # Try alternative pattern for some JDK versions
  if [[ $version_output =~ \"([0-9]+)\.([0-9]+)\. ]]; then
    local major="${BASH_REMATCH[1]}"
    if [ "$major" != "1" ]; then
      echo "$major"
      return
    fi
    echo "${BASH_REMATCH[2]}"
  fi
}

function try_resolve_java_home_from_java_exe() {
  local java_exe="$1"
  # java is usually: <JAVA_HOME>/bin/java
  local bin_dir
  bin_dir="$(dirname "$java_exe")"
  local home_dir
  home_dir="$(dirname "$bin_dir")"
  if [ -f "$home_dir/bin/java" ]; then
    echo "$home_dir"
  fi
}

function ensure_jdk() {
  # 1) Prefer existing JAVA_HOME
  local env_java_home
  env_java_home=$(normalize_env_path_value "${JAVA_HOME:-}")
  if [ -n "$env_java_home" ]; then
    local java_exe="$env_java_home/bin/java"
    local major
    major=$(get_java_major "$java_exe" || echo "")
    if [ -n "$major" ] && [ "$major" -ge "$REQUIRED_JAVA_MAJOR" ]; then
      echo "$env_java_home"
      return
    fi
  fi

  # 2) Try java on PATH
  if command -v java >/dev/null 2>&1; then
    local java_cmd
    java_cmd=$(command -v java)
    local major
    major=$(get_java_major "$java_cmd" || echo "")
    if [ -n "$major" ] && [ "$major" -ge "$REQUIRED_JAVA_MAJOR" ]; then
      local home
      home=$(try_resolve_java_home_from_java_exe "$java_cmd")
      if [ -n "$home" ]; then
        echo "$home"
        return
      fi
    fi
  fi

  # 3) Download Temurin JDK (LTS) from Adoptium to repo-local .tools
  local tools_dir="$REPO_ROOT/.tools"
  local install_root="$tools_dir/jdk$DOWNLOAD_JAVA_MAJOR"
  local tar_path="$tools_dir/temurin-jdk${DOWNLOAD_JAVA_MAJOR}-linux-x64.tar.gz"

  mkdir -p "$tools_dir"

  # If already downloaded+extracted, reuse
  if [ -d "$install_root" ]; then
    local candidate
    candidate=$(find "$install_root" -mindepth 1 -maxdepth 1 -type d | head -n 1)
    if [ -n "$candidate" ] && [ -f "$candidate/bin/java" ]; then
      local major
      major=$(get_java_major "$candidate/bin/java" || echo "")
      if [ -n "$major" ] && [ "$major" -ge "$REQUIRED_JAVA_MAJOR" ]; then
        echo "$candidate"
        return
      fi
    fi
  fi

  # Detect architecture
  local arch
  arch=$(uname -m)
  case "$arch" in
    x86_64)
      arch="x64"
      ;;
    aarch64|arm64)
      arch="aarch64"
      ;;
    *)
      echo "ERROR: Unsupported architecture: $arch" >&2
      exit 1
      ;;
  esac

  local uri="https://api.adoptium.net/v3/binary/latest/$DOWNLOAD_JAVA_MAJOR/ga/linux/$arch/jdk/hotspot/normal/eclipse?project=jdk"

  echo "Downloading JDK $DOWNLOAD_JAVA_MAJOR from Adoptium..." >&2
  if command -v curl >/dev/null 2>&1; then
    curl -L -o "$tar_path" "$uri"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$tar_path" "$uri"
  else
    echo "ERROR: Neither curl nor wget found. Please install one of them." >&2
    exit 1
  fi

  if [ -d "$install_root" ]; then
    rm -rf "$install_root"
  fi
  mkdir -p "$install_root"

  echo "Extracting to $install_root ..." >&2
  # Extract without strip-components first to see the structure
  tar -xzf "$tar_path" -C "$install_root"

  # Find the JDK directory - it should contain bin/java
  local jdk_home
  jdk_home=$(find "$install_root" -type f -name "java" -path "*/bin/java" | head -n 1)
  
  if [ -z "$jdk_home" ]; then
    # Try alternative: look for directories that might be JDK root
    jdk_home=$(find "$install_root" -mindepth 1 -maxdepth 3 -type d -name "jdk*" | head -n 1)
    if [ -n "$jdk_home" ] && [ ! -f "$jdk_home/bin/java" ]; then
      jdk_home=""
    fi
  else
    # Extract the directory path (remove /bin/java)
    jdk_home=$(dirname "$(dirname "$jdk_home")")
  fi

  # If still not found, check if install_root itself contains bin/java
  if [ -z "$jdk_home" ] || [ ! -f "$jdk_home/bin/java" ]; then
    if [ -f "$install_root/bin/java" ]; then
      jdk_home="$install_root"
    else
      echo "ERROR: JDK extract failed: bin/java not found." >&2
      echo "DEBUG: Contents of $install_root:" >&2
      ls -la "$install_root" >&2 || true
      exit 1
    fi
  fi

  if [ ! -f "$jdk_home/bin/java" ]; then
    echo "ERROR: JDK extract failed: bin/java not found in $jdk_home." >&2
    exit 1
  fi

  local major
  major=$(get_java_major "$jdk_home/bin/java" || echo "")
  if [ -z "$major" ] || [ "$major" -lt "$REQUIRED_JAVA_MAJOR" ]; then
    echo "ERROR: Downloaded JDK version is too old: need >= $REQUIRED_JAVA_MAJOR, got ${major:-unknown}." >&2
    exit 1
  fi

  echo "$jdk_home"
}

function set_java_env() {
  local java_home="$1"
  java_home=$(normalize_env_path_value "$java_home")
  if [ -z "$java_home" ]; then
    echo "ERROR: JAVA_HOME is empty." >&2
    exit 1
  fi
  if [ ! -f "$java_home/bin/java" ]; then
    echo "ERROR: Invalid JAVA_HOME: $java_home" >&2
    exit 1
  fi

  export JAVA_HOME="$java_home"
  local java_bin="$java_home/bin"
  if [[ ":$PATH:" != *":$java_bin:"* ]]; then
    export PATH="$java_bin:$PATH"
  fi

  # Note: We don't persist to shell profile automatically in Linux
  # User can add to ~/.bashrc or ~/.profile if needed
}

function build_backend() {
  local docker_app_dir="$REPO_ROOT/docker/java/app"
  mkdir -p "$docker_app_dir"

  local radius_api_dir="$REPO_ROOT/radiusApi"
  local mvnw="$radius_api_dir/mvnw"
  if [ ! -f "$mvnw" ]; then
    echo "ERROR: Maven wrapper not found: $mvnw" >&2
    exit 1
  fi
  chmod +x "$mvnw" 2>/dev/null || true

  # Try to use system Maven first (if available), otherwise use Maven Wrapper
  local maven_cmd
  if command -v mvn >/dev/null 2>&1; then
    echo "Using system Maven installation..."
    maven_cmd="mvn"
  else
    echo "Using Maven Wrapper from radiusApi..."
    maven_cmd="$mvnw"
    
    # Check if Maven is already downloaded in user's .m2 directory
    local maven_dist="$HOME/.m2/wrapper/dists/apache-maven-3.8.6-bin"
    if [ -d "$maven_dist" ]; then
      local maven_bin
      maven_bin=$(find "$maven_dist" -name "mvn" -type f | head -n 1)
      if [ -n "$maven_bin" ] && [ -f "$maven_bin" ]; then
        echo "Maven 3.8.6 found in cache, using it..."
      fi
    else
      echo "Downloading Maven 3.8.6 (this may take a while on first run)..."
      local old_pwd
      old_pwd=$(pwd)
      cd "$radius_api_dir" || exit 1
      if ! "$mvnw" -version >/dev/null 2>&1; then
        echo "Maven Wrapper download may have failed. Retrying..."
        # Clean potentially corrupted downloads
        local wrapper_dists="$HOME/.m2/wrapper/dists"
        if [ -d "$wrapper_dists" ]; then
          find "$wrapper_dists" -name "*.zip" -size 0 -delete 2>/dev/null || true
        fi
        if ! "$mvnw" -version >/dev/null 2>&1; then
          cat >&2 <<EOF

[ERROR] Failed to download Maven via Maven Wrapper.

Solutions:
1. Check your network connection and firewall settings
2. Install Maven manually and add it to PATH:
   Download from: https://maven.apache.org/download.cgi
   Extract and add bin directory to PATH
3. Or manually download Maven 3.8.6 to:
   $HOME/.m2/wrapper/dists/apache-maven-3.8.6-bin/
   From: https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.8.6/apache-maven-3.8.6-bin.zip
EOF
          exit 1
        fi
      fi
      cd "$old_pwd" || exit 1
    fi
  fi

  local old_pwd
  old_pwd=$(pwd)
  cd "$radius_api_dir" || exit 1
  echo "Building backend (skip tests)..."
  if ! $maven_cmd -DskipTests package; then
    echo "ERROR: Maven build failed." >&2
    cd "$old_pwd" || exit 1
    exit 1
  fi
  cd "$old_pwd" || exit 1

  local target_dir="$radius_api_dir/target"
  local jar
  jar=$(find "$target_dir" -maxdepth 1 -name "*.jar" -type f ! -name "original-*.jar" | sort -t/ -k2 -r | head -n 1)

  if [ -z "$jar" ]; then
    echo "ERROR: Build finished but no jar found in $target_dir." >&2
    exit 1
  fi

  # Keep app dir clean so docker-entrypoint can use a stable jar name
  find "$docker_app_dir" -maxdepth 1 -name "*.jar" -type f -delete 2>/dev/null || true
  cp -f "$jar" "$docker_app_dir/$(basename "$jar")"

  echo "OK: $docker_app_dir/$(basename "$jar")"
}

# Main execution
main() {
  local java_home
  # ensure_jdk echoes progress to stderr; only the final path goes to stdout (take last line as safeguard)
  java_home=$(ensure_jdk | tail -n 1 | xargs)
  set_java_env "$java_home"
  echo "JAVA_HOME=$java_home"
  build_backend
}

# Run main function and handle errors
if ! main; then
  exit 1
fi
