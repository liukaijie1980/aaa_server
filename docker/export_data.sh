#!/bin/bash
# 脚本名称: export_data.sh
# 使用方法: ./export_data.sh   swarm01  3306  123456  #导出指定主机的 数据库  关键表到 backup_data.sql
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
rm backup_data.sql
mysqldump -h $host --port=$port  --user=$user --password=$password   --no-create-info  --skip-triggers  --default-character-set=utf8mb4  $database  nas  >>backup_data.sql
mysqldump -h $host --port=$port  --user=$user --password=$password   --no-create-info  --skip-triggers  --default-character-set=utf8mb4  $database  sidebar_tree  >>backup_data.sql
mysqldump -h $host --port=$port  --user=$user --password=$password   --no-create-info  --skip-triggers  --default-character-set=utf8mb4  $database  realm  >>backup_data.sql
mysqldump -h $host --port=$port  --user=$user --password=$password   --no-create-info  --skip-triggers  --default-character-set=utf8mb4  $database  admin  >>backup_data.sql
mysqldump -h $host --port=$port  --user=$user --password=$password   --no-create-info  --skip-triggers  --default-character-set=utf8mb4  $database  account_info  >>backup_data.sql
mysqldump -h $host --port=$port  --user=$user --password=$password   --no-create-info  --skip-triggers  --default-character-set=utf8mb4  $database  offline_user>>backup_data.sql

exit 0
