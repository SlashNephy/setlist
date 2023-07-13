FROM gradle:8.2.1-jdk17@sha256:fa5a07f9d3a738e181c941b3896974a01659f9b28834531f38dd6d321bd3a039 AS cache
WORKDIR /app
ENV GRADLE_USER_HOME /app/gradle
COPY *.gradle.kts gradle.properties /app/
RUN gradle shadowJar --parallel --console=verbose

FROM gradle:8.2.1-jdk17@sha256:fa5a07f9d3a738e181c941b3896974a01659f9b28834531f38dd6d321bd3a039 AS build
WORKDIR /app
COPY --from=cache /app/gradle /home/gradle/.gradle
COPY *.gradle.kts gradle.properties /app/
COPY src/main/ /app/src/main/
RUN gradle shadowJar --parallel --console=verbose

FROM amazoncorretto:18.0.1 as runtime
WORKDIR /app

COPY --from=build /app/build/libs/setlist-all.jar /app/setlist.jar

ENTRYPOINT ["java", "-jar", "/app/setlist.jar"]
