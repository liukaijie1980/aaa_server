#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
简单的CORS代理服务器
用于解决测试客户端的跨域问题

使用方法:
    python proxy_server.py

然后在测试客户端中，将服务器地址改为:
    http://localhost:8888/proxy
"""

from http.server import HTTPServer, BaseHTTPRequestHandler
import urllib.request
import urllib.parse
import json

class ProxyHandler(BaseHTTPRequestHandler):
    def do_OPTIONS(self):
        """处理CORS预检请求"""
        self.send_response(200)
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type, SOAPAction')
        self.send_header('Access-Control-Max-Age', '3600')
        self.end_headers()

    def do_GET(self):
        """处理GET请求（用于测试连接）"""
        if self.path.startswith('/proxy'):
            # 从查询参数获取目标URL
            query = urllib.parse.urlparse(self.path).query
            params = urllib.parse.parse_qs(query)
            target_url = params.get('url', [None])[0]
            
            if not target_url:
                self.send_error(400, "Missing 'url' parameter")
                return
            
            try:
                # 转发请求
                req = urllib.request.Request(target_url)
                with urllib.request.urlopen(req, timeout=10) as response:
                    content = response.read()
                    
                    # 返回响应，添加CORS头
                    self.send_response(200)
                    self.send_header('Access-Control-Allow-Origin', '*')
                    self.send_header('Content-Type', response.headers.get('Content-Type', 'text/xml'))
                    self.end_headers()
                    self.wfile.write(content)
            except Exception as e:
                self.send_error(500, str(e))
        else:
            self.send_error(404)

    def do_POST(self):
        """处理POST请求（用于SOAP请求）"""
        if self.path == '/proxy' or self.path.startswith('/proxy?'):
            # 读取请求体
            content_length = int(self.headers.get('Content-Length', 0))
            body = self.rfile.read(content_length)
            
            # 从查询参数或请求头获取目标URL
            target_url = None
            if '?' in self.path:
                query = urllib.parse.urlparse(self.path).query
                params = urllib.parse.parse_qs(query)
                target_url = params.get('url', [None])[0]
            
            # 如果没有在URL中，尝试从自定义头获取
            if not target_url:
                target_url = self.headers.get('X-Target-URL')
            
            # 如果还是没有，使用默认配置
            if not target_url:
                # 默认RADIUS服务器地址，可以从环境变量或配置文件读取
                import os
                target_url = os.environ.get('RADIUS_SERVER_URL', 
                    'http://10.11.6.92:8090/services/IServiceUopBossToTvManager')
            
            try:
                # 创建转发请求
                req = urllib.request.Request(target_url, data=body)
                
                # 复制原始请求头
                for header, value in self.headers.items():
                    if header.lower() not in ['host', 'content-length', 'connection']:
                        req.add_header(header, value)
                
                # 转发请求
                with urllib.request.urlopen(req, timeout=30) as response:
                    content = response.read()
                    
                    # 返回响应，添加CORS头
                    self.send_response(200)
                    self.send_header('Access-Control-Allow-Origin', '*')
                    self.send_header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
                    self.send_header('Access-Control-Allow-Headers', 'Content-Type, SOAPAction')
                    self.send_header('Content-Type', response.headers.get('Content-Type', 'text/xml; charset=utf-8'))
                    self.end_headers()
                    self.wfile.write(content)
            except urllib.error.HTTPError as e:
                # 转发HTTP错误
                error_content = e.read() if e.fp else b''
                self.send_response(e.code)
                self.send_header('Access-Control-Allow-Origin', '*')
                self.send_header('Content-Type', 'text/xml; charset=utf-8')
                self.end_headers()
                self.wfile.write(error_content)
            except Exception as e:
                self.send_error(500, f"Proxy error: {str(e)}")
        else:
            self.send_error(404)

    def log_message(self, format, *args):
        """自定义日志格式"""
        print(f"[{self.address_string()}] {format % args}")

def run(port=8888):
    """启动代理服务器"""
    server_address = ('', port)
    httpd = HTTPServer(server_address, ProxyHandler)
    print(f"代理服务器已启动")
    print(f"监听地址: http://localhost:{port}")
    print(f"代理路径: http://localhost:{port}/proxy")
    print(f"\n在测试客户端中配置:")
    print(f"  服务器IP: localhost")
    print(f"  端口: {port}")
    print(f"  路径: /proxy")
    print(f"\n按 Ctrl+C 停止服务器")
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("\n服务器已停止")
        httpd.server_close()

if __name__ == '__main__':
    import sys
    port = 8888
    if len(sys.argv) > 1:
        try:
            port = int(sys.argv[1])
        except ValueError:
            print("用法: python proxy_server.py [端口号]")
            sys.exit(1)
    run(port)
