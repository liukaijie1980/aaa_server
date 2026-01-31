-- 数据库初始化：创建 radius 库及 service_user 用户
-- 仅在首次启动且数据目录为空时由 /docker-entrypoint-initdb.d 执行

CREATE DATABASE IF NOT EXISTS radius CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'service_user'@'%' IDENTIFIED BY '123456';
GRANT ALL PRIVILEGES ON radius.* TO 'service_user'@'%';

-- 双主复制用户（栈内 setup_dual_master_replication 会配置复制）
CREATE USER IF NOT EXISTS 'repl'@'%' IDENTIFIED BY '123456';
GRANT REPLICATION SLAVE ON *.* TO 'repl'@'%';

-- MaxScale 监控用户（maxscale.cnf 中 monitor 使用）
CREATE USER IF NOT EXISTS 'monitor_user'@'%' IDENTIFIED BY '123456';
GRANT REPLICATION CLIENT, FILE, SUPER, RELOAD, PROCESS, SHOW DATABASES, EVENT ON *.* TO 'monitor_user'@'%';

FLUSH PRIVILEGES;
