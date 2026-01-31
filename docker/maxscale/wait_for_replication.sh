#!/bin/bash
# 等待双主复制就绪后再启动 MaxScale（栈内与 mysql_replication_init 配合）
# 通过轮询 mysql01/mysql02 的 SHOW REPLICA STATUS，不依赖共享卷
# 若未设置 MYSQL01_HOST 则跳过等待，兼容单机/无复制场景
set -e

MYSQL01_HOST="${MYSQL01_HOST:-}"
MYSQL02_HOST="${MYSQL02_HOST:-}"
MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-123456}"
MAX_WAIT=${MAXSCALE_WAIT_REPLICATION:-300}
SLEEP=2
elapsed=0

if [ -z "$MYSQL01_HOST" ] || [ -z "$MYSQL02_HOST" ]; then
    echo "[wait_for_replication] 未设置 MYSQL01_HOST/MYSQL02_HOST，跳过等待，直接启动"
    exec "$@"
fi

run_sql() {
    mysql -h"$1" -P3306 -uroot -p"$MYSQL_ROOT_PASSWORD" -e "SHOW REPLICA STATUS\G" 2>/dev/null | grep -q "Slave_IO_Running: Yes" || return 1
}

echo "[wait_for_replication] 等待双主复制就绪 (${MYSQL01_HOST}, ${MYSQL02_HOST})，最多 ${MAX_WAIT}s..."
while [ $elapsed -lt $MAX_WAIT ]; do
    if run_sql "$MYSQL01_HOST" && run_sql "$MYSQL02_HOST"; then
        echo "[wait_for_replication] 双主复制已就绪，启动 MaxScale"
        exec "$@"
    fi
    sleep $SLEEP
    elapsed=$((elapsed + SLEEP))
done

echo "[wait_for_replication] 超时未检测到复制就绪，继续启动（可能为单机或手动配置）"
exec "$@"
