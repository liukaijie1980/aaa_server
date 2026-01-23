#!/bin/bash
# 脚本名称: update_database.sh 
# 使用方法: ./update_database.sh   swarm01  3306  123456  #用struct.sql update radius数据库
# 使用前请务必确保安装了和所使用数据库匹配的客户端（mysql和mariadb的客户端并不完全兼容）
# 检查参数数量
if [ "$#" -ne 3 ]; then
    echo "Usage: $0  <host> <port> <password>"
    exit 1
fi
host=$1
port=$2
password=$3
database='radius'
user='root'
#先备份数据
./export_data.sh  $host $port $password

#更新表结构
mysql -h $host --port=$port  --user=$user --password=$password   $database < struct.sql

#重新导入备份的数据
mysql -h $host --port=$port  --user=$user --password=$password   $database < backup_data.sql
