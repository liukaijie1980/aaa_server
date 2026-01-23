#! /bin/bash

yum -y install ntp
#设置开机自启动
systemctl enable ntpd
#手动启动ntpd进程
systemctl start ntpd
#设置时区
timedatectl set-timezone Asia/Shanghai
#对时
# ntpdate -u time.nist.gov
