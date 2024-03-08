FROM gradle:8.6.0-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/upscayl-runner
WORKDIR /home/gradle/upscayl-runner
RUN gradle buildFatJar --no-daemon

FROM openjdk:11
ENV TZ=Asia/Shanghai
EXPOSE 8091:8091
RUN ln -fs /usr/share/zoneinfo/${TZ} /etc/localtime && \
    echo ${TZ} > /etc/timezone &&\
    dpkg-reconfigure --frontend noninteractive tzdata && \
    mkdir /app
COPY upscayl/ /app/upscayl
RUN chmod +x /app/upscayl/linux/upscayl-bin
COPY --from=build /home/gradle/upscayl-runner/build/libs/*.jar /app/upscayl-runner-all.jar
ENTRYPOINT ["java","-jar","/app/upscayl-runner-all.jar"]