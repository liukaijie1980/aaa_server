#!/bin/sh
nohup java -jar /usr/app/radiusApi-0.0.1-SNAPSHOT.jar >/usr/app/run.log 2>&1 &
exec "$@"
