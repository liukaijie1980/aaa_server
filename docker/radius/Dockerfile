ARG from=centos:centos7
FROM ${from} as build
RUN rm -f /etc/localtime \
&& ln -sv /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
&& echo "Asia/Shanghai" > /etc/timezone

COPY ./radius/  /home/radius/radius/
COPY ./mysql80-community-release-el7-1.noarch.rpm  /root
COPY ./docker-entrypoint.sh /
RUN rpm -ivh /root/mysql80-community-release-el7-1.noarch.rpm 
RUN rpm --import https://repo.mysql.com/RPM-GPG-KEY-mysql-2022

RUN yum install mysql-community-{client,common,libs}-* -y
RUN yum groupinstall -y "Development Tools"
RUN yum install  openssl openssl-devel libtalloc-devel libtalloc*  -y
RUN yum install openldap-devel python-devel  -y

RUN yum install net-tools -y
ENTRYPOINT ["/docker-entrypoint.sh"]
CMD tail -f /dev/null
