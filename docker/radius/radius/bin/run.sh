#!/bin/bash
pkill coa.sh
nohup /home/radius/radius/bin/coa.sh >/dev/null 2>&1 &
