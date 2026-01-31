# Docker 脚本说明

## 双主复制自动配置（Swarm）

- **setup_dual_master_replication.sh**：栈内服务 `mysql_replication_init` 使用。
  - 等待 `mysql01`、`mysql02` 可连接后，配置双主复制（CHANGE MASTER / START REPLICA），设置 mysql02 `read_only=0`，然后保持运行。
  - 依赖 initdb 中已创建 `repl` 用户（见 `initdb/01_create_db_and_user.sql`）。
- 启动顺序由栈保证：`mysql01`、`mysql02` → `mysql_replication_init` → `mysql`（MaxScale）→ backend/api 等。
- MaxScale 通过 `wait_for_replication.sh` 轮询两台机的复制状态，双主就绪后再启动，从而保证依赖数据库的应用在双主就绪之后才连上数据库。
