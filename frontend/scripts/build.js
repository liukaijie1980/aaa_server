#!/usr/bin/env node
// 跨平台的构建脚本，自动检测是否需要 --openssl-legacy-provider
const { spawn } = require('child_process');
const path = require('path');

const isWindows = process.platform === 'win32';
const vueCliService = path.join(__dirname, '..', 'node_modules', '.bin', 'vue-cli-service');
const args = process.argv.slice(2);
const command = args[0] || 'build';

// 根据平台决定是否使用 legacy provider
// Linux 上通常不支持该选项，Windows 上 Node.js 17+ 可能需要
const useLegacyProvider = isWindows;

// 运行命令
const nodeArgs = useLegacyProvider ? ['--openssl-legacy-provider', vueCliService] : [vueCliService];
const allArgs = [...nodeArgs, command, ...args.slice(1)];

const child = spawn('node', allArgs, {
  stdio: 'inherit',
  shell: isWindows
});

child.on('error', (error) => {
  console.error('执行失败:', error.message);
  process.exit(1);
});

child.on('exit', (code) => {
  process.exit(code || 0);
});
