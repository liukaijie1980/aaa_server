#!/bin/bash

# Configuration
MYSQL_USER="root"
MYSQL_PASSWORD="123456"
MYSQL_HOST1="mysql01"
MYSQL_HOST2="mysql02"
SLEEP_INTERVAL=10

# Function to set read_only and event_scheduler on the given host
set_variables() {
    local host="$1"
    local read_only="$2"
    local event_scheduler="$3"

    mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" -h"$host" -e \
    "SET GLOBAL read_only=$read_only; SET GLOBAL event_scheduler=$event_scheduler;"
}

# Main loop
while true; do
    # Find the master host
    master_host=$(maxctrl list servers | grep 'Master' | grep -v 'Slave' | awk '{print $4}')

    # Set variables on the master host
    set_variables "$master_host" 0 1

    # Find all slave hosts
    slave_hosts=$(maxctrl list servers | grep 'Slave' | awk '{print $4}')

    # Set variables on all slave hosts
    for slave_host in $slave_hosts; do
        set_variables "$slave_host" 1 0
    done

    sleep "$SLEEP_INTERVAL"
done

