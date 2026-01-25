#!/bin/bash
# 检测 Node.js 版本并决定是否使用 --openssl-legacy-provider
NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)

if [ "$NODE_VERSION" -ge 17 ]; then
  # Node.js 17+ 可能需要 --openssl-legacy-provider
  # 先尝试不使用，如果失败再使用
  node --openssl-legacy-provider ./node_modules/.bin/vue-cli-service serve "$@" 2>/dev/null || \
  node ./node_modules/.bin/vue-cli-service serve "$@"
else
  # Node.js < 17 不需要这个选项
  node ./node_modules/.bin/vue-cli-service serve "$@"
fi
