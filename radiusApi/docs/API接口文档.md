# radiusApi 后端 API 接口文档

## 一、通用说明

### 1.1 基础信息

| 项目 | 说明 |
|------|------|
| 服务端口 | 8088 |
| 基础路径 | `http://{host}:8088` |
| 内容类型 | application/json |
| 字符编码 | UTF-8 |
| 时区/日期格式 | Asia/Shanghai，`yyyy-MM-dd HH:mm:ss` |

### 1.2 认证与鉴权

除以下接口外，**所有接口均需在请求头中携带 Token**：

- `POST /admin/login` — 登录
- `POST /admin/logout` — 登出（Token 过期时仍可调用）

**请求头：**

| 名称 | 说明 |
|------|------|
| x-token | 登录后返回的 JWT，必填（需认证的接口） |
| Content-Type | application/json（有 Body 的接口） |

**Token 校验失败时：**

- HTTP 状态码：500
- Body 示例：`{"msg":"token verify fail","code":"50000"}`

### 1.3 统一响应结构

```json
{
  "success": true,
  "code": 20000,
  "message": "sucess",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| success | boolean | 是否成功 |
| code | integer | 20000=成功，20001=失败 |
| message | string | 提示信息 |
| data | object | 业务数据，key 由各接口约定 |

---

## 二、管理员 (Admin)

**基础路径：** `/admin`

### 2.1 登录

| 项目 | 说明 |
|------|------|
| 方法 | POST |
| 路径 | /admin/login |
| 鉴权 | 否 |

**请求体 (JSON)：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 用户名 |
| password | string | 是 | 密码 |

**成功响应 data：**

| 字段 | 类型 | 说明 |
|------|------|------|
| token | string | JWT，后续请求放在 x-token 中 |

**失败：** `success=false`，`message` 可能为「账号或密码不正确」。

---

### 2.2 获取当前管理员信息

| 项目 | 说明 |
|------|------|
| 方法 | GET |
| 路径 | /admin/info |
| 鉴权 | 是 |

**Query 参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| token | string | 是 | 同 x-token，可从 Query 传 |

**成功响应 data：**

| 字段 | 类型 | 说明 |
|------|------|------|
| name | string | 用户名 |
| roles | string | 角色 |
| nodeid | string | 侧边栏树节点 ID |
| introduction | string | 固定 "superUser" |
| avatar | string | 头像 URL，可为空 |

---

### 2.3 登出

| 项目 | 说明 |
|------|------|
| 方法 | POST |
| 路径 | /admin/logout |
| 鉴权 | 否（建议仍带 x-token） |

**请求头：** `x-token`（可选）

后端仅校验 Token，无状态变更。成功时返回统一成功结构。

---

### 2.4 分页查询管理员列表

| 项目 | 说明 |
|------|------|
| 方法 | GET |
| 路径 | /admin/administrator |
| 鉴权 | 是 |

**Query 参数：**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| role | string | 是 | - | 角色，传 "admin" 表示不按角色过滤 |
| nodeid | string | 是 | - | 侧边栏节点 ID |
| name | string | 是 | - | 按姓名精确过滤，空字符串表示不过滤 |
| pageNo | integer | 否 | 1 | 页码 |
| pageSize | integer | 否 | 3 | 每页条数 |

**成功响应 data：**

| 字段 | 类型 | 说明 |
|------|------|------|
| data | IPage\<Admin\> | 分页结果，结构见下方 Admin 与分页说明 |

**Admin 字段摘要：** name, password, nodeid, role, phone, email

**分页对象常见字段：** records（当前页列表）、total（总记录数）、size、current、pages 等。

---

### 2.5 新增管理员

| 项目 | 说明 |
|------|------|
| 方法 | POST |
| 路径 | /admin/administrator |
| 鉴权 | 是 |

**请求体 (JSON)：** Admin 对象

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 是 | 用户名（主键） |
| password | string | 是 | 密码 |
| nodeid | string | 否 | 侧边栏节点 ID |
| role | string | 否 | 角色 |
| phone | string | 否 | 电话 |
| email | string | 否 | 邮箱 |

---

### 2.6 更新管理员

| 项目 | 说明 |
|------|------|
| 方法 | PUT |
| 路径 | /admin/administrator |
| 鉴权 | 是 |

**说明：** 以 `name` 为条件更新，**不可通过本接口修改 name**。

**请求体 (JSON)：** Admin 对象（需包含要更新的 name，其余为要更新的字段）。

---

### 2.7 删除管理员

| 项目 | 说明 |
|------|------|
| 方法 | DELETE |
| 路径 | /admin/administrator |
| 鉴权 | 是 |

**Query 参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 是 | 要删除的管理员用户名 |

---

## 三、账户信息 (AccountInfo)

**基础路径：** 无前缀，直接 `/AccountInfo`（大小写一致）

### 3.1 分页查询账户

| 项目 | 说明 |
|------|------|
| 方法 | GET |
| 路径 | /AccountInfo |
| 鉴权 | 是 |

**Query 参数：**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| name | string | 是 | - | 用户名，空表示不过滤 |
| realm | string | 是 | - | 域 |
| pageNo | integer | 否 | 1 | 页码 |
| pageSize | integer | 否 | 3 | 每页条数 |

**成功响应 data：** `data` 为分页对象，records 中为 AccountInfo。

**AccountInfo 主要字段：** id, UserName, realm, UserPassword, AuthMode, IsFrozen, AdminName, ValidDate, ModifyDate, ExpireDate, SecondRemain, ByteRemain, SimualUseLimit, MaxSessionTimeout, InboundCar, OutboundCar, QosProfile, UpdateInterval 等。

---

### 3.2 新增账户

| 项目 | 说明 |
|------|------|
| 方法 | POST |
| 路径 | /AccountInfo |
| 鉴权 | 是 |

**请求体 (JSON)：** AccountInfo 对象，字段含义同上。

---

### 3.3 更新账户

| 项目 | 说明 |
|------|------|
| 方法 | PUT |
| 路径 | /AccountInfo |
| 鉴权 | 是 |

**说明：** 以 `user_name`、`realm` 为条件更新，**不可通过本接口修改 user_name、realm**。

**请求体 (JSON)：** AccountInfo 对象，需包含 user_name、realm 及要更新的字段。

---

### 3.4 删除账户

| 项目 | 说明 |
|------|------|
| 方法 | DELETE |
| 路径 | /AccountInfo |
| 鉴权 | 是 |

**Query 参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| UserName | string | 是 | 用户名 |
| realm | string | 是 | 域 |

---

## 四、在线用户 (OnlineUser)

### 4.1 分页查询在线用户

| 项目 | 说明 |
|------|------|
| 方法 | GET |
| 路径 | /OnlineUser |
| 鉴权 | 是 |

**Query 参数：**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| name | string | 是 | - | 用户名，空表示不过滤 |
| realm | string | 是 | - | 域 |
| pageNo | integer | 否 | 1 | 页码 |
| pageSize | integer | 否 | 3 | 每页条数 |

**成功响应 data：** `data` 为分页对象，records 中为 OnlineUser（acctstoptime 为 null 的记录）。

**OnlineUser 主要字段：** radacctid, acctsessionid, username, realm, nasidentifier, nasipaddress, acctstarttime, acctstoptime, acctsessiontime, acctinputoctets, acctoutputoctets, callingstationid, framedipaddress 等。

---

### 4.2 更新在线用户

| 项目 | 说明 |
|------|------|
| 方法 | PUT |
| 路径 | /OnlineUser |
| 鉴权 | 是 |

**说明：** 以 `username`、`acctsessionid`、`realm` 为条件更新，**不可通过本接口修改 username、realm**。

**请求体 (JSON)：** OnlineUser 对象，需包含 username、acctsessionid、realm 及要更新的字段。

---

## 五、离线用户 (OfflineUser)

### 5.1 分页查询离线用户

| 项目 | 说明 |
|------|------|
| 方法 | GET |
| 路径 | /OfflineUser |
| 鉴权 | 是 |

**Query 参数：**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| name | string | 是 | - | 用户名，空表示不过滤 |
| realm | string | 是 | - | 域 |
| framedipaddress | string | 是 | - | 分配 IP，空表示不过滤 |
| callingstationid | string | 是 | - | 认证站 ID（如 MAC），空表示不过滤 |
| from | string | 是 | - | 开始时间，ISO-8601 带时区，如 `2024-01-01T00:00:00+08:00`；与 to 同为空则不过滤时间 |
| to | string | 是 | - | 结束时间，同上 |
| pageNo | integer | 否 | 1 | 页码 |
| pageSize | integer | 否 | 10 | 每页条数 |

**时间过滤逻辑：** 会话满足以下任一条件即命中：  
`acctstarttime ∈ [from, to]` 或 `acctstoptime ∈ [from, to]` 或 `[acctstarttime, acctstoptime] ⊇ [from, to]`。

**成功响应 data：** `data` 为分页对象，records 中为 OfflineUser，字段与 OnlineUser 类似。

---

## 六、认证日志 (Radpostauth)

### 6.1 分页查询认证日志

| 项目 | 说明 |
|------|------|
| 方法 | GET |
| 路径 | /radpostauth |
| 鉴权 | 是 |

**Query 参数：**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| name | string | 是 | - | 用户名，空表示不过滤 |
| realm | string | 是 | - | 域 |
| reply | string | 是 | - | 认证结果，空表示不过滤 |
| callingstationid | string | 是 | - | 认证站 ID，空表示不过滤 |
| nasidentifier | string | 是 | - | NAS 标识，空表示不过滤 |
| from | string | 是 | - | 认证时间起点，ISO-8601；与 to 同为空则不过滤 |
| to | string | 是 | - | 认证时间终点，同上 |
| pageNo | integer | 否 | 1 | 页码 |
| pageSize | integer | 否 | 10 | 每页条数 |

**成功响应 data：** `data` 为分页对象，records 中为 Radpostauth。

**Radpostauth 主要字段：** id, username, realm, pass, reply, callingstationid, nasidentifier, authdate, _class。

---

## 七、NAS 网络接入服务器

**基础路径：** `/nas`

### 7.1 获取 NAS 列表

| 项目 | 说明 |
|------|------|
| 方法 | GET |
| 路径 | /nas |
| 鉴权 | 是 |

无请求参数。**成功响应 data：** `data` 为 Nas 数组。

**Nas 主要字段：** id, nasname, shortname, type, ports, secret, coa, reversal, server, community, description。

---

### 7.2 新增 NAS

| 项目 | 说明 |
|------|------|
| 方法 | POST |
| 路径 | /nas |
| 鉴权 | 是 |

**请求体 (JSON)：** Nas 对象。

---

### 7.3 更新 NAS

| 项目 | 说明 |
|------|------|
| 方法 | PUT |
| 路径 | /nas |
| 鉴权 | 是 |

**说明：** 以 `id` 为条件更新。

**请求体 (JSON)：** Nas 对象，需包含 id 及要更新的字段。

---

### 7.4 删除 NAS

| 项目 | 说明 |
|------|------|
| 方法 | DELETE |
| 路径 | /nas |
| 鉴权 | 是 |

**Query 参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | integer | 是 | NAS 主键 |

---

## 八、域 (Realm)

**基础路径：** `/realm`

### 8.1 按节点查询 Realm 列表

| 项目 | 说明 |
|------|------|
| 方法 | GET |
| 路径 | /realm |
| 鉴权 | 是 |

**Query 参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| node_id | string | 是 | 侧边栏树节点 ID |

**成功响应 data：** `data` 为 Realm 数组。**Realm 字段：** id, node_id, realm。

---

### 8.2 新增 Realm

| 项目 | 说明 |
|------|------|
| 方法 | POST |
| 路径 | /realm |
| 鉴权 | 是 |

**请求体 (JSON)：** Realm 对象（id 可为空，由后端生成 UUID）。

---

### 8.3 更新 Realm

| 项目 | 说明 |
|------|------|
| 方法 | PUT |
| 路径 | /realm |
| 鉴权 | 是 |

**说明：** 以 `realm` 为条件更新。

**请求体 (JSON)：** Realm 对象，需包含 realm 及要更新的字段。

---

### 8.4 删除 Realm

| 项目 | 说明 |
|------|------|
| 方法 | DELETE |
| 路径 | /realm |
| 鉴权 | 是 |

**Query 参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| realm | string | 是 | 域名 |

---

## 九、侧边栏树 (SidebarTree)

### 9.1 获取子树节点

| 项目 | 说明 |
|------|------|
| 方法 | GET |
| 路径 | /SidebarTree |
| 鉴权 | 是 |

**Query 参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| nodeid | string | 是 | 节点 ID，返回该节点及其所有子节点 |

**成功响应 data：** `data` 为 SidebarTree 数组（包含当前节点及其递归子节点）。

**SidebarTree 字段：** id, label, type, owner, pid。

---

### 9.2 批量设置侧边栏树

| 项目 | 说明 |
|------|------|
| 方法 | POST |
| 路径 | /SidebarTree |
| 鉴权 | 是 |

**说明：** 根据前端传入的树结构，对根节点对应的整棵子树做“增改删”同步，事务执行。

**请求体 (JSON)：** SidebarTree 对象数组。每个节点需包含 id, label, type, owner, pid。

**逻辑概要：** 与现有一棵子树对比，新增/更新传入的节点，删除该子树中未在请求中出现的节点。

---

### 9.3 按用户名查节点 ID 列表

| 项目 | 说明 |
|------|------|
| 方法 | GET |
| 路径 | /getNodeIdsByAcccount |
| 鉴权 | 是 |

**Query 参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 用户名，支持模糊（内部会加 %） |

**成功响应 data：** `data` 为 string 数组，表示该用户关联的 nodeId 列表。

---

## 十、节点统计 (NodeStatistic)

### 10.1 按节点获取统计

| 项目 | 说明 |
|------|------|
| 方法 | GET |
| 路径 | /NodeStatistic |
| 鉴权 | 是 |

**说明：** 会先执行统计存储过程再查询。

**Query 参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| node_id | string | 是 | 侧边栏节点 ID |

**成功响应 data：** `data` 为 NodeStatistic 列表（通常按 node_id 一条或少量条）。

**NodeStatistic 字段：** nodeId, totalAccounts, onlineAccounts, onlineTerminals。

---

## 十一、数据库信息 (DbInformation)

### 11.1 分页查询数据库信息

| 项目 | 说明 |
|------|------|
| 方法 | GET |
| 路径 | /DbInformation |
| 鉴权 | 是 |

**Query 参数：**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNo | integer | 否 | 1 | 页码 |
| pageSize | integer | 否 | 3 | 每页条数 |

**成功响应 data：** `data` 为分页对象，records 中为 DbInformation。

**DbInformation 字段：** filename, filesize。

---

## 十二、Docker

**基础路径：** `/docker`

### 12.1 重启 Radius 服务

| 项目 | 说明 |
|------|------|
| 方法 | POST |
| 路径 | /docker/restart |
| 鉴权 | 是 |

无请求参数。调用 Docker Swarm 接口重启 `myradius_radius` 服务。

**成功：** 统一成功结构。**失败：** `success=false`，`data.data` 中为异常信息字符串。

---

## 十三、Tcpdump / 系统命令

### 13.1 执行本地命令（示例）

| 项目 | 说明 |
|------|------|
| 方法 | GET |
| 路径 | /RunCommand |
| 鉴权 | 是 |

当前实现写死为执行 `cmd.exe /c dir d:\`，超时 10 秒。**成功响应 data：** `data` 为命令标准输出字符串。

---

### 13.2 通过 SSH 执行 tcpdump

| 项目 | 说明 |
|------|------|
| 方法 | POST |
| 路径 | /tcpdump |
| 鉴权 | 是 |

**Query 参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| user | string | 是 | SSH 用户名 |
| password | string | 是 | SSH 密码 |
| host | string | 是 | SSH 主机 |
| port | integer | 是 | SSH 端口 |

**说明：** 接口为 void，无 JSON 响应体，用于在远程主机执行 tcpdump 等操作。

---

## 十四、Actuator / 版本信息

### 14.1 获取 Git/Actuator 信息

| 项目 | 说明 |
|------|------|
| 方法 | GET |
| 路径 | /actuator-info |
| 鉴权 | 是 |

无请求参数。**成功响应 data：** `data` 为 Spring Boot `GitProperties` 对象（如 commitId、分支、时间等）。

---

## 十五、接口速查表

| 模块 | 方法 | 路径 | 鉴权 |
|------|------|------|------|
| Admin | POST | /admin/login | 否 |
| Admin | GET | /admin/info | 是 |
| Admin | POST | /admin/logout | 否 |
| Admin | GET | /admin/administrator | 是 |
| Admin | POST | /admin/administrator | 是 |
| Admin | PUT | /admin/administrator | 是 |
| Admin | DELETE | /admin/administrator | 是 |
| AccountInfo | GET | /AccountInfo | 是 |
| AccountInfo | POST | /AccountInfo | 是 |
| AccountInfo | PUT | /AccountInfo | 是 |
| AccountInfo | DELETE | /AccountInfo | 是 |
| OnlineUser | GET | /OnlineUser | 是 |
| OnlineUser | PUT | /OnlineUser | 是 |
| OfflineUser | GET | /OfflineUser | 是 |
| Radpostauth | GET | /radpostauth | 是 |
| Nas | GET | /nas | 是 |
| Nas | POST | /nas | 是 |
| Nas | PUT | /nas | 是 |
| Nas | DELETE | /nas | 是 |
| Realm | GET | /realm | 是 |
| Realm | POST | /realm | 是 |
| Realm | PUT | /realm | 是 |
| Realm | DELETE | /realm | 是 |
| SidebarTree | GET | /SidebarTree | 是 |
| SidebarTree | POST | /SidebarTree | 是 |
| SidebarTree | GET | /getNodeIdsByAcccount | 是 |
| NodeStatistic | GET | /NodeStatistic | 是 |
| DbInformation | GET | /DbInformation | 是 |
| Docker | POST | /docker/restart | 是 |
| Tcpdump | GET | /RunCommand | 是 |
| Tcpdump | POST | /tcpdump | 是 |
| Actuator | GET | /actuator-info | 是 |

---

## 十六、Swagger / OpenAPI

项目集成 SpringDoc，可访问：

- Swagger UI：`http://{host}:8088/swagger-ui.html`
- OpenAPI JSON：`http://{host}:8088/api-docs`

路径可在 `application.properties` 中通过 `springdoc.swagger-ui.path`、`springdoc.api-docs.path` 修改。
