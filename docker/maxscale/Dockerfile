ARG from=mariadb/maxscale:22.08 
FROM ${from} as build
RUN rm -f /etc/localtime \
&& ln -sv /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
&& echo "Asia/Shanghai" > /etc/timezone

COPY ./mariadb.repo /etc/yum.repos.d/
COPY ./setsql.sh  /
COPY ./run.sh /
COPY ./docker-entrypoint.sh  /usr/bin/
RUN dnf -y install MariaDB-client
#ENTRYPOINT ["/docker-entrypoint.sh"]
#CMD tail -f /dev/null
