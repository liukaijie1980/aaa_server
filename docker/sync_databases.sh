#!/bin/bash
current_dir=$(dirname $(readlink -f "$0"))
user="root"
password="123456"
database="radius"



TARGET_IP1="swarm01" # 更改为您要检查的目标 IP 地址
TARGET_PORT1=33061 # 更改为您要检查的目标端口
TARGET_IP2="swarm02" # 更改为您要检查的目标 IP 地址
TARGET_PORT2=33062 # 更改为您要检查的目标端口


nc -z -w 1 $TARGET_IP1 $TARGET_PORT1 > /dev/null 2>&1 && nc -z -w 1 $TARGET_IP2 $TARGET_PORT2 > /dev/null 2>&1
if [ $? -eq 0 ]; then
     echo "Connection to $TARGET_IP1:$TARGET_PORT1 is successful."
     host='swarm01'
     port=33061
     echo $host
     echo $port
     mysql -h $host --port=$port  --user=$user --password=$password --database=$database --execute="reset master;"
     mysql -h $host --port=$port  --user=$user --password=$password --database=$database --execute="stop slave;"
     mysql -h $host --port=$port  --user=$user --password=$password --database=$database --execute="reset slave;"
     mysql -h $host --port=$port  --user=$user --password=$password --database=$database --execute="change master to
     master_host='mysql02',    # Master2的IP
     master_port=3306,          #默认端口
     master_user='repl',        # Master2创建的账户
     master_password='123456',      #密码
     master_use_gtid=slave_pos;"
     mysql -h $host --port=$port --user=$user --password=$password --database=$database --execute="start slave;"
     mysql -h $host --port=$port --user=$user --password=$password --database=$database --execute="show slave status\G;"

     host='swarm02'
     port=33062
     echo $host
     echo $port
     mysql -h $host --port=$port  --user=$user --password=$password --database=$database --execute="reset master;"
     mysql -h $host --port=$port  --user=$user --password=$password --database=$database --execute="stop slave;"
     mysql -h $host --port=$port  --user=$user --password=$password --database=$database --execute="reset slave;"
     mysql -h $host --port=$port  --user=$user --password=$password --database=$database --execute="change master to
     master_host='mysql01',    # Master1的IP
     master_port=3306,          #默认端口
     master_user='repl',        # Master1创建的账户
     master_password='123456',      #密码
     master_use_gtid=slave_pos;"
     mysql -h $host --port=$port --user=$user --password=$password --database=$database --execute="start slave;"
     mysql -h $host --port=$port --user=$user --password=$password --database=$database --execute="show slave status\G;"

     host='swarm01'
     port=33061
     echo $host
     echo $port
     mysql -h $host --port=$port --user=$user --password=$password --database=$database --execute="CALL EnableAllEvents();"
     
else
     echo "Connection to $TARGET_IP1:$TARGET_PORT1 failed." 
     rm -rf /home/mariadb02/data/*
     scp -r root@swarm01:/home/mariadb01/data/* /home/mariadb02/data/
fi
