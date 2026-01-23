#!/bin/bash

# 脚本名称: manage_mysql_access.sh
# 使用方法: ./access_manager.sh eth0 block   # 阻止外部访问
#           ./access_manager.sh eth0 unblock # 恢复外部访问

# 检查参数数量
if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <interface> <block|unblock>"
    exit 1
fi

INTERFACE=$1
ACTION=$2

# 根据操作来设置iptables规则
case $ACTION in
    block)
        sudo iptables -I DOCKER-USER -i $INTERFACE -p tcp --dport 3306 -j DROP
        sudo iptables -I DOCKER-USER -i $INTERFACE -p tcp --dport 33061 -j DROP
        sudo iptables -I DOCKER-USER -i $INTERFACE -p tcp --dport 33062 -j DROP
        sudo iptables -I DOCKER-USER -i $INTERFACE -p tcp --dport 2375 -j DROP
        ;;
    
    unblock)
        sudo iptables -D DOCKER-USER -i $INTERFACE -p tcp --dport 3306 -j DROP
        sudo iptables -D DOCKER-USER -i $INTERFACE -p tcp --dport 33061 -j DROP
        sudo iptables -D DOCKER-USER -i $INTERFACE -p tcp --dport 33062 -j DROP
        sudo iptables -D DOCKER-USER -i $INTERFACE -p tcp --dport 2375 -j DROP
        ;;
    
    *)
        echo "Invalid action. Use 'block' or 'unblock'."
        exit 1
        ;;
esac

exit 0

