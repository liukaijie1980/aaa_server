#!/bin/bash
# 栈内双主 MariaDB 自动配置脚本
# 在 mysql01、mysql02 就绪后配置双主复制，完成后写 /shared/replication_ready，供 MaxScale 等待
set -e

MYSQL_USER="root"
MYSQL_PASS="123456"
MYSQL01="mysql01"
MYSQL02="mysql02"
PORT="3306"
REPL_USER="repl"
REPL_PASS="123456"
MAX_WAIT=300
SLEEP=5

wait_for_mysql() {
    local host=$1
    local elapsed=0
    echo "[$(date +%H:%M:%S)] 等待 ${host}:${PORT} 可连接..."
    while [ $elapsed -lt $MAX_WAIT ]; do
        if mysql -h"$host" -P"$PORT" -u"$MYSQL_USER" -p"$MYSQL_PASS" -e "SELECT 1" &>/dev/null; then
            echo "[$(date +%H:%M:%S)] ${host} 已就绪"
            return 0
        fi
        sleep $SLEEP
        elapsed=$((elapsed + SLEEP))
    done
    echo "等待 ${host} 超时" >&2
    return 1
}

run_sql() {
    local host=$1
    shift
    mysql -h"$host" -P"$PORT" -u"$MYSQL_USER" -p"$MYSQL_PASS" "$@"
}

echo "======== 双主复制自动配置开始 ========"

wait_for_mysql "$MYSQL01"
wait_for_mysql "$MYSQL02"

# 若已配置过（例如重启栈），检查复制状态，已就绪则直接挂起
if run_sql "$MYSQL01" -e "SHOW REPLICA STATUS\G" 2>/dev/null | grep -q "Slave_IO_Running: Yes" && \
   run_sql "$MYSQL02" -e "SHOW REPLICA STATUS\G" 2>/dev/null | grep -q "Slave_IO_Running: Yes"; then
    echo "[$(date +%H:%M:%S)] 复制已存在且正常，跳过配置"
    exec tail -f /dev/null
fi

# mysql01 指向 mysql02
echo "[$(date +%H:%M:%S)] 配置 mysql01 -> mysql02"
run_sql "$MYSQL01" -e "RESET MASTER;"
run_sql "$MYSQL01" -e "STOP REPLICA;" 2>/dev/null || true
run_sql "$MYSQL01" -e "RESET REPLICA ALL;" 2>/dev/null || true
run_sql "$MYSQL01" -e "
  CHANGE MASTER TO
    MASTER_HOST='$MYSQL02',
    MASTER_PORT=$PORT,
    MASTER_USER='$REPL_USER',
    MASTER_PASSWORD='$REPL_PASS',
    MASTER_USE_GTID=slave_pos;
"
run_sql "$MYSQL01" -e "START REPLICA;"

# mysql02 指向 mysql01
echo "[$(date +%H:%M:%S)] 配置 mysql02 -> mysql01"
run_sql "$MYSQL02" -e "RESET MASTER;"
run_sql "$MYSQL02" -e "STOP REPLICA;" 2>/dev/null || true
run_sql "$MYSQL02" -e "RESET REPLICA ALL;" 2>/dev/null || true
run_sql "$MYSQL02" -e "
  CHANGE MASTER TO
    MASTER_HOST='$MYSQL01',
    MASTER_PORT=$PORT,
    MASTER_USER='$REPL_USER',
    MASTER_PASSWORD='$REPL_PASS',
    MASTER_USE_GTID=slave_pos;
"
run_sql "$MYSQL02" -e "START REPLICA;"

# 双主：mysql02 取消只读（与 mariadb02/etc/my.cnf 中 read_only=1 互补，运行时覆盖）
echo "[$(date +%H:%M:%S)] 设置 mysql02 read_only=0"
run_sql "$MYSQL02" -e "SET GLOBAL read_only=0;"

# 启用 radius 库内事件（若存在）
echo "[$(date +%H:%M:%S)] 尝试启用事件调度"
run_sql "$MYSQL01" -e "SET GLOBAL event_scheduler=ON;" 2>/dev/null || true
run_sql "$MYSQL01" -e "CALL radius.EnableAllEvents;" 2>/dev/null || true

# 等待复制就绪
echo "[$(date +%H:%M:%S)] 等待复制 IO 就绪..."
for i in $(seq 1 30); do
    if run_sql "$MYSQL01" -e "SHOW REPLICA STATUS\G" 2>/dev/null | grep -q "Slave_IO_Running: Yes" && \
       run_sql "$MYSQL02" -e "SHOW REPLICA STATUS\G" 2>/dev/null | grep -q "Slave_IO_Running: Yes"; then
        echo "[$(date +%H:%M:%S)] 双主复制已就绪"
        break
    fi
    sleep 2
done

echo "[$(date +%H:%M:%S)] 双主复制配置完成，保持运行以便 MaxScale 依赖本服务"
exec tail -f /dev/null
