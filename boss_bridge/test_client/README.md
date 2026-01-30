# RADIUS服务器接口测试客户端

这是一个基于Web的RADIUS服务器接口测试客户端，使用JavaScript编写，提供图形化界面，方便测试所有19个API接口。

## 功能特点

- ✅ **图形化界面**：现代化的UI设计，操作简单直观
- ✅ **支持所有API**：完整支持19个RADIUS服务器接口方法
- ✅ **动态表单**：根据选择的API方法自动生成参数输入表单
- ✅ **实时响应**：显示SOAP请求和响应内容
- ✅ **错误提示**：自动解析返回码并显示说明
- ✅ **无需安装**：纯HTML+JavaScript，直接在浏览器中打开即可使用

## 支持的API方法

1. **IsExist** - 检查账户是否存在
2. **ForceOffline** - 强制用户下线
3. **CheckPassword** - 验证密码
4. **SearchCustomerStatus** - 查询客户状态
5. **Register** - 注册新用户
6. **IpoeRegister** - IPoE注册
7. **EraseCustomer** - 删除客户
8. **IpoeEraseCustomer** - IPoE删除客户
9. **ChangeUserInfo** - 修改用户信息
10. **ChangeUserPassword** - 修改用户密码
11. **EnableAccount** - 启用/禁用账户
12. **BindMac** - 绑定MAC地址
13. **ModifyBindMac** - 修改绑定的MAC地址
14. **DelBindMac** - 删除MAC绑定
15. **SetTimeLimit** - 设置时间限制
16. **DelTimeLimit** - 删除时间限制
17. **AutoMacOn** - 自动MAC开启
18. **AutoMacOff** - 自动MAC关闭
19. **ClearBlackList** - 清除黑名单

## 使用方法

### 1. 打开测试客户端

**重要说明**：这是一个前端Web应用，JavaScript代码在**浏览器**中运行，不是在Node.js环境中运行。

#### 方式一：直接打开（简单但不推荐）
直接在浏览器中双击打开 `index.html` 文件。但可能会遇到CORS跨域问题。

#### 方式二：使用本地HTTP服务器（推荐）

由于浏览器的安全限制，建议使用本地HTTP服务器来托管HTML文件。可以使用以下任一方式：

**使用Python（如果已安装Python）：**
```bash
cd test_client
python -m http.server 8000
```

**使用Node.js（如果已安装Node.js）：**
```bash
cd test_client
npx http-server -p 8000
# 或者全局安装后使用
npm install -g http-server
http-server -p 8000
```

**使用PHP（如果已安装PHP）：**
```bash
cd test_client
php -S localhost:8000
```

然后在浏览器中访问：`http://localhost:8000/index.html`

> **注意**：这些HTTP服务器只是用来托管静态HTML文件，JavaScript代码仍然是在浏览器中执行的，不是Node.js环境。

### 2. 配置服务地址

在左侧"服务配置"区域，可以分别输入：
- **服务器IP**：例如 `10.11.6.92` 或 `localhost`
- **端口**：例如 `8090`
- **路径**：例如 `/services/IServiceUopBossToTvManager`

系统会自动拼接成完整的服务地址。

**如果遇到CORS跨域问题**：
- 勾选"使用代理服务器（解决CORS问题）"
- 配置代理服务器IP和端口（默认 `localhost:8888`）
- 启动代理服务器（见下方说明）

也可以点击"测试连接"按钮来验证服务器是否可访问。

### 3. 选择API方法

在左侧"API方法"列表中，点击要测试的API方法。

### 4. 填写参数

在右侧"参数设置"区域，根据选择的API方法填写相应的参数：
- 标有 `*` 的参数为必填项
- 数字类型参数会自动转换
- 时间格式：`yyyy-MM-dd HH:mm:ss`，例如：`2025-01-26 10:30:00`

### 5. 发送请求

点击"发送请求"按钮，系统会：
1. 构建SOAP请求
2. 发送到服务器
3. 显示请求和响应内容
4. 解析返回码并显示说明

### 6. 查看结果

在"响应结果"区域可以查看：
- 完整的SOAP响应XML
- 返回码及其说明
- 错误信息（如果有）

## 返回码说明

| 返回码 | 说明 |
|--------|------|
| 0 | 操作成功 |
| 1 | 用户不在线 |
| 3 | 用户在线 |
| -1 | 数据库连接失败 |
| -2 | 账户不存在或无效 |
| -3 | 不是期限用户(账户已报失或禁用状态) |
| -4 | 子系统编码错误 |
| -5 | 金额参数超出了数字表示范围（最大有效数值60000） |
| -6 | 账户已经存在 |
| -7 | 账户已经在线 |
| -8 | 账户未开通 |
| -9 | 未知错误 |
| -10 | 有欠费账单 |
| -11 | 期限用户，预存款余额不足一个月的固定费用 |
| -12 | 充值号码无效 |
| -13 | 账户或密码不能为空 |
| -14 | 计费策略组id无效 |
| -15 | 存在多条用户信息 |
| -16 | 参数不合法 |
| -17 | 强制下线失败 |
| -18 | 密码错误 |

## 注意事项

### CORS跨域问题

**"Failed to fetch"错误通常是CORS跨域问题导致的。**

当测试客户端（如运行在 `http://10.11.6.88:8000`）尝试访问RADIUS服务器（如 `http://10.11.6.92:8090`）时，浏览器会阻止跨域请求。

#### 解决方案（按推荐顺序）：

1. **使用浏览器CORS扩展（最简单，仅测试环境）**
   - Chrome: 安装 "CORS Unblock" 或 "Allow CORS: Access-Control-Allow-Origin"
   - Edge: 安装类似的CORS扩展
   - 启用扩展后刷新页面重试

2. **配置服务器CORS响应头（生产环境推荐）**
   
   在Apache CXF服务端添加CORS过滤器，允许跨域访问：
   ```java
   // 在web.xml或配置类中添加CORS过滤器
   // 允许所有来源（生产环境应限制特定域名）
   Access-Control-Allow-Origin: *
   Access-Control-Allow-Methods: POST, GET, OPTIONS
   Access-Control-Allow-Headers: Content-Type, SOAPAction
   ```

3. **使用代理服务器（推荐用于测试）**
   - 在 `test_client` 目录下运行代理服务器：
     ```bash
     # Python版本
     python proxy_server.py
     
     # 或Node.js版本
     node proxy_server.js
     ```
   - 在测试客户端中勾选"使用代理服务器"选项
   - 代理服务器会自动转发请求到RADIUS服务器
   - 详细说明请查看 `使用代理服务器.md`

4. **在同一服务器运行**
   - 将测试客户端部署到与RADIUS服务器相同的域名和端口
   - 这样就不会有跨域问题

#### 如何判断是CORS问题？

- 错误信息包含 "Failed to fetch"
- 浏览器控制台显示 CORS 相关错误
- 点击"测试连接"按钮，如果显示连接失败，很可能是CORS问题

### 常见问题

1. **连接失败**
   - 检查服务地址是否正确
   - 确认RADIUS服务器正在运行
   - 检查防火墙设置

2. **返回码-9（未知错误）**
   - 查看服务器日志获取详细错误信息
   - 检查参数格式是否正确

3. **MAC地址格式**
   - IPoE相关接口要求MAC地址为12位十六进制字符
   - 例如：`001122334455` 或 `AABBCCDDEEFF`

## 技术实现

- **运行环境**：浏览器（Chrome/Firefox/Safari/Edge）
- **前端技术**：纯HTML + CSS + JavaScript（无任何框架依赖）
- **通信协议**：SOAP/HTTP
- **请求方式**：使用浏览器原生Fetch API发送POST请求
- **XML解析**：使用浏览器原生DOMParser API
- **文件服务**：需要本地HTTP服务器（Python/Node.js/PHP等）来托管HTML文件，避免CORS问题

> **说明**：这不是Node.js应用，JavaScript代码在浏览器中运行。本地HTTP服务器仅用于提供静态文件服务。

## 文件结构

```
test_client/
├── index.html              # 测试客户端主文件
├── README.md              # 本说明文档
├── proxy_server.py        # Python代理服务器（解决CORS问题）
├── proxy_server.js        # Node.js代理服务器（解决CORS问题）
└── 使用代理服务器.md      # 代理服务器使用说明
```

## 浏览器兼容性

- Chrome/Edge (推荐)
- Firefox
- Safari
- 不支持IE浏览器

## 更新日志

### v1.1.0 (2025-01-26)
- 改进服务地址配置：支持独立输入IP、端口、路径
- 添加连接测试功能
- 增强错误处理：提供详细的CORS错误诊断信息
- 优化用户体验

### v1.0.0 (2025-01-26)
- 初始版本
- 支持所有19个API方法
- 图形化界面
- SOAP请求/响应显示
- 返回码自动解析和说明

## 许可证

本测试客户端仅供测试使用。

## 联系支持

如有问题或建议，请联系开发团队。