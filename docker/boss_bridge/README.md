# BOSS Bridge

桥接服务：对外暴露与 BOSS SOAP 接口一致的 11 个方法，供 `boss_api/index.html` 调用；内部将请求转发到 radiusApi。

## 配置

| 环境变量 | 说明 | 默认值 |
|----------|------|--------|
| RADIUS_API_URL | radiusApi 基础地址 | http://localhost:8088 |
| ADMIN_USERNAME | 管理员账号（用于获取 token） | admin |
| ADMIN_PASSWORD | 管理员密码 | admin |
| PORT | 桥接服务监听端口 | 8090 |

可通过 `.env` 或环境变量覆盖。

## 运行

```bash
npm install
npm start
```

SOAP 端点：`http://localhost:8090/services/IServiceUopBossToTvManager`

在 `boss_api/index.html` 中配置：服务器 IP 为桥接服务地址，端口 8090，路径 `/services/IServiceUopBossToTvManager`。

**注意**：桥接服务需能访问 radiusApi（默认 http://localhost:8088），且需配置有效的管理员账号以获取 token。若 radiusApi 未启动或鉴权失败，接口会返回 BOSS 错误码 -9。

## 方法映射

- AreaGroupID → realm
- Account → UserName/username
- 11 个 BOSS 方法对应 radiusApi 的 AccountInfo / OnlineUser 等接口，详见计划文档。
