#!/bin/bash

function exitMaxScale {
    /usr/bin/monit unmonitor all
    /usr/bin/maxscale-stop
    /usr/bin/monit quit
}

rm -f /var/run/*.pid
rsyslogd

trap exitMaxScale SIGTERM
/run.sh
exec "$@" &

wait
