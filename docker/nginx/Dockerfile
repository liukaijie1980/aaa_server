ARG from=centos:centos7
FROM ${from} as build
RUN rm -f /etc/localtime \
&& ln -sv /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
&& echo "Asia/Shanghai" > /etc/timezone

RUN yum install -y epel-release
RUN yum -y update
RUN yum -y install nginx && yum -y install nc

COPY ./docker-entrypoint.sh /
COPY ./nginx.conf /etc/nginx/
ENTRYPOINT ["/docker-entrypoint.sh"]
CMD tail -f /dev/null


