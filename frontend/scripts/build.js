#!/usr/bin/env node
// 跨平台的构建脚本，自动检测是否需要 --openssl-legacy-provider
const { spawn } = require('child_process');
const path = require('path');

const isWindows = process.platform === 'win32';
const args = process.argv.slice(2);
const command = args[0] || 'build';

// 直接使用 vue-cli-service.js 文件路径，而不是 .bin 中的包装脚本
// 这样可以避免在 Windows 上执行 shell 脚本的问题
const vueCliServiceJs = path.join(__dirname, '..', 'node_modules', '@vue', 'cli-service', 'bin', 'vue-cli-service.js');

// 检测 Node.js 版本，决定是否使用 legacy provider
// Node.js 17+ 在某些情况下可能需要 --openssl-legacy-provider
const nodeVersion = process.version.match(/^v(\d+)\./);
const nodeMajor = nodeVersion ? parseInt(nodeVersion[1], 10) : 0;
const useLegacyProvider = nodeMajor >= 17;

// 运行命令
const nodeArgs = useLegacyProvider ? ['--openssl-legacy-provider', vueCliServiceJs] : [vueCliServiceJs];
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
