/**
 * 简单的CORS代理服务器 (Node.js版本)
 * 用于解决测试客户端的跨域问题
 * 
 * 使用方法:
 *   node proxy_server.js
 * 
 * 然后在测试客户端中，将服务器地址改为:
 *   http://localhost:8888/proxy
 */

const http = require('http');
const https = require('https');
const url = require('url');

// 默认目标服务器地址
const DEFAULT_TARGET = process.env.RADIUS_SERVER_URL || 'http://10.11.6.92:8090/services/IServiceUopBossToTvManager';

const PORT = process.argv[2] ? parseInt(process.argv[2]) : 8888;

const server = http.createServer((req, res) => {
    // 处理CORS预检请求
    if (req.method === 'OPTIONS') {
        res.writeHead(200, {
            'Access-Control-Allow-Origin': '*',
            'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
            'Access-Control-Allow-Headers': 'Content-Type, SOAPAction',
            'Access-Control-Max-Age': '3600'
        });
        res.end();
        return;
    }

    // 只处理 /proxy 路径
    if (!req.url.startsWith('/proxy')) {
        res.writeHead(404, { 'Content-Type': 'text/plain' });
        res.end('Not Found');
        return;
    }

    // 获取目标URL
    let targetUrl = DEFAULT_TARGET;
    const parsedUrl = url.parse(req.url, true);
    
    if (parsedUrl.query.url) {
        targetUrl = parsedUrl.query.url;
    } else if (req.headers['x-target-url']) {
        targetUrl = req.headers['x-target-url'];
    }

    console.log(`[${new Date().toISOString()}] ${req.method} ${req.url} -> ${targetUrl}`);

    // 解析目标URL
    const target = url.parse(targetUrl);
    const isHttps = target.protocol === 'https:';
    const httpModule = isHttps ? https : http;

    // 收集请求体
    let body = [];
    req.on('data', chunk => {
        body.push(chunk);
    });

    req.on('end', () => {
        const requestBody = Buffer.concat(body);

        // 创建转发请求的选项
        const options = {
            hostname: target.hostname,
            port: target.port || (isHttps ? 443 : 80),
            path: target.path,
            method: req.method,
            headers: {}
        };

        // 复制原始请求头（排除一些不需要的）
        for (const key in req.headers) {
            if (key.toLowerCase() !== 'host' && 
                key.toLowerCase() !== 'connection' &&
                key.toLowerCase() !== 'content-length') {
                options.headers[key] = req.headers[key];
            }
        }

        // 如果有请求体，设置Content-Length
        if (requestBody.length > 0) {
            options.headers['Content-Length'] = requestBody.length;
        }

        // 发送请求
        const proxyReq = httpModule.request(options, (proxyRes) => {
            // 设置CORS响应头
            res.writeHead(proxyRes.statusCode, {
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
                'Access-Control-Allow-Headers': 'Content-Type, SOAPAction',
                'Content-Type': proxyRes.headers['content-type'] || 'text/xml; charset=utf-8'
            });

            // 转发响应体
            proxyRes.pipe(res);
        });

        proxyReq.on('error', (err) => {
            console.error(`代理错误: ${err.message}`);
            res.writeHead(500, {
                'Access-Control-Allow-Origin': '*',
                'Content-Type': 'text/plain'
            });
            res.end(`代理错误: ${err.message}`);
        });

        // 发送请求体
        if (requestBody.length > 0) {
            proxyReq.write(requestBody);
        }
        proxyReq.end();
    });

    req.on('error', (err) => {
        console.error(`请求错误: ${err.message}`);
        res.writeHead(500, {
            'Access-Control-Allow-Origin': '*',
            'Content-Type': 'text/plain'
        });
        res.end(`请求错误: ${err.message}`);
    });
});

server.listen(PORT, () => {
    console.log('代理服务器已启动');
    console.log(`监听地址: http://localhost:${PORT}`);
    console.log(`代理路径: http://localhost:${PORT}/proxy`);
    console.log(`\n在测试客户端中配置:`);
    console.log(`  服务器IP: localhost`);
    console.log(`  端口: ${PORT}`);
    console.log(`  路径: /proxy`);
    console.log(`\n按 Ctrl+C 停止服务器`);
});
