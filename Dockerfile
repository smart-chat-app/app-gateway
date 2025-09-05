# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY target/gateway-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]