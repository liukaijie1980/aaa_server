# USERST → account_info 迁移脚本

将旧系统 Oracle 库中的 `USERST` 表数据拷贝到新系统 MySQL 的 `account_info` 表。  
与 `docker/migrate_userst/` 下为同一套脚本，部署时通常使用 `docker/migrate_userst/`。  
**MySQL 目标库即 radiusApi 使用的同一数据库**，连接参数可与 radiusApi 的库配置一致（如 `radiusApi/src/main/resources/application.properties` 中的 `spring.datasource.url/username/password` 对应 host、database、user、password）。

## 依赖

- Python 3.7+
- 仅两个 pip 包：`oracledb`、`pymysql`

## 安装

```bash
# 确保已安装 Python 3.7+
python3 --version

# 安装依赖（建议使用虚拟环境）
pip3 install -r requirements.txt
# 或：python3 -m pip install -r requirements.txt
```

**注意**：`oracledb` 使用 thin 模式，无需安装 Oracle Instant Client，可直接在 Linux 上运行。

## 配置

1. 复制示例配置并填写实际连接信息：

   ```bash
   cp migrate.json.example migrate.json
   # 编辑 migrate.json，填写 Oracle、MySQL 的 host/port/user/password 等
   ```

2. 或使用环境变量（会覆盖 `migrate.json` 中的同名字段）：

   - Oracle：`ORACLE_HOST`、`ORACLE_PORT`、`ORACLE_SERVICE_NAME`、`ORACLE_USER`、`ORACLE_PASSWORD`
   - MySQL：`MYSQL_HOST`、`MYSQL_PORT`、`MYSQL_DATABASE`、`MYSQL_USER`、`MYSQL_PASSWORD`

3. Oracle 表名：若带 schema，在 `migrate.json` 的 `oracle.table` 中填写，例如 `"PORTAL.USERST"`，默认为 `"USERST"`。

## 运行

**方式一**：直接执行（推荐，脚本已包含 shebang）

```bash
# 添加执行权限（首次运行）
chmod +x migrate_userst_to_account_info.py

# 直接运行
./migrate_userst_to_account_info.py
```

**方式二**：通过 python3 运行

```bash
python3 migrate_userst_to_account_info.py
```

**指定配置文件路径**：

```bash
./migrate_userst_to_account_info.py --config /path/to/migrate.json
# 或
python3 migrate_userst_to_account_info.py --config /path/to/migrate.json
```

## 行为说明

- 按 `(user_name, realm)` 唯一键写入；若目标库中已存在相同 `(user_name, realm)`，则**更新**该行其余字段（不改 `id`、`user_name`、`realm`）。
- 列对应关系：`REVEAL_USERNAME`→user_name，`AGENT_CODE`→realm，`PASSWD`→user_password，`CREATE_DATE`→valid_date，`LIMIT_DATE`→expire_date，`INPUT_SPEED_LIMIT`→inbound_car，`OUTPUT_SPEED_LIMIT`→outbound_car；其余字段按约定默认或从 USERST 其它列映射（见计划文档）。

## 部署（Linux）

1. 将本目录整体拷贝到 Linux 目标机（如通过 `scp`、`rsync` 等）
2. 在目标机上安装 Python 3.7+ 和 pip（如 `yum install python3 python3-pip` 或 `apt-get install python3 python3-pip`）
3. 安装依赖：`pip3 install -r requirements.txt`
4. 配置 `migrate.json` 或设置环境变量
5. 运行脚本：`./migrate_userst_to_account_info.py` 或 `python3 migrate_userst_to_account_info.py`

**无需**：Java、Maven、Docker、Oracle Instant Client（oracledb thin 模式）。
