FROM gradle:7.6.2-jdk17@sha256:7301820f2a31d4bbd9ccf7cd136cf9deaf19a38072b3edd83ee84d4b36efbf6e AS cache
WORKDIR /app
ENV GRADLE_USER_HOME /app/gradle
COPY *.gradle.kts gradle.properties /app/
RUN gradle shadowJar --parallel --console=verbose

FROM gradle:7.6.2-jdk17@sha256:7301820f2a31d4bbd9ccf7cd136cf9deaf19a38072b3edd83ee84d4b36efbf6e AS build
WORKDIR /app
COPY --from=cache /app/gradle /home/gradle/.gradle
COPY *.gradle.kts gradle.properties /app/
COPY src/main/ /app/src/main/
RUN gradle shadowJar --parallel --console=verbose

FROM amazoncorretto:18.0.1 as runtime
WORKDIR /app

COPY --from=build /app/build/libs/setlist-all.jar /app/setlist.jar

ENTRYPOINT ["java", "-jar", "/app/setlist.jar"]
