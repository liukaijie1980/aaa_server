#!/bin/bash

TARGET_IP="backend" # 更改为您要检查的目标 IP 地址
TARGET_PORT=8088 # 更改为您要检查的目标端口
RETRY_INTERVAL=5 # 设置重试之间的时间间隔（秒）

while true; do
    nc -z -w 1 $TARGET_IP $TARGET_PORT > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo "Connection to $TARGET_IP:$TARGET_PORT is successful."
        break
    else
        echo "Connection to $TARGET_IP:$TARGET_PORT failed. Retrying in $RETRY_INTERVAL seconds..."
        sleep $RETRY_INTERVAL
    fi
done

# 在此处添加您要在连接成功后执行的其他命令
echo "Continuing with the rest of the script..."


/usr/sbin/nginx -g  'daemon off;'
exec "$@"
