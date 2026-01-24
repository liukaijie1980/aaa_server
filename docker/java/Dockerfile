# 方案1: 使用 Eclipse Temurin 官方镜像（已包含 JDK 19，推荐）
# 如果无法访问 Docker Hub，可以尝试使用国内镜像源或私有仓库
ARG from=eclipse-temurin:19-jdk-ubi9-minimal
FROM ${from} AS build
RUN rm -f /etc/localtime \
&& ln -sv /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
&& echo "Asia/Shanghai" > /etc/timezone

# Eclipse Temurin 镜像已包含 JDK 19，无需安装

COPY ./docker-entrypoint.sh /
ENTRYPOINT ["/docker-entrypoint.sh"]
CMD tail -f /dev/null
