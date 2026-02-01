ARG from=mariadb/maxscale:22.08 
FROM ${from} as build
RUN rm -f /etc/localtime \
&& ln -sv /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
&& echo "Asia/Shanghai" > /etc/timezone

COPY ./setsql.sh  /
COPY ./run.sh /
COPY ./docker-entrypoint.sh  /usr/bin/
COPY ./wait_for_replication.sh /
RUN dnf -y install mariadb && chmod +x /wait_for_replication.sh
COPY ./mariadb.repo /etc/yum.repos.d/
# Swarm 双主场景：ENTRYPOINT 等待 /shared/replication_ready 后再启动 MaxScale
ENTRYPOINT ["/wait_for_replication.sh"]
CMD ["/usr/bin/docker-entrypoint.sh"]
