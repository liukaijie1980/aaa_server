#!/bin/sh
/home/radius/radius/sbin/radiusd

while [ $? -ne 0 ] 
do
sleep 1
/home/radius/radius/sbin/radiusd
done

/home/radius/radius/bin/run.sh
exec "$@"
