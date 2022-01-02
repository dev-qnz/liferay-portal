#!/usr/bin/env bash
set -e -o pipefail

docker build -t liferay-portal-build - <<EOF
FROM debian:stretch
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update
RUN apt-get install -y curl git

RUN apt-get install -y openjdk-8-jdk
RUN java -version

RUN apt-get -y install ant
RUN ant -version
ENV ANT_OPTS="-Xmx2560m"

RUN apt-get -y install gradle
RUN gradle --version

RUN su - -c "groupadd -o -g $(id -g) liferay-portal-build"
RUN su - -c "useradd -o -u $(id -u) -g $(id -g) -G liferay-portal-build -s /bin/bash liferay-portal-build"
RUN mkdir /home/liferay-portal-build && chown liferay-portal-build:liferay-portal-build /home/liferay-portal-build
USER liferay-portal-build:liferay-portal-build
WORKDIR /home/liferay-portal-build

RUN curl -L https://raw.githubusercontent.com/liferay/liferay-blade-cli/master/cli/installers/local | sh
ENV PATH=${PATH}:/home/liferay-portal-build/jpm/bin
RUN blade version

RUN git clone https://github.com/liferay/liferay-binaries-cache-2017 --branch master --single-branch --depth 1

RUN echo cd liferay-portal >>build-liferay-portal.sh
RUN echo ant compile install-portal-snapshots >>build-liferay-portal.sh
RUN echo ant snapshot-bundle >>build-liferay-portal.sh
#RUN echo git checkout \$\(cat ../bundles/.githash\) -b $(git rev-parse --abbrev-ref HEAD) >>build-liferay-portal.sh
RUN echo ant compile install-portal-snapshots >>build-liferay-portal.sh
RUN echo ant deploy >>build-liferay-portal.sh
RUN echo ../bundles/tomcat-9.0.56/bin/startup.sh >>build-liferay-portal.sh
RUN echo echo >>build-liferay-portal.sh
RUN echo echo if building and starting went well, the server is now running on http://localhost:8080/. >>build-liferay-portal.sh
RUN echo echo starting shell... / some useful commands may be >>build-liferay-portal.sh
RUN echo echo less +F ../bundles/tomcat-9.0.56/logs/catalina.out >>build-liferay-portal.sh
RUN echo echo cd modules; blade gw :apps:commerce:commerce-test:testIntegration >>build-liferay-portal.sh
RUN echo echo quitting the shell will terminate the container. >>build-liferay-portal.sh
RUN echo echo >>build-liferay-portal.sh
RUN echo set -x >>build-liferay-portal.sh
RUN echo "trap bash INT" >>build-liferay-portal.sh
RUN echo "less +F ../bundles/tomcat-9.0.56/logs/catalina.out || :" >>build-liferay-portal.sh
RUN chmod +x build-liferay-portal.sh

CMD ./build-liferay-portal.sh
EXPOSE 8080
EOF

cd $(dirname $0)
docker run -it --rm \
    --name liferay-portal-build \
    -v ~/.liferay/:/home/liferay-portal-build/.liferay/ \
    -v $(pwd):/home/liferay-portal-build/liferay-portal \
    -p 127.0.0.1:8080:8080 \
    liferay-portal-build
