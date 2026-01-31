#!/bin/bash
set -e

cd frontend

# 判断是否已安装 Node.js 且版本 >= 16
need_install=false
if command -v node &> /dev/null; then
  NODE_MAJOR=$(node -v | sed 's/v//' | cut -d'.' -f1)
  if [ "$NODE_MAJOR" -ge 16 ] 2>/dev/null; then
    echo "Node.js 已安装，版本: $(node -v)，跳过安装"
  else
    echo "当前 Node.js 版本 $(node -v) 低于 16，需要升级"
    need_install=true
  fi
else
  echo "未检测到 Node.js，需要安装"
  need_install=true
fi

if [ "$need_install" = true ]; then
  echo "正在安装 Node.js 16..."
  curl -fsSL https://rpm.nodesource.com/setup_16.x | sudo bash -
  sudo yum install -y nodejs
  echo "Node.js 安装完成: $(node -v)"
fi

# 避免 npm 拉取 GitHub git 依赖时 SSH 主机验证失败（非交互环境）
# 用 HTTPS 替代 SSH 拉取 GitHub，无需配置 SSH 密钥与 known_hosts
git config --global url."https://github.com/".insteadOf "ssh://git@github.com/" 2>/dev/null || true
mkdir -p ~/.ssh
if ! grep -q "github.com" ~/.ssh/known_hosts 2>/dev/null; then
  echo "将 GitHub 加入 SSH known_hosts..."
  ssh-keyscan -t ecdsa,rsa github.com >> ~/.ssh/known_hosts 2>/dev/null || true
fi

npm install
npm run build:prod
