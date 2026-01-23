ARG from=centos:centos7
FROM ${from} as build
RUN rm -f /etc/localtime \
&& ln -sv /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
&& echo "Asia/Shanghai" > /etc/timezone

# 配置 Adoptium (Eclipse Temurin) RPM 仓库并安装 JDK 19
RUN echo -e "[Adoptium]\nname=Adoptium\nbaseurl=https://packages.adoptium.net/artifactory/rpm/centos/7/\$basearch\nenabled=1\ngpgcheck=1\ngpgkey=https://packages.adoptium.net/artifactory/api/gpg/key/public" > /etc/yum.repos.d/adoptium.repo && \
    yum install -y temurin-19-jdk && \
    yum clean all

COPY ./docker-entrypoint.sh /
ENTRYPOINT ["/docker-entrypoint.sh"]
CMD tail -f /dev/null

